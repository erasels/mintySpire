package mintySpire.patches.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.screens.MasterDeckViewScreen;
import com.megacrit.cardcrawl.shop.ShopScreen;
import mintySpire.MintySpire;

public class PurgeCostDisplayPatches {
    private static String uiText = CardCrawlGame.languagePack.getUIString(MintySpire.makeID("PurgeCostDeckView")).TEXT[0];

    @SpirePatch(clz = MasterDeckViewScreen.class, method = "render")
    public static class RenderCardPurgeCost {
        @SpirePrefixPatch
        public static void patch(MasterDeckViewScreen __instance, SpriteBatch sb) {
            if(MintySpire.showPCD()) {
                int actualPurgeCost = ShopScreen.actualPurgeCost;
                String text = uiText + actualPurgeCost;

                Color color = Color.GOLD;
                if (actualPurgeCost > AbstractDungeon.player.gold)
                    color = Color.SALMON;
                FontHelper.renderFontLeft(sb, FontHelper.tipHeaderFont, text, 10 * Settings.scale, Settings.HEIGHT * 0.1f, color);

                Color org = sb.getColor();
                sb.setColor(Color.WHITE);
                sb.draw(ImageMaster.UI_GOLD,
                        FontHelper.getSmartWidth(FontHelper.tipHeaderFont, text, 9999f, 0f),
                        Settings.HEIGHT * 0.1f - ((ImageMaster.UI_GOLD.getWidth() * Settings.scale) / 2f),
                        ImageMaster.UI_GOLD.getWidth() * Settings.scale,
                        ImageMaster.UI_GOLD.getHeight() * Settings.scale);
                sb.setColor(org);
            }
        }
    }
}
