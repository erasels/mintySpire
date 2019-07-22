package mintySpire.patches.monsters;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.monsters.beyond.AwakenedOne;
import com.megacrit.cardcrawl.random.Random;

@SpirePatch(
        clz = AwakenedOne.class,
        method = "changeState"
)
public class AwakenedOnePatch {
    private static final float CHANCE = 0.1f;

    public static void Postfix(AwakenedOne __instance, String key) {
        if (key.equals("ATTACK_1") && new Random().randomBoolean(CHANCE)) {
            try {
                __instance.state.setAnimation(0, "dab", false);
            } catch (IllegalArgumentException ignored) {
                __instance.state.setAnimation(0, "Attack_1", false);
            }
            __instance.state.addAnimation(0, "Idle_1", true, 0);
        }
    }
}
