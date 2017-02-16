package com.constellio.model.services.schemas.builders;

import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.entries.AggregationType.REFERENCE_COUNT;

import java.util.Arrays;
import java.util.List;

import com.constellio.model.entities.calculators.InitializedMetadataValueCalculator;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.JEXLMetadataValueCalculator;
import com.constellio.model.entities.schemas.entries.AggregatedDataEntry;
import com.constellio.model.entities.schemas.entries.AggregationType;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntry;
import com.constellio.model.entities.schemas.entries.ManualDataEntry;
import com.constellio.model.entities.schemas.entries.SequenceDataEntry;
import com.constellio.model.services.schemas.builders.DataEntryBuilderRuntimeException.DataEntryBuilderRuntimeException_AgregatedMetadatasNotSupportedOnCustomSchemas;
import com.constellio.model.services.schemas.builders.DataEntryBuilderRuntimeException.DataEntryBuilderRuntimeException_InvalidMetadataCode;
import com.constellio.model.services.schemas.builders.MetadataBuilderRuntimeException.CannotInstanciateClass;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderRuntimeException.CannotCopyUsingACustomMetadata;
import com.constellio.model.utils.ClassProvider;

public class DataEntryBuilder {

	ClassProvider classProvider;

	MetadataBuilder metadata;

	public DataEntryBuilder(MetadataBuilder metadata) {
		super();
		this.metadata = metadata;
		this.classProvider = metadata.getClassProvider();
	}

	public MetadataBuilder asManual() {
		ManualDataEntry manualDataEntry = new ManualDataEntry();
		metadata.dataEntry = manualDataEntry;
		return metadata;
	}

	public MetadataBuilder asCopied(MetadataBuilder referenceMetadataBuilder, MetadataBuilder copiedMetadataBuilder) {

		String referenceMetadataCode = referenceMetadataBuilder.getCode();
		String copiedMetadataCode = copiedMetadataBuilder.getCode();

		AllowedReferencesBuilder allowedReferencesBuider = referenceMetadataBuilder.getAllowedReferencesBuider();

		for (String schemas : allowedReferencesBuider.getSchemas()) {
			if (!schemas.endsWith("_default")) {
				throw new CannotCopyUsingACustomMetadata(referenceMetadataCode, schemas);
			}
		}

		CopiedDataEntry copiedDataEntry = new CopiedDataEntry(referenceMetadataCode, copiedMetadataCode);
		metadata.dataEntry = copiedDataEntry;
		return metadata;
	}

	public MetadataBuilder asReferenceCount(MetadataBuilder referenceToAgregatingSchemaType) {
		if (!metadata.getCode().contains("_default_")) {
			throw new DataEntryBuilderRuntimeException_AgregatedMetadatasNotSupportedOnCustomSchemas();
		}

		if (referenceToAgregatingSchemaType.getType() != REFERENCE || referenceToAgregatingSchemaType.isMultivalue()) {
			throw new DataEntryBuilderRuntimeException_InvalidMetadataCode("reference",
					referenceToAgregatingSchemaType.getCode(), REFERENCE);
		}

		metadata.dataEntry = new AggregatedDataEntry(null, referenceToAgregatingSchemaType.getCode(), REFERENCE_COUNT);
		return metadata;
	}

	public MetadataBuilder asSum(MetadataBuilder referenceToAgregatingSchemaType, MetadataBuilder number) {
		if (!metadata.getCode().contains("_default_")) {
			throw new DataEntryBuilderRuntimeException_AgregatedMetadatasNotSupportedOnCustomSchemas();
		}

		if (referenceToAgregatingSchemaType.getType() != REFERENCE || referenceToAgregatingSchemaType.isMultivalue()) {
			throw new DataEntryBuilderRuntimeException_InvalidMetadataCode("reference",
					referenceToAgregatingSchemaType.getCode(), REFERENCE);
		}

		if (number.getType() != NUMBER || number.isMultivalue()) {
			throw new DataEntryBuilderRuntimeException_InvalidMetadataCode("number", number.getCode(), NUMBER);
		}

		if (metadata.getType() == null) {
			metadata.setType(number.getType());
		}

		metadata.dataEntry = new AggregatedDataEntry(number.getCode(), referenceToAgregatingSchemaType.getCode(),
				AggregationType.SUM);
		return metadata;
	}

	@SuppressWarnings("unchecked")
	public MetadataBuilder asCalculated(String calculatorClassName) {
		Class<? extends MetadataValueCalculator<?>> calculatorClass;
		try {
			calculatorClass = classProvider.loadClass(calculatorClassName);
		} catch (ClassNotFoundException e) {
			throw new CannotInstanciateClass(calculatorClassName, e);
		}
		return asCalculated(calculatorClass);
	}

	@SuppressWarnings("unchecked")
	public MetadataBuilder asJexlScript(String pattern) {
		metadata.dataEntry = new CalculatedDataEntry(new JEXLMetadataValueCalculator(pattern));
		return metadata;
	}

	public MetadataBuilder asCalculated(MetadataValueCalculator<?> calculator) {
		List<Class<?>> interfaces = Arrays.asList(calculator.getClass().getInterfaces());
		if (interfaces.contains(MetadataValueCalculator.class) || interfaces.contains(InitializedMetadataValueCalculator.class)) {
			metadata.dataEntry = new CalculatedDataEntry(calculator);
			return metadata;
		} else {
			throw new MetadataBuilderRuntimeException.InvalidAttribute(metadata.getLocalCode(), "calculator");
		}
	}

	public MetadataBuilder asCalculated(Class<? extends MetadataValueCalculator<?>> calculatorClass) {
		List<Class<?>> interfaces = Arrays.asList(calculatorClass.getInterfaces());
		if (interfaces.contains(MetadataValueCalculator.class) || interfaces.contains(InitializedMetadataValueCalculator.class)) {
			try {
				metadata.dataEntry = new CalculatedDataEntry(calculatorClass.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				//
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

	public MetadataBuilder asFixedSequence(String fixedSequenceCode) {
		metadata.dataEntry = new SequenceDataEntry(fixedSequenceCode, null);
		if (metadata.getType() == null) {
			metadata.setType(STRING);
		}
		return metadata;
	}

	public MetadataBuilder asSequenceDefinedByMetadata(String metadataLocalCode) {
		metadata.dataEntry = new SequenceDataEntry(null, metadataLocalCode);
		if (metadata.getType() == null) {
			metadata.setType(STRING);
		}
		return metadata;
	}
}
