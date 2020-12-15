package mintySpire.patches.cards.betterUpdatePreview;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.screens.SingleCardViewPopup;
import mintySpire.MintySpire;

import java.util.ArrayList;

public class SingleCardViewPopupPatches {
    @SpirePatch(
            clz = SingleCardViewPopup.class,
            method = "open",
            paramtypez = {AbstractCard.class}
    )
    public static class OpenSingleCardPatch {
        public static void Postfix(SingleCardViewPopup _instance, AbstractCard card) {
            if (MintySpire.showBCUP() && card.upgraded) {
                SingleCardViewPopup.isViewingUpgrade = true;
                ArrayList<AbstractCard> unupgradedCards = CardFields.SCVPopup.unupgradedCardRewards.get(_instance);
                if (unupgradedCards != null) {
                    unupgradedCards.stream()
                            .filter(c -> c.cardID.equals(card.cardID))
                            .findFirst()
                            .ifPresent(currentCard -> ReflectionHacks.setPrivate(_instance,
                                    SingleCardViewPopup.class,
                                    "card",
                                    currentCard));
                }
            }
        }
    }
}
