package com.constellio.app.api.admin.services;

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

	public static String getTotalSystemMemory() {
		String memTotal = findValueOfParameter(MEMINFO_PATH, MEMTOTAL_PARAMETER, ":");
		if (memTotal != null) {
			memTotal = toHumanReadleNumbers(memTotal, "kB");
		}
		return memTotal;
	}

	public static String getAllocatedMemoryForConstellio() {
		FoldersLocator foldersLocator = new FoldersLocator();
		if (foldersLocator.getFoldersLocatorMode() == FoldersLocatorMode.PROJECT) {
			return null;
		}

		String allocatedMemory = findValueOfParameter(foldersLocator.getWrapperConf().getAbsolutePath(), CONSTELLIO_MEMORY_PARAMETER, "=");
		if (allocatedMemory != null) {
			allocatedMemory = toHumanReadleNumbers(allocatedMemory, "MB");
		}
		return allocatedMemory;
	}

	public static String getAllocatedMemoryForSolr() {
		FoldersLocator foldersLocator = new FoldersLocator();
		if (foldersLocator.getFoldersLocatorMode() == FoldersLocatorMode.PROJECT) {
			return null;
		}
		String allocatedMemory = findValueOfParameter(SOLR_CONF_PATH, SOLR_MEMORY_PARAMETER, "=");
		if (allocatedMemory == null) {
			allocatedMemory = findValueOfParameter(SOLR_CURRENT_CONF_PATH, SOLR_MEMORY_PARAMETER, "=");
		}
		if (allocatedMemory != null) {
			allocatedMemory = allocatedMemory.replaceAll("\"", "");
			String[] splittedValue = allocatedMemory.split("Xmx");
			if (splittedValue.length == 2) {
				allocatedMemory = toHumanReadleNumbers(splittedValue[1], "m");
			} else {
				return null;
			}
		}
		return allocatedMemory;
	}

	public static Double getPercentageOfAllocatedMemory(String totalSystemMemory, String allocatedMemoryForConstellio,
														String allocatedMemoryForSolr) {
		if (totalSystemMemory != null && allocatedMemoryForConstellio != null && allocatedMemoryForSolr != null &&
			totalSystemMemory.endsWith(" GB") && allocatedMemoryForConstellio.endsWith(" GB") && allocatedMemoryForSolr.endsWith(" GB")) {
			return roundToTwoDecimals(
					(Double.parseDouble(allocatedMemoryForConstellio.replace(" GB", "")) + Double.parseDouble(allocatedMemoryForSolr.replace(" GB", "")))
					/ Double.parseDouble(totalSystemMemory.replace(" GB", ""))
			);
		} else {
			return null;
		}
	}

	private static String toHumanReadleNumbers(String totalMemory, String currentUnit) {
		String memoryWithoutUnit = totalMemory;
		memoryWithoutUnit = totalMemory.trim();
		if (memoryWithoutUnit.endsWith(currentUnit)) {
			memoryWithoutUnit = memoryWithoutUnit.replace(currentUnit, "");
		}

		int divisionFactor = 0;
		currentUnit = currentUnit.toLowerCase();
		if (currentUnit.equals("kb") || currentUnit.equals("k")) {
			divisionFactor = 1024 * 1024;
		} else if (currentUnit.equals("mb") || currentUnit.equals("m")) {
			divisionFactor = 1024;
		} else if (currentUnit.equals("gb") || currentUnit.equals("g")) {
			divisionFactor = 1;
		}

		if (divisionFactor > 0) {
			try {
				double totalMemoryInGB = Double.parseDouble(memoryWithoutUnit.trim()) / (divisionFactor);
				totalMemoryInGB = roundToTwoDecimals(totalMemoryInGB);
				return totalMemoryInGB + " GB";
			} catch (Exception e) {
				return totalMemory;
			}
		} else {
			return totalMemory;
		}
	}

	private static double roundToTwoDecimals(double value) {
		return Math.round(value * 100.0) / 100.0;
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
