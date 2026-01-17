package ru.timeconqueror.tcneiadditions.mixins.late.thaumcraft;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.recipe.GuiRecipe;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import ru.timeconqueror.tcneiadditions.nei.AspectFromItemStackHandler;
import ru.timeconqueror.tcneiadditions.util.GuiRecipeHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.client.lib.ClientTickEventsFML;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
import thaumcraft.common.lib.research.ScanManager;

@Mixin(ClientTickEventsFML.class)
public class ClientTickEventsFMLMixin {

    /**
     * @author SLPrime
     * @reason Render aspects for scanned items in NEI.
     */
    @Inject(method = "renderAspectsInGui", at = @At("HEAD"), cancellable = true, remap = false)
    private void onRenderAspectsInGui(GuiContainer gui, EntityPlayer player, CallbackInfo ci) {
        final ItemStack stack = GuiContainerManager.getStackMouseOver(gui);

        if (stack == null) {
            return;
        }

        if (gui instanceof GuiRecipe<?> guiRecipe && guiRecipe.getHandler() instanceof AspectFromItemStackHandler) {
            final Point mouse = GuiDraw.getMousePosition();
            int xSize = UtilsFX.getGuiXSize(gui);
            int ySize = UtilsFX.getGuiYSize(gui);
            int guiLeft = GuiRecipeHelper.getGuiLeft(guiRecipe);
            int guiTop = GuiRecipeHelper.getGuiTop(guiRecipe);

            if (mouse.x >= guiLeft && mouse.x <= guiLeft + xSize && mouse.y >= guiTop && mouse.y <= guiTop + ySize) {
                return;
            }
        }

        final int h = ScanManager.generateItemHash(stack.getItem(), stack.getItemDamage());
        final List<String> list = (List) Thaumcraft.proxy.getScannedObjects().get(player.getCommandSenderName());

        if (list != null && (list.contains("@" + h)  || list.contains("#" + h))) {
            renderAspects(gui, player, stack);
            ci.cancel();
        }

    }

    private static void renderAspects(GuiContainer gui, EntityPlayer player, ItemStack stack) {
        AspectList tags = ThaumcraftCraftingManager.getObjectTags(stack);
        tags = ThaumcraftCraftingManager.getBonusTags(stack, tags);

        if (tags != null && tags.size() > 0) {
            GL11.glPushMatrix();
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);

            Point mouse = GuiDraw.getMousePosition();
            int shiftx = -8;
            int shifty = -8;
            if (Thaumcraft.instance.aspectShift) {
                shiftx -= 8;
                shifty -= 8;
            }

            int index = 0;
            for (Aspect tag : tags.getAspectsSortedAmount()) {
                if (tag == null) continue;

                int x = mouse.x + 17 + index * 18;
                int y = mouse.y + 7 - 33;

                UtilsFX.bindTexture("textures/aspects/_back.png");
                GL11.glPushMatrix();
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glTranslated(x + shiftx - 2, y + shifty - 2, 0);
                GL11.glScaled(1.25D, 1.25D, 1);
                UtilsFX.drawTexturedQuadFull(0, 0, UtilsFX.getGuiZLevel(gui));
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glPopMatrix();

                if (Thaumcraft.proxy.playerKnowledge
                        .hasDiscoveredAspect(player.getCommandSenderName(), tag)) {
                    UtilsFX.drawTag(
                            x + shiftx,
                            y + shifty,
                            tag,
                            tags.getAmount(tag),
                            0,
                            UtilsFX.getGuiZLevel(gui)
                    );
                } else {
                    UtilsFX.bindTexture("textures/aspects/_unknown.png");
                    GL11.glPushMatrix();
                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    GL11.glTranslated(x + shiftx, y + shifty, 0);
                    UtilsFX.drawTexturedQuadFull(0, 0, UtilsFX.getGuiZLevel(gui));
                    GL11.glDisable(GL11.GL_BLEND);
                    GL11.glPopMatrix();
                }

                index++;
            }

            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glPopAttrib();
            GL11.glPopMatrix();
        }

    }

}
