package mintySpire.patches.monsters;

import basemod.ReflectionHacks;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.Mugger;
import com.megacrit.cardcrawl.monsters.exordium.Looter;
import javassist.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ThiefStoleGoldDisplayPatches {
    private static final float GOLD_FONT = 32.0F;
    private static BitmapFont goldFont;
    private static FreeTypeFontGenerator.FreeTypeFontParameter param = (FreeTypeFontGenerator.FreeTypeFontParameter) ReflectionHacks.getPrivateStatic(FontHelper.class, "param");

    @SpirePatch(clz = FontHelper.class, method = "initialize")
    public static class AddMyFont {
        @SpireInsertPatch(locator = Locator.class)
        public static void Insert() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            Color tmp = param.borderColor.cpy();
            float tmp_w = param.borderWidth;
            param.borderColor = new Color(Color.GOLD.cpy().mul(0.45f, 0.45f, 0.45f, 0.75f));
            param.borderWidth = 2.0F * Settings.scale;
            Method method = FontHelper.class.getDeclaredMethod("prepFont", float.class, boolean.class);
            method.setAccessible(true);
            goldFont = (BitmapFont)method.invoke(null, new Object[] {GOLD_FONT, true});
            param.borderColor = tmp.cpy();
            param.borderWidth = tmp_w;
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.FieldAccessMatcher(FontHelper.class, "textAboveEnemyFont");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }

    @SpirePatch(clz = Mugger.class, method = SpirePatch.CONSTRUCTOR)
    @SpirePatch(clz = Looter.class, method = SpirePatch.CONSTRUCTOR)
    public static class RenderPostfixWorkaround {
        public static void Raw(CtBehavior ctMethodToPatch) throws NotFoundException, CannotCompileException {
            CtClass ctClass = ctMethodToPatch.getDeclaringClass();
            ClassPool pool = ctClass.getClassPool();
            CtClass ctSpriteBatch = pool.get(SpriteBatch.class.getName());

            CtMethod method = CtNewMethod.make(
                    CtClass.voidType, // Return
                    "render", // Method name
                    new CtClass[]{ctSpriteBatch}, //Paramters
                    null, // Exceptions
                    "{" +
                            "super.render($1);" +
                            ThiefStoleGoldDisplayPatches.class.getName() + ".writeAmount($1, $0.stolenGold, $0);" +
                            "}",
                    ctClass
            );
            ctClass.addMethod(method);
        }
    }

    public static void writeAmount(SpriteBatch sb, int stlGold, AbstractMonster m) {
        if (!m.isDeadOrEscaped()) {
            float nX = m.drawX + (m.hb_w * 0.35f);
            float nY = m.drawY + (15f * Settings.scale);

            FontHelper.renderFont(sb, goldFont, Integer.toString(stlGold), nX, nY, Color.GOLD);
            sb.setColor(Color.WHITE);
            sb.draw(ImageMaster.TP_GOLD, nX + (FontHelper.getWidth(goldFont, Integer.toString(stlGold), Settings.scale) - 5f), nY - ((ImageMaster.TP_GOLD.getHeight() / 2f) + 12f), 32.0F, 32.0F, 64.0F, 64.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 64, 64, false, false);
        }
    }
}
