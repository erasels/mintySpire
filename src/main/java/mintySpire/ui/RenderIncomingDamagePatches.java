package mintySpire.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import javassist.CannotCompileException;
import javassist.CtBehavior;

import java.lang.reflect.Field;

import static mintySpire.MintySpire.runLogger;
import static mintySpire.MintySpire.showTID;

public class RenderIncomingDamagePatches {
   @SpirePatch(clz = AbstractDungeon.class, method = "render")
    public static class TIDHook {
        private static Field multiIntentField;

        @SpireInsertPatch(locator = Locator.class)
        public static void hook(AbstractDungeon __instance, SpriteBatch sb) {
            if(showTID()) {
                if (AbstractDungeon.player != null && AbstractDungeon.currMapNode != null && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
                    if(multiIntentField == null) {
                        try {
                            multiIntentField = AbstractMonster.class.getDeclaredField("intentMultiAmt");
                            multiIntentField.setAccessible(true);
                        } catch (Exception e) {
                            runLogger.error("Exception occurred when getting private field " + "intentMultiAmt" + " of " + AbstractMonster.class.getName(), e);
                        }
                    }
                    int c = 0, dmg = 0, tmp = 0;
                    if(AbstractDungeon.getMonsters() != null) {
                        for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
                            if (!m.isDeadOrEscaped() && isAttacking(m) && !MonsterIntentHiddenField.isIntentHidden.get(m)) {
                                c++;
                                int multiAmt = 0;
                                try {
                                    multiAmt = (int) multiIntentField.get(m);
                                } catch (IllegalAccessException ignored) {
                                }
                                tmp = m.getIntentDmg();
                                if (multiAmt > 1) {
                                    tmp *= multiAmt;
                                }

                                if (tmp > 0) {
                                    dmg += tmp;
                                }
                            }
                        }

                        if (c > 0 && dmg > 0) {
                            float x = AbstractDungeon.player.hb.cX;
                            float y = AbstractDungeon.player.hb.y + AbstractDungeon.player.hb_h;
                            y += 10f * Settings.scale;

                            FontHelper.renderFontCentered(sb, FontHelper.damageNumberFont, Integer.toString(dmg), x, y, Color.SALMON, 0.5f);
                            Texture tex = getAttackIntent(dmg);

                            Color backupCol = sb.getColor();
                            sb.setColor(Color.WHITE);
                            float width = tex.getWidth() / 2.0f;
                            sb.draw(tex,
                                    x - FontHelper.getSmartWidth(FontHelper.damageNumberFont, Integer.toString(dmg), 9999.0F, 0.0F, 0.5f) - (width/2f),
                                    y - (width/2f),
                                    width,
                                    tex.getHeight() / 2f);
                            sb.setColor(backupCol);
                        }
                    }
                }
            }
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(AbstractDungeon.class, "getCurrRoom");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }

    public static boolean isAttacking(AbstractMonster m) {
        return m.intent == AbstractMonster.Intent.ATTACK || m.intent == AbstractMonster.Intent.ATTACK_BUFF || m.intent == AbstractMonster.Intent.ATTACK_DEBUFF || m.intent == AbstractMonster.Intent.ATTACK_DEFEND;
    }

    public static Texture getAttackIntent(int dmg) {
        if (dmg < 5)
            return ImageMaster.INTENT_ATK_1;
        if (dmg < 10)
            return ImageMaster.INTENT_ATK_2;
        if (dmg < 15)
            return ImageMaster.INTENT_ATK_3;
        if (dmg < 20)
            return ImageMaster.INTENT_ATK_4;
        if (dmg < 25)
            return ImageMaster.INTENT_ATK_5;
        if (dmg < 30)
            return ImageMaster.INTENT_ATK_6;
        return ImageMaster.INTENT_ATK_7;
    }

    @SpirePatch(clz = AbstractMonster.class, method=SpirePatch.CLASS)
    public static class MonsterIntentHiddenField {
        public static SpireField<Boolean> isIntentHidden = new SpireField<>(() -> false);
    }

    @SpirePatch(clz = AbstractMonster.class, method = "render")
    public static class CheckIfIntentHidden {
        @SpireInsertPatch(locator = Locator.class)
        public static void patchBeforeRender(AbstractMonster __instance, SpriteBatch sb) {
            MonsterIntentHiddenField.isIntentHidden.set(__instance, true);
        }

        @SpireInsertPatch(locator = Locator2.class)
        public static void patchInRender(AbstractMonster __instance, SpriteBatch sb) {
            MonsterIntentHiddenField.isIntentHidden.set(__instance, false);
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(AbstractPlayer.class, "hasRelic");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }

        private static class Locator2 extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(AbstractMonster.class, "renderIntentVfxBehind");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }
}
