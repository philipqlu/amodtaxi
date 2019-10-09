/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.chicago;

import ch.ethz.idsc.amodeus.util.CsvReader.Row;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.amodtaxi.readers.TaxiTripsReader;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;

/* package */ abstract class ChicagoTripsReaderBasic //
        extends TaxiTripsReader {

    public ChicagoTripsReaderBasic(String delim) {
        super(delim);
    }

    @Override
    public final Scalar getDistance(Row row) {
        return Quantity.of(Double.valueOf(row.get("trip_miles"))//
                * ScenarioConstants.milesToM, SI.METER);
    }

    @Override
    public final Scalar getWaitingTime(Row row) {
        return null;
    }

}