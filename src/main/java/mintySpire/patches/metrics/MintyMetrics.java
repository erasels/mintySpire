package mintySpire.patches.metrics;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModInfo;
import com.evacipated.cardcrawl.modthespire.lib.SpireOverride;
import com.evacipated.cardcrawl.modthespire.lib.SpireSuper;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.PotionHelper;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.metrics.Metrics;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.potions.FirePotion;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.Circlet;
import mintySpire.MintySpire;

import java.util.ArrayList;
import java.util.HashMap;

import static com.megacrit.cardcrawl.helpers.TipTracker.pref;

public class MintyMetrics extends Metrics {
    public static String url = "http://mintymetrics.atwebpages.com";

    public static ArrayList<String> acts = new ArrayList<>();

    @SpireOverride
    private void sendPost(String url, final String fileToDelete) {
        SpireSuper.call(url, fileToDelete);
    }

    @SpireOverride
    private void gatherAllData(boolean death, boolean trueVictor, MonsterGroup monsters) {
        SpireSuper.call(death, trueVictor, monsters);
        HashMap<Object, Object> data = getParams();

        data.remove("is_prod");
        data.remove("seed_source_timestamp");
        data.remove("neow_cost");
        data.remove("special_seed");

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
            AbstractCard c = CardLibrary.getCard(s);
            if(c != null) {
                types.add("Card");
                continue;
            }

            AbstractRelic r = RelicLibrary.getRelic(s);
            if(r != null && !(r instanceof Circlet)) {
                types.add("Relic");
                continue;
            }

            AbstractPotion p = PotionHelper.getPotion(s);
            //Weird check because trying to get a potion that doesn't exist returns a fire potion
            if(!(p instanceof FirePotion) || s.equals(FirePotion.POTION_ID)) {
                types.add("Potion");
                continue;
            }

            types.add("Undefined");
        }
        addData("items_purchased_type", types);
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
}
