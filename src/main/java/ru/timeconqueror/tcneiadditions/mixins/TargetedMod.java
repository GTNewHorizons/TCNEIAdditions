package ru.timeconqueror.tcneiadditions.mixins;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizon.gtnhmixins.builders.ITargetMod;
import com.gtnewhorizon.gtnhmixins.builders.TargetModBuilder;

public enum TargetedMod implements ITargetMod {

    THAUMCRAFT("Thaumcraft"),
    THAUMCRAFTNEIPLUGIN("thaumcraftneiplugin");

    private final TargetModBuilder builder;

    TargetedMod(String modId) {
        this.builder = new TargetModBuilder().setModId(modId);
    }

    @NotNull
    @Override
    public TargetModBuilder getBuilder() {
        return builder;
    }
}
