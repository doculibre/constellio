/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.services.factories;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.conf.AppLayerConfiguration;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateManager;
import com.constellio.app.services.extensions.ConstellioModulesManagerImpl;
import com.constellio.app.services.extensions.ConstellioPluginManager;
import com.constellio.data.dao.managers.StatefullServiceDecorator;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.sdk.tests.ConstellioTest;

public class AppLayerFactoryRealTest extends ConstellioTest {

	StatefullServiceDecorator statefullServiceDecorator = new StatefullServiceDecorator();

	@Mock AppLayerConfiguration appLayerConfiguration;

	ModelLayerFactory modelLayerFactory;
	DataLayerFactory dataLayerFactory;

	AppLayerFactory factory;

	@Before
	public void setUp()
			throws Exception {

		modelLayerFactory = getModelLayerFactory();
		dataLayerFactory = getDataLayerFactory();

		factory = new AppLayerFactory(appLayerConfiguration, modelLayerFactory, dataLayerFactory, statefullServiceDecorator);
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
