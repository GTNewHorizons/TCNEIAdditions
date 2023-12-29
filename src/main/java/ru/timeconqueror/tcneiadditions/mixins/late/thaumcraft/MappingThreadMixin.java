package ru.timeconqueror.tcneiadditions.mixins.late.thaumcraft;

import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.timeconqueror.tcneiadditions.client.ThaumcraftHooks;
import thaumcraft.client.gui.MappingThread;

@Mixin(value = MappingThread.class)
public class MappingThreadMixin {

    @Shadow(remap = false)
    Map<String, Integer> idMappings;

    @Inject(method = "run", at = @At(value = "HEAD"), remap = false)
    public void retrieveTotalToLoad(CallbackInfo ci) {
        ThaumcraftHooks.setTotalToLoad(idMappings.size());
    }

    /**
     * Loaded in the start of main while-loop before try-catch block.
     */
    @Inject(
            method = "run",
            at = @At(value = "JUMP", opcode = Opcodes.IFEQ, shift = At.Shift.AFTER),
            slice = @Slice(
                    to = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/item/Item;getItemById(I)Lnet/minecraft/item/Item;")))
    public void onEveryLoadedItem(CallbackInfo ci) {
        ThaumcraftHooks.incrementLoadedItems();
    }

    @Inject(method = "run", at = @At(value = "TAIL"), remap = false)
    public void onAllDataLoaded(CallbackInfo ci) {
        ThaumcraftHooks.setAllDataLoaded();
    }
}
