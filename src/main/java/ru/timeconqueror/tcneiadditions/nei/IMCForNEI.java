package ru.timeconqueror.tcneiadditions.nei;

import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.common.event.FMLInterModComms;

public class IMCForNEI {

    public static void IMCSender() {
        setNBTAndSend(
                "ru.timeconqueror.tcneiadditions.nei.arcaneworkbench.ArcaneCraftingShapedHandler",
                "Thaumcraft:blockTable:15",
                4,
                138);
        setNBTAndSend(
                "ru.timeconqueror.tcneiadditions.nei.arcaneworkbench.ArcaneCraftingShapelessHandler",
                "Thaumcraft:blockTable:15",
                4,
                138);
        setNBTAndSend(
                "ru.timeconqueror.tcneiadditions.nei.TCNACrucibleRecipeHandler",
                "Thaumcraft:blockMetalDevice",
                -2,
                136);
        setNBTAndSend(
                "ru.timeconqueror.tcneiadditions.nei.TCNAInfusionRecipeHandler",
                "Thaumcraft:blockStoneDevice:2",
                6,
                152);
        setNBTAndSend(
                "ru.timeconqueror.tcneiadditions.nei.AspectFromItemStackHandler",
                "Thaumcraft:ItemResearchNotes",
                0,
                147);
        setNBTAndSend(
                "ru.timeconqueror.tcneiadditions.nei.AspectCombinationHandler",
                "Thaumcraft:ItemResearchNotes",
                -1,
                43);
    }

    private static void setNBTAndSend(String name, String stack, int yShift, int height) {
        NBTTagCompound NBT = new NBTTagCompound();
        NBT.setString("handler", name);
        NBT.setString("modName", "Thaumcraft");
        NBT.setString("modId", "Thaumcraft");
        NBT.setBoolean("modRequired", true);
        NBT.setString("itemName", stack);
        NBT.setInteger("handlerHeight", height);
        NBT.setInteger("yShift", yShift);
        NBT.setInteger("maxRecipesPerPage", 2);
        FMLInterModComms.sendMessage("NotEnoughItems", "registerHandlerInfo", NBT);
    }

}
