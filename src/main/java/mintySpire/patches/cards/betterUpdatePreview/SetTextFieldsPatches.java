package mintySpire.patches.cards.betterUpdatePreview;

import basemod.BaseMod;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.GameDictionary;
import mintySpire.MintySpire;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SetTextFieldsPatches {
    @SpirePatch(
            clz = AbstractCard.class,
            method = "initializeDescription"
    )
    public static class AbstractCardInitializeDescriptionPatch {
        @SpirePostfixPatch
        public static void defaultAndUpgradedText(AbstractCard _instance) {
            if(MintySpire.showBCUP()) {
                if (_instance.upgraded) {
                    CardFields.AbCard.upgradedText.set(_instance, _instance.rawDescription);
                } else {
                    CardFields.AbCard.defaultText.set(_instance, _instance.rawDescription);
                }
            }
        }
    }

    @SpirePatch(
            clz = AbstractCard.class,
            method = "displayUpgrades"
    )
    public static class AbstractCardDisplayUpgradesPatch {
        @SpirePrefixPatch
        public static void diffText(AbstractCard _instance) {
            if(MintySpire.showBCUP()) {
                String defaultText = CardFields.AbCard.defaultText.get(_instance);
                String upgradedText = CardFields.AbCard.upgradedText.get(_instance);
                if ("".equals(CardFields.AbCard.diffText.get(_instance)) && !defaultText.equals(upgradedText) && !"".equals(upgradedText)) {
                    String diffText = calculateTextDiff(defaultText, upgradedText, _instance);
                    CardFields.AbCard.diffText.set(_instance, diffText);
                }
                if (!"".equals(CardFields.AbCard.diffText.get(_instance))) {
                    _instance.rawDescription = CardFields.AbCard.diffText.get(_instance);
                    _instance.initializeDescription();
                }
            }
        }
    }

    private static boolean checkPattern(String original, String pattern) {
        Pattern p = Pattern.compile(pattern);
        Matcher matcher = p.matcher(original);
        return matcher.find();
    }

    private static boolean checkForEnergy(String original) {
        return checkPattern(original, "(\\[[RGBWE]])");
    }

    private static boolean checkForDynVar(String original) {
        return checkPattern(original, "(![a-zA-Z:]+?!)");
    }

    private static boolean checkForColor(String original) {
        return checkPattern(original, "(\\[#\\p{XDigit}{6}].*\\[])");
    }

    private static boolean checkForNewline(String original) {
        return checkPattern(original, "^(NL)$");
    }

    private static boolean checkForWhitespace(String original) {
        return checkPattern(original, "\\s+");
    }

    private static String checkForCustomKeyword(String word, AbstractCard card) {
        String modified = word;
        if (modified.length() > 0 && modified.charAt(modified.length() - 1) != ']' && !Character.isLetterOrDigit(modified.charAt(modified.length() - 1))) {
            modified = word.substring(0, word.length() - 1);
        }
        modified = modified.toLowerCase();
        String parentKeyword = GameDictionary.parentWord.get(modified);
        if (parentKeyword != null) {
            modified = parentKeyword;
            if (GameDictionary.keywords.containsKey(modified)) {
                if (BaseMod.keywordIsUnique(modified)) {
                    ArrayList<String> diffKeywords = CardFields.AbCard.diffedKeywords.get(card);
                    if (diffKeywords == null) {
                        diffKeywords = new ArrayList<>();
                    }
                    diffKeywords.add(modified);
                    CardFields.AbCard.diffedKeywords.set(card, diffKeywords);
                    String prefix = BaseMod.getKeywordPrefix(modified);
                    return word.replaceFirst(prefix, "");
                }
            }
        }
        return null;
    }

    private static String calculateTextDiff(String original, String upgraded, AbstractCard card) {
        try {
            Function<String, List<String>> splitter = line -> {
                List<String> ret = new ArrayList<>();
                if (line != null) {
                    String[] words = line.split("\\s+");
                    for (String word : words) {
                        ret.add(word + " ");
                    }
                }
                return ret;
            };
            DiffRowGenerator generator = DiffRowGenerator.create()
                    .showInlineDiffs(true)
                    .lineNormalizer((s -> s))
                    .mergeOriginalRevised(true)
                    .inlineDiffBySplitter(splitter)
                    .oldTag(start -> start ? " [DiffRmvS] " : " [DiffRmvE] ")
                    .newTag(start -> start ? " [DiffAddS] " : " [DiffAddE] ")
                    .build();
            List<DiffRow> rows = generator.generateDiffRows(Collections.singletonList(original), Collections.singletonList(upgraded));
            String diffStr = rows.get(0).getOldLine();

            return diffStr.replaceAll(" {2}(?=\\[Diff)", " ").replaceAll("(?<=(Rmv|Add)[SE]]) {2}", " ");
        } catch (DiffException e) {
            e.printStackTrace();
            return upgraded;
        }
    }
}
