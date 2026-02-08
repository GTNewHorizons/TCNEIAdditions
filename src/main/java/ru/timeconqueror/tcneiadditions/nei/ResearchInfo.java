package ru.timeconqueror.tcneiadditions.nei;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.recipe.GuiRecipe;
import ru.timeconqueror.tcneiadditions.util.TCUtil;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategoryList;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.client.lib.UtilsFX;

public class ResearchInfo {

    private final boolean isResearched;
    private final String category;
    private final String research;
    private final ResearchItem researchItem;
    private final ResourceLocation icon;
    private int prevX, prevY;

    public ResearchInfo(ResearchItem researchItem, boolean isResearched) {
        this.researchItem = researchItem;
        if (researchItem != null) {
            this.category = ResearchCategories.getCategoryName(researchItem.category);
            ResearchCategoryList list = ResearchCategories.getResearchList(researchItem.category);
            if (list != null && list.icon != null) {
                icon = list.icon;
            } else {
                icon = null;
            }
            this.research = researchItem.getName();
        } else {
            category = null;
            icon = null;
            research = "N/A";
        }
        this.isResearched = isResearched;
    }

    public void onHover(List<String> list) {
        list.add(
                String.format(
                        "%s%s%s : %s",
                        EnumChatFormatting.UNDERLINE,
                        isResearched ? EnumChatFormatting.GREEN : EnumChatFormatting.RED,
                        category != null ? category : "N/A",
                        research));
        try {
            TCUtil.getResearchPrerequisites(list, researchItem);
        } catch (Exception ignored) {} // modded thaum is weird sometimes
        if (list.size() > 1) {
            list.add(1, "");
        }
    }

    public void onDraw(int x, int y) {
        prevX = x;
        prevY = y;
        GL11.glPushMatrix();
        if (icon != null) {
            UtilsFX.bindTexture(icon);
        } else {
            UtilsFX.bindTexture("textures/items/thaumonomicon.png");
        }
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glTranslated(x, y, 0);
        GL11.glScaled(0.8, 0.8, 0);
        UtilsFX.drawTexturedQuadFull(0, 0, 0.0D);

        if (!isResearched) {
            GL11.glTranslated(20, 1, 0);
            GL11.glScaled(1.9, 1.9, 0);
            GuiDraw.drawString("X", 0, 0, 0xAB0000, false);
        }

        GL11.glPopMatrix();
    }

    public Rectangle getRect(GuiRecipe<?> gui, int recipeIndex) {
        final Point offset = gui.getRecipePosition(recipeIndex);
        return new Rectangle(gui.guiLeft + offset.x + prevX, gui.guiTop + offset.y + prevY, 24, 13);
    }

}
