package mintySpire.patches.monsters;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.monsters.beyond.TimeEater;
import com.megacrit.cardcrawl.monsters.city.Champ;
import com.megacrit.cardcrawl.monsters.exordium.SlimeBoss;
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
                                "if("+ MintySpire.class.getName() +".showHH() && ("+SlimeBoss.class.getName()+".ID.equals(this.id) || "+ Champ.class.getName()+".ID.equals(this.id) || "+ TimeEater.class.getName()+".ID.equals(this.id))) {" +
                                FontHelper.class.getName()+".renderFontCentered(sb, "+FontHelper.class.getName()+".healthInfoFont, this.currentHealth + \"/\" + this.maxHealth + \" (\"+this.maxHealth/2+\")\", " +
                                "this.hb.cX, y + HEALTH_BAR_OFFSET_Y + HEALTH_TEXT_OFFSET_Y + 5.0F * "+ Settings.class.getName()+".scale, this.hbTextColor);" +
                                "} else {" +
                                FontHelper.class.getName()+".renderFontCentered(sb, "+FontHelper.class.getName()+".healthInfoFont, this.currentHealth + \"/\" + this.maxHealth, " +
                                "this.hb.cX, y + HEALTH_BAR_OFFSET_Y + HEALTH_TEXT_OFFSET_Y + 5.0F * "+ Settings.class.getName()+".scale, this.hbTextColor);" +
                                "}" +
                                "}");
                    }
                }
            };
        }
    }
}
//FontHelper.renderFontCentered(sb, FontHelper.healthInfoFont, this.currentHealth + "/" + this.maxHealth, this.hb.cX, y + HEALTH_BAR_OFFSET_Y + HEALTH_TEXT_OFFSET_Y + 5.0F * Settings.scale, this.hbTextColor);