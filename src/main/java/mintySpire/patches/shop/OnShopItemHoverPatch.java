package mintySpire.patches.shop;

import com.evacipated.cardcrawl.modthespire.lib.LineFinder;
import com.evacipated.cardcrawl.modthespire.lib.Matcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertLocator;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.shop.ShopScreen;
import javassist.CannotCompileException;
import javassist.CtBehavior;

@SpirePatch(
	clz= ShopScreen.class,
	method="update"
)
public class OnShopCardHoverPatch
{
	@SpireInsertPatch(
		locator=CardHoverCodeLocator.class,
		localvars = {"hoveredCard"} // Capture a card being hovered over
	)
	public static void Insert(ShopScreen __instance, AbstractCard hoveredCard){
		if(AbstractDungeon.player.gold >= hoveredCard.price){
			// Find the unaffordable items as if we bought the hovered item
			ShopItemAffordabilityPredictor.pickFutureUnaffordableItems(__instance, hoveredCard, AbstractCard.class);
		}
	}

	// Used locator class code from: https://github.com/kiooeht/ModTheSpire/wiki/SpirePatch
	// Locates the code run when a colored or colorless card is hovered
	private static class CardHoverCodeLocator extends SpireInsertLocator{
		public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException
		{
			Matcher matcher = new Matcher.MethodCallMatcher(ShopScreen.class, "moveHand");
			// Return the (two) lines that match
			return LineFinder.findAllInOrder(ctMethodToPatch, matcher);
		}
	}
}