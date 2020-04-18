package mintySpire.patches.shop;

import com.evacipated.cardcrawl.modthespire.lib.LineFinder;
import com.evacipated.cardcrawl.modthespire.lib.Matcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertLocator;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.helpers.FontHelper;
import javassist.CannotCompileException;
import javassist.CtBehavior;

// Used locator class code from: https://github.com/kiooeht/ModTheSpire/wiki/SpirePatch
// Locates the code run when rendering the card's price tag
public class RenderPriceTagCodeLocator extends SpireInsertLocator
{
	public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException
	{
		Matcher matcher = new Matcher.MethodCallMatcher(FontHelper.class, "renderFontLeftTopAligned");
		return LineFinder.findAllInOrder(ctMethodToPatch, matcher);
	}
}
