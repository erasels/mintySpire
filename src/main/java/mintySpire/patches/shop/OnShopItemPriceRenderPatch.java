package mintySpire.patches.shop;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.ByRef;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StorePotion;
import com.megacrit.cardcrawl.shop.StoreRelic;
import mintySpire.patches.shop.locators.RenderPriceTagCodeLocator;

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
			if (ShopItemAffordabilityPredictor.futureUnaffordableCards.contains(c))
			{
				color[0] = ShopItemAffordabilityPredictor.getLerpColor(color[0]);
			}
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
			if(ShopItemAffordabilityPredictor.futureUnaffordableRelics.contains(__instance)){
				color[0] = ShopItemAffordabilityPredictor.getLerpColor(color[0]);
			}
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
			if(ShopItemAffordabilityPredictor.futureUnaffordablePotions.contains(__instance)){
				color[0] = ShopItemAffordabilityPredictor.getLerpColor(color[0]);
			}
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
			if(ShopItemAffordabilityPredictor.cannotAffordFutureCardRemoval){
				color[0] = ShopItemAffordabilityPredictor.getLerpColor(color[0]);
			}
		}
	}

/*
	@SpirePatch(
		clz=ShopScreen.class,
		method="update"
	)
	public static class ResetFutureUnaffordableItemLists{
	}
*/
}