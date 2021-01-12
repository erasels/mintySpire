package mintySpire.patches.relics;

import basemod.ReflectionHacks;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.GameCursor;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.map.MapRoomNode;
import javassist.CannotCompileException;
import javassist.CtBehavior;

public class WingedBootsCursorPatches {
    @SpirePatch(clz = MapRoomNode.class, method = "update")
    public static class ExchangeCursor {
        public static boolean revert = false;
        public static Texture cursor;
        public static Texture newCursor;
        private static Hitbox hovered;
        //Inserts after node hoevered true
        @SpireInsertPatch(locator = NodeHoveredLocator.class, localvars = {"wingedConnection", "normalConnection"})
        public static void patch(MapRoomNode __instance, Hitbox ___hb,boolean wingedConnection, boolean normalConnection) {
            //Change cursor if hovering a node with Winged Boots active and eligible
            if(!revert && wingedConnection && !normalConnection) {
                hovered = ___hb;
                if(cursor == null) {
                    cursor = ReflectionHacks.getPrivate(CardCrawlGame.cursor, GameCursor.class, "img");
                    newCursor = new Texture(Gdx.files.internal("images/ui/wingedCursor.png"));
                }
                ReflectionHacks.setPrivate(CardCrawlGame.cursor, GameCursor.class, "img", newCursor);
                revert = true;
            }
        }

        @SpireInsertPatch(locator = NodeSelectedLocator.class)
        public static void patch2(MapRoomNode __instance) {
            //Revert if node clicked so it doesn't get stuck until next map opening
            if(revert) {
                revert();
            }
        }

        @SpirePostfixPatch
        public static void revertIfNeeded(MapRoomNode __instance, Hitbox ___hb) {
            //Revert if the previously hovered hitbox isn't hovered anymore
            if(revert && hovered == ___hb && !__instance.highlighted) {
                revert();
            }
        }

        private static class NodeHoveredLocator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.FieldAccessMatcher(Hitbox.class, "justHovered");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }

        private static class NodeSelectedLocator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(MapRoomNode.class, "playNodeSelectedSound");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }

        private static void revert() {
            ReflectionHacks.setPrivate(CardCrawlGame.cursor, GameCursor.class, "img", cursor);
            hovered = null;
            revert = false;
        }
    }
}
