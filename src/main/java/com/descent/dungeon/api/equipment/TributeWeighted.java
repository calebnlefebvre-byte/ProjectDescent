package com.descent.dungeon.api.equipment;

import net.minecraft.world.item.ItemStack;

/**
 * Lets a custom equipment item define how many entries it contributes to the
 * Early Descent Tribute's weighted-random destruction pool (see design doc:
 * "Dungeon Tribute"). A Legendary Sword implementing this with weight 1000 is
 * far more likely to be destroyed than 300 wooden pickaxes at weight 2 each —
 * this is what prevents players from padding their inventory with junk to
 * dilute the odds.
 * <p>
 * Vanilla items (which cannot implement a mod interface) get their weight
 * from the fallback formula documented on
 * {@code com.descent.dungeon.floors.tribute.TributeWeightRegistry} (added in
 * the Phase 3 tribute system): weapons by attack damage, armor by armor
 * value, tools by mining/power rating. Custom equipment should implement this
 * interface directly instead of relying on that fallback.
 */
public interface TributeWeighted {

    /** Number of entries this stack contributes to the tribute selection pool. Must be {@code >= 0}. */
    int getTributeWeight(ItemStack stack);
}
