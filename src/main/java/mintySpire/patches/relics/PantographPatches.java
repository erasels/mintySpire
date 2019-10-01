package mintySpire.patches.relics;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.relics.Pantograph;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import javassist.*;

import java.util.ArrayList;

public class PantographPatches {
    @SpirePatch(
            clz = Pantograph.class,
            method = SpirePatch.CONSTRUCTOR
    )
    public static class PantographStartPulse {
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
                            "if(mintySpire.patches.relics.PantographPatches.isMapNodeBeforeBossRoom()) {" +
                            "this.beginLongPulse();" +
                            "}" +
                            "}",
                    ctClass
            );
            ctClass.addMethod(method);
        }
    }

    public static boolean isMapNodeBeforeBossRoom() {
        if (CardCrawlGame.isInARun()) {
            MapRoomNode n = AbstractDungeon.getCurrMapNode();
            if (n != null) {
                for (MapRoomNode m : AbstractDungeon.map.get(AbstractDungeon.map.size() - 1)) {
                    //System.out.println("Node: " + n + " " + n.getRoomSymbol(true) + " NY: " + n.y + " M: " + m.y + " M|N: " + n.isConnectedTo(m));
                    if (m.y == n.y) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @SpirePatch(clz = Pantograph.class, method = "atBattleStart")
    public static class PantographStopPulse {
        @SpireInsertPatch(locator = Locator.class)
        public static void Insert(Pantograph __instance) {
            __instance.stopPulse();
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(Pantograph.class, "flash");
                return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<Matcher>(), finalMatcher);
            }
        }
    }
}
/*
"{java.util.ArrayList<com.megacrit.cardcrawl.map.MapRoomNode> visibleMapNodes = "+
                    "(java.util.ArrayList<com.megacrit.cardcrawl.map.MapRoomNode>) basemod.ReflectionHacks.getPrivate(com.megacrit.cardcrawl.dungeons.AbstractDungeon.dungeonMapScreen, com.megacrit.cardcrawl.screens.DungeonMapScreen.class, \"visibleMapNodes\");"+
                        "for(com.megacrit.cardcrawl.map.MapRoomNode n : visibleMapNodes) {"+
                            "if (n.y == com.megacrit.cardcrawl.dungeons.AbstractDungeon.getCurrMapNode().y+1 && (n.room instanceof com.megacrit.cardcrawl.rooms.MonsterRoomBoss)){"+
                                "this.beginLongPulse();"+
                    "}}}"

 */
/*"{if(com.megacrit.cardcrawl.dungeons.AbstractDungeon.nextRoom.room instanceof com.megacrit.cardcrawl.rooms.MonsterRoomBoss) {" +
                            "this.beginLongPulse();" +
                            "}" +
                            "return;" +
                            "}"
                            */
