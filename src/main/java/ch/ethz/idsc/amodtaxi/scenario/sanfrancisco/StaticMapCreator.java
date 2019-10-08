package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

import org.matsim.pt2matsim.run.Osm2MultimodalNetwork;

import ch.ethz.idsc.amodeus.util.OsmLoader;

public class StaticMapCreator {

    public static void now(File processingDir) throws FileNotFoundException, IOException {

        System.out.println("Downloading open street map data, this may take a while if not already downloaded...");
        File osmFile = new File(processingDir, ScenarioLabels.osmData);
        File scenarioOptions = new File(processingDir, ScenarioLabels.amodeusFile);
        Objects.requireNonNull(scenarioOptions);
        OsmLoader osm = new OsmLoader(scenarioOptions);
        osm.saveIfNotAlreadyExists(osmFile);
        /** generate a network using pt2Matsim */
        // TODO only recreate if not existing
        String pt2MatsimSettings = processingDir.getAbsolutePath() + "/" + ScenarioLabels.pt2MatSettings;
        System.out.println(pt2MatsimSettings);
        Osm2MultimodalNetwork.run(pt2MatsimSettings);
    }

}
