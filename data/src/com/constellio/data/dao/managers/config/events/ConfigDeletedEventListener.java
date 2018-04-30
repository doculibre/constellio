package com.constellio.data.dao.managers.config.events;

public interface ConfigDeletedEventListener extends ConfigEventListener {

	void onConfigDeleted(String configPath);

}
