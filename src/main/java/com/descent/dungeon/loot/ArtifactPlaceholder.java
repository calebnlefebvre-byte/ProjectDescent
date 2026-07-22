package com.descent.dungeon.loot;

import net.minecraft.resources.ResourceLocation;

/**
 * Reserved extension point for future Artifact equipment — per the design
 * document, crafted equipment behaves like normal Minecraft gear (loses
 * durability, breaks) until Artifacts exist, which never break and carry
 * unique abilities awarded through achievements/bosses/quests/milestones.
 * See {@code FutureSystems.md}'s "Artifact Equipment" section. Not
 * implemented; this interface only exists so the loot framework has a named
 * seam for it rather than needing one invented from scratch later. A real
 * Artifact should implement {@code api.equipment.TributeWeighted} directly
 * like any other equipment when it's built.
 */
public interface ArtifactPlaceholder {

    ResourceLocation id();
}
