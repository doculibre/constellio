package com.constellio.data.dao.services.factories;

import java.util.ArrayList;
import java.util.List;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.StatefullServiceDecorator;
import com.constellio.data.utils.ImpossibleRuntimeException;

public class LayerFactory {

	private String instanceName;

	private LayerFactory bottomLayerFactory;

	private StatefullServiceDecorator statefullServiceDecorator;

	private List<StatefulService> statefulServices = new ArrayList<>();

	private boolean initializing;

	private boolean initialized;

	public LayerFactory(StatefullServiceDecorator statefullServiceDecorator, String instanceName) {
		this.statefullServiceDecorator = statefullServiceDecorator;
		this.instanceName = instanceName;
	}

	public LayerFactory(LayerFactory bottomLayerFactory, StatefullServiceDecorator statefullServiceDecorator,
			String instanceName) {
		this.bottomLayerFactory = bottomLayerFactory;
		this.statefullServiceDecorator = statefullServiceDecorator;
		this.instanceName = instanceName;
	}

	public String getInstanceName() {
		return instanceName;
	}

	public <T extends StatefulService> T add(T statefulService) {
		T decoratedService = statefullServiceDecorator.decorate(statefulService);
		statefulServices.add(decoratedService);
		if (initializing || initialized) {
			statefullServiceDecorator.beforeInitialize(statefulService);
			statefulService.initialize();
			statefullServiceDecorator.afterInitialize(statefulService);
		}
		return decoratedService;
	}

	public void initialize() {

		initializing = true;
		if (bottomLayerFactory != null) {
			bottomLayerFactory.initialize();
		}
		List<StatefulService> statefulServiceListCopy = new ArrayList<>(statefulServices);
		for (StatefulService statefulService : statefulServiceListCopy) {
			statefullServiceDecorator.beforeInitialize(statefulService);
			statefulService.initialize();
			statefullServiceDecorator.afterInitialize(statefulService);
		}
		initializing = false;
		initialized = true;
	}

	public void close() {
		close(true);
	}

	public void close(boolean closeBottomLayers) {
		for (int i = statefulServices.size() - 1; i >= 0; i--) {
			statefulServices.get(i).close();
		}
		if (closeBottomLayers && bottomLayerFactory != null) {
			bottomLayerFactory.close(closeBottomLayers);
		}
	}

	protected void ensureNotYetInitialized() {
		if (initialized) {
			throw new ImpossibleRuntimeException("Layer is already initialized");
		}
	}

	public final String toResourceName(String name) {
		if (instanceName == null) {
			return name;
		} else {
			return instanceName + ":" + name;
		}
	}
}
