package com.miyabi.copperfastoxidation;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * ・アメジストの欠片で右クリック → 錆が1段階進む（バニラの「斧で削る」ときの青緑の粒）
 * ・斧で右クリック → 錆が1段階戻る（バニラの代わりに銅色の粉を出す）
 */
public final class OxidizeLogic {
    private static final int SCRAPE_PARTICLES = 3005;
    /** 銅色（#E07A50）の粉 */
    private static final DustParticleOptions COPPER_DUST = new DustParticleOptions(0xE07A50, 1.0F);

    private OxidizeLogic() {}

    /** どちらかの処理をしたらtrue（バニラの処理は止める） */
    public static boolean handle(Player player, Level level, BlockPos pos, ItemStack stack, InteractionHand hand) {
        return tryOxidize(player, level, pos, stack) || tryScrape(player, level, pos, stack, hand);
    }

    /** アメジストの欠片：錆を1段階進める */
    private static boolean tryOxidize(Player player, Level level, BlockPos pos, ItemStack stack) {
        if (player.isSpectator() || !stack.is(Items.AMETHYST_SHARD)) return false;
        BlockState state = level.getBlockState(pos);
        Optional<BlockState> next = WeatheringCopper.getNext(state.getBlock()).map(b -> b.withPropertiesOf(state));
        if (next.isEmpty()) return false; // 銅じゃない・ワックス済み・もう最大まで錆びてる
        // ドアとトラップドアはスニーク中だけ（普段は普通に開け閉めできるように）
        if ((state.getBlock() instanceof DoorBlock || state.getBlock() instanceof TrapDoorBlock) && !player.isShiftKeyDown()) return false;

        if (!level.isClientSide()) {
            change(player, level, pos, state, next.get(), true);
            level.levelEvent(null, SCRAPE_PARTICLES, pos, 0);
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        // アイテムの減りはクライアントでも同じようにやる（サーバーとずれると手持ちの表示が揺れるため）
        if (!player.getAbilities().instabuild) stack.shrink(1);
        return true;
    }

    /** 斧：錆を1段階戻す（バニラと同じ動きで、粒だけ銅色にする） */
    private static boolean tryScrape(Player player, Level level, BlockPos pos, ItemStack stack, InteractionHand hand) {
        if (player.isSpectator() || !(stack.getItem() instanceof AxeItem)) return false;
        // バニラと同じ：オフハンドに盾があるときは盾を構えるのが優先
        if (hand == InteractionHand.MAIN_HAND && player.getOffhandItem().is(Items.SHIELD) && !player.isSecondaryUseActive()) return false;
        BlockState state = level.getBlockState(pos);
        Optional<BlockState> prev = WeatheringCopper.getPrevious(state);
        if (prev.isEmpty()) return false; // 錆びてない銅・ワックス済み（ワックス剥がしはバニラに任せる）
        // バニラと同じ：ドアとトラップドアはスニーク中だけ（普段は開け閉め）
        Block block = state.getBlock();
        if ((block instanceof DoorBlock || block instanceof TrapDoorBlock) && !player.isShiftKeyDown()) return false;

        if (!level.isClientSide()) {
            change(player, level, pos, state, prev.get(), false);
            level.playSound(null, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (level instanceof ServerLevel serverLevel) spawnCopperDust(serverLevel, pos);
            player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        }
        // 耐久の減りもバニラと同じく両側で呼ぶ
        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
        return true;
    }

    /** ブロックを置き換える。ドアは上下2ブロックなので、もう片方も一緒に変える */
    private static void change(Player player, Level level, BlockPos pos, BlockState state, BlockState newState, boolean forward) {
        level.setBlock(pos, newState, Block.UPDATE_ALL_IMMEDIATE);
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            BlockPos other = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER
                    ? pos.above() : pos.below();
            BlockState otherState = level.getBlockState(other);
            if (otherState.is(state.getBlock())) {
                Optional<BlockState> o = forward
                        ? WeatheringCopper.getNext(otherState.getBlock()).map(b -> b.withPropertiesOf(otherState))
                        : WeatheringCopper.getPrevious(otherState);
                o.ifPresent(s -> level.setBlock(other, s, Block.UPDATE_ALL_IMMEDIATE));
            }
        }
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, newState));
    }

    /** 斧で錆を削るときと同じように、ブロックの6面それぞれから粉を出す */
    private static void spawnCopperDust(ServerLevel level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            double x = pos.getX() + 0.5 + dir.getStepX() * 0.55;
            double y = pos.getY() + 0.5 + dir.getStepY() * 0.55;
            double z = pos.getZ() + 0.5 + dir.getStepZ() * 0.55;
            Direction.Axis axis = dir.getAxis();
            double sx = axis == Direction.Axis.X ? 0.0 : 0.2;
            double sy = axis == Direction.Axis.Y ? 0.0 : 0.2;
            double sz = axis == Direction.Axis.Z ? 0.0 : 0.2;
            level.sendParticles(COPPER_DUST, x, y, z, 3 + level.getRandom().nextInt(3), sx, sy, sz, 0.0);
        }
    }
}
