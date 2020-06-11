package mintySpire.patches.cards;

import basemod.patches.com.megacrit.cardcrawl.helpers.GameDictionary.PostKeywordInitialize;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.ByRef;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.helpers.GameDictionary;
import com.megacrit.cardcrawl.helpers.TipHelper;
import mintySpire.MintySpire;

import java.util.ArrayList;
import java.util.TreeMap;

public class RemoveKeywordPowertipsPatches {
    private static TreeMap<String, String> keys = new TreeMap<>();

    @SpirePatch(clz = PostKeywordInitialize.class, method = "Postfix")
    public static class BaseKeywordCapture {
        @SpirePrefixPatch
        public static void patch() {
            keys.putAll(GameDictionary.keywords);
        }
    }


    @SpirePatch(clz = TipHelper.class, method = "renderKeywords")
    public static class RemoveBaseKeywords {
        @SpirePrefixPatch
        public static void patch(float x, float y, SpriteBatch sb, @ByRef ArrayList<String>[] keywords) {
            if (MintySpire.showBKR()) {
                keywords[0].removeIf(s -> keys.containsKey(s));
            }
        }
    }
}
