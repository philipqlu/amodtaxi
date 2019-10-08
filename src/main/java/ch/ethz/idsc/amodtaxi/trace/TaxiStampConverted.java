package ch.ethz.idsc.amodtaxi.trace;

import java.util.List;

import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;

public interface TaxiStampConverted {
    // TODO doc
    public TaxiStamp from(List<String> dataFileRow, AmodeusTimeConvert timeConvert);

}
