package amodtaxi.scenario.sanfrancisco;

import ch.ethz.idsc.amodeus.net.RequestContainer;

/* package */ enum RCDeepCopy {
    ;

    public static RequestContainer deepCopy(RequestContainer reqCon) {
        RequestContainer copy = new RequestContainer();
        copy.fromLinkIndex = reqCon.fromLinkIndex;
        copy.toLinkIndex = reqCon.toLinkIndex;
        copy.submissionTime = reqCon.submissionTime;
        copy.requestIndex = reqCon.requestIndex;
        copy.requestStatus = reqCon.requestStatus;
        return copy;
    }
}
