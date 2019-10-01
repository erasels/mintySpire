package mintySpire.patches;

import basemod.ReflectionHacks;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.screens.charSelect.CharacterOption;
import com.megacrit.cardcrawl.screens.charSelect.CharacterSelectScreen;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.util.ArrayList;

public class BetterAscensionSelectorPatches {
    @SpirePatch(clz = CharacterSelectScreen.class, method = "updateAscensionToggle")
    public static class HitboxCancelled {
        @SpireInsertPatch(locator = Locator.class)
        public static SpireReturn<?> Insert(CharacterSelectScreen __instance) {
            if (isAtMaxAsc(__instance)) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.FieldAccessMatcher(CharacterSelectScreen.class, "options");
                return new int[]{LineFinder.findAllInOrder(ctMethodToPatch, new ArrayList<Matcher>(), finalMatcher)[1]};
            }
        }
    }

    public static int counter1;
    public static int counter2;

    @SpirePatch(clz = CharacterSelectScreen.class, method = "renderAscensionMode")
    public static class RenderCareTaker {
        public static ExprEditor Instrument() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getClassName().equals(SpriteBatch.class.getName()) && m.getMethodName().equals("draw")) {
                        counter1++;
                        if (counter1 == 4) {
                            m.replace("{" +
                                    "if(!" + BetterAscensionSelectorPatches.class.getName() + ".isAtMaxAsc(this)) {" +
                                    "$proceed($$);" +
                                    "}" +
                                    "}");
                        }
                        return;
                    }
                    if (m.getClassName().equals(FontHelper.class.getName()) && m.getMethodName().equals("renderFontCentered")) {
                        counter2++;
                        if (counter2 == 3) {
                            m.replace("{" +
                                    "if(" + BetterAscensionSelectorPatches.class.getName() + ".isAtMaxAsc(this)) {" +
                                    "$proceed($1, $2, $3, $4, $5, " + Color.class.getName() + ".SALMON.cpy());" +
                                    "} else {" +
                                    "$proceed($$);" +
                                    "}" +
                                    "}");
                        }
                    }
                }
            };
        }
    }

    public static boolean isAtMaxAsc(CharacterSelectScreen cSS) {
        for (CharacterOption o : cSS.options) {
            if (o.selected) {
                int maxAsc = (int) ReflectionHacks.getPrivate(o, CharacterOption.class, "maxAscensionLevel");
                return cSS.ascensionLevel >= maxAsc;
            }
        }
        return false;
    }
}
