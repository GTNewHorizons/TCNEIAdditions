package ru.timeconqueror.tcneiadditions.client;

import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.StatCollector;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;

import codechicken.nei.recipe.HandlerInfo;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import ru.timeconqueror.tcneiadditions.TCNEIAdditions;
import ru.timeconqueror.tcneiadditions.util.TCNAConfig;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.common.lib.crafting.ArcaneSceptreRecipe;
import thaumcraft.common.lib.crafting.ArcaneWandRecipe;

public class TCNAClient implements IResourceManagerReloadListener {

    private final String[] UNLOCALIZED_COLORS = { "tcneiadditions.gui.textColor",
            "tcneiadditions.gui.instabilityColor0", "tcneiadditions.gui.instabilityColor1",
            "tcneiadditions.gui.instabilityColor2", "tcneiadditions.gui.instabilityColor3",
            "tcneiadditions.gui.instabilityColor4", "tcneiadditions.gui.instabilityColor5",
            "tcneiadditions.gui.researchNameColor", "tcneiadditions.gui.loadingTextColor" };

    public static final int NEI_RECIPE_HEIGHT = HandlerInfo.DEFAULT_HEIGHT;
    public static final int NEI_GUI_WIDTH = HandlerInfo.DEFAULT_WIDTH;
    public static final int NEI_GUI_HEIGHT = 116;

    private static final TCNAClient instance = new TCNAClient();
    private final Queue<FutureTask<?>> tasks = Queues.newArrayDeque();
    /**
     * Detects if any mod turned off ArcaneWandRecipes by deleting them.
     */
    private Boolean wandRecipesDeleted = null;

    private HashMap<String, Integer> colors;

    public static TCNAClient getInstance() {
        return instance;
    }

    @SubscribeEvent
    public void onPlayerEntered(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        wandRecipesDeleted = true;

        for (Object craftingRecipe : ThaumcraftApi.getCraftingRecipes()) {
            if (craftingRecipe instanceof ArcaneWandRecipe || craftingRecipe instanceof ArcaneSceptreRecipe) {
                wandRecipesDeleted = false;
                break;
            }
        }

        if (wandRecipesDeleted) {
            TCNEIAdditions.LOGGER.info(
                    "Detected removing of ArcaneWandRecipe and ArcaneSceptreRecipe by another mod. Applying NEI Wand Recipe searching by ShapedArcaneRecipes...");
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            synchronized (this.tasks) {
                while (!this.tasks.isEmpty()) {
                    FutureTask<?> task = this.tasks.poll();
                    task.run();
                    try {
                        task.get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID.equals(TCNEIAdditions.MODID)) {
            TCNAConfig.syncConfig();
        }
    }

    public void registerResourceReloadListener() {
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(this);
    }

    @Override
    public void onResourceManagerReload(IResourceManager rm) {
        updateColorOverride();
    }

    private void updateColorOverride() {
        colors = new HashMap<String, Integer>();
        for (String c : UNLOCALIZED_COLORS) {
            String hex = StatCollector.translateToLocal(c);
            int color = 0x000000;
            if (hex.length() <= 6) {
                try {
                    color = Integer.parseUnsignedInt(hex, 16);
                } catch (NumberFormatException e) {
                    TCNEIAdditions.LOGGER.warn("Couldn't format color correctly for: " + c);
                }
            }
            colors.put(c, color);
        }
    }

    public int getColor(String key) {
        return colors.get(key) != null ? colors.get(key) : 0x000000;
    }

    /**
     * {@link Minecraft#func_152344_a(Runnable)} which means addScheduledTask doesn't catch and rethrows exception, so
     * here's the right method to support so.
     */
    @SuppressWarnings("UnstableApiUsage")
    public ListenableFuture<Object> addScheduledTask(@NotNull Runnable runnable) {
        Callable<Object> callable = Executors.callable(runnable);

        if (!isMainThread()) {
            ListenableFutureTask<Object> futureTask = ListenableFutureTask.create(callable);

            synchronized (tasks) {
                this.tasks.add(futureTask);
                return futureTask;
            }
        } else {
            try {
                return Futures.immediateFuture(callable.call());
            } catch (Exception exception) {
                return Futures.immediateFailedFuture(exception);
            }
        }
    }

    public boolean isMainThread() {
        return Minecraft.getMinecraft().func_152345_ab();
    }

    public boolean areWandRecipesDeleted() {
        return wandRecipesDeleted;
    }
}
