package mintySpire.patches.scenes;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import mintySpire.MintySpire;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SpirePatch(
        clz = CardCrawlGame.class,
        method = "startOverButShowCredits"
)
public class SkipCreditsAndNeowPatch {
    public static SpireReturn Prefix() {
        if (MintySpire.showCS()) {
            try {
                Field queueCreditsField = CardCrawlGame.class.getDeclaredField("queueCredits");
                queueCreditsField.setAccessible(true);
                queueCreditsField.setBoolean(null, false);

                Method doorUnlockScreenCheckMethod = CardCrawlGame.class.getDeclaredMethod("doorUnlockScreenCheck");
                doorUnlockScreenCheckMethod.setAccessible(true);

                doorUnlockScreenCheckMethod.invoke(null);

                CardCrawlGame.startOver = true;
                CardCrawlGame.fadeToBlack(2.0F);
                CardCrawlGame.music.fadeOutBGM();
                CardCrawlGame.music.fadeOutTempBGM();
                CardCrawlGame.music.changeBGM("MENU");
            } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException |
                     InvocationTargetException e) {
                e.printStackTrace();
            }

            return SpireReturn.Return(null);
        }
        return SpireReturn.Continue();
    }
}
