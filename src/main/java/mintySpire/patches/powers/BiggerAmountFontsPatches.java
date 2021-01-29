package mintySpire.patches.powers;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.powers.AbstractPower;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import mintySpire.MintySpire;
import mintySpire.utility.StsLibChecker;

public class BiggerAmountFontsPatches {
    @SpirePatch(clz = AbstractPower.class, method = "renderAmount")
    public static class NormalIncrease {
        public static ExprEditor Instrument() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getClassName().equals(FontHelper.class.getName()) && m.getMethodName().equals("renderFontRightTopAligned")) {
                        m.replace("{" +
                                "if(" + MintySpire.class.getName() + ".showBPF() && !" + BiggerAmountFontsPatches.class.getName() + ".isTwoAmount(this) ) {" +
                                    "$5 += 5f * " + Settings.class.getName() + ".scale;" + //y
                                    "if(amount > 0 && amount < 10) {" +
                                            "$6 *= 1.75f;" + //Fontscale
                                    "} else {" +
                                            "$4 += 5f * " + Settings.class.getName() + ".scale;" + //x
                                            "$6 *= 1.5f;" + //Fontscale
                                    "}" +
                                "}" +
                                "$proceed($$);" +
                                "}");
                    }
                }
            };
        }
    }

    public static boolean isTwoAmount(AbstractPower p) {
        return MintySpire.hasStSLib && StsLibChecker.checkTwoAmountPower(p);
    }
}
