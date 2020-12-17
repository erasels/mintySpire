package mintySpire.patches.ui;

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
            if(MintySpire.showIC() && !Loader.isModLoaded("ironcluck")) {
                if(Settings.language == Settings.GameLanguage.ENG) {
                    return SpireReturn.Return("the Ironchad");
                } else if(Settings.language == Settings.GameLanguage.ZHS) {
                    return SpireReturn.Return("铁甲战逝");
                }
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
                                    "if("+MintySpire.class.getName () + ".showIC()) {" +
                                        "if(name.equals(\"The Ironclad\")) {" +
                                            "$3 = \"The Ironchad\";" +
                                        "} else if(name.equals(\"铁甲战士\")) {" +
                                            "$3 = \"铁甲战逝\";" +
                                        "}"  +
                                    "}" +
                                    "$proceed($$);" +
                                    "}");
                        }
                    }
                }
            };
        }
    }

}