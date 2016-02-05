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
