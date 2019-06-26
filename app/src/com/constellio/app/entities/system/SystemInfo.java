package com.constellio.app.entities.system;

import com.constellio.app.services.appManagement.AppManagementService.LicenseInfo;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.systemInformations.SystemInformationsService;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.conf.FoldersLocatorMode;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class SystemInfo {

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

	boolean isPrivateRepositoryInstalled;

	public static SystemInfo build(AppLayerFactory appLayerFactory) {
		SystemInfo systemInfo = new SystemInfo();
		systemInfo.init(appLayerFactory);
		return systemInfo;
	}

	private void init(AppLayerFactory appLayerFactory) {
		//TODO merge SystemInformationsService with SystemAnalysisUtils
		SystemInformationsService systemInformationsService = new SystemInformationsService();
		systemMemory = SystemMemory.fetchSystemMemoryInfo();
		licenseInfo = fetchLicenseInfo(appLayerFactory);
		constellioVersion = fetchConstellioVersion(appLayerFactory);

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
}
