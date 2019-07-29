package mintySpire.patches.cards;

import basemod.ReflectionHacks;
import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.blue.FTL;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.cardManip.CardGlowBorder;

public class FTLBorderColor {
    @SpirePatch(clz = CardGlowBorder.class, method = SpirePatch.CONSTRUCTOR)
    public static class CardGlowPatch {
        public static void Postfix(CardGlowBorder __instance, AbstractCard c) {
            if(FTL.ID.equals(c.cardID) && AbstractDungeon.actionManager.cardsPlayedThisTurn.size() < c.magicNumber) {
                Color color = Color.YELLOW.cpy();
                ReflectionHacks.setPrivate(__instance, AbstractGameEffect.class, "color", color);
            }
        }
    }
}
