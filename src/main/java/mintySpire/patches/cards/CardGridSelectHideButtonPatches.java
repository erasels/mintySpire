package mintySpire.patches.cards;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.screens.select.GridCardSelectScreen;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import mintySpire.ui.GridselectHideButton;

import java.util.ArrayList;

public class CardGridSelectHideButtonPatches {
    //Create Button Object for the panel
    @SpirePatch(
            clz= GridCardSelectScreen.class,
            method= SpirePatch.CLASS
    )
    public static class GridselectFields {
        public static SpireField<GridselectHideButton> hideField = new SpireField<>(() -> null);
    }

    //Add it in the ctor
    @SpirePatch(
            clz= GridCardSelectScreen.class,
            method= SpirePatch.CONSTRUCTOR
    )
    public static class ButtonAdder {
        public static void Postfix(GridCardSelectScreen __instance) {
            GridselectFields.hideField.set(__instance, new GridselectHideButton());
        }
    }

    /*@SpirePatch(clz = CardGroup.class, method = "render")
    @SpirePatch(clz = CardGroup.class, method = "renderExceptOneCard")
    public static class OptionsUpdateInjecter {
        public static SpireReturn<?> Prefix(CardGroup __instance) {
            if(AbstractDungeon.screen == AbstractDungeon.CurrentScreen.GRID && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT && GridselectFields.hideField.get(AbstractDungeon.gridSelectScreen).isActive) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }*/

    //Hook in the Update
    @SpirePatch(
            clz= GridCardSelectScreen.class,
            method= "update"
    )
    public static class UpdateInjecter {
        public static void Postfix(GridCardSelectScreen __instance) {
            GridselectFields.hideField.get(__instance).update();
        }
    }

    //Make it render
    @SpirePatch(
            clz= GridCardSelectScreen.class,
            method= "open",
            paramtypez = {CardGroup.class, int.class, String.class, boolean.class, boolean.class, boolean.class, boolean.class}
    )
    public static class ButtonShower {
        public static void Postfix(GridCardSelectScreen __instance, CardGroup group, int numCards, String tipMsg, boolean forUpgrade, boolean forTransform, boolean canCancel, boolean forPurge) {
            GridselectFields.hideField.get(__instance).show();
        }
    }

    @SpirePatch(
            clz= GridCardSelectScreen.class,
            method= "render"
    )
    public static class ButtonRenderer {
        public static void Postfix(GridCardSelectScreen __instance, SpriteBatch sb) {
            GridselectFields.hideField.get(__instance).render(sb);
        }
    }

    //Stop Scrollbar whilst hidden
    @SpirePatch(
            clz= GridCardSelectScreen.class,
            method= "updateScrolling"
    )
    public static class ScrollbarStopper {
        public static SpireReturn<?> Prefix(GridCardSelectScreen __instance) {
            if(GridselectFields.hideField.get(__instance).isActive) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    //Reset when closed
    @SpirePatch(clz=AbstractDungeon.class, method = "closeCurrentScreen")
    public static class StopUnrenderingOnConfirm {
        @SpireInsertPatch(locator = Locator.class)
        public static void Insert() {
            GridselectFields.hideField.get(AbstractDungeon.gridSelectScreen).close();
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(AbstractDungeon.class, "genericScreenOverlayReset");
                return new int[]{LineFinder.findAllInOrder(ctMethodToPatch, new ArrayList<Matcher>(), finalMatcher)[8]};
            }
        }
    }
}
