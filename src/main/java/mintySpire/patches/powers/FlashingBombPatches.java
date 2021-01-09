package mintySpire.patches.powers;

import basemod.ReflectionHacks;
import com.badlogic.gdx.Gdx;
import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.megacrit.cardcrawl.powers.TheBombPower;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.combat.GainPowerEffect;
import javassist.*;

import java.util.ArrayList;

public class FlashingBombPatches {
    public static final float FLASH_TIMER = 1f;

    @SpirePatch(clz = TheBombPower.class, method=SpirePatch.CLASS)
    public static class TimerField {
        public static SpireField<Float> timer = new SpireField<>(() -> FLASH_TIMER);
    }

    @SpirePatch(clz = TheBombPower.class, method = SpirePatch.CONSTRUCTOR)
    public static class ChangeMethods {
        @SpireRawPatch
        public static void Raw(CtBehavior ctMethodToPatch) throws NotFoundException, CannotCompileException {
            CtClass ctClass = ctMethodToPatch.getDeclaringClass();

            //Don't play sfx if the amount is 1 since we'll be abusing a method for the flashing that calls this
            CtMethod method = CtNewMethod.make(
                    CtClass.voidType, // Return
                    "playApplyPowerSfx", // Method name
                    null, //Paramters
                    null, // Exceptions
                    "{" +
                                "if(amount > 1) {" +
                                    "super.playApplyPowerSfx();" +
                                "}" +
                            "}",
                    ctClass
            );
            ctClass.addMethod(method);

            CtMethod method2 = CtNewMethod.make(
                    CtClass.voidType, // Return
                    "update", // Method name
                    new CtClass[]{CtPrimitiveType.intType}, //Paramters
                    null, // Exceptions
                    "{" +
                                "super.update($1);" +
                                ChangeMethods.class.getName()+".doFlashLogic(this);" +
                            "}",
                    ctClass
            );
            ctClass.addMethod(method2);
        }

        public static void doFlashLogic(TheBombPower p) {
            if (p.amount == 1){
                float t = TimerField.timer.get(p);
                if (t <= 0f){
                    ArrayList<AbstractGameEffect> effects = ReflectionHacks.getPrivateInherited(p, TheBombPower.class, "effect");
                    effects.add(new GainPowerEffect(p));
                    TimerField.timer.set(p, FLASH_TIMER);
                } else {
                    TimerField.timer.set(p, t - Gdx.graphics.getRawDeltaTime());
                }
            }
        }
    }
}

