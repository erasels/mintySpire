package mintySpire.patches.shop;

import basemod.ReflectionHacks;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StorePotion;
import com.megacrit.cardcrawl.shop.StoreRelic;
import java.util.ArrayList;
import java.util.HashSet;
import org.graalvm.compiler.core.common.type.ArithmeticOpTable;

public class ShopItemAffordabilityPredictor
{
	public static HashSet<AbstractCard> futureUnaffordableCards = new HashSet<>();
	public static HashSet<StoreRelic> futureUnaffordableRelics = new HashSet<>();
	public static HashSet<StorePotion> futureUnaffordablePotions = new HashSet<>();
	public static boolean canAffordFutureCardRemoval = true;
	public static int playerGoldAfterBuying = 0;

	private static float hoverLerpFactor = 0;
	private static boolean lerpFactorIncreasing = true;

	public static void updateHoverLerpFactor(){
		// 1/3 a second to change alpha from 0 to 1
		float deltaLerpFactor = 3 * Gdx.graphics.getDeltaTime();
		if(lerpFactorIncreasing){
			hoverLerpFactor += deltaLerpFactor;
			if(hoverLerpFactor > 1){
				lerpFactorIncreasing = false;
				hoverLerpFactor = 2 - hoverLerpFactor;
			}
		}else{
			hoverLerpFactor -= deltaLerpFactor;
			if(hoverLerpFactor < 0){
				lerpFactorIncreasing = true;
				hoverLerpFactor = -hoverLerpFactor;
			}
		}
	}

	public static Color getLerpColor(Color destColor){
		return Color.SALMON.cpy().lerp(destColor, hoverLerpFactor);
	}

	// Only called when player hovers over the purge card
	public static void pickFutureUnaffordableItems(int cardPurgeCost)
	{
		pickFutureUnaffordableItems(cardPurgeCost, null, null);
	}

	public static void pickFutureUnaffordableItems(Object hoveredItem, Class<?> hoveredItemClass){
		pickFutureUnaffordableItems(-1, hoveredItem, hoveredItemClass);
	}

	private static void pickFutureUnaffordableItems(int cardPurgeCost, Object hoveredItem, Class<?> hoveredItemClass){

		if(cardPurgeCost == -1){
			cardPurgeCost = (int) ReflectionHacks.getPrivateStatic(ShopScreen.class, "purgeCost");
		}

		// Determine the class of the hovered item, and find the amount of gold left after buying it
		if(hoveredItemClass == AbstractCard.class){
			playerGoldAfterBuying = AbstractDungeon.player.gold - ((AbstractCard)hoveredItem).price;
		}else if(hoveredItemClass == StoreRelic.class){
			playerGoldAfterBuying = AbstractDungeon.player.gold - ((StoreRelic)hoveredItem).price;
		}else if(hoveredItemClass == StorePotion.class){
			playerGoldAfterBuying = AbstractDungeon.player.gold - ((StorePotion)hoveredItem).price;
		}else{
			playerGoldAfterBuying = AbstractDungeon.player.gold - cardPurgeCost;
		}

		// Set the balance to zero if price exceeded the player's gold
		if(playerGoldAfterBuying < 0)
		{
			playerGoldAfterBuying = 0;
		}

		pickFutureUnaffordableItemsFromList(AbstractDungeon.shopScreen.coloredCards, AbstractCard.class, hoveredItem);
		pickFutureUnaffordableItemsFromList(AbstractDungeon.shopScreen.colorlessCards, AbstractCard.class, hoveredItem);

		// StoreRelic and StorePotion lists are private, so we have to use ReflectionHacks to access them
		ArrayList<StoreRelic> storeRelics = (ArrayList<StoreRelic>) ReflectionHacks.getPrivate(AbstractDungeon.shopScreen, ShopScreen.class, "relics");
		ArrayList<StorePotion> storePotions = (ArrayList<StorePotion>) ReflectionHacks.getPrivate(AbstractDungeon.shopScreen, ShopScreen.class, "potions");

		pickFutureUnaffordableItemsFromList(storeRelics, StoreRelic.class, hoveredItem);
		pickFutureUnaffordableItemsFromList(storePotions, StorePotion.class, hoveredItem);

		// Don't re-color the purge card price tag if we can afford it and are hovering over it
		if(hoveredItem != null){
			canAffordFutureCardRemoval = playerGoldAfterBuying >= cardPurgeCost;
		}
	}

	private static void pickFutureUnaffordableItemsFromList(ArrayList<?> shopList, Class shopListClass, Object hoveredItem){
		for(Object item: shopList){
			// Ignore if this is the hovered item
			if(item == hoveredItem) continue;
			// Determine list item type to get the item's price
			int price = 0;
			if(shopListClass == AbstractCard.class){
				price = ((AbstractCard)item).price;
			}else if(shopListClass == StoreRelic.class){
				price = ((StoreRelic)item).price;
			}else if(shopListClass == StorePotion.class){
				price = ((StorePotion)item).price;
			}
			// Only save items that are just now unaffordable
			if(playerGoldAfterBuying < price && AbstractDungeon.player.gold >= price){
				if(shopListClass == AbstractCard.class){
					futureUnaffordableCards.add((AbstractCard) item);
				}else if(shopListClass == StoreRelic.class){
					futureUnaffordableRelics.add((StoreRelic) item);
				}else if(shopListClass == StorePotion.class){
					futureUnaffordablePotions.add((StorePotion) item);
				}
			}
		}
	}
}