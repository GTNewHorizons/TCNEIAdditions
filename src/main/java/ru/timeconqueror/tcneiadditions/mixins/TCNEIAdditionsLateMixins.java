package ru.timeconqueror.tcneiadditions.mixins;

import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;

@LateMixin
public class TCNEIAdditionsLateMixins implements ILateMixinLoader {

    static final Logger LOG = LogManager.getLogger("Thaumcraft NEI Additions Mixins");

    @Override
    public String getMixinConfig() {
        return "mixins.tcneiadditions.late.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedMods) {
        return Mixins.getLateMixins(loadedMods);
    }
}
