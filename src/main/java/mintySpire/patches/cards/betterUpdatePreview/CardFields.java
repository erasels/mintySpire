package mintySpire.patches.cards.betterUpdatePreview;

import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.screens.SingleCardViewPopup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CardFields {
    @SpirePatch(clz = AbstractCard.class, method = SpirePatch.CLASS)
    public static class AbCard {
        public static SpireField<String> defaultText = new SpireField<>(() -> "");
        public static SpireField<String> upgradedText = new SpireField<>(() -> "");
        public static SpireField<Set<String>> keywordSet = new SpireField<>(HashSet::new);
        public static SpireField<String> diffText = new SpireField<>(() -> "");
        public static SpireField<Boolean> isInDiffRmv = new SpireField<>(() -> false);
        public static SpireField<Boolean> isInDiffAdd = new SpireField<>(() -> false);
    }

    @SpirePatch(clz = SingleCardViewPopup.class, method = SpirePatch.CLASS)
    public static class SCVPopup {
        public static SpireField<ArrayList<AbstractCard>> unupgradedCardRewards = new SpireField<>(() -> null);
    }
}
