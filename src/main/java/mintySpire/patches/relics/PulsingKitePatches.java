package mintySpire.patches.relics;

import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.relics.HoveringKite;
import javassist.*;

public class PulsingKitePatches {
    @SpirePatch2(clz = HoveringKite.class, method = "atTurnStart")
    public static class PlsThrobOwO {
        @SpirePostfixPatch
        public static void patch(HoveringKite __instance) {
            __instance.beginLongPulse();
        }
    }

    @SpirePatch2(clz = HoveringKite.class, method = "onManualDiscard")
    public static class OkayStopPlsO_O {
        @SpireInsertPatch(rloc = 1)
        public static void patch(HoveringKite __instance) {
            __instance.stopPulse();
        }
    }

    @SpirePatch(clz = HoveringKite.class, method = SpirePatch.CONSTRUCTOR)
    public static class ISaidStaphUnU {
        public static void Raw(CtBehavior ctMethodToPatch) throws CannotCompileException {
            CtClass ctClass = ctMethodToPatch.getDeclaringClass();

            CtMethod method = CtNewMethod.make(
                    CtClass.voidType, // Return
                    "onVictory", // Method name
                    new CtClass[]{}, //Paramters
                    null, // Exceptions
                    "{" +
                            "this.stopPulse();" +
                            "}",
                    ctClass
            );
            ctClass.addMethod(method);
        }
    }
}
