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
import com.djgiannuzz.thaumcraftneiplugin.nei.NEIHelper;
import com.djgiannuzz.thaumcraftneiplugin.nei.recipehandler.ArcaneShapedRecipeHandler;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.ShapedRecipeHandler;
import ru.timeconqueror.tcneiadditions.client.TCNAClient;
import ru.timeconqueror.tcneiadditions.nei.ResearchInfo;
import ru.timeconqueror.tcneiadditions.util.TCNAConfig;
import ru.timeconqueror.tcneiadditions.util.TCUtil;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.api.wands.WandCap;
import thaumcraft.api.wands.WandRod;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.items.wands.ItemWandCasting;

public class ArcaneCraftingShapedHandler extends ArcaneShapedRecipeHandler {

    private final String userName = Minecraft.getMinecraft().getSession().getUsername();
    private int ySizeNormal, ySizeRod, ySizeCap;
    private TCNAClient tcnaClient = TCNAClient.getInstance();

    @Override
    public void loadTransferRects() {
        TCUtil.loadTransferRects(this);
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals(this.getOverlayIdentifier())) {
            for (Object o : ThaumcraftApi.getCraftingRecipes()) {
                ArcaneWandCachedRecipe wandRec = null;
                if (o instanceof ShapedArcaneRecipe tcRecipe) {
                    if (tcRecipe.getRecipeOutput().getItem() instanceof ItemWandCasting wand) {
                        WandRod rod = wand.getRod(tcRecipe.getRecipeOutput());
                        WandCap cap = wand.getCap(tcRecipe.getRecipeOutput());
                        boolean shouldShowRecipe = false;
                        if (!wand.isSceptre(tcRecipe.getRecipeOutput())
                                || TCUtil.shouldShowRecipe(userName, "SCEPTRE")) {
                            if (TCUtil.shouldShowRecipe(userName, cap.getResearch())
                                    && TCUtil.shouldShowRecipe(userName, rod.getResearch())) {
                                shouldShowRecipe = true;
                            }
                        }
                        if (rod != null || cap != null) {
                            wandRec = new ArcaneWandCachedRecipe(
                                    rod,
                                    cap,
                                    tcRecipe.getRecipeOutput(),
                                    false,
                                    shouldShowRecipe);
                        }
                    }
                    boolean shouldShowRecipe = TCUtil.shouldShowRecipe(this.userName, tcRecipe.getResearch());
                    ArcaneShapedCachedRecipe recipe = new ArcaneShapedCachedRecipe(tcRecipe, shouldShowRecipe);
                    if (wandRec != null) {
                        recipe.prereqs.addAll(wandRec.prereqs);
                    }
                    if (recipe.isValid()) {
                        recipe.computeVisuals();
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
        if (result.getItem() instanceof ItemWandCasting) {
            ItemWandCasting wand = (ItemWandCasting) result.getItem();
            WandRod rod = wand.getRod(result);
            WandCap cap = wand.getCap(result);
            boolean shouldShowRecipe = false;
            if (!wand.isSceptre(result) || TCUtil.shouldShowRecipe(userName, "SCEPTRE")) {
                if (TCUtil.shouldShowRecipe(userName, cap.getResearch())
                        && TCUtil.shouldShowRecipe(userName, rod.getResearch())) {
                    shouldShowRecipe = true;
                }
            }

            if (!TCNAClient.getInstance().areWandRecipesDeleted()) {
                ArcaneWandCachedRecipe recipe = new ArcaneWandCachedRecipe(
                        rod,
                        cap,
                        result,
                        wand.isSceptre(result),
                        shouldShowRecipe);
                recipe.computeVisuals();
                this.arecipes.add(recipe);
                this.aspectsAmount.add(NEIHelper.getWandAspectsWandCost(result));
            }

            loadShapedRecipesForWands(result, shouldShowRecipe);
        } else {
            for (Object o : ThaumcraftApi.getCraftingRecipes()) {
                if (o instanceof ShapedArcaneRecipe) {
                    ShapedArcaneRecipe shapedArcaneRecipe = (ShapedArcaneRecipe) o;
                    boolean shouldShowRecipe = TCUtil.shouldShowRecipe(userName, shapedArcaneRecipe.getResearch());

                    ArcaneShapedCachedRecipe recipe = new ArcaneShapedCachedRecipe(
                            shapedArcaneRecipe,
                            shouldShowRecipe);

                    if (recipe.isValid() && NEIServerUtils
                            .areStacksSameTypeCraftingWithNBT(shapedArcaneRecipe.getRecipeOutput(), result)) {
                        recipe.computeVisuals();
                        this.arecipes.add(recipe);
                        this.aspectsAmount.add(getAmounts(shapedArcaneRecipe));
                    }
                }
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        for (Object o : ThaumcraftApi.getCraftingRecipes()) {
            if (o instanceof ShapedArcaneRecipe) {
                ShapedArcaneRecipe tcRecipe = (ShapedArcaneRecipe) o;
                ArcaneShapedCachedRecipe recipe = new ArcaneShapedCachedRecipe(tcRecipe, true);
                if (recipe.isValid() && recipe.containsWithNBT(recipe.ingredients, ingredient)
                        && TCUtil.shouldShowRecipe(this.userName, tcRecipe.getResearch())) {
                    recipe.computeVisuals();
                    recipe.setIngredientPermutation(recipe.ingredients, ingredient);
                    this.arecipes.add(recipe);
                    this.aspectsAmount.add(getAmounts(tcRecipe));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void loadShapedRecipesForWands(ItemStack wandStack, boolean shouldShowRecipe) {
        if (!(wandStack.getItem() instanceof ItemWandCasting)) {
            throw new RuntimeException("This method works only for Thaumcraft Wands! Provided: " + wandStack);
        }

        ItemWandCasting wand = ((ItemWandCasting) wandStack.getItem());
        WandRod rod = wand.getRod(wandStack);
        WandCap cap = wand.getCap(wandStack);
        boolean isSceptre = wand.isSceptre(wandStack);

        ((List<Object>) ThaumcraftApi.getCraftingRecipes()).stream().filter(o -> o instanceof ShapedArcaneRecipe)
                .filter(r -> {
                    ItemStack output = ((ShapedArcaneRecipe) r).output;
                    if (!(output.getItem() instanceof ItemWandCasting)) return false;

                    if (isSceptre != wand.isSceptre(output)) return false;

                    if (output.getItem().getClass() != wandStack.getItem().getClass()) return false;

                    WandRod outputRod = wand.getRod(output);
                    WandCap outputCap = wand.getCap(output);

                    return outputRod.getTag().equals(rod.getTag()) && outputCap.getTag().equals(cap.getTag());
                }).forEach(o -> {
                    ShapedArcaneRecipe arcaneRecipe = (ShapedArcaneRecipe) o;
                    // this needs to be ArcaneShapedCachedRecipe instead of ArcaneWandCachedRecipe
                    // because of modified recipe
                    ArcaneShapedCachedRecipe recipe = new ArcaneShapedCachedRecipe(arcaneRecipe, shouldShowRecipe);
                    ArcaneWandCachedRecipe wandRec = new ArcaneWandCachedRecipe(
                            rod,
                            cap,
                            wandStack,
                            false,
                            shouldShowRecipe);
                    recipe.prereqs.addAll(wandRec.prereqs);
                    recipe.computeVisuals();
                    this.arecipes.add(recipe);
                    this.aspectsAmount.add(getAmounts(arcaneRecipe));
                });
    }

    @Override
    public void drawBackground(int recipeIndex) {
        boolean shouldShowRecipe;
        CachedRecipe cRecipe = arecipes.get(recipeIndex);
        if (cRecipe instanceof ArcaneShapedCachedRecipe) {
            ArcaneShapedCachedRecipe recipe = (ArcaneShapedCachedRecipe) cRecipe;
            shouldShowRecipe = recipe.shouldShowRecipe;
        } else if (cRecipe instanceof ArcaneWandCachedRecipe) {
            ArcaneWandCachedRecipe recipe = (ArcaneWandCachedRecipe) cRecipe;
            shouldShowRecipe = recipe.shouldShowRecipe;
        } else {
            throw new RuntimeException("Incompatible recipe type found: " + cRecipe.getClass());
        }

        int x = 34;
        int y = -15;
        UtilsFX.bindTexture("textures/gui/gui_researchbook_overlay.png");
        GL11.glPushMatrix();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(3042);
        GL11.glTranslatef((float) x, (float) y, 0.0F);
        GL11.glScalef(1.7F, 1.7F, 1.0F);
        GuiDraw.drawTexturedModalRect(20, 7, 20, 3, 16, 16);
        if (shouldShowRecipe) {
            GuiDraw.drawTexturedModalRect(2, 23, 112, 15, 52, 52);
        }
        GL11.glPopMatrix();

        if (shouldShowRecipe) {
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
        if (cRecipe instanceof ArcaneShapedCachedRecipe cachedRecipe) {
            if (!cachedRecipe.shouldShowRecipe) {
                String textToDraw = StatCollector.translateToLocal("tcneiadditions.research.missing");
                int y = 28;
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
        } else if (cRecipe instanceof ArcaneWandCachedRecipe cachedRecipe) {
            if (!cachedRecipe.shouldShowRecipe) {
                String textToDraw = StatCollector.translateToLocal("tcneiadditions.research.missing");
                int y = 28;
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
                    5,
                    tcnaClient.getColor("tcneiadditions.gui.textColor"),
                    false);
            if (cRecipe instanceof ArcaneShapedCachedRecipe cachedRecipe) {
                int recipeY = 15;
                for (ResearchInfo r : cachedRecipe.prereqs) {
                    r.onDraw(0, recipeY);
                    recipeY += 13;
                }
            } else if (cRecipe instanceof ArcaneWandCachedRecipe cachedRecipe) {
                int recipeY = 15;
                for (ResearchInfo r : cachedRecipe.prereqs) {
                    r.onDraw(0, recipeY);
                    recipeY += 13;
                }
            }
        }

        TCUtil.drawSeeAllRecipesLabel();
    }

    private class ArcaneShapedCachedRecipe extends ShapedRecipeHandler.CachedShapedRecipe
            implements IArcaneOverlayProvider {

        protected AspectList aspects;
        protected Object[] overlay;
        protected final List<ResearchInfo> prereqs;
        protected int width;
        protected int height;
        private final boolean shouldShowRecipe;
        private final ResearchItem researchItem;

        public ArcaneShapedCachedRecipe(ShapedArcaneRecipe recipe, boolean shouldShowRecipe) {
            super(recipe.width, recipe.height, recipe.getInput(), recipe.getRecipeOutput());
            this.result = new PositionedStack(recipe.getRecipeOutput(), 74, 2);
            this.aspects = recipe.getAspects();
            this.overlay = recipe.getInput();
            this.width = recipe.width;
            this.height = recipe.height;
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

        public boolean isValid() {
            return !this.ingredients.isEmpty() && this.result != null;
        }

        @Override
        public void setIngredients(int width, int height, Object[] items) {
            if (items != null && items.length > 0) {
                int y;
                int x;
                int[][][] positions2 = new int[width][height][2];
                int shiftX = 0;
                int shiftY = 0;
                for (x = 0; x < width && x < 3; ++x) {
                    for (y = 0; y < height && y < 3; ++y) {
                        positions2[x][y][0] = positions[y][x][0];
                        positions2[x][y][1] = positions[y][x][1];
                    }
                }
                for (x = 0; x < width && x < 3; ++x) {
                    for (y = 0; y < height && y < 3; ++y) {
                        if (items[y * width + x] == null
                                || !(items[y * width + x] instanceof ItemStack)
                                        && !(items[y * width + x] instanceof ItemStack[])
                                        && !(items[y * width + x] instanceof String)
                                        && !(items[y * width + x] instanceof List)
                                || items[y * width + x] instanceof List && ((List<?>) items[y * width + x]).isEmpty())
                            continue;
                        PositionedStack stack = new PositionedStack(
                                items[y * width + x],
                                positions2[x][y][0] + shiftX,
                                positions2[x][y][1] + shiftY,
                                false);
                        stack.setMaxSize(1);
                        this.ingredients.add(stack);
                    }
                }
            }
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

        @Override
        public ArrayList<PositionedStack> getPositionedStacksForOverlay() {
            ArrayList<PositionedStack> stacks = new ArrayList<>();
            if (overlay != null && overlay.length > 0) {
                for (int x = 0; x < width; ++x) {
                    for (int y = 0; y < height; ++y) {
                        Object object = overlay[y * width + x];
                        if ((object instanceof ItemStack || object instanceof ItemStack[]
                                || object instanceof String
                                || (object instanceof List && !((List<?>) object).isEmpty()))) {
                            stacks.add(new PositionedStack(object, 40 + x * 24, 40 + y * 24));
                        }
                    }
                }
            }

            return stacks;
        }

        // shaped crafting
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

    @Override
    public List<String> handleTooltip(GuiRecipe<?> gui, List<String> list, int recipeIndex) {
        if (TCNAConfig.showResearchKey) {
            if (GuiContainerManager.shouldShowTooltip(gui) && list.isEmpty()) {
                CachedRecipe cRecipe = arecipes.get(recipeIndex);
                Point mousePos = GuiDraw.getMousePosition();

                if (cRecipe instanceof ArcaneShapedCachedRecipe cachedRecipe) {
                    for (ResearchInfo r : cachedRecipe.prereqs) {
                        Rectangle rect = r.getRect(gui, recipeIndex);
                        if (rect.contains(mousePos)) {
                            r.onHover(list);
                        }
                    }
                } else if (cRecipe instanceof ArcaneWandCachedRecipe cachedRecipe) {
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

    private class ArcaneWandCachedRecipe extends ShapedRecipeHandler.CachedShapedRecipe
            implements IArcaneOverlayProvider {

        protected AspectList aspects;
        protected Object[] overlay;
        protected final List<ResearchInfo> prereqs;
        private final boolean shouldShowRecipe;

        public ArcaneWandCachedRecipe(WandRod rod, WandCap cap, ItemStack result, boolean isScepter,
                boolean shouldShowRecipe) {
            super(3, 3, isScepter ? NEIHelper.buildScepterInput(rod, cap) : NEIHelper.buildWandInput(rod, cap), result);
            this.overlay = isScepter ? NEIHelper.buildScepterInput(rod, cap) : NEIHelper.buildWandInput(rod, cap);
            this.result = new PositionedStack(result, 74, 2);
            this.aspects = NEIHelper.getPrimalAspectListFromAmounts(NEIHelper.getWandAspectsWandCost(result));
            this.shouldShowRecipe = shouldShowRecipe;
            this.prereqs = new ArrayList<>();
            if (isScepter) {
                prereqs.add(
                        new ResearchInfo(
                                ResearchCategories.getResearch("SCEPTRE"),
                                ThaumcraftApiHelper.isResearchComplete(userName, "SCEPTRE")));
            }
            if (cap != null && !cap.getResearch().isEmpty()) {
                prereqs.add(
                        new ResearchInfo(
                                ResearchCategories.getResearch(cap.getResearch()),
                                ThaumcraftApiHelper.isResearchComplete(userName, cap.getResearch())));
            }
            if (rod != null && !rod.getResearch().isEmpty()) {
                prereqs.add(
                        new ResearchInfo(
                                ResearchCategories.getResearch(rod.getResearch()),
                                ThaumcraftApiHelper.isResearchComplete(userName, rod.getResearch())));
            }

            this.addAspectsToIngredients(aspects);
        }

        @Override
        public List<PositionedStack> getIngredients() {
            if (!this.shouldShowRecipe) return Collections.emptyList();
            return super.getIngredients();
        }

        @Override
        public void setIngredients(int width, int height, Object[] items) {
            if (items != null && items.length > 0) {
                int[][] positions = new int[][] { { 48, 32 }, { 75, 33 }, { 103, 33 }, { 49, 60 }, { 76, 60 },
                        { 103, 60 }, { 49, 87 }, { 76, 87 }, { 103, 87 } };
                int[][] positions2 = new int[][] { { 48, 32 }, { 75, 33 }, { 49, 60 }, { 76, 60 } };
                int shiftX = 0;
                int shiftY = 0;
                for (int x = 0; x < width; ++x) {
                    for (int y = 0; y < height; ++y) {
                        Object object = items[y * width + x];
                        if (!(object instanceof ItemStack) && !(object instanceof ItemStack[])
                                && !(object instanceof String)
                                && !(object instanceof List) || object instanceof List && ((List<?>) object).isEmpty())
                            continue;
                        if (width == 2 && height == 2) {
                            positions = positions2;
                        }
                        PositionedStack stack = new PositionedStack(
                                object,
                                positions[y * width + x][0] + shiftX,
                                positions[y * width + x][1] + shiftY,
                                object instanceof ItemStack);
                        stack.setMaxSize(1);
                        this.ingredients.add(stack);
                    }
                }
            }
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

        @Override
        public ArrayList<PositionedStack> getPositionedStacksForOverlay() {
            ArrayList<PositionedStack> stacks = new ArrayList<>();
            if (overlay != null && overlay.length > 0) {
                for (int x = 0; x < 3; x++) {
                    for (int y = 0; y < 3; y++) {
                        Object object = overlay[y * 3 + x];
                        if ((object instanceof ItemStack || object instanceof ItemStack[]
                                || object instanceof String
                                || (object instanceof List && !((List<?>) object).isEmpty()))) {
                            stacks.add(new PositionedStack(object, 40 + x * 24, 40 + y * 24));
                        }
                    }
                }
            }
            return stacks;
        }

        // Wand aspects
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
