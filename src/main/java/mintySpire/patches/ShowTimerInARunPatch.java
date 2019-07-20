/*package mintySpire.patches;

import basemod.patches.com.megacrit.cardcrawl.ui.panels.TopPanel.TopPanelPatches;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.ui.panels.TopPanel;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;

public class ShowTimerInARunPatch {
    //@SpirePatch(clz = TopPanel.class, method = "render")
    @SpirePatch(clz = TopPanelPatches.RenderPatch.class, method = "Postfix")
    public static class TimeRenderAlways {
        public static ExprEditor Instrument() {
            return new ExprEditor() {
                @Override
                public void edit(FieldAccess f) throws CannotCompileException {
                    if (f.getClassName().equals(CardCrawlGame.class.getName()) && f.getFieldName().equals("stopClock")) {
                        f.replace("{" +
                                "$_ = ("+CardCrawlGame.class.getName()+".stopClock || " + CardCrawlGame.class.getName() + ".isInARun())" +
                                "}");
                    }
                }
            };
        }
    }
}
*/