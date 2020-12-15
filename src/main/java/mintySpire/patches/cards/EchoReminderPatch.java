package mintySpire.patches.cards;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.blue.EchoForm;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.EchoPower;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import mintySpire.MintySpire;

import java.lang.reflect.Field;

@SpirePatch(clz = AbstractCard.class, method = "renderEnergy")
public class EchoReminderPatch {

    //Since MintySpire doesn't use TextureLoader I'll instantiate the texture this way
    private static Texture doubleTexture = new Texture(Gdx.files.internal("images/powers/echoFormReminder/double_card.png"));
    private static TextureAtlas.AtlasRegion doubleRegion = new TextureAtlas.AtlasRegion(doubleTexture, 0, 0, doubleTexture.getWidth(), doubleTexture.getHeight());

    //TODO Work out an elegant way for the glow text to be slightly larger than the original, while maintaining the same origin
    @SpirePostfixPatch
    public static void patch(AbstractCard __instance, SpriteBatch sb) {
        if (echoFormValidChecker(__instance)) {
            sb.setColor(Color.WHITE);
            renderHelper(sb, doubleRegion, __instance.current_x, __instance.current_y, __instance);
            //glow effect implementation taken from GK's SpicyShops
            sb.setBlendFunction(770, 1);
            sb.setColor(new Color(1.0F, 1.0F, 1.0F, (MathUtils.cosDeg((float) (System.currentTimeMillis() / 5L % 360L)) + 1.25F) / 3.0F));
            renderHelper(sb, doubleRegion, __instance.current_x, __instance.current_y, __instance);
            sb.setBlendFunction(770, 771);
            sb.setColor(Color.WHITE);
        }
    }

    //code to render an image on top of all relevant cards taken from Jedi's Ranger
    private static void renderHelper(SpriteBatch sb, TextureAtlas.AtlasRegion img, float drawX, float drawY, AbstractCard C) {
        sb.draw(img, drawX + img.offsetX - (float) img.originalWidth / 2.0F, drawY + img.offsetY - (float) img.originalHeight / 2.0F, (float) img.originalWidth / 2.0F - img.offsetX, (float) img.originalHeight / 2.0F - img.offsetY, (float) img.packedWidth, (float) img.packedHeight, C.drawScale * Settings.scale, C.drawScale * Settings.scale, C.angle);
    }


    //Overly complicated validation method to check when to draw the double card effect
    private static boolean echoFormValidChecker(AbstractCard __instance) {
        if (MintySpire.showEFR() && AbstractDungeon.player != null &&AbstractDungeon.getCurrMapNode() != null && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) { //This should stop the DoubleImage from rendering if the player has Echo stacks remaining in the card selection screen
            AbstractPower p = AbstractDungeon.player.getPower(EchoForm.ID);
            if (p != null) {
                int amt = p.amount;
                if (amt > 0 && AbstractDungeon.player.hand.contains(__instance)) {
                    return AbstractDungeon.actionManager.cardsPlayedThisTurn.size() - getDoubledAmt((EchoPower) p) < amt;
                }
            }
        }

        return false;
    }

    private static Field echoField;

    private static int getDoubledAmt(EchoPower p) {
        try {
            if (echoField == null) {
                echoField = EchoPower.class.getDeclaredField("cardsDoubledThisTurn");
                echoField.setAccessible(true);
            }
            return echoField.getInt(p);

        } catch (Exception ignore) {}

        return 0;
    }

}