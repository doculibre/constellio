package com.constellio.app.conf;

import java.io.File;
import java.util.Map;

import com.constellio.data.conf.PropertiesConfiguration;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.conf.ModelLayerConfiguration;

public class PropertiesAppLayerConfiguration extends PropertiesConfiguration implements AppLayerConfiguration {

	private FoldersLocator foldersLocator;

	private final ModelLayerConfiguration modelLayerConfiguration;

	public PropertiesAppLayerConfiguration(Map<String, String> configs, ModelLayerConfiguration modelLayerConfiguration,
			FoldersLocator foldersLocator, File constellioProperties) {
		super(configs, constellioProperties);
		this.modelLayerConfiguration = modelLayerConfiguration;
		this.foldersLocator = foldersLocator;
	}

	@Override
	public void validate() {

	}

	@Override
	public File getTempFolder() {
		return modelLayerConfiguration.getTempFolder();
	}

	@Override
	public File getPluginsFolder() {
		return new FoldersLocator().getPluginsJarsFolder();
	}

	@Override
	public File getPluginsManagementOnStartupFile() {
		return new FoldersLocator().getPluginsToMoveOnStartupFile();
	}

	@Override
	public File getSetupProperties() {
		return foldersLocator.getConstellioSetupProperties();
	}

}
