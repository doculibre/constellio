package com.constellio.data.io;

import com.constellio.app.ui.framework.components.viewers.pdftron.PdfTronViewer;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConversionManagerAcceptanceTest extends ConstellioTest {

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule());
	}

	@Test
	public void givenPdfTronEnabledThenSomeExtAreDisabledForPreview() {
		SystemConfigurationsManager systemConfigurationsManager = getAppLayerFactory().getModelLayerFactory().getSystemConfigurationsManager();
		systemConfigurationsManager.setValue(ConstellioEIMConfigs.PDFTRON_LICENSE, "licence");
		systemConfigurationsManager.setValue(ConstellioEIMConfigs.ENABLE_THUMBNAIL_GENERATION, false);

		assertThat(ConversionManager.getAllSupportedExtensions()).contains(getSupportedExtentionMinusExtentionThatAreNotConverted());
		assertThat(ConversionManager.getSupportedExtensions()).doesNotContain(getSupportedExtentionMinusExtentionThatAreNotConverted());

		systemConfigurationsManager.setValue(ConstellioEIMConfigs.PDFTRON_LICENSE, null);

		assertThat(ConversionManager.getAllSupportedExtensions()).contains(getSupportedExtentionMinusExtentionThatAreNotConverted());
		assertThat(ConversionManager.getSupportedExtensions()).contains(getSupportedExtentionMinusExtentionThatAreNotConverted());

		systemConfigurationsManager.setValue(ConstellioEIMConfigs.PDFTRON_LICENSE, "licence");
		systemConfigurationsManager.setValue(ConstellioEIMConfigs.ENABLE_THUMBNAIL_GENERATION, true);

		assertThat(ConversionManager.getAllSupportedExtensions()).contains(getSupportedExtentionMinusExtentionThatAreNotConverted());
		assertThat(ConversionManager.getSupportedExtensions()).contains(getSupportedExtentionMinusExtentionThatAreNotConverted());

		systemConfigurationsManager.setValue(ConstellioEIMConfigs.PDFTRON_LICENSE, null);
		Toggle.ENABLE_PDFTRON_TRIAL.enable();
		systemConfigurationsManager.setValue(ConstellioEIMConfigs.ENABLE_THUMBNAIL_GENERATION, false);

		assertThat(ConversionManager.getAllSupportedExtensions()).contains(getSupportedExtentionMinusExtentionThatAreNotConverted());
		assertThat(ConversionManager.getSupportedExtensions()).doesNotContain(getSupportedExtentionMinusExtentionThatAreNotConverted());

	}

	private String[] getSupportedExtentionMinusExtentionThatAreNotConverted() {
		return ArrayUtils.removeElements(PdfTronViewer.SUPPORTED_EXTENTION, new String[]{"pdf", "pdf/a", "xfdf", "fdf"});
	}
}
