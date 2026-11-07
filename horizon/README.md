# Copper Fast Oxidation for Horizon

A native mod for [Horizon](https://playhorizon.dev) (Minecraft: Java Edition 26.3 in Rust): right-click copper with an
**Amethyst Shard** to weather it one stage. Scraping with an axe is vanilla's own. Doors, trapdoors, copper chests and
copper golem statues weather while sneaking, so they still open, close or change pose normally.

It hooks only Horizon's native hook points (`native_use_item_on`), so the launcher builds it on any Horizon whose
`NATIVE_HOOKS_API` is at least the one in `compatibility.json`.

- `mixins/copper_fast_oxidation/horizon-mixin.toml`: the bundle (one module, two HEAD hooks)
- `runtime.rs`: the logic and its tests (`cargo test` here), also the module added to the game
- `use_on.rs`, `list.rs`: the hook helpers

The license is the repository's (MIT).
