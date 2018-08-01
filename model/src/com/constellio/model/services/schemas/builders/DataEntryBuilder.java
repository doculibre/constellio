package com.constellio.model.services.schemas.builders;

import com.constellio.model.entities.calculators.InitializedMetadataValueCalculator;
import com.constellio.model.entities.calculators.JEXLMetadataValueCalculator;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.schemas.entries.*;
import com.constellio.model.services.schemas.builders.DataEntryBuilderRuntimeException.DataEntryBuilderRuntimeException_AgregatedMetadatasNotSupportedOnCustomSchemas;
import com.constellio.model.services.schemas.builders.DataEntryBuilderRuntimeException.DataEntryBuilderRuntimeException_InvalidMetadataCode;
import com.constellio.model.services.schemas.builders.MetadataBuilderRuntimeException.CannotInstanciateClass;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderRuntimeException.CannotCopyUsingACustomMetadata;
import com.constellio.model.utils.ClassProvider;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.entities.schemas.MetadataValueType.*;
import static com.constellio.model.entities.schemas.entries.AggregationType.*;
import static java.util.Arrays.asList;

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

	public MetadataBuilder asUnion(MetadataBuilder referenceToAgregatingSchemaType, MetadataBuilder... valueMetadatas) {
		if (!metadata.getCode().contains("_default_")) {
			throw new DataEntryBuilderRuntimeException_AgregatedMetadatasNotSupportedOnCustomSchemas();
		}

		if (referenceToAgregatingSchemaType.getType() != REFERENCE || referenceToAgregatingSchemaType.isMultivalue()) {
			throw new DataEntryBuilderRuntimeException_InvalidMetadataCode("reference",
					referenceToAgregatingSchemaType.getCode(), REFERENCE);
		}

		List<String> valueMetadatasCodes = new ArrayList<>();
		for (MetadataBuilder valueMetadata : valueMetadatas) {
			valueMetadatasCodes.add(valueMetadata.getCode());
		}

		metadata.dataEntry = new AggregatedDataEntry(valueMetadatasCodes, referenceToAgregatingSchemaType.getCode(),
				VALUES_UNION);
		return metadata;
	}

	public MetadataBuilder asSum(MetadataBuilder referenceToAgregatingSchemaType, MetadataBuilder number) {
		return asStatAggregation(referenceToAgregatingSchemaType, number, AggregationType.SUM);
	}

	public MetadataBuilder asMin(MetadataBuilder referenceToAgregatingSchemaType, MetadataBuilder number) {
		return asStatAggregation(referenceToAgregatingSchemaType, number, AggregationType.MIN);
	}

	public MetadataBuilder asMax(MetadataBuilder referenceToAgregatingSchemaType, MetadataBuilder number) {
		return asStatAggregation(referenceToAgregatingSchemaType, number, AggregationType.MAX);
	}

	private MetadataBuilder asStatAggregation(MetadataBuilder referenceToAgregatingSchemaType,
											  MetadataBuilder inputMetadata,
											  AggregationType aggregationType) {
		if (!metadata.getCode().contains("_default_")) {
			throw new DataEntryBuilderRuntimeException_AgregatedMetadatasNotSupportedOnCustomSchemas();
		}

		if (referenceToAgregatingSchemaType.getType() != REFERENCE || referenceToAgregatingSchemaType.isMultivalue()) {
			throw new DataEntryBuilderRuntimeException_InvalidMetadataCode("reference",
					referenceToAgregatingSchemaType.getCode());
		}

		if (inputMetadata.getType() != NUMBER && inputMetadata.getType() != DATE && inputMetadata.getType() != DATE_TIME) {
			throw new DataEntryBuilderRuntimeException_InvalidMetadataCode("inputMetadata", inputMetadata.getCode(),
					NUMBER, DATE, DATE_TIME);
		}

		if (metadata.getType() == null) {
			metadata.setType(inputMetadata.getType());
		}

		metadata.dataEntry = new AggregatedDataEntry(
				asList(inputMetadata.getCode()), referenceToAgregatingSchemaType.getCode(), aggregationType);
		return metadata;
	}

	public MetadataBuilder asCalculatedAggregation(MetadataBuilder referenceToAgregatingSchemaType,
												   Class<? extends AggregatedCalculator<?>> calculatorClass) {
		if (!metadata.getCode().contains("_default_")) {
			throw new DataEntryBuilderRuntimeException_AgregatedMetadatasNotSupportedOnCustomSchemas();
		}

		if (referenceToAgregatingSchemaType.getType() != REFERENCE || referenceToAgregatingSchemaType.isMultivalue()) {
			throw new DataEntryBuilderRuntimeException_InvalidMetadataCode("reference",
					referenceToAgregatingSchemaType.getCode(), REFERENCE);
		}

		metadata.dataEntry = new AggregatedDataEntry(null, referenceToAgregatingSchemaType.getCode(), CALCULATED,
				calculatorClass);
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
		List<Class<?>> interfaces = asList(calculator.getClass().getInterfaces());
		if (interfaces.contains(MetadataValueCalculator.class) || interfaces.contains(InitializedMetadataValueCalculator.class)) {
			metadata.dataEntry = new CalculatedDataEntry(calculator);
			return metadata;
		} else {
			throw new MetadataBuilderRuntimeException.InvalidAttribute(metadata.getLocalCode(), "calculator");
		}
	}

	public MetadataBuilder asCalculated(Class<? extends MetadataValueCalculator<?>> calculatorClass) {
		List<Class<?>> interfaces = new ArrayList<>();

		Class<?> aClass = calculatorClass;
		interfaces.addAll(asList(calculatorClass.getInterfaces()));
		while (aClass.getSuperclass() != null) {
			aClass = aClass.getSuperclass();
			interfaces.addAll(asList(aClass.getInterfaces()));
		}

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
