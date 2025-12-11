package ru.timeconqueror.tcneiadditions.nei.arcaneworkbench;

import static com.djgiannuzz.thaumcraftneiplugin.nei.NEIHelper.getPrimalAspectListFromAmounts;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.djgiannuzz.thaumcraftneiplugin.ModItems;
import com.djgiannuzz.thaumcraftneiplugin.items.ItemAspect;
import com.djgiannuzz.thaumcraftneiplugin.nei.recipehandler.ArcaneShapelessRecipeHandler;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.recipe.GuiRecipe;
import ru.timeconqueror.tcneiadditions.client.TCNAClient;
import ru.timeconqueror.tcneiadditions.nei.ResearchInfo;
import ru.timeconqueror.tcneiadditions.util.TCNAConfig;
import ru.timeconqueror.tcneiadditions.util.TCUtil;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.ShapelessArcaneRecipe;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.client.lib.UtilsFX;

public class ArcaneCraftingShapelessHandler extends ArcaneShapelessRecipeHandler {

    private final String userName = Minecraft.getMinecraft().getSession().getUsername();
    private TCNAClient tcnaClient = TCNAClient.getInstance();
    private int ySize;

    @Override
    public void loadTransferRects() {
        TCUtil.loadTransferRects(this, 7);
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals(this.getOverlayIdentifier())) {
            for (Object o : ThaumcraftApi.getCraftingRecipes()) {
                if (o instanceof ShapelessArcaneRecipe) {
                    ShapelessArcaneRecipe tcRecipe = (ShapelessArcaneRecipe) o;
                    boolean shouldShowRecipe = TCUtil.shouldShowRecipe(this.userName, tcRecipe.getResearch());
                    ArcaneShapelessCachedRecipe recipe = new ArcaneShapelessCachedRecipe(tcRecipe, shouldShowRecipe);
                    if (recipe.isValid()) {
                        this.arecipes.add(recipe);
                        this.aspectsAmount.add(getAmounts(tcRecipe));
                    }
                }
            }
        } else if (outputId.equals("item")) {
            super.loadCraftingRecipes(outputId, results);
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        for (Object o : ThaumcraftApi.getCraftingRecipes()) {
            if (o instanceof ShapelessArcaneRecipe) {
                ShapelessArcaneRecipe tcRecipe = (ShapelessArcaneRecipe) o;
                boolean shouldShowRecipe = TCUtil.shouldShowRecipe(this.userName, tcRecipe.getResearch());
                ArcaneShapelessCachedRecipe recipe = new ArcaneShapelessCachedRecipe(tcRecipe, shouldShowRecipe);
                if (recipe.isValid()
                        && NEIServerUtils.areStacksSameTypeCraftingWithNBT(tcRecipe.getRecipeOutput(), result)) {
                    this.arecipes.add(recipe);
                    this.aspectsAmount.add(getAmounts(tcRecipe));
                }
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        for (Object o : ThaumcraftApi.getCraftingRecipes()) {
            if (o instanceof ShapelessArcaneRecipe) {
                ShapelessArcaneRecipe tcRecipe = (ShapelessArcaneRecipe) o;
                ArcaneShapelessCachedRecipe recipe = new ArcaneShapelessCachedRecipe(tcRecipe, true);
                if (recipe.isValid() && recipe.containsWithNBT(recipe.ingredients, ingredient)
                        && TCUtil.shouldShowRecipe(this.userName, tcRecipe.getResearch())) {
                    recipe.setIngredientPermutation(recipe.ingredients, ingredient);
                    this.arecipes.add(recipe);
                    this.aspectsAmount.add(getAmounts(tcRecipe));
                }
            }
        }
    }

    @Override
    public void drawBackground(int recipeIndex) {
        ArcaneShapelessCachedRecipe recipe = (ArcaneShapelessCachedRecipe) arecipes.get(recipeIndex);
        int x = 34;
        int y = -15;
        UtilsFX.bindTexture("textures/gui/gui_researchbook_overlay.png");
        GL11.glPushMatrix();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(3042);
        GL11.glTranslatef((float) x, (float) y, 0.0F);
        GL11.glScalef(1.7F, 1.7F, 1.0F);
        GuiDraw.drawTexturedModalRect(20, 7, 20, 3, 16, 16);
        if (recipe.shouldShowRecipe) {
            GuiDraw.drawTexturedModalRect(2, 23, 112, 15, 52, 52);
        }
        GL11.glPopMatrix();

        if (recipe.shouldShowRecipe) {
            GL11.glPushMatrix();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.4F);
            GL11.glEnable(3042);
            GL11.glTranslatef((float) x - 30, (float) (y + 126), 0.0F);
            GL11.glScalef(2.0F, 2.0F, 1.0F);
            GuiDraw.drawTexturedModalRect(0, 0, 68, 76, 12, 12);
            GL11.glPopMatrix();

            this.drawAspects(recipeIndex);
        }
    }

    public void drawAspects(int recipe) {
        int[] amounts = this.aspectsAmount.get(recipe);
        AspectList aspects = getPrimalAspectListFromAmounts(amounts);

        int baseX = 36;
        int baseY = 115;
        int count = 0;
        int columns = aspects.size();
        int xOffset = (100 - columns * 20) / 2;

        for (int column = 0; column < columns; column++) {
            Aspect aspect = aspects.getAspectsSortedAmount()[count++];
            int posX = baseX + column * 18 + xOffset;
            UtilsFX.drawTag(posX, baseY, aspect, 0, 0, GuiDraw.gui.getZLevel());
        }
    }

    @Override
    public List<PositionedStack> getIngredientStacksForOverlay(int recipeIndex) {
        CachedRecipe recipe = arecipes.get(recipeIndex);
        return recipe instanceof IArcaneOverlayProvider
                ? ((IArcaneOverlayProvider) recipe).getPositionedStacksForOverlay()
                : null;
    }

    @Override
    public void drawExtras(int recipeIndex) {
        CachedRecipe cRecipe = arecipes.get(recipeIndex);
        if (cRecipe instanceof ArcaneShapelessCachedRecipe cachedRecipe) {
            if (!cachedRecipe.shouldShowRecipe) {
                String textToDraw = StatCollector.translateToLocal("tcneiadditions.research.missing");
                int y = 30;
                for (Object text : Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(textToDraw, 162)) {
                    GuiDraw.drawStringC(
                            (String) text,
                            82,
                            y,
                            tcnaClient.getColor("tcneiadditions.gui.textColor"),
                            false);
                    y += 11;
                }
            }
        }

        if (TCNAConfig.showResearchKey) {
            GuiDraw.drawString(
                    EnumChatFormatting.BOLD + StatCollector.translateToLocal("tcneiadditions.research.researchName"),
                    0,
                    7,
                    tcnaClient.getColor("tcneiadditions.gui.textColor"),
                    false);
            if (cRecipe instanceof ArcaneShapelessCachedRecipe cachedRecipe) {
                int recipeY = 17;
                for (ResearchInfo r : cachedRecipe.prereqs) {
                    r.onDraw(0, recipeY);
                    recipeY += 13;
                }
            }
        }

        TCUtil.drawSeeAllRecipesLabel(7);
    }

    private boolean isValidInput(Object input) {
        return NEIServerUtils.extractRecipeItems(input).length != 0;
    }

    @Override
    public List<String> handleTooltip(GuiRecipe<?> gui, List<String> list, int recipeIndex) {
        if (TCNAConfig.showResearchKey) {
            if (GuiContainerManager.shouldShowTooltip(gui) && list.isEmpty()) {
                CachedRecipe cRecipe = arecipes.get(recipeIndex);
                Point mousePos = GuiDraw.getMousePosition();

                if (cRecipe instanceof ArcaneShapelessCachedRecipe cachedRecipe) {
                    for (ResearchInfo r : cachedRecipe.prereqs) {
                        Rectangle rect = r.getRect(gui, recipeIndex);
                        if (rect.contains(mousePos)) {
                            r.onHover(list);
                        }
                    }
                }
            }
        }
        return super.handleTooltip(gui, list, recipeIndex);
    }

    private class ArcaneShapelessCachedRecipe extends CachedShapelessRecipe implements IArcaneOverlayProvider {

        private final AspectList aspects;
        protected Object[] overlay;
        protected final List<ResearchInfo> prereqs;
        private final boolean shouldShowRecipe;
        private final ResearchItem researchItem;

        public ArcaneShapelessCachedRecipe(ShapelessArcaneRecipe recipe, boolean shouldShowRecipe) {
            super(recipe.getInput(), recipe.getRecipeOutput());
            this.result = new PositionedStack(recipe.getRecipeOutput(), 74, 2);
            this.overlay = recipe.getInput().toArray();
            this.aspects = recipe.getAspects();
            this.shouldShowRecipe = shouldShowRecipe;
            this.researchItem = ResearchCategories.getResearch(recipe.getResearch());
            this.prereqs = new ArrayList<>();
            if (researchItem != null && researchItem.key != null) {
                prereqs.add(
                        new ResearchInfo(
                                researchItem,
                                ThaumcraftApiHelper.isResearchComplete(userName, researchItem.key)));
            }
            this.addAspectsToIngredients(aspects);
        }

        public AspectList getAspectList() {
            return this.aspects;
        }

        public boolean isValid() {
            return !this.ingredients.isEmpty() && this.result != null;
        }

        @Override
        public void setIngredients(List<?> items) {
            if (!items.isEmpty()) {
                int[][] positions = new int[][] { { 48, 32 }, { 75, 33 }, { 103, 33 }, { 49, 60 }, { 76, 60 },
                        { 103, 60 }, { 49, 87 }, { 76, 87 }, { 103, 87 } };
                int shiftX = 0;
                int shiftY = 0;

                for (int x = 0; x < items.size(); ++x) {
                    if (items.get(x) != null && isValidInput(items.get(x))) {
                        PositionedStack stack = new PositionedStack(
                                items.get(x),
                                positions[x][0] + shiftX,
                                positions[x][1] + shiftY,
                                items.get(x) instanceof ItemStack);
                        stack.setMaxSize(1);
                        this.ingredients.add(stack);
                    }
                }
            }
        }

        @Override
        public ArrayList<PositionedStack> getPositionedStacksForOverlay() {
            ArrayList<PositionedStack> stacks = new ArrayList<>();
            if (this.overlay != null && this.overlay.length > 0) {
                for (int x = 0; x < this.overlay.length; ++x) {
                    Object object = overlay[x];
                    if ((object instanceof ItemStack || object instanceof ItemStack[]
                            || object instanceof String
                            || (object instanceof List && !((List<?>) object).isEmpty()))) {
                        stacks.add(new PositionedStack(object, 40 + x % 3 * 24, 40 + x / 3 * 24));
                    }
                }
            }

            return stacks;
        }

        @Override
        public List<PositionedStack> getIngredients() {
            if (!this.shouldShowRecipe) return Collections.emptyList();
            return super.getIngredients();
        }

        @Override
        public void setIngredientPermutation(Collection<PositionedStack> ingredients, ItemStack ingredient) {
            if (ingredient.getItem() instanceof ItemAspect) return;
            super.setIngredientPermutation(ingredients, ingredient);
        }

        @Override
        public boolean contains(Collection<PositionedStack> ingredients, ItemStack ingredient) {
            if (ingredient.getItem() instanceof ItemAspect) {
                Aspect aspect = ItemAspect.getAspects(ingredient).getAspects()[0];
                return this.aspects.aspects.containsKey(aspect);
            }
            return super.contains(ingredients, ingredient);
        }

        protected void addAspectsToIngredients(AspectList aspects) {

            int baseX = 36;
            int baseY = 115;
            int count = 0;
            int columns = aspects.size();
            int xOffset = (100 - columns * 20) / 2;

            for (int column = 0; column < columns; column++) {
                Aspect aspect = aspects.getAspectsSortedAmount()[count++];
                int posX = baseX + column * 18 + xOffset;
                ItemStack stack = new ItemStack(ModItems.itemAspect, aspects.getAmount(aspect), 1);
                ItemAspect.setAspect(stack, aspect);
                this.ingredients.add(new PositionedStack(stack, posX, baseY, false));
            }
        }
    }
}
