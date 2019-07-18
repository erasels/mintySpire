package mintySpire.patches.cards;

import basemod.ReflectionHacks;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.Necronomicon;
import com.megacrit.cardcrawl.relics.PenNib;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.cardManip.CardGlowBorder;
import javassist.CannotCompileException;
import javassist.CtBehavior;

import java.util.ArrayList;

public class DoublePlayBorderColorPatch {
    @SpirePatch(clz = AbstractCard.class, method=SpirePatch.CLASS)
    public static class cardFields {
        public static SpireField<Boolean> isNecroAff = new SpireField<>(() -> false);
        public static SpireField<Boolean> isPenAff = new SpireField<>(() -> false);
    }

    private static AbstractRelic pNib = new PenNib();
    private static AbstractRelic nCon = new Necronomicon();

    @SpirePatch(clz = CardGlowBorder.class, method = SpirePatch.CONSTRUCTOR)
    public static class CardGlowPatch {
        public static void Postfix(CardGlowBorder __instance, AbstractCard c) {
            if(shouldChangeGlow(c)) {
                Color color = Color.PURPLE.cpy();
                ReflectionHacks.setPrivate(__instance, AbstractGameEffect.class, "color", color);
            } else {
                cardFields.isNecroAff.set(c, false);
                cardFields.isPenAff.set(c, false);
            }
        }
    }

    public static boolean shouldChangeGlow(AbstractCard c) {
        if(CardCrawlGame.isInARun()) {
            if (c.type == AbstractCard.CardType.ATTACK) {
                //Necro
                if (c.costForTurn > 1 || (c.cost == -1 && AbstractDungeon.player.energy.energy > 1)) {
                    Necronomicon nm = (Necronomicon) AbstractDungeon.player.getRelic(Necronomicon.ID);
                    if (nm != null) {
                        if (nm.checkTrigger()) {
                            cardFields.isNecroAff.set(c, true);
                            return true;
                        }
                    }
                }
                //Pennib
                PenNib pn = (PenNib) AbstractDungeon.player.getRelic(PenNib.ID);
                if (pn != null) {
                    if (pn.counter == PenNib.COUNT - 1) {
                        cardFields.isPenAff.set(c, true);
                        return true;
                    }
                }

            }
        }
        return false;
    }

    @SpirePatch(clz = AbstractCard.class, method = "renderCard")
    public static class RenderPatch {
        public static void Postfix(AbstractCard card, SpriteBatch sb, boolean b1, boolean b2) {
            if (cardFields.isNecroAff.get(card)) {
                nCon.currentX = card.current_x + 390.0f * card.drawScale / 3.0f * Settings.scale;
                nCon.currentY = card.current_y + 546.0f * card.drawScale / 3.0f * Settings.scale;
                nCon.scale = card.drawScale;
                nCon.renderOutline(sb, false);
                nCon.render(sb);
            } else if (cardFields.isPenAff.get(card)) {
                pNib.counter = -1;
                pNib.currentX = card.current_x + 390.0f * card.drawScale / 3.0f * Settings.scale;
                pNib.currentY = card.current_y + 546.0f * card.drawScale / 3.0f * Settings.scale;
                pNib.scale = card.drawScale;
                pNib.renderOutline(sb, false);
                pNib.render(sb);
            }
        }
    }

    @SpirePatch(clz = AbstractCard.class, method = "stopGlowing")
    public static class StopRenderingRelicPls {
        @SpireInsertPatch(locator = Locator.class)
        public static void Insert(AbstractCard __instance) {
            cardFields.isNecroAff.set(__instance, false);
            cardFields.isPenAff.set(__instance, false);
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.FieldAccessMatcher(CardGlowBorder.class, "duration");
                return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<Matcher>(), finalMatcher);
            }
        }
    }
}
