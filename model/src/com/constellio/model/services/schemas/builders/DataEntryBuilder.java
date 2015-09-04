/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.schemas.builders;

import java.util.Arrays;

import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntry;
import com.constellio.model.entities.schemas.entries.ManualDataEntry;
import com.constellio.model.services.schemas.builders.MetadataBuilderRuntimeException.CannotInstanciateClass;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderRuntimeException.CannotCopyUsingACustomMetadata;

public class DataEntryBuilder {

	MetadataBuilder metadata;

	public DataEntryBuilder(MetadataBuilder metadata) {
		super();
		this.metadata = metadata;
	}

	public MetadataBuilder asManual() {
		ManualDataEntry copiedDataEntry = new ManualDataEntry();
		metadata.dataEntry = copiedDataEntry;
		return metadata;
	}

	public MetadataBuilder asCopied(MetadataBuilder referenceMetadataBuilder, MetadataBuilder copiedMetadataBuilder) {

		String referenceMetadataCode = referenceMetadataBuilder.getCode();
		String copiedMetadataCode = copiedMetadataBuilder.getCode();

		for (String schemas : referenceMetadataBuilder.getAllowedReferencesBuider().getSchemas()) {
			if (!schemas.endsWith("_default")) {
				throw new CannotCopyUsingACustomMetadata(referenceMetadataCode, schemas);
			}
		}

		CopiedDataEntry copiedDataEntry = new CopiedDataEntry(referenceMetadataCode, copiedMetadataCode);
		metadata.dataEntry = copiedDataEntry;
		return metadata;
	}

	@SuppressWarnings("unchecked")
	public MetadataBuilder asCalculated(String calculatorClassName) {
		Class<? extends MetadataValueCalculator<?>> calculatorClass;
		try {
			calculatorClass = (Class<? extends MetadataValueCalculator<?>>) Class.forName(calculatorClassName);
		} catch (ClassNotFoundException e) {
			throw new CannotInstanciateClass(calculatorClassName, e);
		}
		return asCalculated(calculatorClass);
	}

	public MetadataBuilder asCalculated(Class<? extends MetadataValueCalculator<?>> calculatorClass) {
		if (Arrays.asList(calculatorClass.getInterfaces()).contains(MetadataValueCalculator.class)) {
			try {
				metadata.dataEntry = new CalculatedDataEntry(calculatorClass.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				throw new MetadataBuilderRuntimeException.InvalidAttribute(metadata.getLocalCode(), "calculator", e);
			}
		} else {
			throw new MetadataBuilderRuntimeException.InvalidAttribute(metadata.getLocalCode(), "calculator");
		}
		return metadata;
	}

	public void as(DataEntry dataEntryValue) {
		metadata.dataEntry = dataEntryValue;
	}
}
