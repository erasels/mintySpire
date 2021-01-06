package mintySpire;

import basemod.*;
import basemod.abstracts.CustomSavable;
import basemod.interfaces.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StoreRelic;
import mintySpire.patches.metrics.MintyMetrics;
import mintySpire.patches.relics.MawBankPatches;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

@SpireInitializer
public class MintySpire implements
        PostInitializeSubscriber,
        EditStringsSubscriber,
        PreStartGameSubscriber,
        OnStartBattleSubscriber,
        PostCreateShopRelicSubscriber {

    private static SpireConfig modConfig = null;
    private static String modID;
    public static final Logger runLogger = LogManager.getLogger(MintySpire.class.getName());
    public static final boolean hasStSLib;
    public static boolean inkHeartCompatibility;
    public static Color removeColor = new Color(0xFF6563FF);
    public static Color addColor = new Color(0x7FFF00FF);

    private final ArrayList<ModColorDisplay> removeColorButtons = new ArrayList<>();
    private final ArrayList<ModColorDisplay> addColorButtons = new ArrayList<>();

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
            defaults.put("ShowMiniMap", Boolean.toString(false));
            defaults.put("MiniMapIconScale", Float.toString(2.5f));
            defaults.put("Ironchad", Boolean.toString(true));
            defaults.put("SummedDamage", Boolean.toString(true));
            defaults.put("TotalIncomingDamage", Boolean.toString(true));
            defaults.put("RemoveBaseKeywords", Boolean.toString(false));
            defaults.put("ShowEchoFormReminder", Boolean.toString(true));
            defaults.put("WarnItemAffordability", Boolean.toString(true));
            defaults.put("MakeHandTransparent", Boolean.toString(true));
            defaults.put("HandOpacity", Float.toString(0.5f));
            defaults.put("ShowUpdatePreview", Boolean.toString(true));
            defaults.put("UpdatePreviewAddColor", addColor.toString());
            defaults.put("UpdatePreviewRemoveColor", removeColor.toString());
            defaults.put("PurgeCostDisplay", Boolean.toString(true));
            defaults.put("TurnDisplay", Boolean.toString(true));
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

    public static boolean showBKR() {
        if (modConfig == null) {
            return false;
        }
        return modConfig.getBool("RemoveBaseKeywords");
    }

    public static boolean showIU() {
        if (modConfig == null) {
            return false;
        }
        return modConfig.getBool("WarnItemAffordability");
    }

    public static boolean makeHandTransparent() {
        if (modConfig == null) {
            return false;
        }
        return modConfig.getBool("MakeHandTransparent");
    }

    public static float getHandOpacity() {
        if (modConfig == null) {
            return 1f;
        }
        return modConfig.getFloat("HandOpacity");
    }

    public static boolean showMM() {
        if (modConfig == null) {
            return false;
        }
        return modConfig.getBool("ShowMiniMap");
    }

    public static float scaleMMIcons() {
        if (modConfig == null) {
            return 2.5f;
        }
        return modConfig.getFloat("MiniMapIconScale");
    }

    public static boolean showEFR() {
        if (modConfig == null) {
            return false;
        }
        return modConfig.getBool("ShowEchoFormReminder");
    }

    public static boolean showBCUP() {
        if (modConfig == null) {
            return false;
        }
        return modConfig.getBool("ShowUpdatePreview");
    }

    public static boolean showPCD() {
        if (modConfig == null) {
            return false;
        }
        return modConfig.getBool("PurgeCostDisplay");
    }

    public static boolean showTD() {
        if (modConfig == null) {
            return false;
        }
        return modConfig.getBool("TurnDisplay");
    }

    private float xPos = 350f, yPos = 750f, orgYPos = 750f;
    private ModPanel settingsPanel;
    private int curPage = 1;

    @Override
    public void receivePostInitialize() {
        UIStrings UIStrings = CardCrawlGame.languagePack.getUIString(MintySpire.makeID("OptionsMenu"));
        String[] TEXT = UIStrings.TEXT;
        addColor = Color.valueOf(modConfig.getString("UpdatePreviewAddColor"));
        removeColor = Color.valueOf(modConfig.getString("UpdatePreviewRemoveColor"));

        settingsPanel = new ModPanel();

        ModLabeledToggleButton BNBtn = new ModLabeledToggleButton(TEXT[1], xPos, yPos, Settings.CREAM_COLOR, FontHelper.charDescFont, showBN(), settingsPanel, l -> {
        },
                button ->
                {
                    if (modConfig != null) {
                        modConfig.setBool("ShowBossName", button.enabled);
                        saveConfig();
                    }
                });
        registerUIElement(BNBtn, true);

        ModLabeledToggleButton MMBtn = new ModLabeledToggleButton(TEXT[6], xPos, yPos, Settings.CREAM_COLOR, FontHelper.charDescFont, showMM(), settingsPanel, l -> {
        },
                button ->
                {
                    if (modConfig != null) {
                        modConfig.setBool("ShowMiniMap", button.enabled);
                        saveConfig();
                    }
                });
        registerUIElement(MMBtn, true);

        ModLabel MMIconScaleLabel = new ModLabel(TEXT[7], xPos + 40, yPos + 8, Settings.CREAM_COLOR, FontHelper.charDescFont, settingsPanel, l -> {
        });
        registerUIElement(MMIconScaleLabel, false);
        float textWidth = FontHelper.getWidth(FontHelper.charDescFont, TEXT[7], 1f / Settings.scale);

        ModMinMaxSlider MMIconScaleSlider = new ModMinMaxSlider("", xPos + 100 + textWidth, yPos + 15, 1, 4, scaleMMIcons(), "x%.2f", settingsPanel, slider -> {
            if (modConfig != null) {
                modConfig.setFloat("MiniMapIconScale", slider.getValue());
                saveConfig();
            }
        });
        registerUIElement(MMIconScaleSlider, true);

        ModLabeledToggleButton TIDBtn = new ModLabeledToggleButton(TEXT[4], xPos, yPos, Settings.CREAM_COLOR, FontHelper.charDescFont, showTID(), settingsPanel, l -> {
        },
                button ->
                {
                    if (modConfig != null) {
                        modConfig.setBool("TotalIncomingDamage", button.enabled);
                        saveConfig();
                    }
                });
        registerUIElement(TIDBtn, true);

        ModLabeledToggleButton BKRBtn = new ModLabeledToggleButton(TEXT[5], xPos, yPos, Settings.CREAM_COLOR, FontHelper.charDescFont, showBKR(), settingsPanel, l -> {
        },
                button ->
                {
                    if (modConfig != null) {
                        modConfig.setBool("RemoveBaseKeywords", button.enabled);
                        saveConfig();
                    }
                });
        registerUIElement(BKRBtn, true);

        ModLabeledToggleButton WIUBtn = new ModLabeledToggleButton(TEXT[9], xPos, yPos, Settings.CREAM_COLOR, FontHelper.charDescFont, showIU(), settingsPanel, l -> {
        },
                button ->
                {
                    if (modConfig != null) {
                        modConfig.setBool("WarnItemAffordability", button.enabled);
                        saveConfig();
                    }
                });
        registerUIElement(WIUBtn, true);

        ModLabeledToggleButton HTBtn = new ModLabeledToggleButton(TEXT[10], xPos, yPos, Settings.CREAM_COLOR, FontHelper.charDescFont, makeHandTransparent(), settingsPanel, l -> {
        },
                button ->
                {
                    if (modConfig != null) {
                        modConfig.setBool("MakeHandTransparent", button.enabled);
                        saveConfig();
                    }
                });
        registerUIElement(HTBtn, false);
        textWidth = FontHelper.getWidth(FontHelper.charDescFont, TEXT[10], 1f / Settings.scale);

        ModMinMaxSlider HandOpacitySlider = new ModMinMaxSlider("", xPos + 100f + textWidth, yPos + 15f, 0, 1, getHandOpacity(), "%.2f", settingsPanel, slider -> {
            if (modConfig != null) {
                modConfig.setFloat("HandOpacity", slider.getValue());
                saveConfig();
            }
        });
        registerUIElement(HandOpacitySlider, true);

        ModLabeledToggleButton PCDBtn = new ModLabeledToggleButton(TEXT[15], xPos, yPos, Settings.CREAM_COLOR, FontHelper.charDescFont, showPCD(), settingsPanel, l -> {
        },
                button ->
                {
                    if (modConfig != null) {
                        modConfig.setBool("PurgeCostDisplay", button.enabled);
                        saveConfig();
                    }
                });
        registerUIElement(PCDBtn, true);

        ModLabeledToggleButton TDBtn = new ModLabeledToggleButton(TEXT[16], xPos, yPos, Settings.CREAM_COLOR, FontHelper.charDescFont, showTD(), settingsPanel, l -> {
        },
                button ->
                {
                    if (modConfig != null) {
                        modConfig.setBool("TurnDisplay", button.enabled);
                        saveConfig();
                    }
                });
        registerUIElement(TDBtn, true);

        //Better Card Upgrade Preview
        ModLabeledToggleButton BCUPBtn = new ModLabeledToggleButton(TEXT[11], xPos, yPos, Settings.CREAM_COLOR, FontHelper.charDescFont, showBCUP(), settingsPanel, l -> {
        },
                button ->
                {
                    if (modConfig != null) {
                        modConfig.setBool("ShowUpdatePreview", button.enabled);
                        saveConfig();
                    }
                });
        registerUIElement(BCUPBtn, true);

        Texture colorButton = new Texture(getModID() + "Resources/img/colorButton.png");
        Texture colorButtonOutline = new Texture(getModID() + "Resources/img/colorButtonOutline.png");
        Consumer<ModColorDisplay> handleRemoveClick = modColorDisplay -> {
            removeColorButtons.forEach(m -> {
                m.rOutline = Color.DARK_GRAY.r;
                m.gOutline = Color.DARK_GRAY.g;
                m.bOutline = Color.DARK_GRAY.b;
            });
            modColorDisplay.rOutline = Color.GOLDENROD.r;
            modColorDisplay.gOutline = Color.GOLDENROD.g;
            modColorDisplay.bOutline = Color.GOLDENROD.b;
            removeColor = new Color(modColorDisplay.r, modColorDisplay.g, modColorDisplay.b, 1.0f);
            modConfig.setString("UpdatePreviewRemoveColor", removeColor.toString());
            saveConfig();
        };
        Consumer<ModColorDisplay> handleAddClick = modColorDisplay -> {
            addColorButtons.forEach(m -> {
                m.rOutline = Color.DARK_GRAY.r;
                m.gOutline = Color.DARK_GRAY.g;
                m.bOutline = Color.DARK_GRAY.b;
            });
            modColorDisplay.rOutline = Color.GOLDENROD.r;
            modColorDisplay.gOutline = Color.GOLDENROD.g;
            modColorDisplay.bOutline = Color.GOLDENROD.b;
            addColor = new Color(modColorDisplay.r, modColorDisplay.g, modColorDisplay.b, 1.0f);
            modConfig.setString("UpdatePreviewAddColor", addColor.toString());
            saveConfig();
        };

        float descWidth = NumberUtils.max(FontHelper.getSmartWidth(FontHelper.charDescFont, TEXT[12], 9999.0F, 0.0F, 1 / Settings.scale), FontHelper.getSmartWidth(FontHelper.charDescFont, TEXT[13], 9999.0F, 0.0F, 1 / Settings.scale));

        List<Color> addColors = new ArrayList<>();
        addColors.add(new Color(0x7FFF00FF));
        addColors.add(new Color(0xE1FF00FF));
        addColors.add(new Color(0x00FFF7FF));
        addColors.add(new Color(0x0095FFFF));
        addColors.add(new Color(0xc300FFFF));
        for (int i = 0; i < addColors.size(); i++) {
            ModColorDisplay modColorDisplay = new ModColorDisplay(xPos + descWidth + i * 96f, yPos - (10f * Settings.scale), 0f, colorButton, colorButtonOutline, handleAddClick);
            Color color = addColors.get(i);
            modColorDisplay.r = color.r;
            modColorDisplay.g = color.g;
            modColorDisplay.b = color.b;
            if (color.equals(addColor)) {
                modColorDisplay.rOutline = Color.GOLDENROD.r;
                modColorDisplay.gOutline = Color.GOLDENROD.g;
                modColorDisplay.bOutline = Color.GOLDENROD.b;
            } else {
                modColorDisplay.rOutline = Color.DARK_GRAY.r;
                modColorDisplay.gOutline = Color.DARK_GRAY.g;
                modColorDisplay.bOutline = Color.DARK_GRAY.b;
            }
            addColorButtons.add(modColorDisplay);
            registerUIElement(modColorDisplay, false);
        }
        ModLabel addColorLabel = new ModLabel(TEXT[12], xPos, yPos, Settings.CREAM_COLOR, FontHelper.charDescFont, settingsPanel, (modLabel -> { }));
        registerUIElement(addColorLabel, true);

        List<Color> removeColors = new ArrayList<>();
        removeColors.add(new Color(0xFF6563FF));
        removeColors.add(new Color(0x666666FF));
        removeColors.add(new Color(0x5c1500FF));
        removeColors.add(new Color(0x5c3500FF));
        removeColors.add(new Color(0x003673FF));
        for (int i = 0; i < removeColors.size(); i++) {
            ModColorDisplay modColorDisplay = new ModColorDisplay(xPos + descWidth + i * 96f, yPos - (10f * Settings.scale), 0f, colorButton, colorButtonOutline, handleRemoveClick);
            Color color = removeColors.get(i);
            modColorDisplay.r = color.r;
            modColorDisplay.g = color.g;
            modColorDisplay.b = color.b;
            if (color.equals(removeColor)) {
                modColorDisplay.rOutline = Color.GOLDENROD.r;
                modColorDisplay.gOutline = Color.GOLDENROD.g;
                modColorDisplay.bOutline = Color.GOLDENROD.b;
            } else {
                modColorDisplay.rOutline = Color.DARK_GRAY.r;
                modColorDisplay.gOutline = Color.DARK_GRAY.g;
                modColorDisplay.bOutline = Color.DARK_GRAY.b;
            }
            removeColorButtons.add(modColorDisplay);
            registerUIElement(modColorDisplay, false);
        }
        ModLabel removeColorLabel = new ModLabel(TEXT[13], xPos, yPos, Settings.CREAM_COLOR, FontHelper.charDescFont, settingsPanel, (modLabel -> { }));
        registerUIElement(removeColorLabel, true);

        ModLabeledToggleButton EFBtn = new ModLabeledToggleButton(TEXT[8], xPos, yPos, Settings.CREAM_COLOR, FontHelper.charDescFont, showSD(), settingsPanel, l -> {
        },
                button ->
                {
                    if (modConfig != null) {
                        modConfig.setBool("ShowEchoFormReminder", button.enabled);
                        saveConfig();
                    }
                });
        registerUIElement(EFBtn, true);

        ModLabeledToggleButton HHBtn = new ModLabeledToggleButton(TEXT[0], xPos, yPos, Settings.CREAM_COLOR, FontHelper.charDescFont, showHH(), settingsPanel, l -> {
        },
                button ->
                {
                    if (modConfig != null) {
                        modConfig.setBool("ShowHalfHealth", button.enabled);
                        saveConfig();
                    }
                });
        registerUIElement(HHBtn, true);

        ModLabeledToggleButton SBBtn = new ModLabeledToggleButton(TEXT[3], xPos, yPos, Settings.CREAM_COLOR, FontHelper.charDescFont, showSD(), settingsPanel, l -> {
        },
                button ->
                {
                    if (modConfig != null) {
                        modConfig.setBool("SummedDamage", button.enabled);
                        saveConfig();
                    }
                });
        registerUIElement(SBBtn, true);

        if (Settings.language == Settings.GameLanguage.ENG || Settings.language == Settings.GameLanguage.ZHS) {
            ModLabeledToggleButton ICBtn = new ModLabeledToggleButton(TEXT[2], xPos, yPos, Settings.CREAM_COLOR, FontHelper.charDescFont, showIC(), settingsPanel, l -> {
            },
                    button ->
                    {
                        if (modConfig != null) {
                            modConfig.setBool("Ironchad", button.enabled);
                            saveConfig();
                        }
                    });
            registerUIElement(ICBtn, true);
        }

        if (pages.size() > 1) {
            ModLabeledButton FlipPageBtn = new ModLabeledButton(TEXT[14], xPos + 450f, orgYPos + 45f, Settings.CREAM_COLOR, Color.WHITE, FontHelper.cardEnergyFont_L, settingsPanel,
                    button ->
                    {
                        if (pages.containsKey(curPage + 1)) {
                            changePage(curPage + 1);
                        } else {
                            changePage(1);
                        }
                    });
            settingsPanel.addUIElement(FlipPageBtn);
        }

        BaseMod.registerModBadge(ImageMaster.loadImage(getModID() + "Resources/img/modBadge.png"), getModID(), "erasels", "", settingsPanel);

        BaseMod.addSaveField("MintyMetricActs", new CustomSavable<ArrayList<String>>() {
            @Override
            public ArrayList<String> onSave() {
                return MintyMetrics.acts;
            }

            @Override
            public void onLoad(ArrayList<String> i) {
                if (i == null) {
                    MintyMetrics.acts = new ArrayList<>();
                } else {
                    MintyMetrics.acts = i;
                }
            }
        });
    }

    private final float pageOffset = 12000f;
    private HashMap<Integer, ArrayList<IUIElement>> pages = new HashMap<Integer, ArrayList<IUIElement>>() {{
        put(1, new ArrayList<>());
    }};
    private float elementSpace = 50f;
    private float yThreshold = yPos - elementSpace * 12;

    private void registerUIElement(IUIElement elem, boolean decrement) {
        settingsPanel.addUIElement(elem);

        int page = pages.size() + (yThreshold == yPos ? 1 : 0);
        if (!pages.containsKey(page)) {
            pages.put(page, new ArrayList<>());
            yPos = orgYPos;
            elem.setY(yPos);
        }
        if (page > curPage) {
            elem.setX(elem.getX() + pageOffset);
        }
        pages.get(page).add(elem);

        if (decrement) {
            yPos -= elementSpace;
        }
    }

    private void changePage(int i) {
        for (IUIElement e : pages.get(curPage)) {
            e.setX(e.getX() + pageOffset);
        }

        for (IUIElement e : pages.get(i)) {
            e.setX(e.getX() - pageOffset);
        }
        curPage = i;
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

    @Override
    public void receivePreStartGame() {
        inkHeartCompatibility = false;
    }

    @Override
    public void receiveOnBattleStart(AbstractRoom abstractRoom) {
        inkHeartCompatibility = AbstractDungeon.player.hasRelic("wanderingMiniBosses:Inkheart");
    }

    private void saveConfig() {
        try {
            modConfig.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receiveCreateShopRelics(ArrayList<StoreRelic> arrayList, ShopScreen shopScreen) {
        MawBankPatches.receive();
    }
}
