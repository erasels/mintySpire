package mintySpire.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

public class CardrewardHideButton extends GridselectHideButton {
    protected static float DRAW_Y = 200.0F * Settings.scale;

    public CardrewardHideButton() {
        super();
        this.hb.move(SHOW_X + 106.0F * Settings.scale, DRAW_Y + 60.0F * Settings.scale);
    }

    @Override
    protected void hideCards() {
        for (AbstractCard c : AbstractDungeon.cardRewardScreen.rewardGroup) {
            if(isActive) {
                c.targetDrawScale = 0.001F;
            } else {
                c.targetDrawScale = 0.75f;
            }
        }
    }

    @Override
    public void render(SpriteBatch sb) {
        if(!(AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT)) {
            return;
        }
        sb.setColor(Color.WHITE);
        renderShadow(sb);
        sb.setColor(this.glowColor);
        renderOutline(sb);
        sb.setColor(Color.SKY);
        renderButton(sb);
        if (this.hb.hovered && !this.isDisabled && !this.hb.clickStarted) {
            sb.setBlendFunction(770, 1);
            sb.setColor(HOVER_BLEND_COLOR);
            renderButton(sb);
            sb.setBlendFunction(770, 771);
        }
        if (this.isDisabled) {
            FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont, this.buttonText, this.current_x + TEXT_OFFSET_X, DRAW_Y + TEXT_OFFSET_Y, TEXT_DISABLED_COLOR);
        } else if (this.hb.clickStarted) {
            FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont, this.buttonText, this.current_x + TEXT_OFFSET_X, DRAW_Y + TEXT_OFFSET_Y, Color.LIGHT_GRAY);
        } else if (this.hb.hovered) {
            FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont, this.buttonText, this.current_x + TEXT_OFFSET_X, DRAW_Y + TEXT_OFFSET_Y, Settings.LIGHT_YELLOW_COLOR);
        } else {
            FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont, this.buttonText, this.current_x + TEXT_OFFSET_X, DRAW_Y + TEXT_OFFSET_Y, Settings.LIGHT_YELLOW_COLOR);
        }
        if (!this.isHidden)
            this.hb.render(sb);
    }

    @Override
    void renderShadow(SpriteBatch sb) {
        sb.draw(ImageMaster.CONFIRM_BUTTON_SHADOW, this.current_x - 256.0F, DRAW_Y - 128.0F, 256.0F, 128.0F, 512.0F, 256.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 512, 256, false, false);
    }

    @Override
    void renderOutline(SpriteBatch sb) {
        sb.draw(ImageMaster.CONFIRM_BUTTON_OUTLINE, this.current_x - 256.0F, DRAW_Y - 128.0F, 256.0F, 128.0F, 512.0F, 256.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 512, 256, false, false);
    }

    @Override
    void renderButton(SpriteBatch sb) {
        sb.draw(ImageMaster.CONFIRM_BUTTON, this.current_x - 256.0F, DRAW_Y - 128.0F, 256.0F, 128.0F, 512.0F, 256.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 512, 256, false, false);
    }
}
