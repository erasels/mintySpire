package mintySpire.patches.shop;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StorePotion;
import com.megacrit.cardcrawl.shop.StoreRelic;
import mintySpire.patches.shop.locators.ItemHoveredCodeLocator;

public class OnShopItemHoverPatch
{
	@SpirePatch(
		clz = ShopScreen.class,
		method = "updatePurgeCard"
	)
	public static class OnShopPurgeCardHoverPatch
	{
		@SpireInsertPatch(
			locator = ItemHoveredCodeLocator.class
		)
		public static void Insert(ShopScreen __instance)
		{
			// Get card purge cost and check if we can afford it
			int cardPurgeCost = (int) ReflectionHacks.getPrivateStatic(ShopScreen.class, "actualPurgeCost");
			if (AbstractDungeon.player.gold >= cardPurgeCost)
			{
				ShopItemAffordabilityPredictor.pickFutureUnaffordableItems(cardPurgeCost);
			}
		}
	}

	@SpirePatch(
		clz = ShopScreen.class,
		method = "update"
	)
	public static class OnShopCardHoverPatch
	{
		@SpireInsertPatch(
			locator = ItemHoveredCodeLocator.class,
			localvars = {"hoveredCard"} // Capture a card being hovered over
		)
		public static void Insert(ShopScreen __instance, AbstractCard hoveredCard)
		{
			if (AbstractDungeon.player.gold >= hoveredCard.price)
			{
				ShopItemAffordabilityPredictor.pickFutureUnaffordableItems(hoveredCard, AbstractCard.class);
			}
		}

		@SpirePrefixPatch
		public static void Prefix(ShopScreen __instance){
			ShopItemAffordabilityPredictor.futureUnaffordablePotions.clear();
			ShopItemAffordabilityPredictor.futureUnaffordableRelics.clear();
			ShopItemAffordabilityPredictor.futureUnaffordableCards.clear();
			ShopItemAffordabilityPredictor.cannotAffordFutureCardRemoval = false;

			ShopItemAffordabilityPredictor.updateHoverLerpFactor();
			ShopItemAffordabilityPredictor.accountForMembershipDiscount = false;
		}
	}

	@SpirePatch(
		clz = StoreRelic.class,
		method = "update",
		paramtypez = {float.class}
	)
	public static class OnShopRelicHoverPatch{
		@SpireInsertPatch(
			locator = ItemHoveredCodeLocator.class
		)
		public static void Insert(StoreRelic __instance)
		{
			if (AbstractDungeon.player.gold >= __instance.price)
			{
				ShopItemAffordabilityPredictor.pickFutureUnaffordableItems(__instance, StoreRelic.class);
			}
		}
	}

	@SpirePatch(
		clz = StorePotion.class,
		method = "update"
	)
	public static class OnShopPotionHoverPatch
	{
		@SpireInsertPatch(
			locator = ItemHoveredCodeLocator.class
		)
		public static void Insert(StorePotion __instance)
		{
			if (AbstractDungeon.player.gold >= __instance.price)
			{
				ShopItemAffordabilityPredictor.pickFutureUnaffordableItems(__instance, StorePotion.class);
			}
		}
	}
}