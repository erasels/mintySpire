package mintySpire.patches.ui;

import basemod.ReflectionHacks;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import mintySpire.MintySpire;

import java.util.ArrayList;

public class DisplayTurnPatches {
    @SpirePatch(clz = AbstractRoom.class, method = "render")
    public static class RenderTurnCounter {
        private static float START_X = Settings.WIDTH - (100f * Settings.scale);
        private static float START_Y = ((float)ReflectionHacks.getPrivateStatic(AbstractRelic.class, "START_Y")) - (64f * Settings.scale);
        private static String text = null;
        @SpireInsertPatch(locator = Locator.class)
        public static void patch(AbstractRoom __instance, SpriteBatch sb) {
            if(text == null) {
                text = CardCrawlGame.languagePack.getUIString(MintySpire.makeID("TurnCounter")).TEXT[0];
                START_X -= FontHelper.getSmartWidth(FontHelper.panelNameFont, text, 999f, 0f);
            }

            if(MintySpire.showTD() && !AbstractDungeon.getCurrRoom().isBattleOver) {
                String msg = text + GameActionManager.turn;
                Color c = sb.getColor();
                sb.setColor(Color.WHITE);
                float w = ImageMaster.TIMER_ICON.getWidth() * Settings.scale, h = ImageMaster.TIMER_ICON.getHeight() * Settings.scale;
                sb.draw(ImageMaster.TIMER_ICON, START_X - (w/2f), START_Y - (h/2f), w, h);
                FontHelper.renderFontLeft(sb, FontHelper.panelNameFont, msg, START_X + (w/2f), START_Y, Settings.CREAM_COLOR);
                sb.setColor(c);
            }
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(AbstractPlayer.class, "renderPlayerBattleUi");
                return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<>(), finalMatcher);
            }
        }
    }
}
