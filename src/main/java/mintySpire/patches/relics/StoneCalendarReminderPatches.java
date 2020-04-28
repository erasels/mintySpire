package mintySpire.patches.relics;

import basemod.BaseMod;
import basemod.ReflectionHacks;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.StoneCalendar;
import com.megacrit.cardcrawl.vfx.PlayerTurnEffect;
import org.apache.commons.lang3.math.NumberUtils;

import java.lang.reflect.Field;


public class StoneCalendarReminderPatches {
    private static final int SC_TURNS = (int) ReflectionHacks.getPrivateStatic(StoneCalendar.class, "TURNS");
    private static Field colField = null;
    private static final Color MY_COLOR = Color.WHITE.cpy();

    @SpirePatch(clz = PlayerTurnEffect.class, method = "render")
    public static class FlashCalendar {
        @SpirePostfixPatch
        public static void patch(PlayerTurnEffect __instance, SpriteBatch sb) {
            if(colField == null) {
                colField = getField(PlayerTurnEffect.class, "color");
                colField.setAccessible(true);
            }
            if(!__instance.isDone) {
                StoneCalendar rel = (StoneCalendar) AbstractDungeon.player.getRelic(StoneCalendar.ID);

                if(rel != null && rel.counter < SC_TURNS) {
                    try {
                        MY_COLOR.a = ((Color)colField.get(__instance)).a;
                    } catch (Exception e) {
                        MY_COLOR.a = 1f;
                    } finally {
                        float i = NumberUtils.max(rel.counter, 1)/(float)SC_TURNS;
                        MY_COLOR.a *= i;
                    }
                    
                    rel.loadLargeImg();
                    sb.setColor(MY_COLOR);
                    sb.draw(rel.largeImg, Settings.WIDTH*0.66f, Settings.HEIGHT*0.66f, rel.largeImg.getWidth() * (0.5f+Settings.scale), rel.largeImg.getHeight() * (0.5f+Settings.scale));
                }
            }
        }

        private static Field getField(Class clazz, String fieldName) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                Class superClass = clazz.getSuperclass();
                if (superClass == null) {
                    BaseMod.logger.warn("Shouldn't see, this, yell at Minty Spire.");
                    return null;
                } else {
                    return getField(superClass, fieldName);
                }
            }
        }
    }
}
