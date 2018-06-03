package com.constellio.app.modules.es.model.connectors;

import static java.util.Arrays.asList;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import com.constellio.app.modules.es.model.connectors.http.enums.FetchFrequency;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;

public class NextFetchCalculator implements MetadataValueCalculator<LocalDateTime> {

	LocalDependency<LocalDateTime> fetchedDateTimeParam = LocalDependency.toADateTime(ConnectorDocument.FETCHED_DATETIME);
	LocalDependency<Double> fetchDelayParam = LocalDependency.toANumber(ConnectorDocument.FETCH_DELAY);
	LocalDependency<FetchFrequency> fetchFrequencyParam = LocalDependency.toAnEnum(ConnectorDocument.FETCH_FREQUENCY);
	LocalDependency<String> fetchErrorCode = LocalDependency.toAString(ConnectorDocument.ERROR_CODE);

	@Override
	public LocalDateTime calculate(CalculatorParameters parameters) {
		LocalDateTime fetchedDateTime = parameters.get(fetchedDateTimeParam);
		Double fetchDelay = parameters.get(fetchDelayParam);
		String errorCode = parameters.get(fetchErrorCode);

		if(StringUtils.isNotBlank(errorCode)) {
			return fetchedDateTime == null ? null : fetchedDateTime.plusHours(24);
		} else {
			return fetchedDateTime == null || fetchDelay == null ? null : fetchedDateTime.plusDays(fetchDelay.intValue());
		}
	}

	@Override
	public LocalDateTime getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.DATE_TIME;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(fetchedDateTimeParam, fetchDelayParam, fetchFrequencyParam, fetchErrorCode);
	}
}
