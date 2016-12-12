package com.constellio.data.dao.services.idGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.PropertiesAlteration;

public class ZeroPaddedSequentialUniqueIdGenerator implements StatefulService, UniqueIdGenerator {

	private final ConfigManager configManager;
	private final int reservedBatchSize;
	private final String configPath;
	private Iterator<Long> idReservedByInstance = Collections.emptyIterator();

	public ZeroPaddedSequentialUniqueIdGenerator(ConfigManager configManager, String configPath) {
		this(configManager, configPath, 1000);
	}

	public ZeroPaddedSequentialUniqueIdGenerator(ConfigManager configManager, String configPath, int reservedBatchSize) {
		this.configManager = configManager;
		this.reservedBatchSize = reservedBatchSize;
		this.configPath = configPath;
	}

	@Override
	public void initialize() {
		configManager.createPropertiesDocumentIfInexistent(configPath, new PropertiesAlteration() {
			@Override
			public void alter(Map<String, String> properties) {
				properties.put("next", "1");
			}
		});
	}

	@Override
	public synchronized String next() {
		if (!idReservedByInstance.hasNext()) {
			reserve();
		}
		return zeroPaddedNumber(idReservedByInstance.next());
	}

	private void reserve() {
		configManager.updateProperties(configPath, new PropertiesAlteration() {
			@Override
			public void alter(Map<String, String> properties) {
				Long current = Long.valueOf(properties.get("next"));

				List<Long> nexts = new ArrayList<>();
				for (int i = 0; i < reservedBatchSize; i++) {
					nexts.add(current + i);
				}
				idReservedByInstance = nexts.iterator();

				properties.put("next", String.valueOf(current + reservedBatchSize));
			}
		});
	}

	public static String zeroPaddedNumber(long seq) {
		String zeroPaddedSeq = ("0000000000" + seq);
		return zeroPaddedSeq.substring(zeroPaddedSeq.length() - 11);
	}

	@Override
	public void close() {

	}

	public String nextWithoutZeros() {
		if (!idReservedByInstance.hasNext()) {
			reserve();
		}
		return "" + idReservedByInstance.next();
	}
}
