package mintySpire.patches.map;

import basemod.ReflectionHacks;
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
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
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
    public static final FrameBuffer FRAME_BUFFER = new FrameBuffer(Pixmap.Format.RGBA8888, Settings.WIDTH, Settings.HEIGHT, false, false);
    public static final OrthographicCamera CAMERA = new OrthographicCamera(FRAME_BUFFER
            .getWidth() * 4.5f, FRAME_BUFFER
            .getHeight() * 4.5f);
    private static Matrix4 saveProjection = null;
    private static float saveOffsetY = 0;
    public static boolean renderingMiniMap = false;

    private static void begin(SpriteBatch sb, OrthographicCamera camera) {
        sb.end();
        FRAME_BUFFER.begin();
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        saveProjection = sb.getProjectionMatrix().cpy();
        sb.setProjectionMatrix(camera.combined);
        sb.begin();
    }

    private static void end(SpriteBatch sb, float x, float y) {
        sb.end();
        FRAME_BUFFER.end();
        sb.setProjectionMatrix(saveProjection);
        sb.begin();

        sb.setColor(Color.WHITE);
        TextureRegion fboTex = new TextureRegion(FRAME_BUFFER.getColorBufferTexture());
        fboTex.flip(false, true);
        sb.draw(
                fboTex,
                x, y,
                0, 0,
                FRAME_BUFFER.getWidth(), FRAME_BUFFER.getHeight(),
                1, 1,
                0
        );
    }

    public static void renderMinimap(SpriteBatch sb, float x, float y, OrthographicCamera camera) {
        AbstractDungeon.dungeonMapScreen.updateImage();

        // The render functions will refuse to work unless they think the map is open, set the
        // screen to MAP for the duration of the render.
        AbstractDungeon.CurrentScreen oldScreen = AbstractDungeon.screen;
        AbstractDungeon.screen = AbstractDungeon.CurrentScreen.MAP;

        // The minimap is centered on the screen no matter where the full map is, save the offset
        // to decouple the minimap rendering.
        saveOffsetY = DungeonMapScreen.offsetY;

        // The map background and boss node are transparent, force them to be opaque for the
        // duration of minimap rendering but restore their values afterwards.
        Color baseMapColor = ReflectionHacks
                .getPrivate(AbstractDungeon.dungeonMapScreen.map, DungeonMap.class, "baseMapColor");
        Color bossNodeColor = ReflectionHacks
                .getPrivate(AbstractDungeon.dungeonMapScreen.map, DungeonMap.class, "bossNodeColor");

        Color tempMapColor = new Color(baseMapColor.r, baseMapColor.g, baseMapColor.b, 1.F);
        Color tempBossColor = new Color(bossNodeColor.r, bossNodeColor.g, bossNodeColor.b, 1.F);

        ReflectionHacks
                .setPrivate(AbstractDungeon.dungeonMapScreen.map, DungeonMap.class, "baseMapColor", tempMapColor);
        ReflectionHacks
                .setPrivate(AbstractDungeon.dungeonMapScreen.map, DungeonMap.class, "bossNodeColor", tempBossColor);


        // DungeonMap.calculateMapSize()
        float mapMidDist = AbstractDungeon.id
                .equals("TheEnding") ? Settings.MAP_DST_Y * 4.0F - 1380.0F * Settings.scale : Settings.MAP_DST_Y * 16.0F - 1380.0F * Settings.scale;
        ReflectionHacks
                .setPrivate(AbstractDungeon.dungeonMapScreen.map, DungeonMap.class, "mapMidDist", mapMidDist);
        ReflectionHacks
                .setPrivateStatic(DungeonMap.class, "mapOffsetY", mapMidDist - 120.0F * Settings.scale);


        float savedTargetOffsetY = ReflectionHacks
                .getPrivate(AbstractDungeon.dungeonMapScreen, DungeonMapScreen.class, "targetOffsetY");

        float defaultMapScroll = ReflectionHacks
                .getPrivateStatic(DungeonMapScreen.class, "MAP_UPPER_SCROLL_DEFAULT");
        DungeonMapScreen.offsetY = defaultMapScroll + 750 * Settings.scale;

        ReflectionHacks
                .setPrivate(AbstractDungeon.dungeonMapScreen, DungeonMapScreen.class, "targetOffsetY", DungeonMapScreen.offsetY);
        renderingMiniMap = true;

        begin(sb, camera);
        AbstractDungeon.dungeonMapScreen.render(sb);
        end(sb, x, y);

        // Restore all of the map attributes to saved values
        DungeonMapScreen.offsetY = saveOffsetY;
        ReflectionHacks
                .setPrivate(AbstractDungeon.dungeonMapScreen, DungeonMapScreen.class, "targetOffsetY", savedTargetOffsetY);


        ReflectionHacks
                .setPrivate(AbstractDungeon.dungeonMapScreen.map, DungeonMap.class, "baseMapColor", baseMapColor);
        ReflectionHacks
                .setPrivate(AbstractDungeon.dungeonMapScreen.map, DungeonMap.class, "bossNodeColor", bossNodeColor);
        renderingMiniMap = false;
        AbstractDungeon.screen = oldScreen;
    }

    public static void hideMinimap() {
        AbstractDungeon.dungeonMapScreen.map.hide();
    }

    @SpirePatch(
            clz = DungeonMapScreen.class,
            method = "render"
    )
    public static class Start {
        public static void Postfix(DungeonMapScreen unusedInstance, SpriteBatch sb) {
            if (MintySpire
                    .showMM() && !renderingMiniMap && AbstractDungeon.screen == AbstractDungeon.CurrentScreen.MAP) {
                renderMinimap(sb, -Settings.WIDTH / 2f, 50 * Settings.scale, CAMERA);
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
