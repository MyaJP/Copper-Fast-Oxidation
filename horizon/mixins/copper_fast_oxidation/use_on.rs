// A right click with an item on a block, once the block let it through: an
// Amethyst Shard on copper weathers it one stage and the click is the mod's.
fn copper_fast_oxidation_use_on(
    game: &mut crate::game::Game<'_>,
    stack: &horizon_client::items::ItemStack,
    pos: [i32; 3],
    claimed: &mut bool,
) {
    use crate::game::copper_fast_oxidation as m;
    if *claimed || &*stack.item != m::SHARD || !game.native_on(m::ID) {
        return;
    }
    // Only the server's world changes; a client's copy hears it from there.
    let Some(player) = game.native_acting_player() else {
        return;
    };
    // The state one weathering stage on from the block at a position, keeping
    // its properties (facing, half, open...), or None when it doesn't weather.
    let next_state = |game: &crate::game::Game<'_>, at: [i32; 3]| {
        let registry = &game.pack.assets.registry;
        let current = game.world.block(at[0], at[1], at[2]);
        let mut state = registry.state(current)?.clone();
        let name = m::next_copper(&state.name)?;
        registry.default_state(&name)?;
        state.name = name.into();
        registry.resolve(&state).ok()
    };
    let Some(next) = next_state(game, pos) else {
        return;
    };
    let dimension = player.dimension.clone();
    let half = game.native_block_property(pos, "half");
    let old = game.native_block(pos);
    if !game.native_set_block(&dimension, pos, next, m::UPDATE_ALL) {
        return;
    }
    // A door's other half follows the update; weather it here if it didn't.
    if let Some(other) = half.as_deref().and_then(|h| m::other_half(pos, h))
        && game.native_block(other) == old
        && let Some(state) = next_state(game, other)
    {
        game.native_set_block(&dimension, other, state, m::UPDATE_ALL);
    }
    game.level_event_at(m::SCRAPE_PARTICLES, pos, 0);
    game.audio.play(m::SOUND, Some(m::center(pos)), 1.0, 1.0);
    if player.game_mode != m::CREATIVE {
        game.native_take_items(m::SHARD, 1);
    }
    *claimed = true;
}
