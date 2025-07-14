package ru.timeconqueror.tcneiadditions.mixins;

import javax.annotation.Nonnull;

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;

public enum Mixins implements IMixins {

    // spotless:off
    FIX_RECIPEHANDLER_NPES(new MixinBuilder()
        .addClientMixins(
            "thaumcraftneiplugin.ArcaneShapedRecipeHandlerMixin",
            "thaumcraftneiplugin.ArcaneShapelessRecipeHandlerMixin",
            "thaumcraftneiplugin.AspectRecipeHandlerMixin",
            "thaumcraftneiplugin.CrucibleRecipeHandlerMixin",
            "thaumcraftneiplugin.InfusionRecipeHandlerMixin")
        .addRequiredMod(TargetedMod.THAUMCRAFTNEIPLUGIN)
        .setPhase(Phase.LATE)),
    MAPPING_THREAD_HOOK(new MixinBuilder()
        .addClientMixins("thaumcraft.MappingThreadMixin")
        .addRequiredMod(TargetedMod.THAUMCRAFT)
        .setPhase(Phase.LATE)),
    FIX_NEICONFIG_VERSION(new MixinBuilder()
        .addClientMixins("thaumcraftneiplugin.NEIConfigMixin")
        .addRequiredMod(TargetedMod.THAUMCRAFTNEIPLUGIN)
        .setPhase(Phase.LATE));
    // spotless:on

    private final MixinBuilder builder;

    Mixins(MixinBuilder builder) {
        this.builder = builder;
    }

    @Nonnull
    @Override
    public MixinBuilder getBuilder() {
        return this.builder;
    }

}
