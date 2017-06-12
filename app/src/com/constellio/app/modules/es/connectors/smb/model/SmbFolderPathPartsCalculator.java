package com.constellio.app.modules.es.connectors.smb.model;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.constellio.app.modules.es.connectors.smb.LastFetchedStatus;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.calculators.PathPartsCalculator;
import org.apache.commons.lang3.StringUtils;

public class SmbFolderPathPartsCalculator extends PathPartsCalculator implements MetadataValueCalculator<List<String>> {

	LocalDependency<List<String>> pathDependency = LocalDependency.toAStringList("path");
	LocalDependency<LastFetchedStatus> statusDependency = LocalDependency.toAnEnum(ConnectorSmbFolder.LAST_FETCHED_STATUS);

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		LastFetchedStatus lastFetchedStatus = parameters.get(statusDependency);
		if (lastFetchedStatus != null && LastFetchedStatus.OK.equals(lastFetchedStatus)) {
			return super.calculate(parameters);
		}
		return new ArrayList<>();
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Arrays.asList(pathDependency, statusDependency);
	}

}
