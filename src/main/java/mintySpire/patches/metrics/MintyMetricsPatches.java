package mintySpire.patches.metrics;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.metrics.Metrics;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.rooms.MonsterRoomBoss;
import com.megacrit.cardcrawl.rooms.MonsterRoomElite;
import com.megacrit.cardcrawl.screens.DeathScreen;
import com.megacrit.cardcrawl.screens.VictoryScreen;
import javassist.CannotCompileException;
import javassist.CtBehavior;

public class MintyMetricsPatches {
    @SpirePatch(clz = Metrics.class, method = "sendPost", paramtypez = {String.class, String.class})
    public static class SendPostPatch {
        @SpireInsertPatch(rloc = 1)
        public static void patch(Metrics __instance, @ByRef String[] url, String fTL) {
            if(__instance instanceof MintyMetrics) {
                url[0] = MintyMetrics.url;
            }
        }

    }

    @SpirePatch(clz = VictoryScreen.class, method = "submitVictoryMetrics")
    public static class RunMintyMetricsOnVictory {
        @SpireInsertPatch(locator = Locator.class)
        public static void patch(VictoryScreen __instance) {
            if (Settings.UPLOAD_DATA) {
                Metrics metrics = new MintyMetrics();
                metrics.setValues(false, true, null, Metrics.MetricRequestType.UPLOAD_METRICS);
                Thread t = new Thread(metrics);
                t.start();
            }
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(Settings.class, "isStandardRun");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }

    @SpirePatch(clz = DeathScreen.class, method = "submitDefeatMetrics")
    public static class RunMintyMetricsOnDefeat {
        @SpirePostfixPatch
        public static void patch(DeathScreen __insatnce, MonsterGroup m) {
            if (Settings.UPLOAD_DATA && (AbstractDungeon.actNum > 1 || AbstractDungeon.getCurrMapNode() != null && AbstractDungeon.getCurrRoom() != null && (AbstractDungeon.getCurrRoom() instanceof MonsterRoomBoss || AbstractDungeon.getCurrRoom() instanceof MonsterRoomElite))) {
                Metrics metrics = new MintyMetrics();
                metrics.setValues(true, false, m, Metrics.MetricRequestType.UPLOAD_METRICS);
                Thread t = new Thread(metrics);
                t.start();
            }
        }
    }

    @SpirePatch(clz = DeathScreen.class, method = "submitVictoryMetrics")
    public static class RunMintyMetricsOnDefeatButVictoryActually {
        @SpireInsertPatch(locator = RunMintyMetricsOnVictory.Locator.class)
        public static void patch(DeathScreen __instance) {
            if (Settings.UPLOAD_DATA) {
                Metrics metrics = new MintyMetrics();
                metrics.setValues(false, false, null, Metrics.MetricRequestType.UPLOAD_METRICS);
                Thread t = new Thread(metrics);
                t.start();
            }
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(Settings.class, "isStandardRun");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }
}
