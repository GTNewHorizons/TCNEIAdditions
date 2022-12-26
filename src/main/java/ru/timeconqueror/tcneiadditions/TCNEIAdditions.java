package ru.timeconqueror.tcneiadditions;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    public static final String VERSION = "GRADLETOKEN_VERSION";
    public static final String DEPENDENCIES =
            "required-after:Thaumcraft;required-after:thaumcraftneiplugin;required-after:gtnhmixins;after:Automagy";
    public static final String GUI_FACTORY = "ru.timeconqueror.tcneiadditions.client.gui.GuiFactory";

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    @Mod.Instance(value = TCNEIAdditions.MODID)
    public static TCNEIAdditions instance;

    @SidedProxy(
            clientSide = "ru.timeconqueror.tcneiadditions.proxy.ClientProxy",
            serverSide = "ru.timeconqueror.tcneiadditions.proxy.ServerProxy")
    public static CommonProxy proxy;

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
