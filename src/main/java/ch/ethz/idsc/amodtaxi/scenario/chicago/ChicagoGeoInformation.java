/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.chicago;

import ch.ethz.idsc.amodeus.data.LocationSpec;
import ch.ethz.idsc.amodeus.data.LocationSpecDatabase;

public class ChicagoGeoInformation {

    public static void setup() {
        for (LocationSpec locationSpec : ChicagoLocationSpecs.values())
            LocationSpecDatabase.INSTANCE.put(locationSpec);
    }
}