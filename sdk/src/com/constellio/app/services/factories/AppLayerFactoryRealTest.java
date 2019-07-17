package com.constellio.app.services.factories;

import com.constellio.app.conf.AppLayerConfiguration;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateManager;
import com.constellio.app.services.extensions.ConstellioModulesManagerImpl;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManager;
import com.constellio.app.services.migrations.MigrationServices;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.StatefullServiceDecorator;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AppLayerFactoryRealTest extends ConstellioTest {

	StatefullServiceDecorator statefullServiceDecorator = new StatefullServiceDecorator();

	@Mock MigrationServices migrationServices;
	@Mock AppLayerConfiguration appLayerConfiguration;

	ModelLayerFactory modelLayerFactory;
	DataLayerFactory dataLayerFactory;

	AppLayerFactory factory;

	@Before
	public void setUp()
			throws Exception {

		modelLayerFactory = getModelLayerFactory();
		dataLayerFactory = getDataLayerFactory();

		factory = spy(
				new AppLayerFactoryImpl(appLayerConfiguration, modelLayerFactory, dataLayerFactory, statefullServiceDecorator,
						null, (short) 0) {
					@Override
					public MigrationServices newMigrationServices() {
						return migrationServices;
					}
				});
	}

	@Test
	public void whenRegisterManagerInitializeOnce()
			throws Exception {

		StatefulService statefulService = Mockito.mock(StatefulService.class);

		AppLayerFactory appLayerFactory = getAppLayerFactory();

		appLayerFactory.registerManager("zeCollection", "myModule", "moduleId", statefulService);

		verify(statefulService, times(1)).initialize();
	}

	@Test
	public void whenRegisterSystemManagerInitializeOnce()
			throws Exception {

		StatefulService statefulService = Mockito.mock(StatefulService.class);

		AppLayerFactory appLayerFactory = getAppLayerFactory();

		appLayerFactory.registerSystemWideManager("myModule", "moduleId", statefulService);

		verify(statefulService, times(1)).initialize();
	}

	@Test
	public void whenGetPluginManagerThenAlwaysSameInstance()
			throws Exception {

		ConstellioPluginManager manager1 = factory.getPluginManager();
		ConstellioPluginManager manager2 = factory.getPluginManager();

		assertThat(manager1).isNotNull().isSameAs(manager2);

	}

	@Test
	public void whenCollectionsServicesThenNotNull()
			throws Exception {

		assertThat(factory.getCollectionsManager()).isNotNull();
	}

	@Test
	public void whenNewMigrationServicesThenNotNull() {
		assertThat(factory.newMigrationServices()).isNotNull();
	}

	@Test
	public void whenGetModulesManagerThenSameInstance()
			throws Exception {

		ConstellioModulesManagerImpl modulesManager1 = (ConstellioModulesManagerImpl) factory.getModulesManager();
		ConstellioModulesManagerImpl modulesManager2 = (ConstellioModulesManagerImpl) factory.getModulesManager();

		assertThat(modulesManager1).isNotNull().isSameAs(modulesManager2);

	}

	@Test
	public void whenGetLabelTemplateManagerThenAlwaysSameInstance()
			throws Exception {

		LabelTemplateManager manager1 = factory.getLabelTemplateManager();
		LabelTemplateManager manager2 = factory.getLabelTemplateManager();

		assertThat(manager1).isNotNull().isSameAs(manager2);

	}

}
