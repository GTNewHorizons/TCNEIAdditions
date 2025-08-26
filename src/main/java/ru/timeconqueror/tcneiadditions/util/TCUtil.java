package ru.timeconqueror.tcneiadditions.util;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import net.glease.tc4tweak.api.infusionrecipe.EnhancedInfusionRecipe;
import net.glease.tc4tweak.api.infusionrecipe.InfusionRecipeExt;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import com.djgiannuzz.thaumcraftneiplugin.items.ItemAspect;
import com.djgiannuzz.thaumcraftneiplugin.nei.NEIHelper;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.recipe.TemplateRecipeHandler;
import cpw.mods.fml.common.Loader;
import ru.timeconqueror.tcneiadditions.client.TCNAClient;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.common.lib.research.ResearchManager;
import tuhljin.automagy.config.ModResearchItems;

public class TCUtil {

    private static TCNAClient tcnaClient = TCNAClient.getInstance();

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
        final ArrayList<InfusionRecipe> list = new ArrayList<>();

        // if input is an Aspect item, pre-read its aspect safely
        Aspect inputAspect = null;
        boolean inputIsAspectItem = input != null && input.getItem() instanceof ItemAspect;
        if (inputIsAspectItem) {
            AspectList alt = ItemAspect.getAspects(input);
            if (alt != null && alt.getAspects() != null && alt.getAspects().length > 0) {
                inputAspect = alt.getAspects()[0];
            } else {
                return list;
            }
        }

        for (Object r : ThaumcraftApi.getCraftingRecipes()) {
            if (!(r instanceof InfusionRecipe)) continue;
            InfusionRecipe raw = (InfusionRecipe) r;

            if (raw.getRecipeOutput() == null) continue;
            Object[] comps = raw.getComponents();
            if (comps == null) continue; // avoid null array stream

            // keep bad recipe from killing the scan
            EnhancedInfusionRecipe tcRecipe;
            try {
                tcRecipe = InfusionRecipeExt.get().convert(raw);
            } catch (RuntimeException e) {
                continue;
            }
            if (tcRecipe == null) continue;

            if (tcRecipe.getCentral() == null || TCUtil.getAssociatedItemStack(tcRecipe.getRecipeOutput()) == null) {
                continue;
            }

            boolean aspectsContain = false;
            if (inputIsAspectItem && inputAspect != null
                    && tcRecipe.getAspects() != null
                    && tcRecipe.getAspects().aspects != null) {
                aspectsContain = tcRecipe.getAspects().aspects.containsKey(inputAspect);
            }

            if (inputIsAspectItem) {
                if (aspectsContain) list.add(tcRecipe);
            } else {
                boolean centralMatches = tcRecipe.getCentral().matches(input);

                boolean componentMatches = false;
                if (tcRecipe.getComponentsExt() != null && !tcRecipe.getComponentsExt().isEmpty()) {
                    componentMatches = tcRecipe.getComponentsExt().stream().anyMatch(c -> {
                        try {
                            return c != null && c.matches(input);
                        } catch (Throwable t) {
                            return false;
                        }
                    });
                }

                if (centralMatches || componentMatches) list.add(tcRecipe);
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

    public static void getResearchPrerequisites(List<String> list, ResearchItem researchItem) {
        if (researchItem != null) {
            String playerName = Minecraft.getMinecraft().getSession().getUsername();
            // Parent research
            getResearchListByName(list, researchItem.parents, playerName, "parents");
            // Parent hidden research
            getResearchListByName(list, researchItem.parentsHidden, playerName, "parentsHidden");
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
        return map.entrySet().stream().filter(entry -> Objects.equals(entry.getValue(), value)).map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public static void getResearchListByName(List<String> list, String[] researchKeys, String playerName,
            String keysName) {
        if (researchKeys != null && researchKeys.length != 0) {
            int needResearch = 0;
            list.add(StatCollector.translateToLocal("tcneiadditions.research.prerequisites." + keysName) + ":");
            for (String researchKey : researchKeys) {
                String researchName = ResearchCategories
                        .getCategoryName(ResearchCategories.getResearch(researchKey).category) + " : "
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
                list.add(
                        EnumChatFormatting.GREEN + "    "
                                + StatCollector
                                        .translateToLocal("tcneiadditions.research.prerequisites.allresearched"));
            }
        }
    }

    public static void loadTransferRects(TemplateRecipeHandler handler) {
        int stringLength = GuiDraw.getStringWidth(
                EnumChatFormatting.BOLD + StatCollector.translateToLocal("tcneiadditions.gui.nei.seeAll"));
        handler.transferRects.add(
                new TemplateRecipeHandler.RecipeTransferRect(
                        new Rectangle(162 - stringLength, 5, stringLength, 9),
                        handler.getOverlayIdentifier(),
                        new Object[0]));
    }

    public static void drawSeeAllRecipesLabel() {
        GuiDraw.drawStringR(
                EnumChatFormatting.BOLD + StatCollector.translateToLocal("tcneiadditions.gui.nei.seeAll"),
                162,
                5,
                tcnaClient.getColor("tcneiadditions.gui.textColor"),
                false);
    }
}
