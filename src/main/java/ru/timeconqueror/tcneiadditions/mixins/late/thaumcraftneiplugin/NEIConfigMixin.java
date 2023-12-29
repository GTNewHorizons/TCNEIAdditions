package ru.timeconqueror.tcneiadditions.mixins.late.thaumcraftneiplugin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.djgiannuzz.thaumcraftneiplugin.nei.NEIConfig;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import ru.timeconqueror.tcneiadditions.TCNEIAdditions;

@Mixin(NEIConfig.class)
public class NEIConfigMixin {

    @ModifyReturnValue(at = @At("RETURN"), method = "getVersion", remap = false)
    private String getVersion(String original) {
        return TCNEIAdditions.thaumcraftNEIPluginVersion;
    }

}
