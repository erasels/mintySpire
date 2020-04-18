package mintySpire.patches.shop;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.LineFinder;
import com.evacipated.cardcrawl.modthespire.lib.Matcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertLocator;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.shop.ShopScreen;
import javassist.CannotCompileException;
import javassist.CtBehavior;

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
		public static void Insert(ShopScreen __instance, SpriteBatch sb, Color color, AbstractCard c)
		{
			if (ShopItemAffordabilityPredictor.futureUnaffordableCards.contains(c))
			{
				color = Color.SALMON.cpy();
			}
			// Clear marked items for next game update
			ShopItemAffordabilityPredictor.futureUnaffordableCards.clear();
		}

		// Used locator class code from: https://github.com/kiooeht/ModTheSpire/wiki/SpirePatch
		// Locates the code run when rendering the card's price tag
		private static class RenderPriceTagCodeLocator extends SpireInsertLocator
		{
			public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException
			{
				Matcher matcher = new Matcher.MethodCallMatcher(FontHelper.class, "renderFontLeftTopAligned");
				return LineFinder.findAllInOrder(ctMethodToPatch, matcher);
			}
		}
	}
}