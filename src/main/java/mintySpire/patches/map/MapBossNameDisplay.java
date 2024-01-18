package mintySpire.patches.map;

import basemod.BaseMod;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.MonsterHelper;
import com.megacrit.cardcrawl.screens.DungeonMapScreen;
import mintySpire.MintySpire;

public class MapBossNameDisplay {
    private static Color oscillatingColor = Color.FIREBRICK.cpy();
    private static float oscillatingTimer = 0.0f;
    private static float oscillatingFader = 0.0f;
    private static String currentID;
    private static String bossName;

    @SpirePatch(clz = DungeonMapScreen.class, method = "render")
    public static class RenderBossName {
        public static void Postfix(DungeonMapScreen __instance, SpriteBatch sb) {
            if (MintySpire.showBN() && AbstractDungeon.screen == AbstractDungeon.CurrentScreen.MAP && !MiniMapDisplay.renderingMiniMap) {
                String name = getBossName();
                if (!name.isEmpty()) {
                    oscillatingFader += Gdx.graphics.getRawDeltaTime();
                    if (oscillatingFader > 1.0F) {
                        oscillatingFader = 1.0F;
                        oscillatingTimer += Gdx.graphics.getRawDeltaTime() * 5.0F;
                    }
                    oscillatingColor.a = (0.33F + (MathUtils.cos(oscillatingTimer) + 1.0F) / 3.0F) * oscillatingFader;
                    FontHelper.renderDeckViewTip(sb, name, Settings.HEIGHT - (180.0f * Settings.scale), oscillatingColor);
                }
            }
        }
    }

    private static String getBossName() {
        if(AbstractDungeon.bossKey.equals(currentID)) {
            return bossName;
        } else {
            currentID = AbstractDungeon.bossKey;
        }
        String encName = MonsterHelper.getEncounterName(currentID);
        String mName = BaseMod.getMonsterName(encName);
        if (!mName.isEmpty()) {
            bossName = mName;
        } else if (!encName.isEmpty()) {
            bossName = encName;
        } else {
            bossName = currentID;
        }
        return bossName;
    }
}
