package ru.timeconqueror.tcneiadditions.nei;

import java.awt.*;
import java.util.*;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.djgiannuzz.thaumcraftneiplugin.ModItems;
import com.djgiannuzz.thaumcraftneiplugin.items.ItemAspect;
import com.djgiannuzz.thaumcraftneiplugin.nei.recipehandler.CrucibleRecipeHandler;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.recipe.GuiRecipe;
import ru.timeconqueror.tcneiadditions.util.GuiRecipeHelper;
import ru.timeconqueror.tcneiadditions.util.TCNAConfig;
import ru.timeconqueror.tcneiadditions.util.TCUtil;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.client.lib.UtilsFX;

public class TCNACrucibleRecipeHandler extends CrucibleRecipeHandler {

    private final String userName = Minecraft.getMinecraft().getSession().getUsername();
    private int ySize;
    private final int aspectsPerRow = 3;

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals(this.getOverlayIdentifier())) {
            for (Object o : ThaumcraftApi.getCraftingRecipes()) {
                if (o instanceof CrucibleRecipe) {
                    CrucibleRecipe tcRecipe = (CrucibleRecipe) o;
                    boolean shouldShowRecipe = TCUtil.shouldShowRecipe(this.userName, tcRecipe.key);
                    CrucibleCachedRecipe recipe = new CrucibleCachedRecipe(tcRecipe, shouldShowRecipe);
                    if (recipe.isValid()) {
                        recipe.computeVisuals();
                        this.arecipes.add(recipe);
                        this.aspectsAmount.add(recipe.aspects);
                    }
                }
            }
        } else if (outputId.equals("item")) {
            this.loadCraftingRecipes((ItemStack) results[0]);
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        for (CrucibleRecipe tcRecipe : TCUtil.getCrucibleRecipes(result)) {
            boolean shouldShowRecipe = TCUtil.shouldShowRecipe(this.userName, tcRecipe.key);
            CrucibleCachedRecipe recipe = new CrucibleCachedRecipe(tcRecipe, shouldShowRecipe);
            recipe.computeVisuals();
            this.arecipes.add(recipe);
            this.aspectsAmount.add(recipe.aspects);
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        List<CrucibleRecipe> tcRecipeList = TCUtil.getCrucibleRecipesByInput(ingredient);

        for (CrucibleRecipe tcRecipe : tcRecipeList) {
            if (tcRecipe != null && TCUtil.shouldShowRecipe(this.userName, tcRecipe.key)) {
                // recipe input is invisible unless complete research
                CrucibleCachedRecipe recipe = new CrucibleCachedRecipe(tcRecipe, true);
                recipe.computeVisuals();
                recipe.setIngredientPermutation(recipe.ingredients, ingredient);
                this.arecipes.add(recipe);
                this.aspectsAmount.add(recipe.aspects);
            }
        }
    }

    @Override
    public void drawBackground(int recipeIndex) {
        CrucibleCachedRecipe recipe = (CrucibleCachedRecipe) arecipes.get(recipeIndex);
        if (recipe.shouldShowRecipe) {
            super.drawBackground(recipeIndex);
            return;
        }

        int x = 30;
        int y = 3;
        GL11.glPushMatrix();
        UtilsFX.bindTexture("textures/gui/gui_researchbook_overlay.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(3042);
        GL11.glTranslatef((float) x, (float) y, 0.0F);
        GL11.glScalef(1.75F, 1.75F, 1.0F);
        GuiDraw.drawTexturedModalRect(0, 0, 0, 3, 56, 17);
        GL11.glPopMatrix();
    }

    // Math in these looks a little weird to show similarity to other method above.
    @Override
    public void drawAspects(int recipe, int x, int y) {
        AspectList aspects = this.aspectsAmount.get(recipe);
        int rows = (int) Math.ceil((double) aspects.size() / aspectsPerRow);

        int xBase = x + 8;
        int yBase = y + 107 - (10 * rows);
        int count = 0;

        for (int row = 0; row < rows; row++) {
            int columns = Math.min(aspects.size() - row * 3, 3);
            int offSet = (100 - columns * 20) / 2;
            for (int column = 0; column < columns; column++) {
                Aspect aspect = aspects.getAspectsSortedAmount()[count++];
                int posX = xBase + column * 20 + offSet;
                int posY = yBase + row * 20;
                UtilsFX.drawTag(posX, posY, aspect, 0, 0, GuiDraw.gui.getZLevel());
            }
        }
    }

    @Override
    public void drawExtras(int recipeIndex) {
        CrucibleCachedRecipe recipe = (CrucibleCachedRecipe) arecipes.get(recipeIndex);
        if (recipe.shouldShowRecipe) {
            super.drawExtras(recipeIndex);
        } else {
            String textToDraw = I18n.format("tcneiadditions.research.missing");
            int y = 28;
            for (Object text : Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(textToDraw, 162)) {
                GuiDraw.drawStringC((String) text, 82, y, Color.BLACK.getRGB(), false);
                y += 11;
            }
        }

        if (TCNAConfig.showResearchKey) {
            int y = 135;
            String researchString = recipe.researchItem != null
                    ? EnumChatFormatting.UNDERLINE + ResearchCategories.getCategoryName(recipe.researchItem.category)
                            + " : "
                            + recipe.researchItem.getName()
                    : EnumChatFormatting.ITALIC + "null";
            List listResearchString = Minecraft.getMinecraft().fontRenderer
                    .listFormattedStringToWidth(researchString, 162);
            this.ySize = listResearchString.size() * 11;
            List<Object> list = new ArrayList<>();
            list.add(StatCollector.translateToLocal("tcneiadditions.research.researchName") + ":");
            list.addAll(listResearchString);
            for (Object text : list) {
                GuiDraw.drawStringC((String) text, 82, y, Color.BLACK.getRGB(), false);
                y += 11;
            }
        }
    }

    @Override
    public List<String> handleTooltip(GuiRecipe<?> gui, List<String> list, int recipeIndex) {
        if (TCNAConfig.showResearchKey) {
            if (GuiContainerManager.shouldShowTooltip(gui) && list.size() == 0) {
                CrucibleCachedRecipe recipe = (CrucibleCachedRecipe) arecipes.get(recipeIndex);
                Rectangle rectangle = getResearchRect(gui, recipeIndex);
                Point mousePos = GuiDraw.getMousePosition();
                if (rectangle.contains(mousePos)) {
                    TCUtil.getResearchPrerequisites(list, recipe.researchItem);
                }
            }
        }
        return super.handleTooltip(gui, list, recipeIndex);
    }

    protected Rectangle getResearchRect(GuiRecipe<?> gui, int recipeIndex) {
        Point offset = gui.getRecipePosition(recipeIndex);
        return new Rectangle(
                GuiRecipeHelper.getGuiLeft(gui) + offset.x + 2,
                GuiRecipeHelper.getGuiTop(gui) + offset.y + 146,
                GuiRecipeHelper.getXSize(gui) - 9,
                this.ySize);
    }

    private class CrucibleCachedRecipe extends CachedRecipe {

        public List<PositionedStack> ingredients;
        public PositionedStack result;
        private AspectList aspects;
        private final boolean shouldShowRecipe;
        private final ResearchItem researchItem;

        public CrucibleCachedRecipe(CrucibleRecipe recipe, boolean shouldShowRecipe) {
            this.setIngredient(recipe.catalyst);
            this.setResult(recipe.getRecipeOutput());
            this.setAspectList(recipe.aspects);
            this.shouldShowRecipe = shouldShowRecipe;
            this.researchItem = ResearchCategories.getResearch(recipe.key);
            this.addAspectsToIngredients(aspects);
        }

        protected void setIngredient(Object in) {
            if (in != null && NEIServerUtils.extractRecipeItems(in).length > 0) {
                PositionedStack stack = new PositionedStack(in, 51, 30, false);
                stack.setMaxSize(1);
                this.ingredients = new ArrayList<>(Collections.singletonList(stack));
            }
        }

        @Override
        public void setIngredientPermutation(Collection<PositionedStack> ingredients, ItemStack ingredient) {
            if (ingredient.getItem() instanceof ItemAspect) return;
            super.setIngredientPermutation(ingredients, ingredient);
        }

        protected void setResult(ItemStack out) {
            if (out != null) {
                this.result = new PositionedStack(out, 71, 8, false);
            }
        }

        protected void setAspectList(AspectList aspects) {
            this.aspects = aspects;
        }

        @Override
        public PositionedStack getResult() {
            return this.result;
        }

        public AspectList getAspectList() {
            return this.aspects;
        }

        @Override
        public List<PositionedStack> getIngredients() {
            if (!this.shouldShowRecipe) return Collections.emptyList();
            return getCycledIngredients(cycleticks / 20, this.ingredients);
        }

        public void computeVisuals() {
            for (PositionedStack p : this.ingredients) {
                p.generatePermutations();
            }
        }

        public boolean isValid() {
            return !this.ingredients.isEmpty() && this.result != null;
        }

        @Override
        public boolean contains(Collection<PositionedStack> ingredients, ItemStack ingredient) {
            if (ingredient.getItem() instanceof ItemAspect) {
                return false;
            }
            return super.contains(ingredients, ingredient);
        }

        // Math in these looks a little weird to show similarity to other method above.
        protected void addAspectsToIngredients(AspectList aspects) {
            int rows = (int) Math.ceil((double) aspects.size() / aspectsPerRow);

            int xBase = 23 + 8;
            int yBase = -21 + 107 - (10 * rows);
            int count = 0;

            for (int row = 0; row < rows; row++) {
                int columns = Math.min(aspects.size() - row * 3, 3);
                int offSet = (100 - columns * 20) / 2;
                for (int column = 0; column < columns; column++) {
                    Aspect aspect = aspects.getAspectsSortedAmount()[count++];
                    int posX = xBase + column * 20 + offSet;
                    int posY = yBase + row * 20;
                    ItemStack stack = new ItemStack(ModItems.itemAspect, aspects.getAmount(aspect), 1);
                    ItemAspect.setAspect(stack, aspect);
                    this.ingredients.add(new PositionedStack(stack, posX, posY, false));
                }
            }
        }
    }
}
