package mintySpire.patches.metrics;

import basemod.BaseMod;
import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModInfo;
import com.evacipated.cardcrawl.modthespire.lib.SpireOverride;
import com.evacipated.cardcrawl.modthespire.lib.SpireSuper;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.PotionHelper;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.metrics.Metrics;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import mintySpire.MintySpire;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;

import static com.megacrit.cardcrawl.helpers.TipTracker.pref;

public class MintyMetrics extends Metrics {
    public static String url = "http://mintymetrics2.atwebpages.com";

    public static ArrayList<String> acts = new ArrayList<>();

    @SpireOverride
    private void sendPost(String url, final String fileToDelete) {
        SpireSuper.call(url, fileToDelete);
    }

    @SpireOverride
    private void gatherAllData(boolean death, boolean trueVictor, MonsterGroup monsters) {
        SpireSuper.call(death, trueVictor, monsters);
        HashMap<Object, Object> data = getParams();

        //Fix win_rate field
        int numVictory = pref.getInteger("WIN_COUNT", 0);
        int numDeath = pref.getInteger("LOSE_COUNT", 0);
        if (numVictory <= 0) {
            addData("win_rate", 0.0F);
        } else {
            addData("win_rate", (float)numVictory / (float)(numDeath + numVictory));
        }

        //Add modlist to toplevel
        ArrayList<String> modsinfos = new ArrayList<>();
        for(ModInfo m : Loader.MODINFOS) {
            modsinfos.add(m.Name);
        }
        modsinfos.sort(String::compareTo);
        addData("mods", modsinfos);

        //Add Ids of acts visited this run
        addData("acts_visited", acts);

        //Polish up items_purchased with prefix denoting their type
        ArrayList<String> purchases = (ArrayList<String>) data.get("items_purchased");
        ArrayList<String> types = new ArrayList<>();
        for(String s : purchases) {
            int loc = s.lastIndexOf("+");
            if(loc != -1 && StringUtils.isNumeric(s.substring(loc+1))) {
                s = s.substring(0, loc);
            }

            if(CardLibrary.cards.containsKey(s)) {
                types.add("Card");
            } else if(isRelicId(s)) {
                types.add("Relic");
            } else if(PotionHelper.isAPotion(s)) {
                types.add("Potion");
            } else {
                types.add("Undefined");
            }
        }
        addData("items_purchased_type", types);

        addData("language", Settings.language);
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
        acts.clear();
    }

    public HashMap<Object, Object> getParams() {
        return ReflectionHacks.getPrivateInherited(this, MintyMetrics.class, "params");
    }

    @SpireOverride
    private void addData(Object key, Object value) {
        SpireSuper.call(key, value);
    }

    private static boolean isRelicId(String key) {
        if (key == null) return false;
        if (RelicLibrary.isARelic(key)) return true;

        // BaseMod custom pools
        // Mirrors BaseMod.getCustomRelic's scan but returns boolean instead of Circlet.
        HashMap<AbstractCard.CardColor, HashMap<String, AbstractRelic>> pools = BaseMod.getAllCustomRelics();
        if (pools != null) {
            for (HashMap<String, AbstractRelic> map : pools.values()) {
                if (map != null && map.containsKey(key)) return true;
            }
        }

        return false;
    }
}
