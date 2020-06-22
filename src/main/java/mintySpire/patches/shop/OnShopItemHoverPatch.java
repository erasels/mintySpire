package mintySpire.patches.shop;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.LineFinder;
import com.evacipated.cardcrawl.modthespire.lib.Matcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertLocator;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StorePotion;
import com.megacrit.cardcrawl.shop.StoreRelic;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import mintySpire.MintySpire;

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

		// Reset all the lists and flags for our main class
		@SpirePrefixPatch
		public static void patch(ShopScreen __instance)
		{
			if (MintySpire.showIU())
			{
				ShopItemAffordabilityPredictor.futureUnaffordablePotions.clear();
				ShopItemAffordabilityPredictor.futureUnaffordableRelics.clear();
				ShopItemAffordabilityPredictor.futureUnaffordableCards.clear();
				ShopItemAffordabilityPredictor.cannotAffordFutureCardRemoval = false;
				ShopItemAffordabilityPredictor.accountForMembershipDiscount = false;
			}
			if (MintySpire.makeHandTransparent())
			{
				ShopItemAffordabilityPredictor.updateHoverLerpFactor();
			}
		}
	}

	@SpirePatch(
		clz = StoreRelic.class,
		method = "update",
		paramtypez = {float.class}
	)
	public static class OnShopRelicHoverPatch
	{
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

	private static class ItemHoveredCodeLocator extends SpireInsertLocator
	{
		public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException
		{
			Matcher matcher = new Matcher.MethodCallMatcher(ShopScreen.class, "moveHand");
			// Return all the lines that match in the passed method
			return LineFinder.findAllInOrder(ctMethodToPatch, matcher);
		}
	}
}