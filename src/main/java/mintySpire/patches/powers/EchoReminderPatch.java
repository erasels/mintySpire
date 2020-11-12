package mintySpire.patches.powers;

import basemod.ReflectionHacks;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.EchoPower;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import mintySpire.MintySpire;

@SpirePatch(clz = AbstractCard.class, method = "renderEnergy")

public class EchoReminderPatch {

    private static Texture doubleTexture = new Texture(Gdx.files.internal("images/powers/echoFormReminder/double_card.png"));
    private static TextureAtlas.AtlasRegion doubleRegion = new TextureAtlas.AtlasRegion(doubleTexture, 0, 0, doubleTexture.getWidth(), doubleTexture.getHeight());

    public static void Postfix(AbstractCard __instance, SpriteBatch sb){
            if(MintySpire.showEFR() && AbstractDungeon.player != null && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT && !AbstractDungeon.player.drawPile.contains(__instance) && (AbstractDungeon.player.hasPower(EchoPower.POWER_ID)) &&
                    (!__instance.purgeOnUse && AbstractDungeon.player.getPower(EchoPower.POWER_ID).amount > 0 && AbstractDungeon.actionManager.cardsPlayedThisTurn.size() - (int)ReflectionHacks.getPrivate(AbstractDungeon.player.getPower(EchoPower.POWER_ID), EchoPower.class, "cardsDoubledThisTurn") < AbstractDungeon.player.getPower(EchoPower.POWER_ID).amount)){
            renderHelper(sb, doubleRegion, __instance.current_x, __instance.current_y, __instance);
        }
    }

    private static void renderHelper(SpriteBatch sb, TextureAtlas.AtlasRegion img, float drawX, float drawY, AbstractCard C){
        sb.setColor(Color.WHITE);
        sb.draw(img, drawX + img.offsetX - (float) img.originalWidth / 2.0F, drawY + img.offsetY - (float) img.originalHeight / 2.0F, (float) img.originalWidth / 2.0F - img.offsetX, (float) img.originalHeight / 2.0F - img.offsetY, (float) img.packedWidth, (float) img.packedHeight, C.drawScale * Settings.scale, C.drawScale * Settings.scale, C.angle);
    }

}