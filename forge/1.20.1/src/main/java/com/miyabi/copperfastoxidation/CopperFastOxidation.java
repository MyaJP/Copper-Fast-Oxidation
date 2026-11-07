package com.miyabi.copperfastoxidation;

import net.minecraft.world.InteractionResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(CopperFastOxidation.MODID)
public class CopperFastOxidation {
    public static final String MODID = "copper_fast_oxidation";

    public CopperFastOxidation() {
        MinecraftForge.EVENT_BUS.addListener(this::onRightClickBlock);
    }

    private void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (OxidizeLogic.handle(event.getEntity(), event.getLevel(), event.getPos(), event.getItemStack(), event.getHand())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
        }
    }
}
