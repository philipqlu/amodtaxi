/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario;

import java.util.List;
import java.util.Objects;

import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodtaxi.trace.TaxiStamp;
import ch.ethz.idsc.amodtaxi.trace.TaxiTrail;

public enum Consistency {
    ;

    public static void checkTrail(List<TaxiTrail> trails) {
        /** are all values recorded properly */
        for (TaxiTrail taxiTrail : trails)
            for (TaxiStamp taxiStamp : taxiTrail.getTaxiStamps().values()) {
                GlobalAssert.that(Objects.nonNull(taxiStamp.roboTaxiStatus));
                GlobalAssert.that(Objects.nonNull(taxiStamp.gps));
                // if (taxiStamp.roboTaxiStatus == RoboTaxiStatus.DRIVEWITHCUSTOMER)
                //     GlobalAssert.that(taxiStamp.requestStatus == RequestStatus.DRIVING); // deprecated
            }
    }

    public static void check(Population population) {
        /** leg departure times */
        for (Person person : population.getPersons().values())
            for (Plan plan : person.getPlans())
                for (PlanElement pElem : plan.getPlanElements())
                    if (pElem instanceof Leg) {
                        Leg leg = (Leg) pElem;
                        GlobalAssert.that(leg.getDepartureTime().seconds() >= 0);
                    }
    }
}