package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.io.File;

import amodeus.amodeus.util.io.GZHandler;
import ch.ethz.idsc.amodtaxi.scenario.ScenarioLabels;
import ch.ethz.idsc.amodtaxi.scenario.ScenarioSetup;
import ch.ethz.idsc.amodtaxi.scenario.TestDirectories;

/* package */ enum SanFranciscoTestSetup {
    ;

    public static void in(File workingDir) throws Exception {
        SanFranciscoGeoInformation.setup();
        ScenarioSetup.in(workingDir, TestDirectories.SAN_FRANCISCO, ScenarioLabels.networkGz);
        GZHandler.extract(new File(workingDir, ScenarioLabels.networkGz), new File(workingDir, ScenarioLabels.network));
    }
}
