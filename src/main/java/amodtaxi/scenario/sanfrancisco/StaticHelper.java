package amodtaxi.scenario.sanfrancisco;

import java.io.File;

import ch.ethz.idsc.tensor.io.DeleteDirectory;

/* package */ enum StaticHelper {
    ;

    public static File prepareFolder(File workingDirectory, File outputDirectory) throws Exception {
        System.out.println("INFO working folder: " + workingDirectory.getAbsolutePath());
        System.out.println(outputDirectory.getAbsolutePath());
        System.out.println("WARN All files in the that folder will be deleted in:");
        for (int i = 2; i > 0; i--) {
            Thread.sleep(1000);
            System.err.println(i + " seconds");
        }
        if (workingDirectory.exists()) {
            if (outputDirectory.exists())
                DeleteDirectory.of(outputDirectory, 5, 200000);
            outputDirectory.mkdir();
        }
        return outputDirectory;
    }

}
