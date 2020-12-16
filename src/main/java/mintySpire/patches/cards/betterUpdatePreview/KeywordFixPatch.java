package mintySpire.patches.cards.betterUpdatePreview;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import javassist.CtBehavior;

import java.util.ArrayList;
import java.util.List;

@SpirePatch(clz = AbstractCard.class, method = "initializeDescription")
public class KeywordFixPatch {
    @SpireInsertPatch(locator = KeywordFixPatchLocator.class)
    public static void UpgradeDiffKeywordFix(AbstractCard _instance) {
        ArrayList<String> savedKeywords = CardFields.AbCard.diffedKeywords.get(_instance);
        if (savedKeywords != null) {
            _instance.keywords.addAll(savedKeywords);
            savedKeywords.clear();
        }
    }

    private static class KeywordFixPatchLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctBehavior) throws Exception {
            Matcher matcher = new Matcher.MethodCallMatcher(ArrayList.class, "clear");
            List<Matcher> matchers = new ArrayList<>();
            matchers.add(matcher);
            return LineFinder.findInOrder(ctBehavior, matchers, matcher);
        }
    }

}
