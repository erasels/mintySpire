package mintySpire.patches.relics;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.TinyChest;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.EventRoom;
import com.megacrit.cardcrawl.screens.DungeonMapScreen;
import javassist.*;

public class TinyChestPatches {
    @SpirePatch(
            clz = TinyChest.class,
            method = SpirePatch.CONSTRUCTOR
    )
    public static class TCPulse {
        public static void Raw(CtBehavior ctMethodToPatch) throws NotFoundException, CannotCompileException {
            CtClass ctClass = ctMethodToPatch.getDeclaringClass();
            ClassPool pool = ctClass.getClassPool();
            CtClass ctAbstractRoom = pool.get(AbstractRoom.class.getName());

            CtMethod method = CtNewMethod.make(
                    CtClass.voidType, // Return
                    "justEnteredRoom", // Method name
                    new CtClass[]{ctAbstractRoom}, //Paramters
                    null, // Exceptions
                    "{" +
                            "if(counter == (ROOM_COUNT - 1)) {" +
                            "beginLongPulse();" +
                            "} else {" +
                            "stopPulse();" +
                            "}" +
                            "}",
                    ctClass
            );
            ctClass.addMethod(method);
        }
    }

    @SpirePatch(clz = MapRoomNode.class, method = "render")
    public static class RenderTCBehindRelevantNode {
        @SpireInsertPatch(locator = Locator.class)
        public static void patch(MapRoomNode __instance, SpriteBatch sb, float ___scale, float ___SPACING_X, float ___OFFSET_X, float ___OFFSET_Y) {
            if(__instance.room instanceof EventRoom) {
                TinyChest r = (TinyChest) betterGetRelic(TinyChest.ID);
                if (r != null && r.counter == 3) {
                    if (___scale == (0.25F + __instance.color.a)) {
                        sb.draw(r.img,
                                __instance.x * ___SPACING_X + ___OFFSET_X - 64.0F + __instance.offsetX,
                                __instance.y * Settings.MAP_DST_Y + ___OFFSET_Y + DungeonMapScreen.offsetY - 64.0F + __instance.offsetY,
                                64.0F,
                                64.0F,
                                128.0F,
                                128.0F,
                                ___scale * Settings.scale,
                                ___scale * Settings.scale,
                                0.0F,
                                0,
                                0,
                                128,
                                128,
                                false,
                                false);
                    }
                }
            }
        }

        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.FieldAccessMatcher(Settings.class, "isMobile");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }

    public static int TCLoc = -1;
    public static AbstractRelic betterGetRelic(String id) {
        if(TCLoc > -1 && TCLoc < AbstractDungeon.player.relics.size() && AbstractDungeon.player.relics.get(TCLoc).relicId.equals(id)) {
            return AbstractDungeon.player.relics.get(TCLoc);
        } else {
            for(int i = 0; i < AbstractDungeon.player.relics.size() ; i++) {
                AbstractRelic r = AbstractDungeon.player.relics.get(i);
                if(r.relicId.equals(id)) {
                    TCLoc = i;
                    return r;
                }
            }
        }
        return null;
    }
}
