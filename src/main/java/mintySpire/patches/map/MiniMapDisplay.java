package mintySpire.patches.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.map.DungeonMap;
import com.megacrit.cardcrawl.map.Legend;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.screens.DungeonMapScreen;
import com.megacrit.cardcrawl.vfx.FlameAnimationEffect;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import mintySpire.MintySpire;

public class MiniMapDisplay {
    private static FrameBuffer fbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false, false);
    private static OrthographicCamera camera = new OrthographicCamera(fbo.getWidth() * 4.5f, fbo.getHeight() * 4.5f);
    private static Matrix4 saveProjection = null;
    private static float saveOffsetY = 0;
    public static boolean renderingMiniMap = false;

    private static void begin(SpriteBatch sb) {
        sb.end();
        fbo.begin();
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        saveProjection = sb.getProjectionMatrix().cpy();
        sb.setProjectionMatrix(camera.combined);
        sb.begin();
    }

    private static void end(SpriteBatch sb) {
        sb.end();
        fbo.end();
        sb.setProjectionMatrix(saveProjection);
        sb.begin();

        sb.setColor(Color.WHITE);
        TextureRegion fboTex = new TextureRegion(fbo.getColorBufferTexture());
        fboTex.flip(false, true);
        sb.draw(
                fboTex,
                -Settings.WIDTH / 2f, 50 * Settings.scale,
                0, 0,
                fbo.getWidth(), fbo.getHeight(),
                1, 1,
                0
        );
    }

    @SpirePatch(
            clz = DungeonMapScreen.class,
            method = "render"
    )
    public static class Start {
        public static void Postfix(DungeonMapScreen __instance, SpriteBatch sb, @ByRef float[] ___targetOffsetY, float ___MAP_UPPER_SCROLL_DEFAULT) {
            if (MintySpire.showMM() && !renderingMiniMap) {
                saveOffsetY = DungeonMapScreen.offsetY;
                float saveTargetOffsetY = ___targetOffsetY[0];
                DungeonMapScreen.offsetY = ___MAP_UPPER_SCROLL_DEFAULT + 750 * Settings.scale;
                ___targetOffsetY[0] = DungeonMapScreen.offsetY;
                renderingMiniMap = true;
                begin(sb);
                __instance.render(sb);
                end(sb);
                DungeonMapScreen.offsetY = saveOffsetY;
                ___targetOffsetY[0] = saveTargetOffsetY;
                renderingMiniMap = false;
            }
        }

        @SpireInsertPatch(
                locator = Locator.class
        )
        public static SpireReturn<Void> Insert(DungeonMapScreen __instance, SpriteBatch sb, @ByRef float[] ___targetOffsetY) {
            if (renderingMiniMap) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }

        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.FieldAccessMatcher(DungeonMapScreen.class, "dismissable");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }

    @SpirePatch(
            clz = MapRoomNode.class,
            method = "render"
    )
    public static class BiggerIcons {
        @SpireInsertPatch(
                rlocs = {4, 15}
        )
        public static void Insert(MapRoomNode __instance, SpriteBatch sb, @ByRef float[] ___scale) {
            if (renderingMiniMap) {
                ___scale[0] *= MintySpire.scaleMMIcons();
            }
        }

        public static void Postfix(MapRoomNode __instance, SpriteBatch sb, @ByRef float[] ___scale) {
            if (renderingMiniMap) {
                ___scale[0] /= MintySpire.scaleMMIcons();
            }
        }
    }

    @SpirePatch(
            clz = FlameAnimationEffect.class,
            method = "render",
            paramtypez = {
                    SpriteBatch.class,
                    float.class
            }
    )
    public static class FixFlamingEliteVFX {
        public static ExprEditor Instrument() {
            return new ExprEditor() {
                @Override
                public void edit(FieldAccess f) throws CannotCompileException {
                    if (f.isReader() && f.getFieldName().equals("cY")) {
                        f.replace(String.format("$_ = %s.fixOffsetY($proceed($$));", FixFlamingEliteVFX.class.getName()));
                    }
                }
            };
        }

        public static float fixOffsetY(float cY) {
            if (renderingMiniMap) {
                return cY - saveOffsetY + DungeonMapScreen.offsetY;
            }
            return cY;
        }
    }

    @SpirePatch(
            clz = DungeonMap.class,
            method = "renderMapBlender"
    )
    public static class SkipBlenderRender {
        public static SpireReturn<Void> Prefix(DungeonMap __instance, SpriteBatch sb) {
            if (renderingMiniMap) {
                // Skips rendering the map blender on the mini-map because it doesn't work correctly
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = Legend.class,
            method = "render"
    )
    public static class End {
        public static SpireReturn<Void> Prefix(Legend __instance, SpriteBatch sb) {
            if (renderingMiniMap) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
