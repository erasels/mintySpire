package mintySpire.patches.cards.betterUpdatePreview;

import basemod.BaseMod;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.GameDictionary;
import mintySpire.MintySpire;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SetTextFieldsPatches {
    @SpirePatch(
            clz = AbstractCard.class,
            method = "initializeDescription"
    )
    public static class AbstractCardInitializeDescriptionPatch {
        @SpirePostfixPatch
        public static void defaultAndUpgradedText(AbstractCard _instance) {
            if (MintySpire.showBCUP()) {
                if (Settings.lineBreakViaCharacter) {
                    CardFields.AbCard.keywordSet.get(_instance).addAll(_instance.keywords);
                }
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
            if (MintySpire.showBCUP()) {
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

    private static String calculateTextDiff(String original, String upgraded, AbstractCard card) {
        try {
            DiffRowGenerator.Builder builder = DiffRowGenerator.create()
                    .showInlineDiffs(true)
                    .lineNormalizer((s -> s))
                    .mergeOriginalRevised(true)
                    .oldTag(start -> start ? " [diffRmvS] " : " [diffRmvE] ")
                    .newTag(start -> start ? " [diffAddS] " : " [diffAddE] ");
            // Use default splitter for chinese/japanese, as they don't use spaces in card descriptions
            if (!Settings.lineBreakViaCharacter) {
                builder.inlineDiffBySplitter(SetTextFieldsPatches::splitter);
            } else {
                builder.inlineDiffBySplitter(createSplitterCN(CardFields.AbCard.keywordSet.get(card)));
            }
            DiffRowGenerator generator = builder.build();
            List<DiffRow> rows = generator.generateDiffRows(Collections.singletonList(original), Collections.singletonList(upgraded));
            String diffStr = rows.get(0).getOldLine();
            if (diffStr.matches(".*diffAdd.*")) {
                builder = DiffRowGenerator.create()
                        .showInlineDiffs(true)
                        .lineNormalizer((s -> s))
                        .newTag(start -> start ? " [diffAddS] " : " [diffAddE] ");
                // Use default splitter for chinese/japanese, as they don't use spaces in card descriptions
                if (!Settings.lineBreakViaCharacter) {
                    builder.inlineDiffBySplitter(SetTextFieldsPatches::splitter);
                } else {
                    builder.inlineDiffBySplitter(createSplitterCN(CardFields.AbCard.keywordSet.get(card)));
                }
                generator = builder.build();
                rows = generator.generateDiffRows(Collections.singletonList(original), Collections.singletonList(upgraded));
                diffStr = rows.get(0).getNewLine();
            }

            return diffStr.replaceAll(" {2}(?=\\[diff)", " ").replaceAll("(?<=(Rmv|Add)[SE]]) {2}", " ");
        } catch (DiffException e) {
            e.printStackTrace();
            return upgraded;
        }
    }

    private static List<String> splitter(String line) {
        List<String> ret = new ArrayList<>();
        if (line != null) {
            String[] words = line.split("\\s+");
            for (String word : words) {
                ret.add(word + " ");
            }
        }
        return ret;
    }

    private static Function<String, List<String>> createSplitterCN(Set<String> keywords) {
        Pattern splitBySpaces = Pattern.compile("(?<=\\s)(?=[^\\s])");
        Pattern asciiChars = Pattern.compile("[\\x00-\\x7F]+");
        Pattern splitByAsciiAndNonAscii = Pattern.compile("(?<=[^\\x00-\\x7F])(?=[\\x00-\\x7F])|(?<=[\\x00-\\x7F])(?=[^\\x00-\\x7F])");
        return line -> {
            List<String> ret = new ArrayList<>();
            if (line != null) {
                String[] words = splitBySpaces.split(line);
                for (String word : words) {
                    if (asciiChars.matcher(word).matches() || keywords.contains(word.trim().toLowerCase())) {
                        ret.add(word);
                    } else {
                        String[] words1 = splitByAsciiAndNonAscii.split(word);
                        for (String word1 : words1) {
                            if (asciiChars.matcher(word1).matches()) {
                                ret.add(word1);
                            } else {
                                ret.addAll(word1.chars().mapToObj(c -> String.valueOf((char) c)).collect(Collectors.toList()));
                            }
                        }
                    }
                }
            }
            return ret;
        };
    }
}
