/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodtaxi.scenario.fleetconvert;

import org.matsim.api.core.v01.network.Network;

import amodtaxi.scenario.readers.TaxiTripsReader;
import amodtaxi.scenario.tripfilter.DeprcTripDistanceFilter;
import amodtaxi.scenario.tripfilter.DeprcTripNetworkFilter;
import amodtaxi.scenario.tripfilter.TaxiTripFilter;
import amodtaxi.scenario.tripfilter.TripDurationFilter;
import amodtaxi.scenario.tripmodif.TaxiDataModifier;
import amodtaxi.scenario.tripmodif.TripBasedModifier;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.qty.Quantity;

public class ChicagoTripFleetConverter extends TripFleetConverter {

    public ChicagoTripFleetConverter(ScenarioOptions scenarioOptions, Network network, TaxiTripFilter cleaner, //
            TripBasedModifier corrector, TaxiDataModifier generalModifier, TaxiTripFilter finalFilters, //
            TaxiTripsReader tripsReader) {
        super(scenarioOptions, network, cleaner, corrector, generalModifier, finalFilters, tripsReader);
    }

    @Override
    public void setFilters() {
        // TODO trips were redistributed in 15 minutes interval randomly before,
        // add this again if necessary...
        primaryFilter.addFilter(new DeprcTripNetworkFilter(scenarioOptions, network));
        primaryFilter.addFilter(new TripDurationFilter(Quantity.of(0, SI.SECOND), Quantity.of(20000, SI.SECOND)));
        primaryFilter.addFilter(new DeprcTripDistanceFilter(Quantity.of(500, SI.METER), Quantity.of(50000, SI.METER)));
    }

}
