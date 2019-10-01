package mintySpire.patches.cards;

import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.red.PerfectedStrike;
import com.megacrit.cardcrawl.core.CardCrawlGame;

public class PerfectedStrikeCounterPatches {
    @SpirePatch(clz = PerfectedStrike.class, method=SpirePatch.CLASS)
    public static class pSFields {
        public static SpireField<Integer> strikes = new SpireField<>(() -> 0);
    }

    //calculateCardDamage may not be often enough
    @SpirePatch(clz = PerfectedStrike.class, method = "applyPowers")
    public static class DescriptionChanger {
        public static void Postfix(PerfectedStrike __instance) {
            int tmp = PerfectedStrike.countCards();
            if(pSFields.strikes.get(__instance) != tmp) {
                pSFields.strikes.set(__instance, tmp);
            } else {
                return;
            }

            if(__instance.rawDescription.endsWith(")")) {
                __instance.rawDescription = __instance.rawDescription.substring(0, __instance.rawDescription.lastIndexOf(".")+1) + newCounter(tmp);
            } else {
                __instance.rawDescription = __instance.rawDescription + newCounter(tmp);
            }
            __instance.initializeDescription();
        }
    }

    private static String newCounter(int num) {
        String localizedCard = CardCrawlGame.languagePack.getUIString("RunHistoryScreen").TEXT[9];
        return " (" + num + " "+localizedCard+".)";
    }
}
