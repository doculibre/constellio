package com.constellio.model.services.schemas.builders;

import com.constellio.model.entities.calculators.InitializedMetadataValueCalculator;
import com.constellio.model.entities.calculators.JEXLMetadataValueCalculator;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.MultiMetadatasValueCalculator;
import com.constellio.model.entities.schemas.entries.AggregatedCalculator;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE_TIME;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.entries.AggregationType.CALCULATED;
import static com.constellio.model.entities.schemas.entries.AggregationType.LOGICAL_AND;
import static com.constellio.model.entities.schemas.entries.AggregationType.LOGICAL_OR;
import static com.constellio.model.entities.schemas.entries.AggregationType.REFERENCE_COUNT;
import static com.constellio.model.entities.schemas.entries.AggregationType.VALUES_UNION;
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
		if (MetadataValueCalculator.class.isAssignableFrom(calculator.getClass()) ||
			InitializedMetadataValueCalculator.class.isAssignableFrom(calculator.getClass())) {
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

		if (interfaces.contains(MetadataValueCalculator.class) || interfaces.contains(MultiMetadatasValueCalculator.class) || interfaces.contains(InitializedMetadataValueCalculator.class)) {
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

	public MetadataBuilder asAggregatedOr(MetadataBuilder referenceToAggregatingSchemaType,
										  MetadataBuilder... booleanMetadatas) {
		Map<MetadataBuilder, List<MetadataBuilder>> metadataByReferenceMetadata = new HashMap<>();
		metadataByReferenceMetadata.put(referenceToAggregatingSchemaType, asList(booleanMetadatas));
		return asLogicalAggregation(metadataByReferenceMetadata, LOGICAL_OR);
	}

	public MetadataBuilder asAggregatedOr(
			Map<MetadataBuilder, List<MetadataBuilder>> booleanMetadataByReferenceToAggregatingSchemaType) {
		return asLogicalAggregation(booleanMetadataByReferenceToAggregatingSchemaType, LOGICAL_OR);
	}

	public MetadataBuilder asAggregatedAnd(MetadataBuilder referenceToAggregatingSchemaType,
										   MetadataBuilder... booleanMetadatas) {
		Map<MetadataBuilder, List<MetadataBuilder>> metadataByReferenceMetadata = new HashMap<>();
		metadataByReferenceMetadata.put(referenceToAggregatingSchemaType, asList(booleanMetadatas));
		return asLogicalAggregation(metadataByReferenceMetadata, LOGICAL_AND);
	}

	public MetadataBuilder asAggregatedAnd(
			Map<MetadataBuilder, List<MetadataBuilder>> booleanMetadataByReferenceToAggregatingSchemaType) {
		return asLogicalAggregation(booleanMetadataByReferenceToAggregatingSchemaType, LOGICAL_AND);
	}

	private MetadataBuilder asLogicalAggregation(
			Map<MetadataBuilder, List<MetadataBuilder>> booleanMetadataByReferenceToAggregatingSchemaType,
			AggregationType aggregationType) {
		if (!metadata.getCode().contains("_default_")) {
			throw new DataEntryBuilderRuntimeException_AgregatedMetadatasNotSupportedOnCustomSchemas();
		}

		Map<String, List<String>> booleanMetadataCodesByReferenceMetadataCode = new HashMap<>();
		for (MetadataBuilder referenceMetadata : booleanMetadataByReferenceToAggregatingSchemaType.keySet()) {
			if (referenceMetadata.getType() != REFERENCE || referenceMetadata.isMultivalue()) {
				throw new DataEntryBuilderRuntimeException_InvalidMetadataCode("reference",
						referenceMetadata.getCode());
			}

			List<String> metadataCodes = new ArrayList<>();
			for (MetadataBuilder metadata : booleanMetadataByReferenceToAggregatingSchemaType.get(referenceMetadata)) {
				if (metadata.getType() != BOOLEAN) {
					throw new DataEntryBuilderRuntimeException_InvalidMetadataCode("booleanMetadata",
							metadata.getCode(), BOOLEAN);
				}
				metadataCodes.add(metadata.getCode());
			}
			booleanMetadataCodesByReferenceMetadataCode.put(referenceMetadata.getCode(), metadataCodes);
		}

		if (metadata.getType() == null) {
			metadata.setType(BOOLEAN);
		}

		metadata.dataEntry = new AggregatedDataEntry(booleanMetadataCodesByReferenceMetadataCode, aggregationType);
		return metadata;
	}
}
