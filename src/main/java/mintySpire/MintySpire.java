package mintySpire;

import basemod.BaseMod;
import basemod.ModLabeledToggleButton;
import basemod.ModPanel;
import basemod.interfaces.EditStringsSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import basemod.interfaces.PostRenderSubscriber;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.relics.RunicDome;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;

@SpireInitializer
public class MintySpire implements
        PostInitializeSubscriber,
        EditStringsSubscriber,
        PostRenderSubscriber {

    private static SpireConfig modConfig = null;
    private static String modID;
    public static final Logger runLogger = LogManager.getLogger(MintySpire.class.getName());
    public static final boolean hasStSLib;

    static {
        hasStSLib = Loader.isModLoaded("stslib");
        if (hasStSLib) {
            runLogger.info("Detected StSLib");
        }
    }

    public static void initialize() {
        BaseMod.subscribe(new MintySpire());
        setModID("mintySpire");

        try {
            Properties defaults = new Properties();
            defaults.put("ShowHalfHealth", Boolean.toString(true));
            defaults.put("ShowBossName", Boolean.toString(true));
            defaults.put("Ironchad", Boolean.toString(true));
            defaults.put("SummedDamage", Boolean.toString(true));
            defaults.put("TotalIncomingDamage", Boolean.toString(true));
            modConfig = new SpireConfig("MintySpire", "Config", defaults);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean showHH() {
        if (modConfig == null) {
            return false;
        }
        return modConfig.getBool("ShowHalfHealth");
    }

    public static boolean showBN() {
        if (modConfig == null) {
            return false;
        }
        return modConfig.getBool("ShowBossName");
    }

    public static boolean showIC() {
        if (modConfig == null) {
            return false;
        }
        return modConfig.getBool("Ironchad");
    }

    public static boolean showSD() {
        if (modConfig == null) {
            return false;
        }
        return modConfig.getBool("SummedDamage");
    }

    public static boolean showTID() {
        if (modConfig == null) {
            return false;
        }
        return modConfig.getBool("TotalIncomingDamage");
    }

    @Override
    public void receivePostInitialize() {
        runLogger.info("Minty Spire is active.");

        UIStrings UIStrings = CardCrawlGame.languagePack.getUIString(MintySpire.makeID("OptionsMenu"));
        String[] TEXT = UIStrings.TEXT;

        int xPos = 350, yPos = 700;
        ModPanel settingsPanel = new ModPanel();
        ModLabeledToggleButton HHBtn = new ModLabeledToggleButton(TEXT[0], xPos, yPos, Settings.CREAM_COLOR, FontHelper.charDescFont, showHH(), settingsPanel, l -> {
        },
                button ->
                {
                    if (modConfig != null) {
                        modConfig.setBool("ShowHalfHealth", button.enabled);
                        try {
                            modConfig.save();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        settingsPanel.addUIElement(HHBtn);
        yPos-=50;

        ModLabeledToggleButton BNBtn = new ModLabeledToggleButton(TEXT[1], xPos, yPos, Settings.CREAM_COLOR, FontHelper.charDescFont, showBN(), settingsPanel, l -> {
        },
                button ->
                {
                    if (modConfig != null) {
                        modConfig.setBool("ShowBossName", button.enabled);
                        try {
                            modConfig.save();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        settingsPanel.addUIElement(BNBtn);
        yPos-=50;

        if (Settings.language == Settings.GameLanguage.ENG) {
            ModLabeledToggleButton ICBtn = new ModLabeledToggleButton(TEXT[2], xPos, yPos, Settings.CREAM_COLOR, FontHelper.charDescFont, showIC(), settingsPanel, l -> {
            },
                    button ->
                    {
                        if (modConfig != null) {
                            modConfig.setBool("Ironchad", button.enabled);
                            try {
                                modConfig.save();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            settingsPanel.addUIElement(ICBtn);
            yPos-=50;
        }

        ModLabeledToggleButton SBBtn = new ModLabeledToggleButton(TEXT[3], xPos, yPos, Settings.CREAM_COLOR, FontHelper.charDescFont, showSD(), settingsPanel, l -> {},
                button ->
                {
                    if (modConfig != null) {
                        modConfig.setBool("SummedDamage", button.enabled);
                        try {
                            modConfig.save();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        settingsPanel.addUIElement(SBBtn);
        yPos-=50;

        ModLabeledToggleButton TIDBtn = new ModLabeledToggleButton(TEXT[4], xPos, yPos, Settings.CREAM_COLOR, FontHelper.charDescFont, showTID(), settingsPanel, l -> {},
                button ->
                {
                    if (modConfig != null) {
                        modConfig.setBool("TotalIncomingDamage", button.enabled);
                        try {
                            modConfig.save();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        settingsPanel.addUIElement(TIDBtn);
        yPos-=50;

        BaseMod.registerModBadge(ImageMaster.loadImage(getModID() + "Resources/img/modBadge.png"), getModID(), "erasels, kiooeht", "TODO", settingsPanel);
    }

    private static Field multiIntentField;
    @Override
    public void receivePostRender(SpriteBatch spriteBatch) {
        if(showTID()) {
            if (AbstractDungeon.player != null && AbstractDungeon.currMapNode != null && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT && !AbstractDungeon.isScreenUp && !AbstractDungeon.player.hasRelic(RunicDome.ID)) {
                if(multiIntentField == null) {
                    try {
                        multiIntentField = AbstractMonster.class.getDeclaredField("intentMultiAmt");
                        multiIntentField.setAccessible(true);
                    } catch (Exception e) {
                        runLogger.error("Exception occurred when getting private field " + "intentMultiAmt" + " of " + AbstractMonster.class.getName(), e);
                    }
                }
                int c = 0, dmg = 0, tmp = 0;
                for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
                    if (!m.isDeadOrEscaped() && isAttacking(m)) {
                        c++;
                        int multiAmt = 0;
                        try {
                            multiAmt = (int) multiIntentField.get(m);
                        } catch (IllegalAccessException ignored) { }
                        tmp = m.getIntentDmg();
                        if (multiAmt > 1) {
                            tmp *= multiAmt;
                        }

                        if (tmp > 0) {
                            dmg += tmp;
                        }
                    }
                }

                if (c > 1 && dmg > 0) {
                    float x = AbstractDungeon.player.hb.cX;
                    float y = AbstractDungeon.player.hb.cY + AbstractDungeon.player.hb_h / 2.0f;
                    y += 10f * Settings.scale;

                    FontHelper.renderFontCentered(spriteBatch, FontHelper.damageNumberFont, Integer.toString(dmg), x, y, Color.SALMON, 0.5f);
                    Texture tex = getAttackIntent(dmg);
                    float xOffset = 70f;
                    int l = Integer.toString(dmg).length();
                    if(l > 2) {
                        xOffset+= 15f * (l-2);
                    } else if (l < 2) {
                        xOffset -= 15f;
                    }
                    spriteBatch.draw(tex, x - (xOffset * Settings.scale), y - (30f * Settings.scale), tex.getWidth() / 2.0f, tex.getHeight() / 2f);
                }
            }
        }
    }

    private boolean isAttacking(AbstractMonster m) {
        return m.intent == AbstractMonster.Intent.ATTACK || m.intent == AbstractMonster.Intent.ATTACK_BUFF || m.intent == AbstractMonster.Intent.ATTACK_DEBUFF || m.intent == AbstractMonster.Intent.ATTACK_DEFEND;
    }

    private Texture getAttackIntent(int dmg) {
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

    @Override
    public void receiveEditStrings() {
        loadLocStrings("eng");
        if (!languageSupport().equals("eng")) {
            loadLocStrings(languageSupport());
        }
    }

    public static String getModID() {
        return modID;
    }

    public static void setModID(String id) {
        modID = id;
    }

    public static String makeID(String idText) {
        return getModID() + ":" + idText;
    }

    private String languageSupport() {
        switch (Settings.language) {
            case ZHS:
                return "zhs";
            case KOR:
                return "kor";
            default:
                return "eng";
        }
    }

    private void loadLocStrings(String language) {
        BaseMod.loadCustomStringsFile(UIStrings.class, getModID() + "Resources/localization/" + language + "/UI-Strings.json");
    }
}
