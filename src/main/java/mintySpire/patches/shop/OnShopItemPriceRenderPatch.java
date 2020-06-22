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

	// Save hand opacity to pass it from HandOpacityRenderPatch to RestoreHandOpacityPatch
	private static float handOpacity;

	@SpirePatch(
		clz = ShopScreen.class,
		method = "render"
	)
	public static class MakeHandTransparentPatch
	{
		@SpireInsertPatch(
			locator = PreDrawHandCodeLocator.class,
			localvars = {"sb"}
		)
		public static void Insert(ShopScreen __instance, @ByRef SpriteBatch[] sb)
		{
			if (MintySpire.makeHandTransparent())
			{
				// Save hand opacity and make hand transparent
				handOpacity = sb[0].getColor().a;
				sb[0].setColor(sb[0].getColor().r, sb[0].getColor().g, sb[0].getColor().b, handOpacity / 2);
			}
		}

		// Locate code right before the hand is being drawn
		private static class PreDrawHandCodeLocator extends SpireInsertLocator
		{
			public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException
			{
				Matcher matcher = new Matcher.MethodCallMatcher(ShopScreen.class, "renderPurge");
				int[] results = LineFinder.findInOrder(ctMethodToPatch, matcher);
				results[0]++;
				return results;
			}
		}
	}

	@SpirePatch(
		clz = ShopScreen.class,
		method = "render"
	)
	public static class RestoreHandOpacityPatch
	{
		@SpireInsertPatch(
			locator = PostDrawHandCodeLocator.class,
			localvars = {"sb"}
		)
		public static void Insert(ShopScreen __instance, @ByRef SpriteBatch[] sb)
		{
			sb[0].setColor(sb[0].getColor().r, sb[0].getColor().g, sb[0].getColor().b, handOpacity);
		}

		// Locate the code after the hand was drawn
		private static class PostDrawHandCodeLocator extends SpireInsertLocator
		{
			public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException
			{
				Matcher matcher = new Matcher.FieldAccessMatcher(ShopScreen.class, "speechBubble");
				return LineFinder.findInOrder(ctMethodToPatch, matcher);
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