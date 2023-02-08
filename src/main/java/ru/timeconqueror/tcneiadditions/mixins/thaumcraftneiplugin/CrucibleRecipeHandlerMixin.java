package ru.timeconqueror.tcneiadditions.mixins.thaumcraftneiplugin;

import net.minecraft.client.entity.EntityClientPlayerMP;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.djgiannuzz.thaumcraftneiplugin.nei.recipehandler.CrucibleRecipeHandler;

@Mixin(CrucibleRecipeHandler.class)
public class CrucibleRecipeHandlerMixin {

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/entity/EntityClientPlayerMP;getDisplayName()Ljava/lang/String;",
                    remap = false))
    public String redirectGetDisplayName(EntityClientPlayerMP thePlayer) {
        if (thePlayer != null) {
            return thePlayer.getDisplayName();
        } else {
            return "";
        }
    }
}
