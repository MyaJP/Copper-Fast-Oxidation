package com.miyabi.copperfastoxidation;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.InteractionResult;

public class CopperFastOxidation implements ModInitializer {
    public static final String MODID = "copper_fast_oxidation";

    @Override
    public void onInitialize() {
        UseBlockCallback.EVENT.register((player, level, hand, hit) ->
                OxidizeLogic.handle(player, level, hit.getBlockPos(), player.getItemInHand(hand), hand)
                        ? InteractionResult.sidedSuccess(level.isClientSide())
                        : InteractionResult.PASS);
    }
}
