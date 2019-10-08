// code by jph
package amodtaxi.scenario.sanfrancisco;

import java.time.LocalDateTime;

import org.matsim.api.core.v01.Coord;

import ch.ethz.idsc.amodeus.dispatcher.core.RequestStatus;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;

public class TaxiStamp {

    // public int globalTime; /* unix epoch time in [s] */
    public LocalDateTime globalTime; /* unix epoch time in [s] */
    public RoboTaxiStatus roboTaxiStatus;
    public int requestIndex;
    public Coord gps; /* in format [longitude, latitude] */
    public int linkIndex;
    public double linkSpeed;
    public boolean occupied = false;

    // TODO remove this after integrating Zurich code
    @Deprecated
    public RequestStatus requestStatus;

}
