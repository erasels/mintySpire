package mintySpire.patches.cards.betterUpdatePreview;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.FontHelper;
import javassist.CtBehavior;
import mintySpire.MintySpire;

public class CardTextRenderCNPatches {
    @SpirePatch(
            clz = AbstractCard.class,
            method = "initializeDescriptionCN"
    )
    public static class FixDiffMarkerSizePatchCN {
        @SpireInsertPatch(
                locator = FixDiffMarkerSizePatchCNLocator.class,
                localvars = {"gl", "word", "sbuilder"}
        )
        public static void Insert(AbstractCard _instance, @ByRef GlyphLayout[] gl, String word, @ByRef StringBuilder[] currentLine) {
            if(MintySpire.showBCUP()) {
                if (word.length() > 0 && word.charAt(0) == '[') {
                    if (word.equals("[diffAddS]") ||
                            word.equals("[diffAddE]") ||
                            word.equals("[diffRmvS]") ||
                            word.equals("[diffRmvE]")
                    ) {
                        gl[0].setText(FontHelper.cardDescFont_N, "");
                        gl[0].width = 0;
                        currentLine[0].append(" ").append(word).append(" ");
                    }
                }
            }
        }
    }

    public static class FixDiffMarkerSizePatchCNLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctBehavior) throws Exception {
            Matcher matcher = new Matcher.MethodCallMatcher(String.class, "trim");
            int[] lines = LineFinder.findAllInOrder(ctBehavior, matcher);
            return new int[]{lines[lines.length-1]}; // Only last occurrence
        }
    }

}
