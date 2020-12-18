package mintySpire.patches.relics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.ByRef;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.relics.CursedKey;
import com.megacrit.cardcrawl.rewards.chests.AbstractChest;
import com.megacrit.cardcrawl.vfx.ChestShineEffect;
import com.megacrit.cardcrawl.vfx.scene.SpookyChestEffect;
import mintySpire.vfx.BetterDebuffParticle;

public class CursedKeyPatches {
    private static final int VFX_AMT = 4;
    private static final float VFXINTERVAL = 2f;
    private static float vfxTimer = 0;
    @SpirePatch(clz = AbstractChest.class, method = "render")
    public static class RenderCursedKeyChest {
        @SpirePostfixPatch
        public static void patch(AbstractChest __instance, SpriteBatch sb, Hitbox ___hb) {
            if (!__instance.isOpen) {
                CursedKey r = (CursedKey) AbstractDungeon.player.getRelic(CursedKey.ID);
                if (r != null) {
                    sb.draw(r.img,
                            ___hb.x - (r.img.getWidth() / 2f),
                            ___hb.cY - (r.img.getHeight() / 2f),
                            64.0F,
                            64.0F,
                            128.0F,
                            128.0F,
                            Settings.scale,
                            Settings.scale,
                            42.0F,
                            0,
                            0,
                            128,
                            128,
                            false,
                            false);

                    if(vfxTimer <= 0) {
                        vfxTimer = VFXINTERVAL;
                        Color col;
                        for (int i = 0; i < VFX_AMT; i++) {
                            if(MathUtils.randomBoolean()) {
                                col = Color.PURPLE;
                            } else {
                                col = Color.FIREBRICK;
                            }
                            AbstractDungeon.effectsQueue.add(new BetterDebuffParticle(___hb, col));
                        }
                    }
                    vfxTimer -= Gdx.graphics.getRawDeltaTime();
                }
            }
        }
    }

    @SpirePatch(clz = SpookyChestEffect.class, method = SpirePatch.CONSTRUCTOR)
    public static class SpookierChestEffect {
        @SpirePostfixPatch
        public static void patch(SpookyChestEffect __instance, @ByRef Color[] ___color) {
            if(AbstractDungeon.player.hasRelic(CursedKey.ID))
                ___color[0] = MathUtils.randomBoolean()?Color.PURPLE.cpy():Color.FIREBRICK.cpy();
        }
    }

    @SpirePatch(clz = ChestShineEffect.class, method = SpirePatch.CONSTRUCTOR)
    public static class SpookierChestShineEffect {
        @SpirePostfixPatch
        public static void patch(ChestShineEffect __instance, @ByRef Color[] ___color) {
            if(AbstractDungeon.player.hasRelic(CursedKey.ID)) {
                Color c = Color.FIREBRICK.cpy();
                c.a = ___color[0].a;
                c.b += (___color[0].r - ___color[0].b);
                ___color[0] = c;
            }
        }
    }
}
