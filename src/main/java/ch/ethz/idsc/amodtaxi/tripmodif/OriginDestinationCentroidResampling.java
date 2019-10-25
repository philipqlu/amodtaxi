/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.tripmodif;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;

import ch.ethz.idsc.amodeus.dispatcher.util.TensorLocation;
import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import ch.ethz.idsc.amodeus.net.TensorCoords;
import ch.ethz.idsc.amodeus.prep.NetworkCreatorUtils;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.util.network.NodeAdjacencyMap;
import ch.ethz.idsc.amodeus.virtualnetwork.CentroidVirtualNetworkCreator;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetworkIO;
import ch.ethz.idsc.tensor.Tensor;

public class OriginDestinationCentroidResampling implements TripModifier {

    private final Random random;
    private final Network network;
    private final FastLinkLookup fastLinkLookup;
    private final File vNetworkExportFile;
    private final Set<Tensor> uniqueOrigins = new HashSet<>();
    private final Set<Tensor> uniqueDestins = new HashSet<>();
    private final Set<Tensor> uniqueLocations = new HashSet<>();
    private VirtualNetwork<Link> centroidVirtualNetwork;
    private final Set<String> airportBoundaryLinks;

    private boolean modificationStarted = false; // flag to detect start of modification process

    public OriginDestinationCentroidResampling(Random random, Network network, //
            FastLinkLookup fastLinkLookup, File vNetworkExportFile) {
        this.random = random;
        this.network = network;
        this.fastLinkLookup = fastLinkLookup;// new FastLinkLookup(network, db);
        this.vNetworkExportFile = vNetworkExportFile;
        airportBoundaryLinks = ChicagoAirportBoundaryLinks.get(fastLinkLookup);
    }

    @Override
    public TaxiTrip modify(TaxiTrip taxiTrip) {
        if (!modificationStarted) {
            System.out.println("uniqueOrigins: " + uniqueOrigins.size());
            System.out.println("uniqueDestins: " + uniqueDestins.size());
            System.out.println("uniqueLocations: " + uniqueLocations.size());

            // add airport links to ensure airport related traffic stays in
            // small concentrated node
            for (Link link : network.getLinks().values()) {
                if (airportBoundaryLinks.contains(link.getId().toString())) {
                    uniqueLocations.add(fastLinkLookup.getWGS84fromLink(link));
                }
            }

            // do everything that needs to be done once the first time
            centroidVirtualNetwork = getCentroidVirtualNetwork();
            try {
                VirtualNetworkIO.toByte(vNetworkExportFile, centroidVirtualNetwork);
            } catch (IOException e) {
                System.err.println("Unable to export vitual network used for request distribution: ");
                e.printStackTrace();
            }
            // --
            modificationStarted = true;
        }
        // modify the trip
        TaxiTrip originalTrip = taxiTrip;
        // get WGS84 origin / destination
        Tensor origin = originalTrip.pickupLoc;
        Tensor destin = originalTrip.dropoffLoc;
        // get origin /destination links
        Link lOrigin = fastLinkLookup.getLinkFromWGS84(TensorCoords.toCoord(origin));
        Link lDestin = fastLinkLookup.getLinkFromWGS84(TensorCoords.toCoord(destin));
        // using virtual node of lOrigin / lDestin, randomly distribute in virtual Node

        // origin
        Set<Link> origCentroidLinks = centroidVirtualNetwork.getVirtualNode(lOrigin).getLinks();
        int numOrig = random.nextInt(origCentroidLinks.size());
        Link lOriginDist = origCentroidLinks.stream().skip(numOrig).findFirst().get();

        // destination
        Set<Link> destCentroidLinks = centroidVirtualNetwork.getVirtualNode(lDestin).getLinks();
        int numDest = random.nextInt(destCentroidLinks.size());
        Link lDestinDist = destCentroidLinks.stream().skip(numDest).findFirst().get();

        return TaxiTrip.of( //
                originalTrip.localId, //
                originalTrip.taxiId, //
                fastLinkLookup.getWGS84fromLink(lOriginDist), //
                fastLinkLookup.getWGS84fromLink(lDestinDist), //
                originalTrip.distance, //
                originalTrip.waitTime, //
                originalTrip.pickupDate, //
                originalTrip.duration);
    }

    @Override
    public void notify(TaxiTrip taxiTrip) {
        uniqueOrigins.add(taxiTrip.pickupLoc);
        uniqueDestins.add(taxiTrip.dropoffLoc);
        uniqueLocations.add(taxiTrip.pickupLoc);
        uniqueLocations.add(taxiTrip.dropoffLoc);
    }

    private VirtualNetwork<Link> getCentroidVirtualNetwork() {
        @SuppressWarnings("unchecked")
        Collection<Link> elements = (Collection<Link>) network.getLinks().values();
        Map<Node, Set<Link>> uElements = NodeAdjacencyMap.of(network);

        /** generate the link centroids based on the unique locations */
        List<Link> centroids = new ArrayList<>();
        uniqueLocations.stream().forEach(loc -> {
            Link link = fastLinkLookup.getLinkFromWGS84(TensorCoords.toCoord(loc));
            centroids.add(link);
        });

        /** create the virtual network using the centroidvirtualNetworkCreator */

        CentroidVirtualNetworkCreator<Link, Node> vnc = new CentroidVirtualNetworkCreator<>(//
                elements, centroids, TensorLocation::of, NetworkCreatorUtils::linkToID, uElements, true);

        return vnc.getVirtualNetwork();

    }

}
