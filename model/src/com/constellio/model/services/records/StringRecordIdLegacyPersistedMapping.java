package com.constellio.model.services.records;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.PropertiesAlteration;
import com.constellio.data.dao.managers.config.events.ConfigUpdatedEventListener;

import java.util.HashMap;
import java.util.Map;

public class StringRecordIdLegacyPersistedMapping implements StringRecordIdLegacyMapping, ConfigUpdatedEventListener {

	private static final String PATH = "/legacyConstellioIdsMapping.properties";

	private ConfigManager configManager;
	private Map<Integer, String> isMapping;
	private Map<String, Integer> siMapping;
	int seq;

	public StringRecordIdLegacyPersistedMapping(ConfigManager configManager) {
		this.configManager = configManager;
		this.configManager.registerListener(PATH, this);

		if (isMapping == null) {
			synchronized (StringRecordIdLegacyPersistedMapping.class) {
				if (configManager.exist(PATH)) {
					reload();
				} else {
					isMapping = new HashMap<>();
					siMapping = new HashMap<>();
				}
			}
		}
	}

	public int getIntId(String id) {
		Integer value = siMapping.get(id);
		if (value == null) {
			synchronized (StringRecordIdLegacyPersistedMapping.class) {


				value = siMapping.get(id);
				if (value == null) {
					if (!configManager.exist(PATH)) {
						configManager.createPropertiesDocumentIfInexistent(PATH, new PropertiesAlteration() {
							@Override
							public void alter(Map<String, String> properties) {

							}
						});
					}
					configManager.updateProperties(PATH, new PropertiesAlteration() {
						@Override
						public void alter(Map<String, String> properties) {
							int assignedIntIt = seq - 1;
							properties.put(id, "" + assignedIntIt);
						}
					});

				}
			}
		}
		return siMapping.get(id);
	}

	private void reload() {
		Map<Integer, String> isMapping = new HashMap<>();
		Map<String, Integer> siMapping = new HashMap<>();
		Map<String, String> properties = configManager.getProperties(PATH).getProperties();
		seq = 0;
		for (Map.Entry<String, String> entry : properties.entrySet()) {
			String stringId = entry.getKey();
			int intId = Integer.valueOf(entry.getValue());
			isMapping.put(intId, stringId);
			siMapping.put(stringId, intId);
			seq = Math.min(seq, intId);
		}
		this.isMapping = isMapping;
		this.siMapping = siMapping;
	}

	public String getStringId(int intId) {
		if (intId > 0) {
			throw new IllegalArgumentException("Id is not legacy : " + intId);
		} else {
			String stringId = isMapping.get(intId);
			if (stringId == null) {
				throw new IllegalStateException("Int value '" + intId + "' isn't mapped to any legacy string id");
			}
			return stringId;
		}
	}

	@Override
	public void onConfigUpdated(String configPath) {
		reload();
	}
}
