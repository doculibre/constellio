package com.constellio.app.entities.system;

import com.constellio.app.services.appManagement.AppManagementService.LicenseInfo;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.systemInformations.SystemInformationsService;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.conf.FoldersLocatorMode;
import com.constellio.model.frameworks.validation.ValidationErrors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class SystemInfo {

	private static final String MISSING_INFORMATION_ON_MEMORY_CONFIGURATION = "missingInformationOnMemoryConfiguration";
	private static final String INVALID_LICENSE = "invalidLicense";
	private static final String LICENSE_EXPIRED = "licenseExpired";
	private static final String OPT_DISK_USAGE = "optDiskUsage";
	private static final String OPT_DISK_USAGE_MISSING_INFO = "optDiskUsageMissingInfo";
	private static final String SOLR_DISK_USAGE = "solrDiskUsage";
	private static final String SOLR_DISK_USAGE_MISSING_INFO = "solrDiskUsageMissingInfo";
	private static final String CONSTELLIO_MEMORY_CONSUMPTION = "memoryConsumption";
	private static final String PRIVATE_REPOSITORY_IS_NOT_INSTALLED = "privateRepositoryIsNotInstalled";
	private static final String LOW_UNALLOCATED_MEMORY = "lowUnallocatedMemory";

	private static SystemInfo instance;

	LocalDateTime lastTimeUpdated;

	SystemMemory systemMemory;
	LicenseInfo licenseInfo;

	String constellioVersion;
	String kernelVersion;
	String wrapperJavaVersion;
	String linuxJavaVersion;
	String solrVersion;
	String userRunningSolr;
	String userRunningConstellio;
	String optDiskUsage;
	String solrDiskUsage;
	ValidationErrors validationErrors;

	boolean isPrivateRepositoryInstalled;

	private SystemInfo() {
		recalculate();
	}

	public static SystemInfo getInstance() {
		if(instance == null) {
			instance = new SystemInfo();
		}
		return instance;
	}

	synchronized public void recalculate() {
		//TODO merge SystemInformationsService with SystemAnalysisUtils
		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		SystemInformationsService systemInformationsService = new SystemInformationsService();
		this.lastTimeUpdated = TimeProvider.getLocalDateTime();
		systemMemory = SystemMemory.fetchSystemMemoryInfo();
		licenseInfo = fetchLicenseInfo(appLayerFactory);
		constellioVersion = fetchConstellioVersion(appLayerFactory);
		validationErrors = new ValidationErrors();

		FoldersLocator locator = new FoldersLocator();

		if (locator.getFoldersLocatorMode() == FoldersLocatorMode.WRAPPER) {
			kernelVersion = systemInformationsService.getLinuxVersion();
			isPrivateRepositoryInstalled = systemInformationsService.isPrivateRepositoryInstalled();
			wrapperJavaVersion = systemInformationsService.getWrapperJavaVersion();
			linuxJavaVersion = systemInformationsService.getLinuxVersion();
			solrVersion = systemInformationsService.getSolrVersion();
			userRunningSolr = systemInformationsService.getSolrUser();
			userRunningConstellio = systemInformationsService.getConstellioUser();
			optDiskUsage = systemInformationsService.getDiskUsage("/opt");
			solrDiskUsage = systemInformationsService.getDiskUsage("/var/solr");
		}

		analyzeSystemAndFindValidationErrors();
	}

	private static String fetchConstellioVersion(AppLayerFactory appLayerFactory) {
		String version = appLayerFactory.newApplicationService().getWarVersion();
		if (version == null || version.equals("5.0.0")) {
			File versionFile = new File(new FoldersLocator().getConstellioProject(), "version");
			if (versionFile.exists()) {
				try {
					version = FileUtils.readFileToString(versionFile);
				} catch (IOException e) {
					version = "error when reading version file";
				}
			} else {
				version = "no version file";
			}
		}
		return version;
	}

	private static LicenseInfo fetchLicenseInfo(AppLayerFactory appLayerFactory) {
		return appLayerFactory.newApplicationService().getLicenseInfo();
	}

	public SystemMemory getSystemMemory() {
		return systemMemory;
	}

	public LicenseInfo getLicenseInfo() {
		return licenseInfo;
	}

	public String getConstellioVersion() {
		return constellioVersion;
	}

	public String getKernelVersion() {
		return kernelVersion;
	}

	public String getWrapperJavaVersion() {
		return wrapperJavaVersion;
	}

	public String getLinuxJavaVersion() {
		return linuxJavaVersion;
	}

	public String getSolrVersion() {
		return solrVersion;
	}

	public String getUserRunningSolr() {
		return userRunningSolr;
	}

	public String getUserRunningConstellio() {
		return userRunningConstellio;
	}

	public String getOptDiskUsage() {
		return optDiskUsage;
	}

	public String getSolrDiskUsage() {
		return solrDiskUsage;
	}

	public boolean isPrivateRepositoryInstalled() {
		return isPrivateRepositoryInstalled;
	}

	public LocalDateTime getLastTimeUpdated() {
		return lastTimeUpdated;
	}

	public ValidationErrors getValidationErrors() {
		return validationErrors;
	}

	private void analyzeSystemAndFindValidationErrors() {
		validationErrors.clearAll();

//		if(systemMemory.getConstellioAllocatedMemory().getAmount() == null || systemMemory.getSolrAllocatedMemory().getAmount() == null) {
//			HashMap<String, Object> parameters = new HashMap<>();
//			validationErrors.addWarning(SystemInfo.class, MISSING_INFORMATION_ON_MEMORY_CONFIGURATION, parameters);
//		} else {
//			HashMap<String, Object> unallocatedMemoryParameters = new HashMap<>();
//			unallocatedMemoryParameters.put("unallocatedMemory", systemMemory.getUnallocatedMemory().toNumberOfGigaBytes() + " GB");
//			if(systemMemory.getUnallocatedMemory().isLessThan(new MemoryDetails(2d, MemoryUnit.GB))) {
//				validationErrors.addWarning(SystemInfo.class, LOW_UNALLOCATED_MEMORY, unallocatedMemoryParameters);
//			} else {
//				validationErrors.addLog(SystemInfo.class, LOW_UNALLOCATED_MEMORY, unallocatedMemoryParameters);
//			}
//		}
		HashMap<String, Object> objectObjectHashMap = new HashMap<>();
		objectObjectHashMap.put("unallocatedMemory", "0.5 GB");
		validationErrors.add(SystemInfo.class, LOW_UNALLOCATED_MEMORY, objectObjectHashMap);

		if(licenseInfo == null || licenseInfo.getExpirationDate() == null) {
			validationErrors.addWarning(SystemInfo.class, INVALID_LICENSE);
		} else if(TimeProvider.getLocalDate().isAfter(licenseInfo.getExpirationDate())) {
			HashMap<String, Object> parameters = new HashMap<>();
			parameters.put("expirationDate", licenseInfo.getExpirationDate().toString("yyyy-MM-dd"));
			validationErrors.addWarning(SystemInfo.class, LICENSE_EXPIRED, parameters);
		}

		if(StringUtils.isNotBlank(optDiskUsage) && optDiskUsage.endsWith("%")) {
			try {
				int consumptionPercentage = Integer.parseInt(optDiskUsage.replace("%", ""));
				if(isInRange(consumptionPercentage, 0, 75)) {
					HashMap<String, Object> parameters = new HashMap<>();
					validationErrors.addLog(SystemInfo.class, OPT_DISK_USAGE, parameters);
				} else if(isInRange(consumptionPercentage, 75, 90)) {
					HashMap<String, Object> parameters = new HashMap<>();
					validationErrors.addWarning(SystemInfo.class, OPT_DISK_USAGE, parameters);
				} else {
					HashMap<String, Object> parameters = new HashMap<>();
					validationErrors.add(SystemInfo.class, OPT_DISK_USAGE, parameters);
				}
			} catch (Exception e) {
				HashMap<String, Object> parameters = new HashMap<>();
				validationErrors.addWarning(SystemInfo.class, OPT_DISK_USAGE_MISSING_INFO, parameters);
			}
		} else {
			HashMap<String, Object> parameters = new HashMap<>();
			validationErrors.addWarning(SystemInfo.class, OPT_DISK_USAGE_MISSING_INFO, parameters);
		}

		if(StringUtils.isNotBlank(solrDiskUsage) && solrDiskUsage.endsWith("%")) {
			try {
				int consumptionPercentage = Integer.parseInt(solrDiskUsage.replace("%", ""));
				if(isInRange(consumptionPercentage, 0, 75)) {
					HashMap<String, Object> parameters = new HashMap<>();
					validationErrors.addLog(SystemInfo.class, SOLR_DISK_USAGE, parameters);
				} else if(isInRange(consumptionPercentage, 75, 90)) {
					HashMap<String, Object> parameters = new HashMap<>();
					validationErrors.addWarning(SystemInfo.class, SOLR_DISK_USAGE, parameters);
				} else {
					HashMap<String, Object> parameters = new HashMap<>();
					validationErrors.add(SystemInfo.class, SOLR_DISK_USAGE, parameters);
				}
			} catch (Exception e) {
				HashMap<String, Object> parameters = new HashMap<>();
				validationErrors.addWarning(SystemInfo.class, SOLR_DISK_USAGE_MISSING_INFO, parameters);
			}
		} else {
			HashMap<String, Object> parameters = new HashMap<>();
			validationErrors.addWarning(SystemInfo.class, SOLR_DISK_USAGE_MISSING_INFO, parameters);
		}

		int currentConstellioMemoryConsumption = (int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) * 100d / Runtime.getRuntime().totalMemory());
		HashMap<String, Object> constellioMemoryConsumptionParameters = new HashMap<>();
		constellioMemoryConsumptionParameters.put("consumptionPercentage", currentConstellioMemoryConsumption + "%");
		if(isInRange(currentConstellioMemoryConsumption, 0, 75)) {
			validationErrors.addLog(SystemInfo.class, CONSTELLIO_MEMORY_CONSUMPTION, constellioMemoryConsumptionParameters);
		} else if(isInRange(currentConstellioMemoryConsumption, 75, 90)) {
			validationErrors.addWarning(SystemInfo.class, CONSTELLIO_MEMORY_CONSUMPTION, constellioMemoryConsumptionParameters);
		} else {
			validationErrors.add(SystemInfo.class, CONSTELLIO_MEMORY_CONSUMPTION, constellioMemoryConsumptionParameters);
		}

		//		String currentSolrMemoryConsumption = "60%";
		//		if(StringUtils.isNotBlank(currentSolrMemoryConsumption) && currentSolrMemoryConsumption.endsWith("%")) {
//			try {
//				int consumptionPercentage = Integer.parseInt(currentSolrMemoryConsumption.replace("%", ""));
//				if(isInRange(consumptionPercentage, 0, 75)) {
//					systemState = SystemState.getHighestUrgency(systemState, SystemState.OK);
//				} else if(isInRange(consumptionPercentage, 75, 90)) {
//					systemState = SystemState.getHighestUrgency(systemState, SystemState.WARNING);
//				} else {
//					systemState = SystemState.getHighestUrgency(systemState, SystemState.CRITIC);
//				}
//			} catch (Exception e) {
//				systemState = SystemState.getHighestUrgency(systemState, SystemState.WARNING);
//			}
//		}

		if(!isPrivateRepositoryInstalled) {
			validationErrors.addWarning(SystemInfo.class, PRIVATE_REPOSITORY_IS_NOT_INSTALLED);
		}
	}

	private boolean isInRange(int percentage, int includedLowestPercentage, int excludedHighestPercentage) {
		return includedLowestPercentage <= percentage && excludedHighestPercentage > percentage;
	}
}
