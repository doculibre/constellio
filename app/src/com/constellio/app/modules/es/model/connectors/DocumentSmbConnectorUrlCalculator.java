package com.constellio.app.modules.es.model.connectors;

import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.http.enums.FetchFrequency;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;
import org.joda.time.LocalDateTime;

import java.util.List;

import static java.util.Arrays.asList;

public class DocumentSmbConnectorUrlCalculator implements MetadataValueCalculator<String> {

	LocalDependency<String> urlParam = LocalDependency.toAString(ConnectorSmbDocument.URL);
	LocalDependency<String> connectorParam = LocalDependency.toAReference(ConnectorSmbDocument.CONNECTOR);

	@Override
	public String calculate(CalculatorParameters parameters) {
		String url = parameters.get(urlParam);
		String connector = parameters.get(connectorParam);
		return calculate(url, connector);
	}

	@Override
	public String getDefaultValue() {
		return null;
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.STRING;
	}

	@Override
	public boolean isMultiValue() {
		return false;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(urlParam, connectorParam);
	}

	public static String calculate(String url, String connector) {
		return url != null && connector != null ? connector + "/?/" + url : null;
	}
}
