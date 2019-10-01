package mintySpire.patches.cards;

import basemod.ReflectionHacks;
import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.blue.FTL;
import com.megacrit.cardcrawl.cards.colorless.Impatience;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.cardManip.CardGlowBorder;

import java.util.ArrayList;

public class CardBorderColorPatches {
    @SpirePatch(clz = CardGlowBorder.class, method = SpirePatch.CONSTRUCTOR)
    public static class CardGlowPatch {
        public static void Postfix(CardGlowBorder __instance, AbstractCard c) {
            if(FTL.ID.equals(c.cardID) && AbstractDungeon.actionManager.cardsPlayedThisTurn.size() < c.magicNumber) {
                setBorderColor(__instance, Color.YELLOW.cpy());
            } else if(Impatience.ID.equals(c.cardID) && !hasAttack(AbstractDungeon.player.hand.group)) {
                setBorderColor(__instance, Color.YELLOW.cpy());
            }
        }
    }

    private static boolean hasAttack(ArrayList<AbstractCard> cards) {
        for(AbstractCard c : cards) {
            if(c.type == AbstractCard.CardType.ATTACK) {
                return true;
            }
        }
        return false;
    }

    private static void setBorderColor(CardGlowBorder cgb, Color c) {
        ReflectionHacks.setPrivate(cgb, AbstractGameEffect.class, "color", c);
    }
}
