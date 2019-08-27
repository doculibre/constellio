package com.constellio.app.entities.system;

import com.constellio.app.entities.system.SystemMemory.MemoryDetails;
import com.constellio.app.entities.system.SystemMemory.MemoryUnit;
import com.constellio.app.services.appManagement.AppManagementService.LicenseInfo;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.systemInformations.SystemInformationsService;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.conf.FoldersLocatorMode;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SystemInfo {

	private static final String MISSING_INFORMATION_ON_CONSTELLIO_MEMORY_CONFIGURATION = "missingInformationOnConstellioMemoryConfiguration";
	private static final String MISSING_INFORMATION_ON_SOLR_MEMORY_CONFIGURATION = "missingInformationOnSolrMemoryConfiguration";
	private static final String MISSING_INFORMATION_ON_TOTAL_SERVER_MEMORY = "missingInformationOnTotalServerMemory";
	private static final String CONSTELLIO_ALLOCATED_MEMORY = "constellioAllocatedMemory";
	private static final String SOLR_ALLOCATED_MEMORY = "solrAllocatedMemory";
	private static final String TOTAL_SERVER_MEMORY = "totalServerMemory";
	private static final String UNALLOCATED_MEMORY = "unallocatedMemory";
	private static final String INVALID_LICENSE = "invalidLicense";
	private static final String LICENSE_EXPIRED = "licenseExpired";
	private static final String VALID_LICENSE = "validLicense";
	private static final String OPT_DISK_USAGE = "optDiskUsage";
	private static final String OPT_DISK_USAGE_MISSING_INFO = "optDiskUsageMissingInfo";
	private static final String SOLR_DISK_USAGE = "solrDiskUsage";
	private static final String SOLR_DISK_USAGE_MISSING_INFO = "solrDiskUsageMissingInfo";
	private static final String CONSTELLIO_MEMORY_CONSUMPTION_HIGH = "constellioMemoryConsumptionHigh";
	private static final String CONSTELLIO_MEMORY_CONSUMPTION_LOW = "constellioMemoryConsumptionLow";
	private static final String PRIVATE_REPOSITORY_IS_NOT_INSTALLED = "privateRepositoryIsNotInstalled";

	private static SystemInfo instance;

	LocalDateTime lastTimeUpdated;

	SystemMemory systemMemory;
	List<MemoryDetails> constellioFreeMemory = new ArrayList<>();
	List<MemoryDetails> solrFreeMemory = new ArrayList<>();

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
		if (instance == null) {
			instance = new SystemInfo();
		}
		return instance;
	}

	synchronized public void recalculate() {
		//TODO merge SystemInformationsService with SystemAnalysisUtils
		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		ConstellioEIMConfigs configs = new ConstellioEIMConfigs(appLayerFactory.getModelLayerFactory());
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

		analyzeSystemAndFindValidationErrors(configs);
	}

	synchronized public void appendConstellioFreeMemory() {
		constellioFreeMemory.add(new MemoryDetails((double) Runtime.getRuntime().freeMemory(), MemoryUnit.B));
		while (constellioFreeMemory.size() > 5) {
			constellioFreeMemory.remove(0);
		}
	}

	synchronized public void appendSolrFreeMemory() {
		//		constellioFreeMemory.add(new MemoryDetails((double) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) * 100 / Runtime.getRuntime().totalMemory()), MemoryUnit.B));
		//		while (constellioFreeMemory.size() > 5) {
		//			constellioFreeMemory.remove(0);
		//		}
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

	private void analyzeSystemAndFindValidationErrors(
			ConstellioEIMConfigs configs) {
		validationErrors.clearAll();

		//		FOR TEST PURPOSE
		//		HashMap<String, Object> objectObjectHashMap = new HashMap<>();
		//		objectObjectHashMap.put("memory", "0.5 GB");
		//		validationErrors.add(SystemInfo.class, UNALLOCATED_MEMORY, objectObjectHashMap);

		if (configs.isSystemStateLicenseValidationEnabled()) {
			validateLicense();
		}

		if (configs.isSystemStateMemoryAllocationValidationEnabled()) {
			validateMemoryAllocation();
		}

		validateDiskUsage(configs);


		//		validateMemoryConsumption();
		//		validateRepository();
	}

	private void validateMemoryAllocation() {
		String parameterKey = "memory";
		HashMap<String, Object> constellioMemoryParameters = buildSingleValueParameters(parameterKey, systemMemory.getConstellioAllocatedMemory().toString(MemoryUnit.GB));
		HashMap<String, Object> solrMemoryParameters = buildSingleValueParameters(parameterKey, systemMemory.getSolrAllocatedMemory().toString(MemoryUnit.GB));
		HashMap<String, Object> totalServerMemoryParameters = buildSingleValueParameters(parameterKey, systemMemory.getTotalSystemMemory().toString(MemoryUnit.GB));
		HashMap<String, Object> unallocatedMemoryParameters = buildSingleValueParameters(parameterKey, systemMemory.getUnallocatedMemory().toString(MemoryUnit.GB));

		if (systemMemory.getConstellioAllocatedMemory().getAmount() != null && systemMemory.getSolrAllocatedMemory().getAmount() != null && systemMemory.getTotalSystemMemory().getAmount() != null) {

			double constellioMemoryPercentage = systemMemory.getConstellioAllocatedMemory().toNumberOfBytes() * 100 / systemMemory.getTotalSystemMemory().toNumberOfBytes();
			double solrMemoryPercentage = systemMemory.getSolrAllocatedMemory().toNumberOfBytes() * 100 / systemMemory.getTotalSystemMemory().toNumberOfBytes();
			double unallocatedMemoryPercentage = systemMemory.getUnallocatedMemory().toNumberOfBytes() * 100 / systemMemory.getTotalSystemMemory().toNumberOfBytes();

			validationErrors.addLog(SystemInfo.class, TOTAL_SERVER_MEMORY, totalServerMemoryParameters);

			if (constellioMemoryPercentage <= 20) {
				validationErrors.add(SystemInfo.class, CONSTELLIO_ALLOCATED_MEMORY, constellioMemoryParameters);
			} else if (constellioMemoryPercentage < 40) {
				validationErrors.addWarning(SystemInfo.class, CONSTELLIO_ALLOCATED_MEMORY, constellioMemoryParameters);
			} else {
				validationErrors.addLog(SystemInfo.class, CONSTELLIO_ALLOCATED_MEMORY, constellioMemoryParameters);
			}

			if (solrMemoryPercentage <= 20) {
				validationErrors.add(SystemInfo.class, SOLR_ALLOCATED_MEMORY, solrMemoryParameters);
			} else if (solrMemoryPercentage < 40) {
				validationErrors.addWarning(SystemInfo.class, SOLR_ALLOCATED_MEMORY, solrMemoryParameters);
			} else {
				validationErrors.addLog(SystemInfo.class, SOLR_ALLOCATED_MEMORY, solrMemoryParameters);
			}

			if (unallocatedMemoryPercentage < 20 && systemMemory.getUnallocatedMemory().isLessThan(new MemoryDetails(2d, MemoryUnit.GB))) {
				validationErrors.addWarning(SystemInfo.class, UNALLOCATED_MEMORY, unallocatedMemoryParameters);
			} else {
				validationErrors.addLog(SystemInfo.class, UNALLOCATED_MEMORY, unallocatedMemoryParameters);
			}
		} else {
			if (systemMemory.getConstellioAllocatedMemory().getAmount() == null) {
				validationErrors.addWarning(SystemInfo.class, MISSING_INFORMATION_ON_CONSTELLIO_MEMORY_CONFIGURATION, constellioMemoryParameters);
			} else {
				validationErrors.addLog(SystemInfo.class, CONSTELLIO_ALLOCATED_MEMORY, constellioMemoryParameters);
			}

			if (systemMemory.getSolrAllocatedMemory().getAmount() == null) {
				validationErrors.addWarning(SystemInfo.class, MISSING_INFORMATION_ON_SOLR_MEMORY_CONFIGURATION, solrMemoryParameters);
			} else {
				validationErrors.addLog(SystemInfo.class, SOLR_ALLOCATED_MEMORY, solrMemoryParameters);
			}

			if (systemMemory.getTotalSystemMemory().getAmount() == null) {
				validationErrors.addWarning(SystemInfo.class, MISSING_INFORMATION_ON_TOTAL_SERVER_MEMORY, totalServerMemoryParameters);
			} else {
				validationErrors.addLog(SystemInfo.class, TOTAL_SERVER_MEMORY, totalServerMemoryParameters);
			}
		}
	}

	private void validateMemoryConsumption() {
		if (constellioFreeMemory.size() >= 5) {
			boolean hadEnoughMemoryRemaining = false;
			for (MemoryDetails freeMemory : constellioFreeMemory) {
				if (freeMemory.isGreaterThan(new MemoryDetails(1d, MemoryUnit.GB))) {
					hadEnoughMemoryRemaining = true;
					break;
				}
			}

			if (hadEnoughMemoryRemaining) {
				validationErrors.addLog(SystemInfo.class, CONSTELLIO_MEMORY_CONSUMPTION_LOW, new HashMap<String, Object>());
			} else {
				validationErrors.addWarning(SystemInfo.class, CONSTELLIO_MEMORY_CONSUMPTION_HIGH, new HashMap<String, Object>());
			}
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
	}

	private void validateLicense() {
		if (licenseInfo == null || licenseInfo.getExpirationDate() == null) {
			validationErrors.addWarning(SystemInfo.class, INVALID_LICENSE);
		} else if (TimeProvider.getLocalDate().isAfter(licenseInfo.getExpirationDate())) {
			validationErrors.addWarning(SystemInfo.class, LICENSE_EXPIRED, buildSingleValueParameters("expirationDate", licenseInfo.getExpirationDate().toString("yyyy-MM-dd")));
		} else {
			validationErrors.addLog(SystemInfo.class, VALID_LICENSE, buildSingleValueParameters("expirationDate", licenseInfo.getExpirationDate().toString("yyyy-MM-dd")));
		}
	}

	private void validateDiskUsage(ConstellioEIMConfigs configs) {
		String parameterKey = "consumptionPercentage";

		if (configs.isSystemStateOptDiskUsageValidationEnabled()) {
			if (StringUtils.isNotBlank(optDiskUsage) && optDiskUsage.endsWith("%")) {
				try {
					int consumptionPercentage = Integer.parseInt(optDiskUsage.replace("%", ""));
					HashMap<String, Object> parameters = buildSingleValueParameters(parameterKey, consumptionPercentage + "%");
					if (isInRange(consumptionPercentage, 0, 75)) {
						validationErrors.addLog(SystemInfo.class, OPT_DISK_USAGE, parameters);
					} else if (isInRange(consumptionPercentage, 75, 90)) {
						validationErrors.addWarning(SystemInfo.class, OPT_DISK_USAGE, parameters);
					} else {
						validationErrors.add(SystemInfo.class, OPT_DISK_USAGE, parameters);
					}
				} catch (Exception e) {
					validationErrors.addWarning(SystemInfo.class, OPT_DISK_USAGE_MISSING_INFO, new HashMap<String, Object>());
				}
			} else {
				validationErrors.addWarning(SystemInfo.class, OPT_DISK_USAGE_MISSING_INFO, new HashMap<String, Object>());
			}
		}

		if (configs.isSystemStateSolrDiskUsageValidationEnabled()) {
			if (StringUtils.isNotBlank(solrDiskUsage) && solrDiskUsage.endsWith("%")) {
				try {
					int consumptionPercentage = Integer.parseInt(solrDiskUsage.replace("%", ""));
					HashMap<String, Object> parameters = buildSingleValueParameters(parameterKey, consumptionPercentage + "%");
					if (isInRange(consumptionPercentage, 0, 75)) {
						validationErrors.addLog(SystemInfo.class, SOLR_DISK_USAGE, parameters);
					} else if (isInRange(consumptionPercentage, 75, 90)) {
						validationErrors.addWarning(SystemInfo.class, SOLR_DISK_USAGE, parameters);
					} else {
						validationErrors.add(SystemInfo.class, SOLR_DISK_USAGE, parameters);
					}
				} catch (Exception e) {
					validationErrors.addWarning(SystemInfo.class, SOLR_DISK_USAGE_MISSING_INFO, new HashMap<String, Object>());
				}
			} else {
				validationErrors.addWarning(SystemInfo.class, SOLR_DISK_USAGE_MISSING_INFO, new HashMap<String, Object>());
			}
		}
	}

	private void validateRepository() {
		if (!isPrivateRepositoryInstalled) {
			validationErrors.addWarning(SystemInfo.class, PRIVATE_REPOSITORY_IS_NOT_INSTALLED);
		}
	}

	private boolean isInRange(int percentage, int includedLowestPercentage, int excludedHighestPercentage) {
		return includedLowestPercentage <= percentage && excludedHighestPercentage > percentage;
	}

	private HashMap<String, Object> buildSingleValueParameters(String key, Object value) {
		HashMap<String, Object> parameters = new HashMap<>();
		parameters.put(key, value);
		return parameters;
	}
}
