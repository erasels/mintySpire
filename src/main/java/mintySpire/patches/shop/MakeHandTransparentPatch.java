package mintySpire.patches.shop;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.ByRef;
import com.evacipated.cardcrawl.modthespire.lib.LineFinder;
import com.evacipated.cardcrawl.modthespire.lib.Matcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertLocator;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.shop.ShopScreen;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import mintySpire.MintySpire;

public class MakeHandTransparentPatch
{
	// Save hand opacity to pass it from HandOpacityRenderPatch to RestoreHandOpacityPatch
	private static float handOpacity;

	@SpirePatch(
		clz = ShopScreen.class,
		method = "render"
	)
	public static class RenderTransparentHandPatch
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
}
