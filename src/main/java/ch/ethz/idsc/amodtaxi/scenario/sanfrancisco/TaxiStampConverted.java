package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.util.List;

import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;

/* package */ interface TaxiStampConverted {

    public TaxiStamp from(List<String> dataFileRow, AmodeusTimeConvert timeConvert);

}
