package mintySpire;

import basemod.BaseMod;
import basemod.ModLabeledToggleButton;
import basemod.ModPanel;
import basemod.interfaces.EditStringsSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.UIStrings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

@SpireInitializer
public class MintySpire implements
        PostInitializeSubscriber,
        EditStringsSubscriber {

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

    @Override
    public void receivePostInitialize() {
        runLogger.info("Minty Spire is active.");

        UIStrings UIStrings = CardCrawlGame.languagePack.getUIString(MintySpire.makeID("OptionsMenu"));
        String[] TEXT = UIStrings.TEXT;

        ModPanel settingsPanel = new ModPanel();
        ModLabeledToggleButton HHBtn = new ModLabeledToggleButton(TEXT[0], 350, 700, Settings.CREAM_COLOR, FontHelper.charDescFont, showHH(), settingsPanel, l -> {
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

        ModLabeledToggleButton BNBtn = new ModLabeledToggleButton(TEXT[1], 350, 650, Settings.CREAM_COLOR, FontHelper.charDescFont, showBN(), settingsPanel, l -> {
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

        if(Settings.language == Settings.GameLanguage.ENG) {
            ModLabeledToggleButton ICBtn = new ModLabeledToggleButton(TEXT[2], 350, 600, Settings.CREAM_COLOR, FontHelper.charDescFont, showIC(), settingsPanel, l -> {
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
        }

        BaseMod.registerModBadge(ImageMaster.loadImage(getModID() + "Resources/img/modBadge.png"), getModID(), "erasels, kiooeht", "TODO", settingsPanel);
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
