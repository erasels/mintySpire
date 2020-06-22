package mintySpire.patches.shop;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StorePotion;
import com.megacrit.cardcrawl.shop.StoreRelic;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import mintySpire.MintySpire;
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
			if (MintySpire.showIU() && ShopItemAffordabilityPredictor.futureUnaffordableCards.contains(c))
			{
				color[0] = ShopItemAffordabilityPredictor.getLerpColor(color[0]);
			}
		}
	}

	// Save hand opacity to pass it from HandOpacityRenderPatch to RestoreHandOpacityPatch
	private static float handOpacity;

	@SpirePatch(
		clz = ShopScreen.class,
		method = "render"
	)
	public static class HandOpacityRenderPatch{
		@SpireInsertPatch(
			locator = PreDrawHandCodeLocator.class,
			localvars = {"sb"}
		)
		public static void Insert(ShopScreen __instance, @ByRef SpriteBatch[] sb){
			handOpacity = -1;
			if(ShopItemAffordabilityPredictor.makeHandTransparent){
				// Save hand opacity and make hand transparent
				handOpacity = sb[0].getColor().a;
				sb[0].setColor(sb[0].getColor().r, sb[0].getColor().g, sb[0].getColor().b, handOpacity/2);
			}
		}

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
	public static class RestoreHandOpacityPatch{
		@SpireInsertPatch(
			locator = PostDrawHandCodeLocator.class,
			localvars = {"sb"}
		)
		public static void Insert(ShopScreen __instance, @ByRef SpriteBatch[] sb){
			sb[0].setColor(sb[0].getColor().r, sb[0].getColor().g, sb[0].getColor().b, handOpacity);
		}

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
		private static float savedHandOpacity;

		@SpireInsertPatch(
			locator = RenderPriceTagCodeLocator.class,
			localvars = {"color"}
		)
		public static void Insert(StoreRelic __instance, SpriteBatch sb, @ByRef Color[] color)
		{
			if(MintySpire.showIU() && ShopItemAffordabilityPredictor.futureUnaffordableRelics.contains(__instance)){
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
			if(MintySpire.showIU() && ShopItemAffordabilityPredictor.futureUnaffordablePotions.contains(__instance)){
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
			if (MintySpire.showIU() && ShopItemAffordabilityPredictor.cannotAffordFutureCardRemoval)
			{
				color[0] = ShopItemAffordabilityPredictor.getLerpColor(color[0]);
			}
		}
	}
}