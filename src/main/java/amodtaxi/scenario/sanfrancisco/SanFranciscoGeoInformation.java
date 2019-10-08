/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodtaxi.scenario.sanfrancisco;

import ch.ethz.idsc.amodeus.data.LocationSpec;
import ch.ethz.idsc.amodeus.data.LocationSpecDatabase;

public class SanFranciscoGeoInformation {

    public static void setup() {
        for (LocationSpec locationSpec : SanFranciscoLocationSpecs.values())
            LocationSpecDatabase.INSTANCE.put(locationSpec);
    }
}
