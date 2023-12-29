package ru.timeconqueror.tcneiadditions;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import ru.timeconqueror.tcneiadditions.proxy.CommonProxy;

@Mod(
        modid = TCNEIAdditions.MODID,
        name = TCNEIAdditions.NAME,
        version = TCNEIAdditions.VERSION,
        dependencies = TCNEIAdditions.DEPENDENCIES,
        guiFactory = TCNEIAdditions.GUI_FACTORY)
public class TCNEIAdditions {

    public static final String MODID = "tcneiadditions";
    public static final String NAME = "Thaumcraft NEI Additions";
    public static final String VERSION = Tags.VERSION;
    public static final String DEPENDENCIES = "required-after:Thaumcraft;required-after:thaumcraftneiplugin;required-after:gtnhmixins;after:Automagy";
    public static final String GUI_FACTORY = "ru.timeconqueror.tcneiadditions.client.gui.GuiFactory";

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    @Mod.Instance(value = TCNEIAdditions.MODID)
    public static TCNEIAdditions instance;

    @SidedProxy(
            clientSide = "ru.timeconqueror.tcneiadditions.proxy.ClientProxy",
            serverSide = "ru.timeconqueror.tcneiadditions.proxy.ServerProxy")
    public static CommonProxy proxy;

    public static String thaumcraftNEIPluginVersion;

    @Mod.EventHandler
    public void construct(FMLConstructionEvent event) {
        Loader.instance().getModList().stream().filter(mod -> "thaumcraftneiplugin".equals(mod.getModId())).findAny()
                .ifPresent(mod -> {
                    thaumcraftNEIPluginVersion = mod.getMetadata().version;
                    try {
                        // replace @VERSION@ with actual mod version
                        FieldUtils.writeField(mod, "internalVersion", thaumcraftNEIPluginVersion, true);
                    } catch (Exception e) {
                        LOGGER.warn("Failed to set internal version of Thaumcraft NEI Plugin!", e);
                    }
                });
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
