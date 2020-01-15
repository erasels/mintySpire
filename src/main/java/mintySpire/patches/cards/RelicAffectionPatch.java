package mintySpire.patches.cards;

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
import com.megacrit.cardcrawl.vfx.cardManip.CardGlowBorder;
import javassist.CannotCompileException;
import javassist.CtBehavior;

import java.util.ArrayList;

public class RelicAffectionPatch {
    @SpirePatch(clz = AbstractCard.class, method=SpirePatch.CLASS)
    public static class cardFields {
        public static SpireField<Boolean> isNecroAff = new SpireField<>(() -> false);
        public static SpireField<Boolean> isPenAff = new SpireField<>(() -> false);
    }

    private static AbstractRelic pNib = new PenNib();
    private static AbstractRelic nCon = new Necronomicon();

    @SpirePatch(clz = CardGlowBorder.class, method = SpirePatch.CONSTRUCTOR, paramtypez = {AbstractCard.class, Color.class})
    public static class CardGlowPatch {
        @SpirePostfixPatch
        public static void patch(CardGlowBorder __instance, AbstractCard c, Color col, @ByRef Color[] ___color) {
            if(shouldChangeGlow(c)) {
                ___color[0] = Color.PURPLE.cpy();
            }
        }
    }

    public static boolean shouldChangeGlow(AbstractCard c) {
        if(CardCrawlGame.isInARun()) {
            boolean isAffected = false;
            if (c.type == AbstractCard.CardType.ATTACK) {
                //Pennib
                PenNib pn = (PenNib) AbstractDungeon.player.getRelic(PenNib.ID);
                if (pn != null) {
                    if (pn.counter == PenNib.COUNT - 1) {
                        cardFields.isPenAff.set(c, true);
                        isAffected = true;
                    } else {
                        cardFields.isPenAff.set(c, false);
                    }
                } else {
                    cardFields.isPenAff.set(c, false);
                }

                //Necro
                if (c.costForTurn > 1 || (c.cost == -1 && AbstractDungeon.player.energy.energy > 1)) {
                    Necronomicon nm = (Necronomicon) AbstractDungeon.player.getRelic(Necronomicon.ID);
                    if (nm != null) {
                        if (nm.checkTrigger()) {
                            cardFields.isNecroAff.set(c, true);
                            isAffected = true;
                        } else {
                            cardFields.isNecroAff.set(c, false);
                        }
                    }
                } else {
                    cardFields.isNecroAff.set(c, false);
                }
            } else {
                cardFields.isNecroAff.set(c, false);
                cardFields.isPenAff.set(c, false);
            }
            return isAffected;
        }
        return false;
    }

    @SpirePatch(clz = AbstractCard.class, method = "renderCard")
    public static class RenderPatch {
        public static void Postfix(AbstractCard card, SpriteBatch sb, boolean b1, boolean b2) {
            int numRelics = 0;
            if (cardFields.isNecroAff.get(card)) {
                nCon.currentX = card.current_x + 390.0f * card.drawScale / 3.0f * Settings.scale;
                nCon.currentY = card.current_y + 546.0f * card.drawScale / 3.0f * Settings.scale;
                nCon.scale = card.drawScale;
                nCon.renderOutline(sb, false);
                nCon.render(sb);
                numRelics++;
            }

            if (cardFields.isPenAff.get(card)) {
                pNib.counter = -1;
                pNib.currentX = card.current_x + (390.0f + (numRelics*pNib.img.getWidth())) * card.drawScale / 3.0f * Settings.scale;
                pNib.currentY = card.current_y + 546.0f * card.drawScale / 3.0f * Settings.scale;
                pNib.scale = card.drawScale;
                pNib.renderOutline(sb, false);
                pNib.render(sb);
                //numRelics++;
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
