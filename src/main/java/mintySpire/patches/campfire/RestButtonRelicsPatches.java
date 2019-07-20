package mintySpire.patches.campfire;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.DreamCatcher;
import com.megacrit.cardcrawl.relics.RegalPillow;
import com.megacrit.cardcrawl.ui.campfire.RestOption;
import javassist.*;

public class RestButtonRelicsPatches {
    @SpirePatch(
            clz = RestOption.class,
            method = SpirePatch.CONSTRUCTOR
    )
    public static class OverrideButtonRender {
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
                                "if(this.usable) {" +
                                    RestButtonRelicsPatches.class.getName()+".renderRestRelics($1, this);" +
                                "}" +
                            "}",
                    ctClass
            );
            ctClass.addMethod(method);
        }
    }

    private static AbstractRelic rPil = new RegalPillow();
    private static AbstractRelic dCat = new DreamCatcher();

    public static void renderRestRelics(SpriteBatch sb, RestOption rOpt) {
        int numRelics = 0;
        if (AbstractDungeon.player.hasRelic(RegalPillow.ID)) {
            rPil.currentX = rOpt.hb.cX + 1.0f * Settings.scale;
            rPil.currentY = rOpt.hb.cY + 64.0f * Settings.scale;
            rPil.renderOutline(sb, false);
            rPil.render(sb);
            numRelics++;
        }

        if (AbstractDungeon.player.hasRelic(DreamCatcher.ID)) {
           dCat.currentX = rOpt.hb.cX + ((numRelics*rPil.img.getWidth()) / 3.0F) * Settings.scale;
           dCat.currentY = rOpt.hb.cY + 64.0f * Settings.scale;
           dCat.renderOutline(sb, false);
           dCat.render(sb);
        }
    }
}
