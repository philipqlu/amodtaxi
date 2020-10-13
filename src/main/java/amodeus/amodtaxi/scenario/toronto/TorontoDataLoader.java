/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodtaxi.scenario.toronto;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

import amodeus.amodeus.options.ScenarioOptionsBase;
import amodeus.amodeus.util.math.GlobalAssert;

public enum TorontoDataLoader {
    ;
	
	public static File from(String propertiesName, File dir) throws Exception {
		GlobalAssert.that(dir.isDirectory());
		File propertiesFile = new File(dir, propertiesName);
		GlobalAssert.that(propertiesFile.isFile());
		
		/* Load properties */
		Properties properties = new Properties();
		try (FileInputStream fis = new FileInputStream(propertiesFile)) {
			properties.load(fis);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		File tripsFile = null;
		try {
			String tripsFileName = properties.getProperty("tripsFile");
			tripsFile = new File(dir, tripsFileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tripsFile;
	}
}
