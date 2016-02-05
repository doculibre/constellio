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
		ManualDataEntry manualDataEntry = new ManualDataEntry();
		metadata.dataEntry = manualDataEntry;
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
