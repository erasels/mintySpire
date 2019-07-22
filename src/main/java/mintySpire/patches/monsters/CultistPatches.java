package mintySpire.patches.monsters;

import com.esotericsoftware.spine.AnimationState;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.Cultist;
import com.megacrit.cardcrawl.powers.RitualPower;
import com.megacrit.cardcrawl.random.Random;
import javassist.CannotCompileException;
import javassist.CtBehavior;


public class CultistPatches {
    private static final float CHANCE = 0.1f;

    @SpirePatch(clz = Cultist.class, method=SpirePatch.CLASS)
    public static class cultistFields {
        public static SpireField<Boolean> willDoThing = new SpireField<>(() -> false);
    }

    @SpirePatch(
            clz = Cultist.class,
            method = "getMove"
    )
    public static class CultistMoveNamePatch {
        /*public static ExprEditor Instrument() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getClassName().equals(AbstractMonster.class.getName()) && m.getMethodName().equals("setMove")) {
                        m.replace("{" +
                                    "if(new "+ Random.class.getName()+ "().randomBoolean((float)"+CultistPatches.CHANCE+")) {" +
                                        CultistPatches.class.getName()+".doThing = true;" +
                                        "$0.setMove(\"DAB\", $2, $3);" +
                                    "} else {" +
                                        CultistPatches.class.getName()+".doThing = false;" +
                                        "$0.setMove($$);" +
                                    "}" +
                                "}");
                    }
                }
            };
        }*/

        @SpireInsertPatch(
                locator = Locator.class
        )
        public static SpireReturn<?> Insert(Cultist __instance, int num) {
            if(new Random().randomBoolean(CHANCE)) {
                cultistFields.willDoThing.set(__instance, true);
                __instance.setMove("DAB", (byte)3, AbstractMonster.Intent.BUFF);
                return SpireReturn.Return(null);
            }
            cultistFields.willDoThing.set(__instance, false);
            return SpireReturn.Continue();
        }

        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(Cultist.class, "setMove");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }

    @SpirePatch(
            clz = Cultist.class,
            method = "takeTurn"
    )
    public static class CultistPatch {
        @SpireInsertPatch(
                locator = Locator.class
        )
        public static void Insert(Cultist __instance) {
            if(cultistFields.willDoThing.get(__instance)) {
                try {
                    AnimationState.TrackEntry entry = __instance.state.setAnimation(1, "dab", false);
                    entry.setMix(0.8f);
                    entry.setEndTime(1);
                } catch (IllegalArgumentException ignored) {

                }
            }
        }

        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
                Matcher finalMatcher = new Matcher.NewExprMatcher(RitualPower.class);
                return LineFinder.findAllInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }
}