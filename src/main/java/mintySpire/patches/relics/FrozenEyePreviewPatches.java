package mintySpire.patches.relics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.relics.FrozenEye;
import com.megacrit.cardcrawl.ui.panels.DrawPilePanel;
import javassist.CtBehavior;

import java.lang.reflect.Field;

@SpirePatch(
        clz = DrawPilePanel.class,
        method = "render"
)
public class FrozenEyePreviewPatches {
    private static final float HB_W = 300.0F * Settings.scale;
    private static final float HB_H = 420.0F * Settings.scale;

    @SpireInsertPatch(
            locator = Locator.class
    )
    public static void Insert(DrawPilePanel __instance, SpriteBatch sb) {
        if (!AbstractDungeon.player.hasRelic(FrozenEye.ID) || AbstractDungeon.isScreenUp) {
            return;
        }

        AbstractCard hovered = null;
        int hoveredIndex = -1;

        for (int i = AbstractDungeon.player.gameHandSize - 1; i >= 0; --i) {
            if (i >= AbstractDungeon.player.drawPile.size()) {
                continue;
            }

            AbstractCard card = AbstractDungeon.player.drawPile.getNCardFromTop(i);
            AbstractCard ret = renderCard(__instance, sb, card, i, 0.45f, true);
            if (ret != null) {
                hovered = ret;
                hoveredIndex = i;
            }
        }

        if (hovered != null) {
            renderCard(__instance, sb, hovered, hoveredIndex, 0.80f, false);
        }
    }

    private static AbstractCard renderCard(DrawPilePanel __instance, SpriteBatch sb, AbstractCard card, int i, float scale, boolean hitbox) {
        AbstractCard hovered = null;

        // Save card's previous location etc
        float prev_current_x = card.current_x;
        float prev_current_y = card.current_y;
        float prev_drawScale = card.drawScale;
        float prev_angle = card.angle;

        card.current_x = __instance.current_x + (hitbox ? 75 : 245) * Settings.scale;
        card.current_y = __instance.current_y + (220 + (i * 27)) * Settings.scale;
        card.drawScale = scale;
        card.angle = 0;
        card.lighten(true);

        if (hitbox) {
            card.hb.move(card.current_x, card.current_y);
            card.hb.resize(HB_W * card.drawScale, HB_H * card.drawScale);
            card.hb.update();
            if (card.hb.hovered) {
                hovered = card;
            }
        }

        // This removes the shadow on the small cards because it looks ugly with them all overlapping
        Color frameShadowColor = null;
        float prev_frameShadow_a = 0;
        if (hitbox) {
            try {
                Field f = AbstractCard.class.getDeclaredField("frameShadowColor");
                f.setAccessible(true);
                frameShadowColor = (Color) f.get(card);
                prev_frameShadow_a = frameShadowColor.a;
                frameShadowColor.a = 0;
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        card.render(sb);

        if (hitbox) {
            frameShadowColor.a = prev_frameShadow_a;
        }

        // Reset the card to not mess up other rendering
        card.current_x = prev_current_x;
        card.current_y = prev_current_y;
        card.drawScale = prev_drawScale;
        card.angle = prev_angle;
        // TODO: undo lighten? Not sure if it's necessary

        return hovered;
    }

    public static class Locator extends SpireInsertLocator {

        @Override
        public int[] Locate(CtBehavior ctBehavior) throws Exception {
            Matcher matcher = new Matcher.MethodCallMatcher(Hitbox.class, "render");
            return LineFinder.findInOrder(ctBehavior, matcher);
        }
    }
}
