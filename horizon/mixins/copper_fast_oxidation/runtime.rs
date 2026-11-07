// Copper Fast Oxidation for Horizon: right-click a copper block with an
// Amethyst Shard and it weathers one stage, as it would over time. This file
// is both the mod's Cargo library (its tests) and the module the bundle adds
// to the game.
//
// Scraping with an axe is vanilla's own and is left to the game. Doors,
// trapdoors, copper chests and copper golem statues react to a right click
// themselves, so they only weather while sneaking: the game hands the click to
// the item only once the block let it through.

/// The mod's id (Options > Mods), name and version.
pub const ID: &str = "copper_fast_oxidation";
pub const NAME: &str = "Copper Fast Oxidation";
pub const VERSION: &str = "1.1.0";
pub const DESCRIPTION: &str =
    "Right-click copper with an Amethyst Shard to weather it one stage. Sneak for doors, trapdoors, chests and statues.";

/// The item that weathers copper.
pub const SHARD: &str = "minecraft:amethyst_shard";
/// `LevelEvent.PARTICLES_SCRAPE`: the blue-green flakes vanilla shows when copper changes.
pub const SCRAPE_PARTICLES: i32 = 3005;
pub const SOUND: &str = "minecraft:block.amethyst_block.break";
/// `Block.UPDATE_ALL`: neighbours and clients hear the change (a door's other half follows it).
pub const UPDATE_ALL: i32 = 3;
/// Game mode 1 is creative: the shard isn't used up there.
pub const CREATIVE: i32 = 1;

/// `WeatheringCopper.getNext` by registry naming: the block one weathering
/// stage on from `name`, or None for anything that doesn't weather any more
/// (not copper, waxed, or already oxidized).
pub fn next_copper(name: &str) -> Option<String> {
    let path = name.strip_prefix("minecraft:")?;
    if path.starts_with("waxed_") || path.starts_with("oxidized_") {
        return None;
    }
    let next = if path == "copper_block" {
        "exposed_copper".to_owned()
    } else if let Some(rest) = path.strip_prefix("exposed_") {
        format!("weathered_{rest}")
    } else if let Some(rest) = path.strip_prefix("weathered_") {
        format!("oxidized_{rest}")
    } else if weathers(path) {
        format!("exposed_{path}")
    } else {
        return None;
    };
    Some(format!("minecraft:{next}"))
}

/// Unweathered blocks of the copper family: everything else stays as it is.
/// The game also checks that the named next block exists.
fn weathers(path: &str) -> bool {
    (path.starts_with("copper_") && path != "copper_ore")
        || path.starts_with("cut_copper")
        || path == "chiseled_copper"
        || path == "lightning_rod"
        || path == "copper_golem_statue"
}

/// The other half of a door: above the lower half, below the upper one.
pub fn other_half(pos: [i32; 3], half: &str) -> Option<[i32; 3]> {
    match half {
        "lower" => Some([pos[0], pos[1] + 1, pos[2]]),
        "upper" => Some([pos[0], pos[1] - 1, pos[2]]),
        _ => None,
    }
}

/// The middle of the block at `pos`, where its sound plays.
pub fn center(pos: [i32; 3]) -> [f64; 3] {
    [
        f64::from(pos[0]) + 0.5,
        f64::from(pos[1]) + 0.5,
        f64::from(pos[2]) + 0.5,
    ]
}

#[cfg(test)]
mod tests {
    use super::*;

    fn next(name: &str) -> Option<String> {
        next_copper(name)
    }

    #[test]
    fn copper_weathers_one_stage_at_a_time() {
        assert_eq!(next("minecraft:copper_block").as_deref(), Some("minecraft:exposed_copper"));
        assert_eq!(next("minecraft:exposed_copper").as_deref(), Some("minecraft:weathered_copper"));
        assert_eq!(next("minecraft:weathered_copper").as_deref(), Some("minecraft:oxidized_copper"));
        assert_eq!(next("minecraft:oxidized_copper"), None);
    }

    #[test]
    fn the_whole_family_weathers() {
        assert_eq!(next("minecraft:cut_copper_stairs").as_deref(), Some("minecraft:exposed_cut_copper_stairs"));
        assert_eq!(next("minecraft:copper_door").as_deref(), Some("minecraft:exposed_copper_door"));
        assert_eq!(next("minecraft:weathered_copper_bulb").as_deref(), Some("minecraft:oxidized_copper_bulb"));
        assert_eq!(next("minecraft:lightning_rod").as_deref(), Some("minecraft:exposed_lightning_rod"));
        assert_eq!(next("minecraft:chiseled_copper").as_deref(), Some("minecraft:exposed_chiseled_copper"));
    }

    #[test]
    fn waxed_and_other_blocks_stay() {
        assert_eq!(next("minecraft:waxed_copper_block"), None);
        assert_eq!(next("minecraft:waxed_exposed_cut_copper"), None);
        assert_eq!(next("minecraft:stone"), None);
        assert_eq!(next("minecraft:copper_ore"), None);
        assert_eq!(next("minecraft:raw_copper_block"), None);
        assert_eq!(next("othermod:copper_block"), None);
    }

    #[test]
    fn a_doors_other_half_is_above_or_below() {
        assert_eq!(other_half([1, 64, -2], "lower"), Some([1, 65, -2]));
        assert_eq!(other_half([1, 65, -2], "upper"), Some([1, 64, -2]));
        assert_eq!(other_half([1, 65, -2], "top"), None);
    }
}
