package mintySpire.patches.shop;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StorePotion;
import com.megacrit.cardcrawl.shop.StoreRelic;
import java.util.HashSet;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import mintySpire.MintySpire;

public class OnShopItemPriceRenderPatch
{
	@SpirePatch(
		clz = ShopScreen.class,
		method = "renderCardsAndPrices"
	)
	public static class OnShopCardPriceRenderPatch
	{
		@SpireInsertPatch(
			locator = RenderPriceTagCodeLocator.class,
			localvars = {"color", "c"}
		)
		public static void Insert(ShopScreen __instance, SpriteBatch sb, @ByRef Color[] color, AbstractCard c)
		{
			updatePriceColors(c, color, ShopItemAffordabilityPredictor.futureUnaffordableCards);
		}
	}

	@SpirePatch(
		clz = StoreRelic.class,
		method = "render"
	)
	public static class OnShopRelicPriceRenderPatch
	{
		@SpireInsertPatch(
			locator = RenderPriceTagCodeLocator.class,
			localvars = {"color"}
		)
		public static void Insert(StoreRelic __instance, SpriteBatch sb, @ByRef Color[] color)
		{
			updatePriceColors(__instance, color, ShopItemAffordabilityPredictor.futureUnaffordableRelics);
		}
	}

	@SpirePatch(
		clz = StorePotion.class,
		method = "render"
	)
	public static class OnShopPotionPriceRenderPatch
	{
		@SpireInsertPatch(
			locator = RenderPriceTagCodeLocator.class,
			localvars = {"color"}
		)
		public static void Insert(StorePotion __instance, SpriteBatch sb, @ByRef Color[] color)
		{
			updatePriceColors(__instance, color, ShopItemAffordabilityPredictor.futureUnaffordablePotions);
		}
	}

	@SpirePatch(
		clz = ShopScreen.class,
		method = "renderPurge"
	)
	public static class OnShopPurgePriceRenderPatch
	{
		@SpireInsertPatch(
			locator = RenderPriceTagCodeLocator.class,
			localvars = {"color"}
		)
		public static void Insert(ShopScreen __instance, SpriteBatch sb, @ByRef Color[] color)
		{
			updatePriceColors(null, color, null);
		}
	}

	private static void updatePriceColors(Object item, Color[] color, HashSet<?> futureUnaffordableItems)
	{
		// We are updating the card purge price tag and we can still afford it, no reason to update the color
		if(futureUnaffordableItems == null && !ShopItemAffordabilityPredictor.cannotAffordFutureCardRemoval){
			return;
		}
		if (MintySpire.showIU() && (futureUnaffordableItems == null || futureUnaffordableItems.contains(item)))
		{
			color[0] = ShopItemAffordabilityPredictor.getLerpColor(color[0]);
		}
	}

	private static class RenderPriceTagCodeLocator extends SpireInsertLocator
	{
		public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException
		{
			Matcher matcher = new Matcher.MethodCallMatcher(FontHelper.class, "renderFontLeftTopAligned");
			return LineFinder.findAllInOrder(ctMethodToPatch, matcher);
		}
	}
}