package com.constellio.model.services.factories;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.services.records.RecordImpl;

public class ModelLayerLogger {

	private static final Logger LOGGER = LoggerFactory.getLogger(ModelLayerLogger.class);

	private Set<String> loggedRecord = new HashSet<>();

	public void logRecord(String id) {
		loggedRecord.add(id);
	}

	public void logCalculatedValue(RecordImpl record, MetadataValueCalculator<?> calculator,
			Map<Dependency, Object> values) {
		if (loggedRecord.contains(record.getId())) {
			StringBuilder logText = new StringBuilder(
					"Calculator '" + calculator.getClass().getSimpleName() + "' of record '" + record.getIdTitle() + "'");
			for (Map.Entry<Dependency, Object> param : values.entrySet()) {
				logText.append("\n\t" + param.getKey() + " => " + param.getValue());
			}
			LOGGER.info(logText.toString());
		}
	}

}
