package com.constellio.app.entities.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.configs.SystemConfiguration;

public abstract class DefaultComplementaryModule implements InstallableSystemModule {

	private String id, name, publisher;

	public DefaultComplementaryModule(String id, String name, String publisher) {
		this.id = id;
		this.name = name;
		this.publisher = publisher;
	}

	@Override
	public List<MigrationScript> getMigrationScripts() {
		return new ArrayList<>();
	}

	@Override
	public void configureNavigation(NavigationConfig config) {
	}

	@Override
	public void start(String collection, AppLayerFactory appLayerFactory) {

	}

	@Override
	public void start(AppLayerFactory appLayerFactory) {

	}

	@Override
	public void stop(AppLayerFactory appLayerFactory) {

	}

	@Override
	public void stop(String collection, AppLayerFactory appLayerFactory) {

	}

	@Override
	public void addDemoData(String collection, AppLayerFactory appLayerFactory) {

	}

	@Override
	public final boolean isComplementary() {
		return true;
	}

	@Override
	public List<SystemConfiguration> getConfigurations() {
		return new ArrayList<>();
	}

	@Override
	public Map<String, List<String>> getPermissions() {
		return new HashMap<>();
	}

	@Override
	public List<String> getRolesForCreator() {
		return new ArrayList<>();
	}

	@Override
	public final String getId() {
		return id;
	}

	@Override
	public final String getName() {
		return name;
	}

	@Override
	public final String getPublisher() {
		return publisher;
	}

}
