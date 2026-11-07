package com.miyabi.copperfastoxidation;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.InteractionResult;

public class CopperFastOxidation implements ModInitializer {
    public static final String MODID = "copper_fast_oxidation";

    @Override
    public void onInitialize() {
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            // 斧を持ってスニークしてないときは、像などを削らずに素手と同じ動きにする
            InteractionResult instead = OxidizeLogic.useWithoutAxe(player, level, hit, player.getItemInHand(hand), hand);
            if (instead != null) return instead;
            return OxidizeLogic.handle(player, level, hit.getBlockPos(), player.getItemInHand(hand), hand)
                    ? InteractionResult.SUCCESS
                    : InteractionResult.PASS;
        });
    }
}
