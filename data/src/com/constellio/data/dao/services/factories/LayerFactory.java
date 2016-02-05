package com.constellio.data.dao.services.factories;

import java.util.ArrayList;
import java.util.List;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.StatefullServiceDecorator;
import com.constellio.data.utils.ImpossibleRuntimeException;

public class LayerFactory {

	private LayerFactory bottomLayerFactory;

	private StatefullServiceDecorator statefullServiceDecorator;

	private List<StatefulService> statefulServices = new ArrayList<>();

	private boolean initializing;

	private boolean initialized;

	public LayerFactory(StatefullServiceDecorator statefullServiceDecorator) {
		this.statefullServiceDecorator = statefullServiceDecorator;
	}

	public LayerFactory(LayerFactory bottomLayerFactory, StatefullServiceDecorator statefullServiceDecorator) {
		this.bottomLayerFactory = bottomLayerFactory;
		this.statefullServiceDecorator = statefullServiceDecorator;
	}

	public <T extends StatefulService> T add(T statefullService) {
		if (initializing) {
			throw new IllegalStateException("Cannot add stateful service during initialization");
		}
		T decoratedService = statefullServiceDecorator.decorate(statefullService);
		statefulServices.add(decoratedService);
		return decoratedService;
	}

	public void initialize() {

		initializing = true;
		if (bottomLayerFactory != null) {
			bottomLayerFactory.initialize();
		}
		List<StatefulService> statefulServiceListCopy = new ArrayList<>(statefulServices);
		for (StatefulService statefulService : statefulServiceListCopy) {
			statefulService.initialize();
		}
		initializing = false;
		initialized = true;
	}

	public void close() {
		for (int i = statefulServices.size() - 1; i >= 0; i--) {
			statefulServices.get(i).close();
		}
		if (bottomLayerFactory != null) {
			bottomLayerFactory.close();
		}
	}

	protected void ensureNotYetInitialized() {
		if (initialized) {
			throw new ImpossibleRuntimeException("Layer is already initialized");
		}
	}
}
