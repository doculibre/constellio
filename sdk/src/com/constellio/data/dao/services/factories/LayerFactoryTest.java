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
package com.constellio.data.dao.services.factories;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.StatefullServiceDecorator;
import com.constellio.sdk.tests.ConstellioTest;

public class LayerFactoryTest extends ConstellioTest {

	StatefullServiceDecorator statefullServiceDecorator = new StatefullServiceDecorator();

	LayerFactory layerFactory;

	LayerFactory bottomLayerFactory;

	@Mock StatefulService layerService1, layerService2, layerService3, bottomLayerService;

	@Before
	public void setUp()
			throws Exception {

		bottomLayerFactory = new LayerFactory(statefullServiceDecorator);
		layerFactory = new LayerFactory(bottomLayerFactory, statefullServiceDecorator);

		bottomLayerFactory.add(bottomLayerService);
		layerFactory.add(layerService1);
		layerFactory.add(layerService2);
		layerFactory.add(layerService3);
	}

	@Test
	public void whenInitializeServicesThenStartByTheBottomLayerThenInOrderOfAdd()
			throws Exception {

		layerFactory.initialize();

		InOrder inOrder = Mockito.inOrder(layerService1, layerService2, layerService3, bottomLayerService);
		inOrder.verify(bottomLayerService).initialize();
		inOrder.verify(layerService1).initialize();
		inOrder.verify(layerService2).initialize();
		inOrder.verify(layerService3).initialize();
	}

	@Test
	public void whenCloseServicesThenCloseInInvertedOrderOfAddThenTheBottomLayerServices()
			throws Exception {

		layerFactory.close();

		InOrder inOrder = Mockito.inOrder(layerService1, layerService2, layerService3, bottomLayerService);

		inOrder.verify(layerService3).close();
		inOrder.verify(layerService2).close();
		inOrder.verify(layerService1).close();
		inOrder.verify(bottomLayerService).close();
	}
}
