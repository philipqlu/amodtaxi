package amodtaxi.scenario.sanfrancisco;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.data.ReferenceFrame;
import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.io.CopyFiles;
import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.amodeus.util.io.MultiFileReader;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;

public class CreateSanFranciscoScenario {

    private static final Collection<LocalDate> dates = DateSelect.specific(06, 04);
    private static final int numTraceFiles = 536;// maximum taxis are: 536;
    private static final AmodeusTimeConvert timeConvert = new AmodeusTimeConvert(ZoneId.of("America/Los_Angeles"));
    private static final ReferenceFrame referenceFrame = SanFranciscoReferenceFrames.SANFRANCISCO;
    private static final Scalar timeStep = Quantity.of(10, "s");

    /** @param args
     *            = [dataDirectory, workingDirectory, destinationDirectory],
     *            meaning [raw data directory, directory for processing,
     *            directory to save created scenario]
     * 
     *            The data directory must contain all needed information to
     *            create the San Francisco scenario, the processig Directory can
     *            be empty and the destinationDirectory as well. On clruch's
     *            file system:
     * 
     *            /home/clruch/polybox/DataSets/SanFranciscoTaxi
     *            /home/clruch/Downloads/2018_06_07_SanFranciscoScenarioFinal
     *            /home/clruch/Downloads/2018_06_07_SanFranciscoScenarioFinal
     * 
     * @throws Exception */
    public static void main(String[] args) throws Exception {
        File dataDir = new File(args[0]);
        File processingDir = new File(args[1]);
        File destinDir = new File(args[2]);
        run(dataDir, processingDir, destinDir);
    }

    public static void run(File dataDir, File processingDir, File destinDir) throws Exception {
        SanFranciscoGeoInformation.setup();

        /** copy taxi trace files */
        System.out.println("dataDir: " + dataDir.getAbsolutePath());
        List<File> taxiFiles = new MultiFileReader(new File(dataDir, "cabspottingdata"), "new_").getFolderFiles();
        List<File> traceFiles = (new TraceFileChoice(taxiFiles)).random(numTraceFiles);
        // List<File> traceFiles = (new
        // TraceFileChoice(taxiFiles)).specified("equioc", "onvahe", "epkiapme",
        // "ippfeip");

        /** copy other scenario files */
        File settingsDir = new File(Locate.repoFolder(CreateSanFranciscoScenario.class, "amodeus"), "resources/sanFranciscoScenario");
        CopyFiles.now(settingsDir.getAbsolutePath(), processingDir.getAbsolutePath(), //
                Arrays.asList(new String[] { "AmodeusOptions.properties", "av.xml", "config_full.xml", "config.xml", //
                        "config_fullPublish.xml", "pt2matsim_settings.xml" }),
                true);

        /** download of open street map data to create scenario map using
         * pt2matsim */
        StaticMapCreator.now(processingDir);

        /** remove all links except car from network */
        Network network = InitialNetworkPreparerSF.run(processingDir);
        MatsimAmodeusDatabase db = MatsimAmodeusDatabase.initialize(network, referenceFrame);

        /** get dayTaxiRecord from trace files */
        // QuadTree<Link> qt = CreateQuadTree.of(network, db);
        FastLinkLookup qt = new FastLinkLookup(network, db);
        DayTaxiRecord dayTaxiRecord = ReadTraceFiles.in(qt, traceFiles, db);

        /** create scenario */
        HashMap<LocalDate, File> scenarioDirs = new HashMap<>();
        for (LocalDate localDate : dates) {
            try {
                /** compute scenario */
                StandaloneFleetConverterSF sfc = new StandaloneFleetConverterSF(processingDir, //
                        dayTaxiRecord, db, network, timeStep, timeConvert);
                sfc.run(localDate, true);

                /** copy scenario to new location */
                String destinDirDayStr = destinDir + "/" + localDate.toString();
                File destinDirDay = new File(destinDirDayStr);
                if (!destinDirDay.isDirectory())
                    destinDirDay.mkdirs();
                scenarioDirs.put(localDate, destinDirDay);
                try {
                    ScenarioAssemblerSF.copyFinishedScenario(processingDir.getAbsolutePath(), destinDirDay);
                } catch (Exception ex) {
                    System.err.println("failed to copy scenario for " + localDate);
                }
            } catch (Exception ex) {
                System.err.println("could not create scenario for localDate " + localDate);
                ex.printStackTrace();
            }
        }

        // /** test consistency of created scenarios with independent analysis
        // */
        // TaxiData scenarioData =
        // CreateSanFranciscoStaticAnalysis.runStaticAnalysis(destinDir,
        // traceFiles, network, db);
        // HashMap<LocalDate, Integer> dataReqCount = scenarioData.reqPerDay;
        // ScenarioRequestCountSF.compareRequestCount(dates, dataReqCount,
        // scenarioDirs);
    }
}
