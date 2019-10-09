/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.tripfilter;

import java.util.function.Predicate;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.taxitrip.ShortestDurationCalculator;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodtaxi.linkspeed.iterative.DurationCompare;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;

/** This filter calculates the min-time-path in the network without traffic.
 * Then, only trips are kept which:
 * - are slower than the network allows (speeding etc.)
 * - have less delay w.r.t. free speed than maxDelay
 * - have an average speed faster than minSpeed (endless waiting, leaving taxi meter on, ...)
 * - are longer than a minimum distance
 * - have a nontrivial path of more than 1 link */
public class TripNetworkFilter implements Predicate<TaxiTrip> {

    private final ShortestDurationCalculator calc;
    private final Scalar maxDelay;
    private final Scalar minSpeed;
    private final Scalar minDistance;

    public TripNetworkFilter(Network network, MatsimAmodeusDatabase db, //
            Scalar minSpeed, Scalar maxDelay, Scalar minDistance) {
        calc = new ShortestDurationCalculator(network, db);
        this.maxDelay = maxDelay;
        this.minSpeed = minSpeed;
        this.minDistance = minDistance;
    }

    @Override
    public boolean test(TaxiTrip trip) {

        /** getting the data */
        DurationCompare compare = new DurationCompare(trip, calc);

        /** evaluating criteria */
        boolean slowerThanNetwork = Scalars.lessEquals(compare.nwPathDurationRatio, RealScalar.ONE);
        boolean belowMaxDelay = Scalars.lessEquals(trip.duration.subtract(compare.pathTime), maxDelay);
        boolean fasterThanMinSpeed = Scalars.lessEquals(minSpeed, compare.pathDist.divide(trip.duration));
        boolean longerThanMinDistance = Scalars.lessEquals(minDistance, compare.pathDist);
        boolean hasRealPath = compare.path.links.size() > 1;

        /** return true if all ok */
        return slowerThanNetwork && belowMaxDelay && fasterThanMinSpeed && longerThanMinDistance && hasRealPath;
    }
}
