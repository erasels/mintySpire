package mintySpire.ui;

import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.characters.Ironclad;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.screens.charSelect.CharacterOption;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import mintySpire.MintySpire;

public class IronchadName {
    @SpirePatch(clz = Ironclad.class, method = "getTitle")
    public static class TitleChanger {
        public static SpireReturn<?> Prefix(Ironclad __instance, AbstractPlayer.PlayerClass pc) {
            if(Settings.language == Settings.GameLanguage.ENG && MintySpire.showIC() && !Loader.isModLoaded("ironcluck")) {
                return SpireReturn.Return("the Ironchad");
            }
            return SpireReturn.Continue();
        }
    }

    private static boolean firstRun = true;
    @SpirePatch(clz = CharacterOption.class, method = "renderInfo")
    public static class CharSelectTitleChanger {
        public static ExprEditor Instrument() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getClassName().equals(FontHelper.class.getName()) && m.getMethodName().equals("renderSmartText")) {
                        if (firstRun && !Loader.isModLoaded("ironcluck")) {
                            firstRun = false;
                            m.replace("{" +
                                    "if("+MintySpire.class.getName () + ".showIC() && name.equals(\"The Ironclad\")) {" +
                                    "$proceed($1, $2, \"The Ironchad\", $4, $5, $6, $7, $8);"  +
                                    //sb, FontHelper.bannerNameFont, this.name, this.infoX - 35.0F * Settings.scale, this.infoY + NAME_OFFSET_Y, 99999.0F, 38.0F * Settings.scale, Settings.GOLD_COLOR
                                    "} else {" +
                                    "$proceed($$);" +
                                    "}" +
                                    "}");
                        }
                    }
                }
            };
        }
    }

}


/*
        @SpireInsertPatch(locator = Locator.class)
        public static void Insert(CharacterOption __instance, SpriteBatch sb) {
            if(__instance.name.equals("The Ironclad"))
            FontHelper.renderSmartText(sb, FontHelper.bannerNameFont, "The Ironchad", (float)ReflectionHacks.getPrivate(__instance, CharacterOption.class, "infoX") - 35.0F * Settings.scale, (float)ReflectionHacks.getPrivate(__instance, CharacterOption.class, "infoY") + NAME_OFFSET_Y, 99999.0F, 38.0F * Settings.scale, Settings.GOLD_COLOR.cpy());
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(SpriteBatch.class, "draw");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }*/