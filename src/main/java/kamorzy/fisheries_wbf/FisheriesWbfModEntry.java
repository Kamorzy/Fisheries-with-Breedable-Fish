package kamorzy.fisheries_wbf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;

public final class FisheriesWbfModEntry implements ModInitializer {
    public static final String MOD_ID = "fisheries_wbf";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Fisheries with Breedable Fish initialized.");
    }
}
