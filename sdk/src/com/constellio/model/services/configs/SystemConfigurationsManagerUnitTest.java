package com.constellio.model.services.configs;

import static java.util.Arrays.asList;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.PropertiesAlteration;
import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.Delayed;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.configs.SystemConfigurationScript;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.configs.SystemConfigurationsManagerRuntimeException.SystemConfigurationsManagerRuntimeException_UpdateScriptFailed;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.ConstellioTest;

public class SystemConfigurationsManagerUnitTest extends ConstellioTest {
	@Mock MetadataSchemasManager metadataSchemasManager;
	@Mock ModelLayerFactory modelLayerFactory;
	@Mock BatchProcessesManager batchProcessesManager;
	@Mock ConfigManager configManager;
	SystemConfigurationsManager systemConfigurationsManager;
	@Mock SystemConfiguration aSystemConfiguration;
	@Mock BatchProcess aBatchProcess, anotherBatchProcess;
	@Mock SystemConfigurationScript script;
	@Mock CollectionsListManager collectionsListManager;
	@Mock ConstellioModulesManager constellioModulesManager;
	@Mock IOServices ioServices;
	@Mock DataLayerFactory dataLayerFactory;
	@Mock IOServicesFactory ioServicesFactory;
	@Mock ConstellioCacheManager cacheManager;
	@Mock ConstellioCache cache;

	@Before
	public void setUp()
			throws Exception {
		when(modelLayerFactory.getMetadataSchemasManager()).thenReturn(metadataSchemasManager);
		when(modelLayerFactory.getBatchProcessesManager()).thenReturn(batchProcessesManager);
		when(modelLayerFactory.getCollectionsListManager()).thenReturn(collectionsListManager);
		when(modelLayerFactory.getDataLayerFactory()).thenReturn(dataLayerFactory);
		when(dataLayerFactory.getIOServicesFactory()).thenReturn(ioServicesFactory);
		when(ioServicesFactory.newIOServices()).thenReturn(ioServices);
		when(cacheManager.getCache(any(String.class))).thenReturn(cache);

		systemConfigurationsManager = spy(
				new SystemConfigurationsManager(modelLayerFactory, configManager, new Delayed<>(constellioModulesManager), cacheManager));
	}

	@Test
	public void whenSettingAConfigValueThenInOrderCreateBatchProcessesCallScriptsSetValueAndMarkBatchProcessesAsPending()
			throws Exception {

		doReturn(asList("firstCollection", "secondCollection")).when(collectionsListManager).getCollections();
		doReturn(script).when(systemConfigurationsManager).getInstanciatedScriptFor(aSystemConfiguration);
		doReturn(asList(aBatchProcess, anotherBatchProcess)).when(systemConfigurationsManager)
				.startBatchProcessesToReindex(aSystemConfiguration);
		doReturn("currentValue").when(systemConfigurationsManager).getValue(aSystemConfiguration);

		systemConfigurationsManager.setValue(aSystemConfiguration, "theNewValue");

		InOrder inOrder = inOrder(batchProcessesManager, configManager, systemConfigurationsManager,
				script);
		inOrder.verify(script).validate(eq("theNewValue"), any(ValidationErrors.class));
		inOrder.verify(systemConfigurationsManager).startBatchProcessesToReindex(aSystemConfiguration);
		inOrder.verify(script).onValueChanged("currentValue", "theNewValue", modelLayerFactory);
		inOrder.verify(script).onValueChanged("currentValue", "theNewValue", modelLayerFactory, "firstCollection");
		inOrder.verify(script).onValueChanged("currentValue", "theNewValue", modelLayerFactory, "secondCollection");
		inOrder.verify(configManager)
				.updateProperties(eq(SystemConfigurationsManager.CONFIG_FILE_PATH), any(PropertiesAlteration.class));
		inOrder.verify(batchProcessesManager).markAsPending(aBatchProcess);
		inOrder.verify(batchProcessesManager).markAsPending(anotherBatchProcess);
	}

	@Test
	public void givenRuntimeExceptionThrownByScriptWhenSettingAConfigValueThenCancelBatchProcessesAndDoNotSaveConfig()
			throws Exception {

		doReturn(asList("firstCollection", "secondCollection")).when(collectionsListManager).getCollections();
		doReturn(script).when(systemConfigurationsManager).getInstanciatedScriptFor(aSystemConfiguration);
		doReturn(asList(aBatchProcess, anotherBatchProcess)).when(systemConfigurationsManager)
				.startBatchProcessesToReindex(aSystemConfiguration);
		doReturn("currentValue").when(systemConfigurationsManager).getValue(aSystemConfiguration);
		doThrow(RuntimeException.class).when(script).onValueChanged("currentValue", "theNewValue", modelLayerFactory,
				"secondCollection");

		try {
			systemConfigurationsManager.setValue(aSystemConfiguration, "theNewValue");
			fail("SystemConfigurationsManagerRuntimeException_UpdateScriptFailed expected");
		} catch (SystemConfigurationsManagerRuntimeException_UpdateScriptFailed e) {
			//OK
		}

		InOrder inOrder = inOrder(batchProcessesManager, configManager, systemConfigurationsManager,
				script);
		inOrder.verify(script).validate(eq("theNewValue"), any(ValidationErrors.class));
		inOrder.verify(systemConfigurationsManager).startBatchProcessesToReindex(aSystemConfiguration);
		inOrder.verify(script).onValueChanged("currentValue", "theNewValue", modelLayerFactory);
		inOrder.verify(script).onValueChanged("currentValue", "theNewValue", modelLayerFactory, "firstCollection");
		inOrder.verify(script).onValueChanged("currentValue", "theNewValue", modelLayerFactory, "secondCollection");
		inOrder.verify(script).onValueChanged("theNewValue", "currentValue", modelLayerFactory);
		inOrder.verify(script).onValueChanged("theNewValue", "currentValue", modelLayerFactory, "firstCollection");
		inOrder.verify(script).onValueChanged("theNewValue", "currentValue", modelLayerFactory, "secondCollection");
		inOrder.verify(configManager, never())
				.updateProperties(anyString(), any(PropertiesAlteration.class));
		inOrder.verify(batchProcessesManager).cancelStandByBatchProcess(aBatchProcess);
		inOrder.verify(batchProcessesManager).cancelStandByBatchProcess(anotherBatchProcess);
	}
}
