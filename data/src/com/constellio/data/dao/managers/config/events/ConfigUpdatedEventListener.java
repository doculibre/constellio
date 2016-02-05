package com.constellio.data.dao.managers.config.events;

public interface ConfigUpdatedEventListener extends ConfigEventListener {

	void onConfigUpdated(String configPath);

}
