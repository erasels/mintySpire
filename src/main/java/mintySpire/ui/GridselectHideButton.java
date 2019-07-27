package mintySpire.ui;

import basemod.ReflectionHacks;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.screens.select.GridCardSelectScreen;
import mintySpire.MintySpire;

import java.util.ArrayList;

public class GridselectHideButton {
    private static final int W = 512;
    private static final int H = 256;
    static final Color HOVER_BLEND_COLOR = new Color(1.0F, 1.0F, 1.0F, 0.3F);
    static final Color TEXT_DISABLED_COLOR = new Color(0.6F, 0.6F, 0.6F, 1.0F);
    protected static final float SHOW_X = Settings.WIDTH - 256.0F * Settings.scale, DRAW_Y = 28.0F * Settings.scale;
    private static final float HIDE_X = SHOW_X + 400.0F * Settings.scale;
    float current_x;
    private float target_x;
    boolean isHidden;
    public boolean isDisabled;
    public boolean isHovered;
    public boolean isActive;
    private float glowAlpha;
    Color glowColor;
    String buttonText;

    static final float TEXT_OFFSET_X = 136.0F * Settings.scale;
    static final float TEXT_OFFSET_Y = 57.0F * Settings.scale;
    private static final float HITBOX_W = 300.0F * Settings.scale;
    private static final float HITBOX_H = 100.0F * Settings.scale;

    public static final String ID = MintySpire.makeID("GridselectHideButton");
    public static com.megacrit.cardcrawl.localization.UIStrings UIStrings = CardCrawlGame.languagePack.getUIString(ID);
    public static final String[] TEXT = UIStrings.TEXT;

    public Hitbox hb;

    public GridselectHideButton() {
        this.current_x = HIDE_X;
        this.target_x = this.current_x;
        this.isHidden = true;
        this.isDisabled = true;
        this.isHovered = false;
        this.isActive = false;
        this.glowAlpha = 0.0F;
        this.glowColor = Color.WHITE.cpy();
        this.buttonText = "NOT_SET";
        this.hb = new Hitbox(0.0F, 0.0F, HITBOX_W, HITBOX_H);
        updateText(TEXT[0]);
        this.hb.move(SHOW_X + 106.0F * Settings.scale, DRAW_Y + 60.0F * Settings.scale);
    }

    public void updateText(String label) {
        this.buttonText = label;
    }

    public void update() {
        if(!(AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT)) {
            return;
        }
        if (!this.isHidden) {
            updateGlow();
            this.hb.update();
            if (InputHelper.justClickedLeft && this.hb.hovered && !this.isDisabled) {
                this.hb.clickStarted = true;
                if (isActive) {
                    isActive = false;
                    updateText(TEXT[0]);
                    AbstractDungeon.overlayMenu.showBlackScreen(0.7f);
                } else {
                    this.isActive = true;
                    updateText(TEXT[1]);
                    AbstractDungeon.overlayMenu.showBlackScreen(0.2f);
                }
                hideCards();
                CardCrawlGame.sound.play("UI_CLICK_1");
            }
            if (this.hb.justHovered && !this.isDisabled)
                CardCrawlGame.sound.play("UI_HOVER");
            this.isHovered = this.hb.hovered;
        }
        if (this.current_x != this.target_x) {
            this.current_x = MathUtils.lerp(this.current_x, this.target_x, Gdx.graphics.getDeltaTime() * 9.0F);
            if (Math.abs(this.current_x - this.target_x) < Settings.UI_SNAP_THRESHOLD)
                this.current_x = this.target_x;
        }
    }

    protected void updateGlow() {
        this.glowAlpha += Gdx.graphics.getDeltaTime() * 3.0F;
        if (this.glowAlpha < 0.0F)
            this.glowAlpha *= -1.0F;
        float tmp = MathUtils.cos(this.glowAlpha);
        if (tmp < 0.0F) {
            this.glowColor.a = -tmp / 2.0F + 0.3F;
        } else {
            this.glowColor.a = tmp / 2.0F + 0.3F;
        }
    }

    public void hideInstantly() {
        this.current_x = HIDE_X;
        this.target_x = HIDE_X;
        this.isHidden = true;
    }

    public void hide() {
        if (!this.isHidden) {
            this.target_x = HIDE_X;
            this.isHidden = true;
        }
    }

    public void show() {
        if (this.isHidden) {
            isDisabled = false;
            this.glowAlpha = 0.0F;
            this.target_x = SHOW_X;
            this.isHidden = false;
        }
    }

    public void close() {
        isActive = false;
        updateText(TEXT[0]);
        hideInstantly();
    }

    protected void hideCards() {
        ArrayList<AbstractCard> cards = ((CardGroup) ReflectionHacks.getPrivate(AbstractDungeon.gridSelectScreen, GridCardSelectScreen.class, "targetGroup")).group;
        for (AbstractCard c : cards) {
            if(isActive) {
                c.targetDrawScale = 0.001F;
            } else {
                c.targetDrawScale = 0.75f;
            }
        }
    }

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

    void renderShadow(SpriteBatch sb) {
        sb.draw(ImageMaster.CONFIRM_BUTTON_SHADOW, this.current_x - 256.0F, DRAW_Y - 128.0F, 256.0F, 128.0F, 512.0F, 256.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 512, 256, false, false);
    }

    void renderOutline(SpriteBatch sb) {
        sb.draw(ImageMaster.CONFIRM_BUTTON_OUTLINE, this.current_x - 256.0F, DRAW_Y - 128.0F, 256.0F, 128.0F, 512.0F, 256.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 512, 256, false, false);
    }

    void renderButton(SpriteBatch sb) {
        sb.draw(ImageMaster.CONFIRM_BUTTON, this.current_x - 256.0F, DRAW_Y - 128.0F, 256.0F, 128.0F, 512.0F, 256.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 512, 256, false, false);
    }
}