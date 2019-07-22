package mintySpire.patches.monsters;

import basemod.ReflectionHacks;
import com.badlogic.gdx.Gdx;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Skeleton;
import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.city.Byrd;
import com.megacrit.cardcrawl.relics.PhilosopherStone;
import com.megacrit.cardcrawl.vfx.AwakenedEyeParticle;
import javassist.*;

public class WokeByrdsPatches {
    @SpirePatch(clz = Byrd.class, method=SpirePatch.CLASS)
    public static class wokeFields {
        public static SpireField<Boolean> canWoke = new SpireField<>(() -> true);
        public static SpireField<Boolean> isWoke = new SpireField<>(() -> false);
        public static SpireField<Bone> wokePos = new SpireField<>(() -> null);
        public static SpireField<Skeleton> wokeSkel = new SpireField<>(() -> null);
        public static SpireField<Float> wokeTimer = new SpireField<>(() -> 0.0f);
    }

    @SpirePatch(clz = Byrd.class, method = SpirePatch.CONSTRUCTOR)
    public static class WokeEyePositioner {
        public static void Postfix(Byrd __instance, float x, float y) {
            wokeFields.isWoke.set(__instance, AbstractDungeon.player.hasRelic(PhilosopherStone.ID));
            wokeFields.wokeSkel.set(__instance, (Skeleton) ReflectionHacks.getPrivate(__instance, AbstractCreature.class, "skeleton"));
            wokeFields.wokePos.set(__instance, wokeFields.wokeSkel.get(__instance).findBone("eye"));
        }
    }

    @SpirePatch(
            clz = Byrd.class,
            method = SpirePatch.CONSTRUCTOR
    )
    public static class Wokifier {
        public static void Raw(CtBehavior ctMethodToPatch) throws NotFoundException, CannotCompileException {
            CtClass ctClass = ctMethodToPatch.getDeclaringClass();

            CtMethod method = CtNewMethod.make(
                    CtClass.voidType, // Return
                    "update", // Method name
                    new CtClass[]{}, //Paramters
                    null, // Exceptions
                    "{" +
                                "super.update();" +
                                WokeByrdsPatches.class.getName()+".becomeWOKE($0);" +
                            "}",
                    ctClass
            );
            ctClass.addMethod(method);
        }
    }

    @SpirePatch(clz = Byrd.class, method = "changeState")
    public static class WokeChecker {
        public static void Prefix(Byrd __instance, String sName) {
            wokeFields.canWoke.set(__instance, sName.equals("FLYING"));
        }
    }

    public static void becomeWOKE(Byrd __instance) {
        if (!__instance.isDying && wokeFields.isWoke.get(__instance) && wokeFields.canWoke.get(__instance)) {
            wokeFields.wokeTimer.set(__instance, wokeFields.wokeTimer.get(__instance)-Gdx.graphics.getDeltaTime());
            if (wokeFields.wokeTimer.get(__instance) < 0.0F) {
                wokeFields.wokeTimer.set(__instance, 0.1F);
                Skeleton wS = wokeFields.wokeSkel.get(__instance);
                Bone wP = wokeFields.wokePos.get(__instance);
                AbstractDungeon.effectList.add(new AwakenedEyeParticle(wS.getX() + wP.getWorldX(), wS.getY() + wP.getWorldY()));
            }
        }
    }
}