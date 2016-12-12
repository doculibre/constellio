package com.constellio.sdk.dev.tools.i18n;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.constellio.data.utils.PropertyFileUtils;

public class CombinePropertyFilesServices {

	List<File> inputFiles;
	File output;

	public static void combine(List<File> inputFiles, File output) {
		combine(inputFiles, output, new HashMap<String, String>());
	}

	public static void combine(List<File> inputFiles, File output, Map<String, String> extra) {

		try {
			FileUtils.deleteQuietly(output);

			Collections.sort(inputFiles, new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});

			Map<String, String> properties = new HashMap<>();

			for (File inputFile : inputFiles) {
				properties.putAll(PropertyFileUtils.loadKeyValues(inputFile));
			}

			properties.putAll(extra);

			List<String> keys = new ArrayList<>(properties.keySet());

			Collections.sort(keys);

			BufferedWriter bw = new BufferedWriter(new FileWriter(output));

			for (String key : keys) {
				bw.append(key);
				bw.append("=");
				bw.append(properties.get(key));
				bw.newLine();
			}

			bw.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
