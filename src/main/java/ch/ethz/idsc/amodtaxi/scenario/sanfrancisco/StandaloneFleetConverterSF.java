package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.io.File;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.utils.collections.QuadTree;

import ch.ethz.idsc.amodeus.analysis.Analysis;
import ch.ethz.idsc.amodeus.linkspeed.create.FlowTimeInvLinkSpeed;
import ch.ethz.idsc.amodeus.linkspeed.create.GLPKLinOptDelayCalculator;
import ch.ethz.idsc.amodeus.linkspeed.create.LinkSpeedsExport;
import ch.ethz.idsc.amodeus.linkspeed.create.TaxiLinkSpeedEstimator;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.geo.ClosestLinkSelect;
import ch.ethz.idsc.amodeus.util.math.CreateQuadTree;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodtaxi.trace.DayTaxiRecord;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.qty.Quantity;

/* package */ class StandaloneFleetConverterSF {

    private static final String maxAverageSpeedIdentifier = "maxAvergaeSpeed";
    private final File workingDirectory;
    private final MatsimAmodeusDatabase db;
    private final Network network;
    private final File configFile;
    private final Config configFull;
    private DayTaxiRecord dayTaxiRecord;
    private ScenarioOptions simOptions;

    private final QuadTree<Link> qt;
    private File outputDirectory;
    private final AmodeusTimeConvert timeConvert;

    private final Scalar TIME_STEP;

    private final Quantity maxAverageSpeed;

    public StandaloneFleetConverterSF(File workingDirectory, DayTaxiRecord dayTaxiRecord, //
            MatsimAmodeusDatabase db, Network network, Scalar TIME_STEP, //
            AmodeusTimeConvert timeConvert) throws Exception {
        this.workingDirectory = workingDirectory;
        this.dayTaxiRecord = dayTaxiRecord;
        this.db = db;
        this.TIME_STEP = TIME_STEP;
        this.timeConvert = timeConvert;
        simOptions = new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault());
        configFile = new File(simOptions.getPreparerConfigName());
        GlobalAssert.that(configFile.exists());
        configFull = ConfigUtils.loadConfig(configFile.toString());
        this.network = network;
        GlobalAssert.that(Objects.nonNull(network));
        qt = CreateQuadTree.of(network);
        maxAverageSpeed = (Quantity) Scalars.fromString(simOptions.getString(maxAverageSpeedIdentifier));
        System.out.println("maxAverageSpeed: " + maxAverageSpeed);
        System.out.println("maxAverageSpeed unit: " + maxAverageSpeed.unit());
        System.out.println("maxAverageSpeed value: " + maxAverageSpeed.value());
    }

    public void run(LocalDate simulationDate, boolean printDebugFile) throws Exception {

        /** STEP 0: Prepare Environment and load all configuration files */
        outputDirectory = StaticHelper.prepareFolder(workingDirectory, new File(workingDirectory, configFull.controler().getOutputDirectory()));

        /** STEP 1: Generate simobjs from daytaxirecords for visualization */
        SimulationFleetDumperSF sfd = new SimulationFleetDumperSF(db, network, TIME_STEP, qt, timeConvert);
        outputDirectory.mkdirs();
        GlobalAssert.that(outputDirectory.isDirectory());
        sfd.createDumpOf(dayTaxiRecord, outputDirectory, simulationDate);

        /** STEP 2: Find relevant trips */
        Collection<TaxiTrip> tripsAll = AllTaxiTrips.in(dayTaxiRecord).on(simulationDate);
        ClosestLinkSelect linkSelect = new ClosestLinkSelect(db, qt);
        AverageNetworkSpeed speedFilter = new AverageNetworkSpeed(network, linkSelect, simulationDate, timeConvert);
        Collection<TaxiTrip> trips = tripsAll.stream().filter(t -> speedFilter.isBelow(t, maxAverageSpeed)).collect(Collectors.toList());

        /** STEP 3: Compute minimum distance to cover in this configuration */

        System.out.println("Original number of trips:              " + tripsAll.size());
        System.out.println("Trips with reasonable avereage speeds: " + trips.size());

        /** STEP 2: Generate population.xml using the recordings */
        // OLDPopulationCreator.createAdamAndEva(workingDirectory, outputDirectory.getParentFile(), network, db);
        AdamAndEve.create(workingDirectory, trips, network, db, timeConvert, qt, simulationDate);

        /** STEP 3: Generate the report */
        try {
            Analysis analysis = Analysis.setup(simOptions, outputDirectory, network, db);
            analysis.run();
        } catch (Exception exception) {
            System.err.println("could not produce report, analysis failed.");
            exception.printStackTrace();
        }
        /** STEP 4: Create Link Speed Data File */
        try {
            // other options
            // TaxiLinkSpeedEstimator lscalc = new ConventionalLinkSpeedCalculator(db, dayTaxiRecord, simulationDate, network, timeConvert);
            // TaxiLinkSpeedEstimator lsls = new FlowLinkSpeed(trips, network, timeConvert, db, qt, simulationDate);
            TaxiLinkSpeedEstimator lsCalc = new FlowTimeInvLinkSpeed(trips, network, db, GLPKLinOptDelayCalculator.INSTANCE);
            // currently best option
            

            File linkSpeedsFile = new File(simOptions.getLinkSpeedDataName() + "");
            LinkSpeedsExport.using(linkSpeedsFile, lsCalc);//
        } catch (Exception exception) {
            System.err.println("could not generate link speed data...");
            exception.printStackTrace();
        }
    }
}