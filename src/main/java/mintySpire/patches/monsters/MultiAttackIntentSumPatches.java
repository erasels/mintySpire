package mintySpire.patches.monsters;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import mintySpire.MintySpire;

public class MultiAttackIntentSumPatches {
    private static boolean firstRun = true;
    @SpirePatch(clz = AbstractMonster.class, method = "renderDamageRange")
    public static class TextRender {
        public static ExprEditor Instrument() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getClassName().equals(FontHelper.class.getName()) && m.getMethodName().equals("renderFontLeftTopAligned") && firstRun) {
                        firstRun = false;
                        m.replace("{" +
                                "if(" + MintySpire.class.getName() + ".showSD()) {" +
                                FontHelper.class.getName()+".renderFontLeftTopAligned($1, $2, $3 + \" (\" + intentDmg*intentMultiAmt + \")\", $4, $5, $6);" +
                                "} else {" +
                                "$proceed($$);" +
                                "}" +
                                "}");
                    }
                }
            };
        }
    }
}
