package mintySpire.patches.metrics;

import com.evacipated.cardcrawl.modthespire.lib.SpireOverride;
import com.evacipated.cardcrawl.modthespire.lib.SpireSuper;
import com.megacrit.cardcrawl.metrics.Metrics;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import mintySpire.MintySpire;

public class MintyMetrics extends Metrics {
    public static String url = "mintymetrics.atwebpages.com";

    @SpireOverride
    private void sendPost(String url, final String fileToDelete) {
        SpireSuper.call(url, fileToDelete);
    }

    @SpireOverride
    private void gatherAllData(boolean death, boolean trueVictor, MonsterGroup monsters) {
        SpireSuper.call(death, trueVictor, monsters);
    }

    @SpireOverride
    private void gatherAllDataAndSend(boolean death, boolean trueVictor, MonsterGroup monsters) {
        gatherAllData(death, trueVictor, monsters);
        sendPost(url, null);
    }

    @Override
    public void run() {
        MintySpire.runLogger.info("Minty is gathering data.");
        gatherAllDataAndSend(this.death, this.trueVictory, this.monsters);
    }
}
