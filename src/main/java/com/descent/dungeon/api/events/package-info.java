/**
 * Domain events posted on {@code NeoForge.EVENT_BUS} at the key moments
 * gameplay systems care about: a floor discovered, a player descending, a
 * timer expiring, a collapse starting/ending, a tribute taken, a boss
 * defeated, victory. These exist so future systems (the Dungeon Director,
 * achievements, the announcer, RPG hooks, analytics, an eventual add-on)
 * subscribe to what already happened instead of requiring a change to the
 * gameplay code that made it happen — see {@code Architecture.md}'s
 * "Event-driven architecture" section for the reasoning and
 * {@code hooks.HookBridgeEvents} for the one place that currently listens.
 * <p>
 * These are plain NeoForge {@code Event}s (posted via the existing
 * {@code NeoForge.EVENT_BUS}), not a bespoke pub/sub system — the mod
 * already depends on that bus for everything else, so introducing a second
 * event mechanism would be new infrastructure for no real benefit.
 */
package com.descent.dungeon.api.events;
