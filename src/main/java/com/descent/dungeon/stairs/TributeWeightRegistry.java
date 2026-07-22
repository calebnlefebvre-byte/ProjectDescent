package com.descent.dungeon.stairs;

import com.descent.dungeon.api.equipment.TributeWeighted;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;

/**
 * Resolves how many Dungeon Tribute pool entries an {@link ItemStack}
 * contributes (see design doc: "Dungeon Tribute"). Custom equipment should
 * implement {@link TributeWeighted} directly and define its own weight —
 * that's what the interface is for. This class is the fallback for
 * equipment that doesn't (i.e. every vanilla weapon/armor/tool piece).
 * <p>
 * The formula is the design document's overall-value formula, read directly
 * off the item's real combat/utility stats rather than approximated from
 * durability:
 * <ul>
 *     <li><b>Weapons</b> — attack damage, read from the item's
 *     {@code minecraft:attack_damage} attribute modifier (plus the game's
 *     implicit base 1.0, so the result matches the tooltip-displayed number:
 *     a wooden sword's modifier is {@code +3}, displayed/weighted as 4, a
 *     diamond sword's is {@code +6}, displayed/weighted as 7 — exactly the
 *     design document's own examples).</li>
 *     <li><b>Armor</b> — armor value and armor toughness, read the same way
 *     from {@code minecraft:armor} / {@code minecraft:armor_toughness}
 *     (toughness weighted higher per point since it's the rarer stat).</li>
 *     <li><b>Tools</b> — mining speed, read from the item's {@code Tool}
 *     data component (works uniformly for pickaxe/axe/shovel/hoe without
 *     needing to special-case tiers).</li>
 *     <li><b>Enchantments</b> — a proportional bonus on top of whatever
 *     combat/tool value was found.</li>
 *     <li><b>Durability</b> — a small, capped modifier only, per the design
 *     document; it is never the primary signal.</li>
 * </ul>
 */
public final class TributeWeightRegistry {

    private static final double ARMOR_TOUGHNESS_MULTIPLIER = 2.0;
    private static final double ENCHANTED_BONUS_FRACTION = 0.5;
    private static final double DURABILITY_BONUS_CAP = 0.2;
    private static final double DURABILITY_BONUS_DIVISOR = 5000.0;
    private static final double FLAT_MINIMUM_VALUE = 2.0;

    private TributeWeightRegistry() {
    }

    /** Returns this stack's tribute pool weight; {@code 0} means it never enters the pool at all. */
    public static int getWeight(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        if (stack.getItem() instanceof TributeWeighted tributeWeighted) {
            return Math.max(0, tributeWeighted.getTributeWeight(stack));
        }
        if (!isEquipment(stack.getItem())) {
            return 0;
        }

        double value = combatAndToolValue(stack);
        if (value <= 0) {
            value = FLAT_MINIMUM_VALUE;
        }

        if (stack.isEnchanted()) {
            value += value * ENCHANTED_BONUS_FRACTION;
        }

        int maxDamage = stack.getMaxDamage();
        if (maxDamage > 0) {
            value *= 1.0 + Math.min(DURABILITY_BONUS_CAP, maxDamage / DURABILITY_BONUS_DIVISOR);
        }

        return Math.max(1, (int) Math.round(value));
    }

    /** Sums attack damage, armor value, armor toughness, and mining speed off the item's real stats. */
    private static double combatAndToolValue(ItemStack stack) {
        double value = 0;

        ItemAttributeModifiers modifiers = stack.getAttributeModifiers();
        double attackDamage = 0;
        boolean hasAttackDamage = false;
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            if (entry.attribute().is(Attributes.ATTACK_DAMAGE)) {
                attackDamage += entry.modifier().amount();
                hasAttackDamage = true;
            } else if (entry.attribute().is(Attributes.ARMOR)) {
                value += entry.modifier().amount();
            } else if (entry.attribute().is(Attributes.ARMOR_TOUGHNESS)) {
                value += entry.modifier().amount() * ARMOR_TOUGHNESS_MULTIPLIER;
            }
        }
        if (hasAttackDamage) {
            // +1.0 for the game's implicit base attack damage, matching the tooltip-displayed value.
            value += 1.0 + attackDamage;
        }

        Tool tool = stack.get(DataComponents.TOOL);
        if (tool != null) {
            value += tool.defaultMiningSpeed();
        }

        return value;
    }

    /** Whether {@code item} is equipment at all for tribute purposes (matches the design doc's "everything is eligible" list). */
    private static boolean isEquipment(Item item) {
        return item instanceof ArmorItem
                || item instanceof SwordItem
                || item instanceof DiggerItem
                || item instanceof ShieldItem
                || item instanceof TridentItem
                || item instanceof BowItem
                || item instanceof CrossbowItem;
    }
}
