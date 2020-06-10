package com.constellio.sdk.tests;

import com.constellio.model.conf.FoldersLocator;

import java.io.File;

public class SDKFoldersLocator extends FoldersLocator {

	public SDKFoldersLocator() {
		super();
	}

	public File getInitialStatesFolder() {
		return new File(getSDKProject(), "initialStates");
	}

	public File getPluginsJarsFolder() {
		return new File(getSDKProject(), "pluginsJars");
	}

	public File getConstellioProject() {
		return getConstellioWebinfFolder().getParentFile();
	}

	public File getConstellioPluginsProject() {
		return new File(getConstellioWebinfFolder().getParentFile().getParentFile(), "constellio-plugins");
	}

	public File getSDKProperties() {
		return new File(getSDKProject(), concatTenantFolder("sdk.properties"));
	}

	@Override
	protected String concatTenantFolder(String folder) {
		String tenantFolder = getTenantFolder();
		return !tenantFolder.isEmpty() ?
			   "tenants".concat(File.separator).concat(getTenantFolder()).concat(File.separator).concat(folder) :
			   folder;
	}

}
