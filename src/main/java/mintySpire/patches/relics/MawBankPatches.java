package mintySpire.patches.relics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.relics.MawBank;
import com.megacrit.cardcrawl.shop.ShopScreen;

public class MawBankPatches {
    public static MawBank mb;

    @SpirePatch(clz = ShopScreen.class, method = "render")
    public static class RenderMB {
        @SpirePostfixPatch
        public static void patch(ShopScreen __instance, SpriteBatch sb, boolean ___somethingHovered) {
            if (mb != null && !mb.usedUp) {
                Color org = sb.getColor();

                sb.setColor(Color.WHITE);
                sb.draw(mb.img,
                        InputHelper.mX,
                        InputHelper.mY - (mb.img.getHeight() / 2f),
                        64.0F,
                        64.0F,
                        128.0F,
                        128.0F,
                        Settings.scale,
                        Settings.scale,
                        0.0F,
                        0,
                        0,
                        128,
                        128,
                        false,
                        false);

                sb.setColor(org);
            }
        }
    }


    public static void receive() {
        mb = (MawBank) AbstractDungeon.player.getRelic(MawBank.ID);
    }
}
