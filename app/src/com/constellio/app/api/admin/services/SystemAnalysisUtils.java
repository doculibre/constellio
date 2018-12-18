package com.constellio.app.api.admin.services;

import com.constellio.app.entities.system.SystemMemory.MemoryDetails;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.conf.FoldersLocatorMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class SystemAnalysisUtils {
	private static final String MEMINFO_PATH = "/proc/meminfo";
	private static final String MEMTOTAL_PARAMETER = "MemTotal";
	private static final String CONSTELLIO_MEMORY_PARAMETER = "wrapper.java.maxmemory";
	private static final String SOLR_CONF_PATH = "/opt/solr/bin/solr.in.sh";
	private static final String SOLR_CURRENT_CONF_PATH = "/opt/solr-current/bin/solr.in.sh";
	private static final String SOLR_MEMORY_PARAMETER = "SOLR_JAVA_MEM";

	public static MemoryDetails getTotalSystemMemory() {
		String memTotal = findValueOfParameter(MEMINFO_PATH, MEMTOTAL_PARAMETER, ":");
		return MemoryDetails.build(memTotal, "KB");
	}

	public static MemoryDetails getAllocatedMemoryForConstellio() {
		FoldersLocator foldersLocator = new FoldersLocator();
		if (foldersLocator.getFoldersLocatorMode() == FoldersLocatorMode.PROJECT) {
			return null;
		}

		String allocatedMemory = findValueOfParameter(foldersLocator.getWrapperConf().getAbsolutePath(), CONSTELLIO_MEMORY_PARAMETER, "=");
		return MemoryDetails.build(allocatedMemory, "MB");
	}

	public static MemoryDetails getAllocatedMemoryForSolr() {
		String allocatedMemory = findValueOfParameter(SOLR_CONF_PATH, SOLR_MEMORY_PARAMETER, "=");
		if (allocatedMemory == null) {
			allocatedMemory = findValueOfParameter(SOLR_CURRENT_CONF_PATH, SOLR_MEMORY_PARAMETER, "=");
		}
		if (allocatedMemory != null) {
			allocatedMemory = allocatedMemory.replaceAll("\"", "");
			String[] splittedValue = allocatedMemory.split("Xmx");
			if (splittedValue.length == 2) {
				allocatedMemory = splittedValue[1];
			}
		}
		return MemoryDetails.build(allocatedMemory, "MB");
	}

	private static String findValueOfParameter(String fileUrl, String parameter, String separator) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(new File(fileUrl)));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] params = line.split(separator);
				if (params.length == 2) {
					String argument = params[0];
					String value = params[1];
					if (argument.equals(parameter)) {
						return value;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
