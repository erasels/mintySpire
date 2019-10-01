package mintySpire.patches.cards;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.screens.CardRewardScreen;
import mintySpire.ui.CardrewardHideButton;

import java.util.ArrayList;

public class MidCombatCardrewardHideButtonPatches {
    //Generic hooks
    @SpirePatch(
            clz= CardRewardScreen.class,
            method= SpirePatch.CLASS
    )
    public static class HideFields {
        public static SpireField<CardrewardHideButton> hideField = new SpireField<>(() -> null);
    }
    @SpirePatch(
            clz= CardRewardScreen.class,
            method= SpirePatch.CONSTRUCTOR
    )
    public static class ButtonAdder {
        public static void Postfix(CardRewardScreen __instance) {
            HideFields.hideField.set(__instance, new CardrewardHideButton());
        }
    }
    @SpirePatch(
            clz= CardRewardScreen.class,
            method= "update"
    )
    public static class UpdateInjecter {
        public static void Postfix(CardRewardScreen __instance) {
            HideFields.hideField.get(__instance).update();
        }
    }
    @SpirePatch(
            clz= CardRewardScreen.class,
            method= "render"
    )
    public static class ButtonRenderer {
        public static void Postfix(CardRewardScreen __instance, SpriteBatch sb) {
            HideFields.hideField.get(__instance).render(sb);
        }
    }

    //Open when needed
    @SpirePatch(clz = CardRewardScreen.class, method = "reopen") //probably unneeded
    @SpirePatch(clz = CardRewardScreen.class, method = "discoveryOpen", paramtypez = {})
    public static class Shower1{
        public static void Postfix(CardRewardScreen __instance) {
            HideFields.hideField.get(__instance).show();
        }
    }

    @SpirePatch(clz = CardRewardScreen.class, method = "discoveryOpen", paramtypez = {AbstractCard.CardType.class})
    public static class Shower2{
        public static void Postfix(CardRewardScreen __instance, AbstractCard.CardType cT) {
            HideFields.hideField.get(__instance).show();
        }
    }

    @SpirePatch(clz = CardRewardScreen.class, method = "open")
    public static class Shower3{
        public static void Postfix(CardRewardScreen __instance, ArrayList<AbstractCard> cards, RewardItem rI, String s) {
            HideFields.hideField.get(__instance).show();
        }
    }

    //Close when needed
    @SpirePatch(clz = CardRewardScreen.class, method = "onClose")
    @SpirePatch(clz = CardRewardScreen.class, method = "reset")
    public static class Hider {
        public static void Postfix(CardRewardScreen __instance) {
            HideFields.hideField.get(__instance).close();
        }
    }

    //Disable Scrollbar while hidden
    @SpirePatch(clz = CardRewardScreen.class, method = "updateScrolling")
    @SpirePatch(clz = CardRewardScreen.class, method = "resetScrolling")
    public static class ScrollDisabler {
        public static SpireReturn<?> Prefix(CardRewardScreen __insatnce) {
            if(HideFields.hideField.get(__insatnce).isActive) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
