package mintySpire.patches.cards.betterUpdatePreview;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import javassist.CtBehavior;

import java.util.ArrayList;
import java.util.List;

public class CardRewardOriginalCardsPatches {
    @SpirePatch(
            clz = AbstractDungeon.class,
            method = "getRewardCards"
    )
    public static class CardPoolPatch {
        @SpireInsertPatch(
                locator = SaveUnupgradedRewardCardsLocator.class,
                localvars = {"retVal"}
        )
        public static void SaveUnupgradedRewardCards(ArrayList<AbstractCard> retVal) {
            ArrayList<AbstractCard> copiedList = new ArrayList<>();
            retVal.forEach(c -> copiedList.add(c.makeCopy()));
            CardFields.SCVPopup.unupgradedCardRewards.set(CardCrawlGame.cardPopup, copiedList);
        }

        private static class SaveUnupgradedRewardCardsLocator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher matcher = new Matcher.NewExprMatcher(ArrayList.class);
                List<Matcher> matchers = new ArrayList<>();
                matchers.add(matcher);
                return LineFinder.findInOrder(ctBehavior, matchers, matcher);
            }
        }
    }

    @SpirePatch(
            clz = AbstractDungeon.class,
            method = "getColorlessRewardCards"
    )
    public static class ColorlessPoolPatch {
        @SpireInsertPatch(
                locator = CardPoolPatch.SaveUnupgradedRewardCardsLocator.class,
                localvars = {"retVal"}
        )
        public static void SaveUnupgradedColorlessRewardCards(ArrayList<AbstractCard> retVal) {
            ArrayList<AbstractCard> copiedList = new ArrayList<>();
            retVal.forEach(c -> copiedList.add(c.makeCopy()));
            CardFields.SCVPopup.unupgradedCardRewards.set(CardCrawlGame.cardPopup, copiedList);
        }
    }
}
