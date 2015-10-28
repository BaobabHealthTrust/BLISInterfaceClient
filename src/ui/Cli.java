package ui;

import log.logger;
import system.settings;

/**
 * Author : Jeremy Espino MD
 * Created  10/27/15 8:37 PM
 */
public class Cli {

    public static void main(String[] args) throws Exception {
        settings.CLI_MODE = true;
        InterfaceHandler ih = new InterfaceHandler();

        ih.getConfigurations();

        logger.info("Source configuration: " + ih.getSourceConfiguration());
        logger.info("Configured " + ih.getEquipment() + " handler");

        ih.startAppropriateHandler();

    }
}
