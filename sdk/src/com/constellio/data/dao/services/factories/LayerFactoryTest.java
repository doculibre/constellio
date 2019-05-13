package com.constellio.data.dao.services.factories;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.StatefullServiceDecorator;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.doAnswer;

public class LayerFactoryTest extends ConstellioTest {

	StatefullServiceDecorator statefullServiceDecorator = new StatefullServiceDecorator();

	LayerFactory layerFactory;

	LayerFactory bottomLayerFactory;

	@Mock StatefulService layerService1, layerService2, layerService3, bottomLayerService;

	@Mock StatefulService layerService1a, layerService1b, layerService2a;

	@Before
	public void setUp()
			throws Exception {

		bottomLayerFactory = new LayerFactoryImpl(statefullServiceDecorator, null, (short) 0);
		layerFactory = new LayerFactoryImpl(bottomLayerFactory, statefullServiceDecorator, null, (short) 0);

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
	public void givenALayerServiceIsAddingOtherLayerServicesDuringInitializeThenInitializedInCorrectOrder()
			throws Exception {

		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation)
					throws Throwable {
				layerFactory.add(layerService1a);
				layerFactory.add(layerService1b);
				return null;
			}
		}).when(layerService1).initialize();

		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation)
					throws Throwable {
				layerFactory.add(layerService2a);
				return null;
			}
		}).when(layerService2).initialize();

		layerFactory.initialize();

		InOrder inOrder = Mockito
				.inOrder(layerService1, layerService1a, layerService1b, layerService2, layerService2a, layerService3,
						bottomLayerService);
		inOrder.verify(bottomLayerService).initialize();
		inOrder.verify(layerService1).initialize();
		inOrder.verify(layerService1a).initialize();
		inOrder.verify(layerService1b).initialize();
		inOrder.verify(layerService2).initialize();
		inOrder.verify(layerService2a).initialize();
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
