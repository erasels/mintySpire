package mintySpire.patches.shop;

import com.evacipated.cardcrawl.modthespire.lib.LineFinder;
import com.evacipated.cardcrawl.modthespire.lib.Matcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertLocator;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.shop.ShopScreen;
import javassist.CannotCompileException;
import javassist.CtBehavior;

// Used locator class code from: https://github.com/kiooeht/ModTheSpire/wiki/SpirePatch
// Locates the code run when a colored or colorless card is hovered
public class ItemHoveredCodeLocator extends SpireInsertLocator
{
	public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException
	{
		Matcher matcher = new Matcher.MethodCallMatcher(ShopScreen.class, "moveHand");
		// Return all the lines that match in the passed method
		return LineFinder.findAllInOrder(ctMethodToPatch, matcher);
	}
}
