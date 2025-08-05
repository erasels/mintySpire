package mintySpire.patches.monsters;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.monsters.beyond.TimeEater;
import com.megacrit.cardcrawl.monsters.city.Champ;
import com.megacrit.cardcrawl.monsters.exordium.AcidSlime_L;
import com.megacrit.cardcrawl.monsters.exordium.SlimeBoss;
import com.megacrit.cardcrawl.monsters.exordium.SpikeSlime_L;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import mintySpire.MintySpire;

public class HalfwayHealthbarTextPatch {
    @SpirePatch(clz = AbstractCreature.class, method = "renderHealthText")
    public static class TextRender {
        public static ExprEditor Instrument() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getClassName().equals(FontHelper.class.getName()) && m.getMethodName().equals("renderFontCentered")) {
                        m.replace("{" +
                                "if("+ MintySpire.class.getName() +".showHH() && ("+SlimeBoss.class.getName()+".ID.equals(this.id) || " +AcidSlime_L.class.getName()+".ID.equals(this.id) || " + SpikeSlime_L.class.getName()+".ID.equals(this.id) || "  +  Champ.class.getName()+".ID.equals(this.id) || "+ TimeEater.class.getName()+".ID.equals(this.id))) {" +
                                "$3 += " + TextRender.class.getName() + ".getHPTextAddition(this);" +
                                "}" +
                                "$proceed($$);" +
                                "}");
                    }
                }
            };
        }

        public static String getHPTextAddition(AbstractCreature c) {
            String addition = " (%d)";
            if(!(c.isDead || c.isDying)) {
                return String.format(addition, c.maxHealth/2);
            }
            return "";
        }
    }
}
