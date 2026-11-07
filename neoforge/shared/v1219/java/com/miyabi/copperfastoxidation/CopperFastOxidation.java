package com.miyabi.copperfastoxidation;

import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@Mod(CopperFastOxidation.MODID)
public class CopperFastOxidation {
    public static final String MODID = "copper_fast_oxidation";

    public CopperFastOxidation(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(this::onRightClickBlock);
    }

    private void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        // 斧を持ってスニークしてないときは、像などを削らずに素手と同じ動きにする
        InteractionResult instead = OxidizeLogic.useWithoutAxe(event.getEntity(), event.getLevel(), event.getHitVec(), event.getItemStack(), event.getHand());
        if (instead != null) {
            event.setCanceled(true);
            event.setCancellationResult(instead);
            return;
        }
        if (OxidizeLogic.handle(event.getEntity(), event.getLevel(), event.getPos(), event.getItemStack(), event.getHand())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
}
