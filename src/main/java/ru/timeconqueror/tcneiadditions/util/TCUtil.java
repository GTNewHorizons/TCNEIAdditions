package ru.timeconqueror.tcneiadditions.util;

import codechicken.nei.NEIServerUtils;
import com.djgiannuzz.thaumcraftneiplugin.items.ItemAspect;
import com.djgiannuzz.thaumcraftneiplugin.nei.NEIHelper;
import cpw.mods.fml.common.Loader;
import java.util.*;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.oredict.OreDictionary;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.common.lib.research.ResearchManager;
import tuhljin.automagy.config.ModResearchItems;

public class TCUtil {
    public static List<InfusionRecipe> getInfusionRecipes(ItemStack result) {
        List<InfusionRecipe> list = new ArrayList<>();
        for (Object r : ThaumcraftApi.getCraftingRecipes()) {
            if (r instanceof InfusionRecipe && ((InfusionRecipe) r).getRecipeOutput() instanceof ItemStack) {
                ItemStack output = (ItemStack) ((InfusionRecipe) r).getRecipeOutput();
                if (NEIServerUtils.areStacksSameTypeCraftingWithNBT(output, result)) {
                    list.add((InfusionRecipe) r);
                }
            }
        }
        return list;
    }

    public static List<CrucibleRecipe> getCrucibleRecipes(ItemStack result) {
        List<CrucibleRecipe> list = new ArrayList<>();
        for (Object r : ThaumcraftApi.getCraftingRecipes()) {
            if (r instanceof CrucibleRecipe && ((CrucibleRecipe) r).getRecipeOutput() != null) {
                ItemStack output = ((CrucibleRecipe) r).getRecipeOutput();
                if (NEIServerUtils.areStacksSameTypeCraftingWithNBT(output, result)) {
                    list.add((CrucibleRecipe) r);
                }
            }
        }
        return list;
    }

    public static List<InfusionRecipe> getInfusionRecipesByInput(ItemStack input) {
        ArrayList<InfusionRecipe> list = new ArrayList<>();
        for (Object r : ThaumcraftApi.getCraftingRecipes()) {
            if (!(r instanceof InfusionRecipe)) continue;
            InfusionRecipe tcRecipe = (InfusionRecipe) r;
            if (tcRecipe.getRecipeInput() == null || TCUtil.getAssociatedItemStack(tcRecipe.getRecipeOutput()) == null)
                continue;

            if (input.getItem() instanceof ItemAspect) {
                Aspect aspect = ItemAspect.getAspects(input).getAspects()[0];
                if (tcRecipe.getAspects().aspects.containsKey(aspect)) {
                    list.add(tcRecipe);
                }
            } else {
                if (NEIServerUtils.areStacksSameTypeCraftingWithNBT(tcRecipe.getRecipeInput(), input)
                        || matchInfusionComponents(tcRecipe.getComponents(), input)) {
                    list.add(tcRecipe);
                }
            }
        }
        return list;
    }

    public static List<CrucibleRecipe> getCrucibleRecipesByInput(ItemStack input) {
        ArrayList<CrucibleRecipe> list = new ArrayList<>();
        for (Object r : ThaumcraftApi.getCraftingRecipes()) {
            if (r == null) continue;
            if (!(r instanceof CrucibleRecipe)) continue;
            CrucibleRecipe tcRecipe = (CrucibleRecipe) r;

            if (input.getItem() instanceof ItemAspect) {
                Aspect aspect = ItemAspect.getAspects(input).getAspects()[0];
                if (tcRecipe.aspects.aspects.containsKey(aspect)) {
                    list.add(tcRecipe);
                }
            } else {
                if (tcRecipe.catalystMatches(input)) {
                    list.add(tcRecipe);
                }
            }
        }
        return list;
    }

    public static boolean matchInfusionComponents(ItemStack[] components, ItemStack stack) {
        for (ItemStack component : components) {
            for (ItemStack toCompare : getOreDictionaryMatchingItemsForInfusion(component)) {
                if (NEIServerUtils.areStacksSameTypeCraftingWithNBT(toCompare, stack)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean shouldShowRecipe(String username, String researchKey) {
        return ThaumcraftApiHelper.isResearchComplete(username, researchKey) || TCNAConfig.showLockedRecipes;
    }

    // Fix crash with broken item
    public static ItemStack getAssociatedItemStack(Object o) {
        if (o instanceof ItemStack) {
            ItemStack stack = (ItemStack) o;
            if (stack.getItem() == null) {
                return stack;
            }
        }
        return NEIHelper.getAssociatedItemStack(o);
    }

    @SuppressWarnings("deprecation")
    public static List<ItemStack> getOreDictionaryMatchingItemsForInfusion(ItemStack stack) {
        // TC uses these deprecated methods
        // See thaumcraft.api.crafting.InfusionRecipe#areItemStacksEqual
        int oreID = OreDictionary.getOreID(stack);
        if (oreID != -1) {
            if (!OreDictionary.getOreName(oreID).equals("dye")) {
                // TODO: some recipes accept any type of dye (e.g. Vambrace) (even color doesn't matter!)
                // but some require exact item (e.g. Lamp of Growth) (even same color doesn't work)
                // We need to figure out how TC handles these recipes,
                // but for now it won't hurt to be too strict.
                return OreDictionary.getOres(oreID);
            }
        }
        return Collections.singletonList(stack);
    }

    public static void getResearchPrerequisites(List<String> list, ResearchItem researchItem) {
        if (researchItem != null) {
            String playerName = Minecraft.getMinecraft().getSession().getUsername();
            // Parent research
            getResearchListBuName(list, researchItem.parents, playerName, "parents");
            // Parent hidden research
            getResearchListBuName(list, researchItem.parentsHidden, playerName, "parentsHidden");
            // Item scan
            if (researchItem.getItemTriggers() != null && researchItem.getItemTriggers().length != 0) {
                list.add(StatCollector.translateToLocal("tcneiadditions.research.prerequisites.item") + ":");
                for (ItemStack itemStack : researchItem.getItemTriggers()) {
                    String displayName = itemStack.getDisplayName();
                    list.add("    " + displayName);
                }
            }
            // Entity scan
            if (researchItem.getEntityTriggers() != null && researchItem.getEntityTriggers().length != 0) {
                list.add(StatCollector.translateToLocal("tcneiadditions.research.prerequisites.entity") + ":");
                for (String entityKey : researchItem.getEntityTriggers()) {
                    String entityName = StatCollector.translateToLocal("entity." + entityKey + ".name");
                    list.add("    " + entityName);
                }
            }
            // Aspect scan
            if (researchItem.getAspectTriggers() != null && researchItem.getAspectTriggers().length != 0) {
                list.add(StatCollector.translateToLocal("tcneiadditions.research.prerequisites.aspect") + ":");
                for (Aspect aspect : researchItem.getAspectTriggers()) {
                    String aspectName = aspect.getName() + " - " + aspect.getLocalizedDescription();
                    list.add("    " + aspectName);
                }
            }
            // Kill scan
            if (Loader.isModLoaded("Automagy") && researchItem.category.equals("AUTOMAGY")) {
                Set<String> killList = getKeysByValue(ModResearchItems.cluesOnKill, researchItem.key);
                if (!killList.isEmpty()) {
                    list.add(StatCollector.translateToLocal("tcneiadditions.research.prerequisites.kill") + ":");
                    for (String entityKey : killList) {
                        list.add("    " + StatCollector.translateToLocal("entity." + entityKey + ".name"));
                    }
                }
            }
        }
    }

    public static <T, E> Set<T> getKeysByValue(Map<T, E> map, E value) {
        return map.entrySet().stream()
                .filter(entry -> Objects.equals(entry.getValue(), value))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public static void getResearchListBuName(
            List<String> list, String[] researchKeys, String playerName, String keysName) {
        if (researchKeys != null && researchKeys.length != 0) {
            int needResearch = 0;
            list.add(StatCollector.translateToLocal("tcneiadditions.research.prerequisites." + keysName) + ":");
            for (String researchKey : researchKeys) {
                String researchName =
                        ResearchCategories.getCategoryName(ResearchCategories.getResearch(researchKey).category)
                                + " : "
                                + ResearchCategories.getResearch(researchKey).getName();
                if (ResearchManager.isResearchComplete(playerName, researchKey)) {
                    if (researchKeys.length <= 10) {
                        researchName = EnumChatFormatting.GREEN + "" + EnumChatFormatting.STRIKETHROUGH + researchName;
                        list.add(EnumChatFormatting.RESET + "    " + researchName);
                    }
                } else {
                    needResearch++;
                    researchName = EnumChatFormatting.RED + researchName;
                    list.add(EnumChatFormatting.RESET + "    " + researchName);
                }
            }
            if (researchKeys.length > 10 && needResearch == 0) {
                list.add(EnumChatFormatting.GREEN + "    "
                        + StatCollector.translateToLocal("tcneiadditions.research.prerequisites.allresearched"));
            }
        }
    }
}
