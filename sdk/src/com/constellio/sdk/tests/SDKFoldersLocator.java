package com.constellio.sdk.tests;

import java.io.File;

import com.constellio.model.conf.FoldersLocator;

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

	File getConstellioProject() {
		return getConstellioWebinfFolder().getParentFile();
	}

	public File getSDKProperties() {
		return new File(getSDKProject(), "sdk.properties");
	}
}
