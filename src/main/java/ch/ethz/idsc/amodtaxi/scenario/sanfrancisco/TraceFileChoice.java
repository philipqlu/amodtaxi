/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.amodeus.util.io.MultiFileReader;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodtaxi.util.RandomElements;

public class TraceFileChoice {
    public static TraceFileChoice getOrDefault(File directory, String sharedFileName) {
        try {
            return get(directory, sharedFileName);
        } catch (RuntimeException e1) {
            try {
                System.err.println("Unable to find specified files; proceeding with default mini-batch.\n" //
                        + "Full data by by Michal Piorkowski, Natasa Sarafijanovic-Djukic, and Matthias Grossglauser can be downloaded from: " //
                        + new URL("https://crawdad.org/epfl/mobility/20090224/"));
            } catch (MalformedURLException e2) {
                e2.printStackTrace();
            }
            return getDefault();
        }
    }

    public static TraceFileChoice getDefault() {
        File testScenario = new File(Locate.repoFolder(TraceFileChoice.class, "amodtaxi"), "resources/sanFranciscoScenario");
        return get(new File(testScenario, "cabspottingdata"), "new_");
    }

    public static TraceFileChoice get(File directory, String sharedFileName) {
        List<File> taxiFiles = new MultiFileReader(directory, sharedFileName).getFolderFiles();
        return new TraceFileChoice(taxiFiles);
    }

    // ---

    private final List<File> taxiFiles;

    public TraceFileChoice(List<File> taxiFiles) {
        GlobalAssert.that(taxiFiles.size() > 0);
        this.taxiFiles = taxiFiles;
    }

    /** @return all trace files containing the @param names in their name , sample usage
     *         List<File> traceFiles = (new TraceFileChoice(taxiFiles)).specified("new_aupclik","new_ojumna") */
    public List<File> specified(String... nameSegments) {
        Predicate<File> inNameSegments = file -> Arrays.stream(nameSegments).anyMatch(file.getName()::contains);
        return taxiFiles.stream().filter(inNameSegments).collect(Collectors.toList());
    }

    /** @return random choice of @param numTaxiTraces files */
    public List<File> random(int numTaxiTraces) {
        return RandomElements.of(new ArrayList<>(taxiFiles), numTaxiTraces);
    }
}
