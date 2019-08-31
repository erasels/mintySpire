package mintySpire.patches.monsters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.Skeleton;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.IntangiblePlayerPower;
import com.megacrit.cardcrawl.powers.IntangiblePower;
import javassist.CannotCompileException;
import javassist.CtBehavior;

import java.util.ArrayList;

public class CreatureIntagibleTransparency {
    //If performance hit too big, add a boolean to abstract monster and set it to true false if the intagible power gets applied and removed. Check for it instead of hasPower
    //Monsters
    @SpirePatch(clz = AbstractMonster.class, method = "render")
    public static class RenderAtlasColorChanger {
        @SpireInsertPatch(locator = Locator.class)
        public static void Insert(AbstractMonster __instance, SpriteBatch sb) {
            if(hasAnyPower(__instance, IntangiblePlayerPower.POWER_ID, IntangiblePower.POWER_ID)) {
                sb.setColor(oscillarator(__instance.tint.color));
            }
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(SpriteBatch.class, "draw");
                return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<Matcher>(), finalMatcher);
            }
        }
    }

    @SpirePatch(clz = AbstractMonster.class, method = "render")
    public static class RenderSkeletonColorChanger {
        @SpireInsertPatch(locator = Locator.class, localvars = {"skeleton"})
        public static void Insert(AbstractMonster __instance, SpriteBatch sb, @ByRef Skeleton[] tmp) {
            if(hasAnyPower(__instance, IntangiblePlayerPower.POWER_ID, IntangiblePower.POWER_ID)) {
                tmp[0].setColor(oscillarator(__instance.tint.color));
            }
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(Skeleton.class, "setFlip");
                return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<Matcher>(), finalMatcher);
            }
        }
    }

    public static boolean hasAnyPower(AbstractCreature c, String... powers) {
        for(String s : powers) {
            if(c.hasPower(s)) {
                return true;
            }
        }
        return false;
    }


    //Players
    @SpirePatch(clz = AbstractPlayer.class, method = "renderPlayerImage")
    public static class PlayerRenderAtlasColorChanger1 {
        @SpireInsertPatch(locator = Locator.class)
        public static void Insert(AbstractPlayer __instance, SpriteBatch sb) {
            if(__instance.hasPower(IntangiblePlayerPower.POWER_ID)) {
                sb.setColor(oscillarator(Color.WHITE));
            }
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(SpriteBatch.class, "draw");
                return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<Matcher>(), finalMatcher);
            }
        }
    }

    @SpirePatch(clz = AbstractPlayer.class, method = "render")
    public static class PlayerRenderAtlasColorChanger2 {
        @SpireInsertPatch(locator = Locator.class)
        public static void Insert(AbstractPlayer __instance, SpriteBatch sb) {
            if(__instance.hasPower(IntangiblePlayerPower.POWER_ID)) {
                sb.setColor(oscillarator(Color.WHITE));
            }
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(SpriteBatch.class, "draw");
                return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<Matcher>(), finalMatcher);
            }
        }
    }

    @SpirePatch(clz = AbstractPlayer.class, method = "renderPlayerImage")
    public static class PlayerRenderSkeletonColorChanger {
        @SpireInsertPatch(locator = Locator.class, localvars = {"skeleton"})
        public static void Insert(AbstractPlayer __instance, SpriteBatch sb, @ByRef Skeleton[] tmp) {
            if(__instance.hasPower(IntangiblePlayerPower.POWER_ID)) {
                tmp[0].setColor(oscillarator(__instance.tint.color));
            }
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(Skeleton.class, "setFlip");
                return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<Matcher>(), finalMatcher);
            }
        }
    }

    private static float oscillatingTimer = 0.0f;
    private static float oscillatingFader = 0.0f;
    public static Color oscillarator(Color c) {
        oscillatingFader += Gdx.graphics.getRawDeltaTime();
        if (oscillatingFader > 0.66F) {
            oscillatingFader = 0.66F;
            oscillatingTimer += Gdx.graphics.getRawDeltaTime() * 1.5f;
        }
        Color col = c.cpy();
        col.a = (0.33F + (MathUtils.cos(oscillatingTimer) + 1.0F) / 3.0F) * oscillatingFader;
        return col;
    }
}

