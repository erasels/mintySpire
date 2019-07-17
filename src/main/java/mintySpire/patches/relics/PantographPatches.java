package mintySpire.patches.relics;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.IntangiblePlayerPower;
import com.megacrit.cardcrawl.powers.IntangiblePower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.Pantograph;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoomBoss;
import javassist.*;
import mintySpire.patches.CreatureIntagibleTransparency;

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
                    null, // Return
                    "onEnterRoom", // Method name
                    new CtClass[]{ctAbstractRoom}, //Paramters
                    null, // Exceptions
                    "if(com.megacrit.cardcrawl.dungeons.AbstractDungeon.nextRoom.room instanceof com.megacrit.cardcrawl.rooms.MonsterRoomBoss) {" +
                            "this.beginLongPulse();" +
                            "}",
                    ctClass
            );
            ctClass.addMethod(method);
        }
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
