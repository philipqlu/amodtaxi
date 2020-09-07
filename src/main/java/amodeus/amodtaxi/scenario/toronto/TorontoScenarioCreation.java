package amodeus.amodtaxi.scenario.toronto;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;

import amodeus.amodeus.net.FastLinkLookup;
import amodeus.amodeus.net.MatsimAmodeusDatabase;
import amodeus.amodeus.options.ScenarioOptions;
import amodeus.amodeus.options.ScenarioOptionsBase;
import amodeus.amodeus.taxitrip.TaxiTrip;
import amodeus.amodeus.util.AmodeusTimeConvert;
import amodeus.amodeus.util.io.CopyFiles;
import amodeus.amodeus.util.io.MultiFileTools;
import amodeus.amodeus.util.math.GlobalAssert;
import amodeus.amodeus.util.math.SI;
import amodeus.amodeus.util.matsim.NetworkLoader;
import amodeus.amodtaxi.fleetconvert.TorontoTripFleetConverter;
import amodeus.amodtaxi.fleetconvert.TripFleetConverter;
import amodeus.amodtaxi.osm.StaticMapCreator;
import amodeus.amodtaxi.scenario.FinishedScenario;
import amodeus.amodtaxi.scenario.Scenario;
import amodeus.amodtaxi.scenario.ScenarioBasicNetworkPreparer;
import amodeus.amodtaxi.scenario.ScenarioCreation;
import amodeus.amodtaxi.scenario.ScenarioLabels;
import amodeus.amodtaxi.scenario.TaxiTripsReader;
import amodeus.amodtaxi.tripfilter.TaxiTripFilterCollection;
import amodeus.amodtaxi.tripfilter.TripNetworkFilter;
import amodeus.amodtaxi.tripmodif.OriginDestinationCentroidResampling;
import amodeus.amodtaxi.tripmodif.TaxiDataModifier;
import amodeus.amodtaxi.tripmodif.TaxiDataModifierCollection;
import amodeus.amodtaxi.tripmodif.TorontoFormatModifier;
import amodeus.amodtaxi.tripmodif.TripStartTimeShiftResampling;
import amodeus.amodtaxi.util.LocalDateConvert;
import ch.ethz.idsc.tensor.io.DeleteDirectory;
import ch.ethz.idsc.tensor.qty.Quantity;

public class TorontoScenarioCreation extends ScenarioCreation {
	private static final AmodeusTimeConvert TIME_CONVERT = new AmodeusTimeConvert(ZoneId.of("-05:00"));
	private static final Random RANDOM = new Random(123);
	
	public static TorontoScenarioCreation in(File workingDirectory) throws Exception {
		TorontoSetup.in(workingDirectory);
		
		/* Download of open street map data to create scenario */
		StaticMapCreator.now(workingDirectory);
		
	    /* Prepare the network */
        ScenarioBasicNetworkPreparer.run(workingDirectory);
        
        /* Load Toronto trips data */
        File tripsFile = TorontoDataLoader.from(ScenarioLabels.amodeusFile, workingDirectory);
        
        /* Create empty scenario folder */
        File processingDir = new File(workingDirectory, "Scenario");
        if (!processingDir.isDirectory()) {
        	processingDir.mkdir();
        } else {
        	DeleteDirectory.of(processingDir, 2, 50);
        }

        CopyFiles.now(workingDirectory.getAbsolutePath(), processingDir.getAbsolutePath(), Arrays.asList(//
                ScenarioLabels.amodeusFile, //
                ScenarioLabels.config, //
                ScenarioLabels.network, //
                ScenarioLabels.networkGz, //
                ScenarioLabels.LPFile));
        ScenarioOptions scenarioOptions = new ScenarioOptions(processingDir, ScenarioOptionsBase.getDefault());
        LocalDate simulationDate = LocalDateConvert.ofOptions(scenarioOptions.getString("date"));
        
        /* Based on the trips data, create a population and assemble a AMoDeus scenario */
        File configFile = new File(scenarioOptions.getPreparerConfigName());
        GlobalAssert.that(configFile.exists());
        Config configFull = ConfigUtils.loadConfig(configFile.toString());
        final Network network = NetworkLoader.fromNetworkFile(new File(processingDir, configFull.network().getInputFile()));
        MatsimAmodeusDatabase db = MatsimAmodeusDatabase.initialize(network, scenarioOptions.getLocationSpec().referenceFrame());
        FastLinkLookup fll = new FastLinkLookup(network, db);
        
        /* Read and parse trips CSV file */
        TaxiTripsReader tripsReader = new TripsReaderToronto();
        
        TaxiDataModifier tripModifier;

        TaxiDataModifierCollection taxiDataModifierCollection = new TaxiDataModifierCollection();
        taxiDataModifierCollection.addModifier(new TripStartTimeShiftResampling(RANDOM, Quantity.of(900, SI.SECOND)));
        File vNFile = new File(processingDir, "virtualNetworkToronto");
        taxiDataModifierCollection.addModifier(new OriginDestinationCentroidResampling(RANDOM, network, fll, vNFile));
        tripModifier = taxiDataModifierCollection;

        TaxiTripFilterCollection taxiTripFilterCollection = new TaxiTripFilterCollection();
        /** trips which are faster than the network freeflow speeds would allow are removed */
        taxiTripFilterCollection.addFilter(new TripNetworkFilter(network, db,
                Quantity.of(2.235200008, "m*s^-1"), Quantity.of(3600, "s"), Quantity.of(200, "m"), true));

        File destinDir = new File(workingDirectory, "CreatedScenario");
        
        // prepare final scenario
        TripFleetConverter converter = //
                new TorontoTripFleetConverter(scenarioOptions, network, tripModifier, //
                        new TorontoFormatModifier(), taxiTripFilterCollection, tripsReader, tripsFile, new File(processingDir, "tripData"));
        File finalTripsFile = Objects.requireNonNull(Scenario.create(workingDirectory, tripsFile, converter, processingDir, simulationDate, TIME_CONVERT));

        System.out.println("The final trips file is: " + finalTripsFile.getAbsolutePath());

        FinishedScenario.copyToDir(processingDir.getAbsolutePath(), //
                destinDir.getAbsolutePath(), //
                ScenarioLabels.amodeusFile, ScenarioLabels.networkGz, ScenarioLabels.populationGz, //
                ScenarioLabels.LPFile, ScenarioLabels.config, "virtualNetworkToronto");

        return new TorontoScenarioCreation(network, db, finalTripsFile, destinDir);
        
	}

	private TorontoScenarioCreation(Network network, MatsimAmodeusDatabase db, File taxiTripsFile, File directory) {
		super(network, db, taxiTripsFile, directory);
	}

	public static void main(String[] args) throws Exception {
        TorontoScenarioCreation scenario = TorontoScenarioCreation.in(MultiFileTools.getDefaultWorkingDirectory());
        scenario.linkSpeedData(100_000);
	}

}
