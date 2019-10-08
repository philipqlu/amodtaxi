package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.NavigableMap;

import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;

/** A TaxiTrail contains a sorted map <Integer, TaxiStamp> with all the
 * {@link TaxiStamp} of the dataset.
 * 
 * @author clruch */
public interface TaxiTrail {

    /** part 1: filling with data
     * 
     * @param list of data file for a certain time */
    public void insert(List<String> list);

    /** part 2: processing of the data */
    public void processFilledTrail() throws Exception;

    /** part 3: access functions */
    public LocalDateTime getMaxTime();

    public NavigableMap<LocalDateTime, TaxiStamp> getTaxiStamps();

    public String getID();

    /** @return {@link Collection} of all {@link TaxiTrip}s with
     *         pickup taking place on {@link LocalDate} @param localDate */
    public Collection<TaxiTrip> allTripsBeginningOn(LocalDate localDate);

}
