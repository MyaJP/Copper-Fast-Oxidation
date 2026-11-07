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
        if (OxidizeLogic.handle(event.getEntity(), event.getLevel(), event.getPos(), event.getItemStack(), event.getHand())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
        }
    }
}
