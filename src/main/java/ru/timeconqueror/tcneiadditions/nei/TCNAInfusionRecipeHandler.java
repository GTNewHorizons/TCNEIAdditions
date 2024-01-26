package ru.timeconqueror.tcneiadditions.nei;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.djgiannuzz.thaumcraftneiplugin.ModItems;
import com.djgiannuzz.thaumcraftneiplugin.items.ItemAspect;
import com.djgiannuzz.thaumcraftneiplugin.nei.recipehandler.InfusionRecipeHandler;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.recipe.GuiRecipe;
import ru.timeconqueror.tcneiadditions.util.GuiRecipeHelper;
import ru.timeconqueror.tcneiadditions.util.TCNAConfig;
import ru.timeconqueror.tcneiadditions.util.TCUtil;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.client.lib.UtilsFX;

public class TCNAInfusionRecipeHandler extends InfusionRecipeHandler {

    private final String userName = Minecraft.getMinecraft().getSession().getUsername();
    private int ySize;
    private final int aspectsPerRow = 7;

    @Override
    public void loadTransferRects() {
        TCUtil.loadTransferRects(this);
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals(this.getOverlayIdentifier())) {
            for (Object o : ThaumcraftApi.getCraftingRecipes()) {
                if (o instanceof InfusionRecipe) {
                    InfusionRecipe tcRecipe = (InfusionRecipe) o;
                    if (tcRecipe.getRecipeInput() == null
                            || TCUtil.getAssociatedItemStack(tcRecipe.getRecipeOutput()) == null) {
                        continue;
                    }
                    boolean shouldShowRecipe = TCUtil.shouldShowRecipe(this.userName, tcRecipe.getResearch());
                    InfusionCachedRecipe recipe = new InfusionCachedRecipe(tcRecipe, shouldShowRecipe);
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
        for (InfusionRecipe tcRecipe : TCUtil.getInfusionRecipes(result)) {
            boolean shouldShowRecipe = TCUtil.shouldShowRecipe(this.userName, tcRecipe.getResearch());
            InfusionCachedRecipe recipe = new InfusionCachedRecipe(tcRecipe, shouldShowRecipe);
            recipe.computeVisuals();
            this.arecipes.add(recipe);
            this.aspectsAmount.add(recipe.aspects);
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        List<InfusionRecipe> tcRecipeList = TCUtil.getInfusionRecipesByInput(ingredient);

        for (InfusionRecipe tcRecipe : tcRecipeList) {
            if (tcRecipe != null && TCUtil.shouldShowRecipe(this.userName, tcRecipe.getResearch())) {
                // recipe input is invisible unless complete research
                InfusionCachedRecipe recipe = new InfusionCachedRecipe(tcRecipe, true);
                recipe.computeVisuals();
                recipe.setIngredientPermutation(recipe.ingredients, ingredient);
                this.arecipes.add(recipe);
                this.aspectsAmount.add(recipe.aspects);
            }
        }
    }

    @Override
    public void drawBackground(int recipeIndex) {
        InfusionCachedRecipe recipe = (InfusionCachedRecipe) arecipes.get(recipeIndex);
        if (recipe.shouldShowRecipe) {
            super.drawBackground(recipeIndex);
            return;
        }

        int x = 34;
        int y = -24;
        UtilsFX.bindTexture("textures/gui/gui_researchbook_overlay.png");
        GL11.glPushMatrix();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(3042);
        GL11.glTranslatef((float) x, (float) (y + 19), 0.0F);
        GL11.glScalef(1.75F, 1.75F, 1.0F);
        GuiDraw.drawTexturedModalRect(0, 0, 0, 3, 56, 17);
        GL11.glPopMatrix();
    }

    @Override
    public void drawExtras(int recipeIndex) {
        InfusionCachedRecipe recipe = (InfusionCachedRecipe) arecipes.get(recipeIndex);
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
            int y = 170;
            String researchString = recipe.researchItem != null
                    ? EnumChatFormatting.UNDERLINE + ResearchCategories.getCategoryName(recipe.researchItem.category)
                            + " : "
                            + recipe.researchItem.getName()
                    : EnumChatFormatting.ITALIC + "null";
            List<String> listResearchString = Minecraft.getMinecraft().fontRenderer
                    .listFormattedStringToWidth(researchString, 162);
            this.ySize = listResearchString.size() * 11;
            List<String> list = new ArrayList<>();
            list.add(StatCollector.translateToLocal("tcneiadditions.research.researchName") + ":");
            list.addAll(listResearchString);
            for (String text : list) {
                GuiDraw.drawStringC(text, 82, y, Color.BLACK.getRGB(), false);
                y += 11;
            }
        }

        TCUtil.drawSeeAllRecipesLabel();
    }

    @Override
    public void drawAspects(int recipe, int x, int y) {
        AspectList aspects = this.aspectsAmount.get(recipe);
        int rows = (int) Math.ceil((double) aspects.size() / aspectsPerRow);
        int baseX = x + 8;
        int baseY = y + 173;
        int count = 0;
        for (int row = 0; row < rows; row++) {
            int reversedRow = -row + rows - 1;
            // distribute evenly
            int columns = (aspects.size() + reversedRow) / rows;
            int xOffset = (100 - columns * 20) / 2;
            for (int column = 0; column < columns; column++) {
                Aspect aspect = aspects.getAspectsSortedAmount()[count++];
                int posX = baseX + column * 20 + xOffset;
                int posY = baseY + row * 20;
                UtilsFX.drawTag(posX, posY, aspect, 0, 0, GuiDraw.gui.getZLevel());
            }
        }
    }

    @Override
    public void drawInstability(int recipeIndex, int x, int y) {
        InfusionCachedRecipe recipe = (InfusionCachedRecipe) this.arecipes.get(recipeIndex);
        if (!recipe.shouldShowRecipe) return;

        if (TCNAConfig.showInstabilityNumber) {
            final int[] colors = { 0x0000AA, 0x5555FF, 0xAA00AA, 0xFFFF55, 0xFFAA00, 0xAA0000 };
            int colorIndex = Math.min(5, recipe.getInstability() / 2);
            String text = StatCollector.translateToLocal("tc.inst") + recipe.getInstability();
            GuiDraw.drawString(
                    text,
                    x + 56 - GuiDraw.fontRenderer.getStringWidth(text) / 2,
                    y + 263,
                    colors[colorIndex],
                    false);
        } else {
            int inst = Math.min(5, recipe.getInstability() / 2);
            String text = StatCollector.translateToLocal("tc.inst." + inst);
            GuiDraw.drawString(text, x + 56 - GuiDraw.fontRenderer.getStringWidth(text) / 2, y + 263, 0xffffff, false);
        }
    }

    @Override
    public List<String> handleTooltip(GuiRecipe<?> gui, List<String> list, int recipeIndex) {
        if (TCNAConfig.showResearchKey) {
            if (GuiContainerManager.shouldShowTooltip(gui) && list.size() == 0) {
                InfusionCachedRecipe recipe = (InfusionCachedRecipe) arecipes.get(recipeIndex);
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
                GuiRecipeHelper.getGuiTop(gui) + offset.y + 181,
                GuiRecipeHelper.getXSize(gui) - 9,
                this.ySize);
    }

    private class InfusionCachedRecipe extends CachedRecipe {

        private final AspectList aspects;
        private PositionedStack result;
        private List<PositionedStack> ingredients;
        private int instability;
        private final boolean shouldShowRecipe;
        private final ResearchItem researchItem;

        public InfusionCachedRecipe(InfusionRecipe recipe, boolean shouldShowRecipe) {
            this.setIngredients(recipe);
            this.setOutput(recipe);
            this.aspects = recipe.getAspects();
            this.setInstability(recipe.getInstability());
            this.shouldShowRecipe = shouldShowRecipe;
            this.addAspectsToIngredients(this.aspects);
            this.researchItem = ResearchCategories.getResearch(recipe.getResearch());
        }

        protected void setInstability(int inst) {
            this.instability = inst;
        }

        protected int getInstability() {
            return this.instability;
        }

        protected void setIngredients(InfusionRecipe recipe) {
            this.ingredients = new ArrayList<>();
            this.ingredients.add(new PositionedStack(recipe.getRecipeInput(), 75, 58));
            int x = 27;
            int y = -35;
            int le = recipe.getComponents().length;
            ArrayList<Point> coords = new ArrayList<>();
            float pieSlice = (float) (360 / le);
            float currentRot = -90.0F;

            int total;
            int sx;
            int sy;
            for (total = 0; total < le; ++total) {
                sx = (int) (MathHelper.cos(currentRot / 180.0F * 3.141593F) * 40.0F) - 8;
                sy = (int) (MathHelper.sin(currentRot / 180.0F * 3.141593F) * 40.0F) - 8;
                currentRot += pieSlice;
                coords.add(new Point(sx, sy));
            }

            total = 0;
            sx = x + 56;
            sy = y + 102;

            for (ItemStack itemStack : recipe.getComponents()) {
                ItemStack ingredient = TCUtil.getAssociatedItemStack(itemStack);
                ingredient.stackSize = 1;
                int vx = sx + coords.get(total).x;
                int vy = sy + coords.get(total).y;
                this.ingredients
                        .add(new PositionedStack(TCUtil.getOreDictionaryMatchingItemsForInfusion(ingredient), vx, vy));
                ++total;
            }
        }

        protected void setOutput(InfusionRecipe recipe) {
            ItemStack res;
            if (recipe.getRecipeOutput() instanceof ItemStack) {
                res = TCUtil.getAssociatedItemStack(recipe.getRecipeOutput());
            } else {
                res = TCUtil.getAssociatedItemStack(recipe.getRecipeOutput()).copy();
                Object[] obj = (Object[]) recipe.getRecipeOutput();
                NBTBase tag = (NBTBase) obj[1];
                res.setTagInfo((String) obj[0], tag);
            }

            this.result = new PositionedStack(res, 75, 0);
        }

        @Override
        public void setIngredientPermutation(Collection<PositionedStack> ingredients, ItemStack ingredient) {
            if (ingredient.getItem() instanceof ItemAspect) return;
            super.setIngredientPermutation(ingredients, ingredient);
        }

        public AspectList getAspectList() {
            return this.aspects;
        }

        @Override
        public PositionedStack getResult() {
            return this.result;
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

        protected void addAspectsToIngredients(AspectList aspects) {
            int rows = (int) Math.ceil((double) aspects.size() / aspectsPerRow);
            int baseX = 35;
            int baseY = 129;
            int count = 0;
            for (int row = 0; row < rows; row++) {
                int reversedRow = -row + rows - 1;
                // distribute evenly
                int columns = (aspects.size() + reversedRow) / rows;
                int xOffset = (100 - columns * 20) / 2;
                for (int column = 0; column < columns; column++) {
                    Aspect aspect = aspects.getAspectsSortedAmount()[count++];
                    int posX = baseX + column * 20 + xOffset;
                    int posY = baseY + row * 20;
                    ItemStack stack = new ItemStack(ModItems.itemAspect, aspects.getAmount(aspect), 1);
                    ItemAspect.setAspect(stack, aspect);
                    this.ingredients.add(new PositionedStack(stack, posX, posY, false));
                }
            }
        }
    }
}
