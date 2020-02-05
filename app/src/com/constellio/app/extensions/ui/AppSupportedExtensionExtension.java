package com.constellio.app.extensions.ui;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.components.viewers.pdftron.PdfTronViewer;
import com.constellio.data.extensions.extensions.configManager.SupportedExtensionExtension;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class AppSupportedExtensionExtension extends SupportedExtensionExtension {
	private AppLayerFactory appLayerFactory;

	public AppSupportedExtensionExtension(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	public List<String> getExtentionDisabledForPreviewConvertion() {
		List<String> extentionToExclude = new ArrayList<>();
		String licenseForPdftron = PdfTronViewer.getPdfTronKey(appLayerFactory);

		SystemConfigurationsManager systemConfigurationsManager = appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager();

		boolean isThumnailGenerationIsActivated = systemConfigurationsManager.getValue(ConstellioEIMConfigs.ENABLE_THUMBNAIL_GENERATION);

		if (!isThumnailGenerationIsActivated && (StringUtils.isNotBlank(licenseForPdftron) || Toggle.ENABLE_PDFTRON_TRIAL.isEnabled())) {
			extentionToExclude.addAll(asList(PdfTronViewer.SUPPORTED_EXTENTION));
		}

		return extentionToExclude;
	}
}
