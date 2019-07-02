package com.constellio.model.services.schemas.builders;

import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.calculators.InitializedMetadataValueCalculator;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataNetwork;
import com.constellio.model.entities.schemas.MetadataNetworkBuilder;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.InvalidCodeFormat;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.SchemaComparators;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderRuntimeException.CannotDeleteSchemaTypeSinceItHasRecords;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.utils.ClassProvider;
import com.constellio.model.utils.DependencyUtils;
import com.constellio.model.utils.DependencyUtils.MultiMapDependencyResults;
import com.constellio.model.utils.DependencyUtilsParams;
import com.constellio.model.utils.DependencyUtilsRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class MetadataSchemaTypesBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(MetadataSchemaTypesBuilder.class);

	private static final String UNDERSCORE = "_";
	private static final String DEFAULT = "default";
	private int version;
	private final List<MetadataSchemaTypeBuilder> schemaTypes = new ArrayList<>();
	private final CollectionInfo collectionInfo;
	private ClassProvider classProvider;
	private List<Language> languages = new ArrayList<>();
	private SchemasIdSequence schemasTypeIdSequence;

	private MetadataSchemaTypesBuilder(CollectionInfo collectionInfo, int version, ClassProvider classProvider,
									   List<Language> languages) {
		super();
		this.collectionInfo = collectionInfo;
		this.version = version;
		this.classProvider = classProvider;
		this.languages = Collections.unmodifiableList(languages);
	}

	public static MetadataSchemaTypesBuilder modify(MetadataSchemaTypes types, ClassProvider classProvider) {
		MetadataSchemaTypesBuilder typesBuilder = new MetadataSchemaTypesBuilder(types.getCollectionInfo(), types.getVersion(),
				classProvider, types.getLanguages());
		for (MetadataSchemaType type : types.getSchemaTypes()) {
			typesBuilder.schemaTypes.add(MetadataSchemaTypeBuilder.modifySchemaType(type, classProvider));
		}
		return typesBuilder;
	}

	public static MetadataSchemaTypesBuilder createWithVersion(CollectionInfo collectionInfo, int version,
															   ClassProvider classProvider,
															   List<Language> languages) {
		return new MetadataSchemaTypesBuilder(collectionInfo, version, classProvider, languages);
	}

	public MetadataSchemaTypes build(DataStoreTypesFactory typesFactory, ModelLayerFactory modelLayerFactory) {

		validateAutomaticMetadatas();
		List<String> dependencies = validateNoCyclicDependenciesBetweenSchemas();

		List<MetadataSchemaType> buildedSchemaTypes = new ArrayList<>();
		for (MetadataSchemaTypeBuilder schemaType : schemaTypes) {
			buildedSchemaTypes.add(schemaType.build(typesFactory, this, modelLayerFactory));
		}

		List<String> referenceDefaultValues = new ArrayList<>();
		for (MetadataSchemaType buildedSchemaType : buildedSchemaTypes) {
			for (Metadata metadata : buildedSchemaType.getAllMetadatas().onlyWithType(MetadataValueType.REFERENCE)
					.onlyWithDefaultValue()) {
				if (metadata.getDefaultValue() instanceof List) {
					referenceDefaultValues.addAll((List) metadata.getDefaultValue());
				} else if (metadata.getDefaultValue() instanceof String) {
					referenceDefaultValues.add((String) metadata.getDefaultValue());
				}
			}
		}

		Collections.sort(buildedSchemaTypes, SchemaComparators.SCHEMA_TYPE_COMPARATOR_BY_ASC_CODE);

		MetadataSchemaTypes tempTypes = new MetadataSchemaTypes(collectionInfo, version + 1, buildedSchemaTypes, dependencies,
				referenceDefaultValues, languages, MetadataNetwork.EMPTY());

		for (MetadataSchemaType type : tempTypes.getSchemaTypes()) {
			for (MetadataSchema schema : type.getAllSchemas()) {
				for (Metadata metadata : schema.getMetadatas().onlyCalculated()) {
					MetadataValueCalculator<?> calculator = ((CalculatedDataEntry) metadata.getDataEntry())
							.getCalculator();
					if (calculator instanceof InitializedMetadataValueCalculator) {
						((InitializedMetadataValueCalculator) calculator).initialize(tempTypes, schema, metadata);
					}
				}
			}
		}
		MetadataSchemaTypes types = new MetadataSchemaTypes(collectionInfo, version + 1, buildedSchemaTypes, dependencies,
				referenceDefaultValues, languages, MetadataNetworkBuilder.buildFrom(buildedSchemaTypes));

		return types;
	}

	public MetadataSchemaTypeBuilder createNewSchemaType(String code) {
		return createNewSchemaType(code, true);
	}

	public MetadataSchemaTypeBuilder createNewSchemaType(String code, boolean initialize) {
		MetadataSchemaTypeBuilder typeBuilder;
		if (hasSchemaType(code)) {
			throw new MetadataSchemaTypesBuilderRuntimeException.SchemaTypeExistent(code);
		}

		typeBuilder = MetadataSchemaTypeBuilder.createNewSchemaType(collectionInfo, code, this, initialize);

		schemaTypes.add(typeBuilder);
		return typeBuilder;
	}

	public boolean hasSchemaType(String code) {
		for (MetadataSchemaTypeBuilder schemaType : schemaTypes) {
			if (schemaType.getCode().equals(code)) {
				return true;
			}
		}
		return false;
	}

	public MetadataSchemaTypeBuilder getSchemaType(String code) {
		for (MetadataSchemaTypeBuilder schemaType : schemaTypes) {
			if (schemaType.getCode().equals(code)) {
				return schemaType;
			}
		}
		throw new MetadataSchemaTypesBuilderRuntimeException.NoSuchSchemaType(code);
	}

	public List<MetadataSchemaTypeBuilder> getTypes() {
		List<MetadataSchemaTypeBuilder> types = new ArrayList<>();
		types.addAll(schemaTypes);
		return Collections.unmodifiableList(types);
	}

	public MetadataSchemaTypeBuilder getOrCreateNewSchemaType(String code) {
		try {
			return getSchemaType(code);
		} catch (Exception e) {
			LOGGER.debug("No schema type with code '{}', creating one", code, e);
			return createNewSchemaType(code);
		}
	}

	public MetadataSchemaBuilder getDefaultSchema(String typeCode) {
		return getSchemaType(typeCode).getDefaultSchema();
	}

	public MetadataSchemaBuilder getSchema(String code) {

		String[] parsedCode = code.split(UNDERSCORE);

		if (parsedCode.length > 2) {
			throw new InvalidCodeFormat(code);
		}

		String typeCode = parsedCode[0];
		String schemaCode = parsedCode[1];

		MetadataSchemaTypeBuilder schemaType = getSchemaType(typeCode);
		MetadataSchemaBuilder schema = null;
		if (schemaCode.equals(DEFAULT)) {
			schema = schemaType.getDefaultSchema();
		} else {
			schema = schemaType.getCustomSchema(schemaCode);
		}

		if (schema == null) {
			throw new MetadataSchemaTypesBuilderRuntimeException.NoSuchSchema(code);
		} else {
			return schema;
		}
	}

	public MetadataBuilder getMetadata(String code) {
		String[] parsedCode = code.split(UNDERSCORE);

		String typeCode;
		String schemaCode;
		String metadataCode;

		if (parsedCode.length == 3) {
			typeCode = parsedCode[0];
			schemaCode = parsedCode[1];
			metadataCode = parsedCode[2];
		} else {
			throw new InvalidCodeFormat(code);
		}

		MetadataSchemaTypeBuilder schemaType = getSchemaType(typeCode);
		MetadataBuilder metadata = null;
		if (schemaCode.equals(DEFAULT)) {
			metadata = schemaType.getDefaultSchema().getMetadata(metadataCode);
		} else {
			metadata = schemaType.getCustomSchema(schemaCode).getMetadata(metadataCode);
		}
		if (metadata == null) {
			throw new MetadataSchemaTypesBuilderRuntimeException.NoSuchMetadata(metadataCode);
		} else {
			return metadata;
		}
	}

	public int getVersion() {
		return version;
	}

	public List<Language> getLanguages() {
		return languages;
	}

	@Override
	public String toString() {
		return "MetadataSchemaTypesBuilder [version=" + version + ", schemaTypes=" + schemaTypes + "]";
	}

	public Set<MetadataBuilder> getAllMetadatas() {
		Set<MetadataBuilder> metadatas = new HashSet<>();
		for (MetadataSchemaTypeBuilder schemaType : schemaTypes) {
			add(metadatas, schemaType);
		}
		return metadatas;
	}

	public Set<MetadataBuilder> getAllCopiedMetadatas() {
		Set<MetadataBuilder> copiedMetadatas = new HashSet<>();
		for (MetadataBuilder metadataBuilder : getAllMetadatas()) {
			if (metadataBuilder.getDataEntry() != null && metadataBuilder.getDataEntry().getType() == DataEntryType.COPIED) {
				copiedMetadatas.add(metadataBuilder);
			}
		}
		return copiedMetadatas;
	}

	public Set<MetadataBuilder> getAllCalculatedMetadatas() {
		Set<MetadataBuilder> calculatedMetadatas = new HashSet<>();
		for (MetadataBuilder metadataBuilder : getAllMetadatas()) {
			if (metadataBuilder.getDataEntry() != null && metadataBuilder.getDataEntry().getType() == DataEntryType.CALCULATED) {
				calculatedMetadatas.add(metadataBuilder);
			}
		}
		return calculatedMetadatas;
	}

	public Set<MetadataBuilder> getAllMetadatasOfType(MetadataValueType type) {
		Set<MetadataBuilder> filteredMetadatas = new HashSet<>();
		for (MetadataBuilder metadataBuilder : getAllMetadatas()) {
			if (metadataBuilder.getType() == type) {
				filteredMetadatas.add(metadataBuilder);
			}
		}
		return filteredMetadatas;
	}

	List<String> validateNoCyclicDependenciesBetweenSchemas() {
		Map<String, Set<String>> primaryTypesDependencies = new HashMap<>();
		Map<String, Set<String>> secondaryTypesDependencies = new HashMap<>();
		for (MetadataSchemaTypeBuilder metadataSchemaType : schemaTypes) {
			Set<String> primaryTypes = new HashSet<>();
			Set<String> secondaryTypes = new HashSet<>();
			for (MetadataBuilder metadata : metadataSchemaType.getAllMetadatas()) {

				if (metadata.getType() == REFERENCE) {
					if (metadata.allowedReferencesBuilder == null) {
						throw new MetadataSchemaTypesBuilderRuntimeException.NoAllowedReferences(metadata.getCode());
					}
				}

				if (metadata.getType() == REFERENCE && (metadata.isDependencyOfAutomaticMetadata() || metadata
						.isChildOfRelationship() || metadata.isTaxonomyRelationship())) {

					primaryTypes.add(metadata.allowedReferencesBuilder.getSchemaType());
					for (String schema : metadata.allowedReferencesBuilder.getSchemas()) {
						primaryTypes.add(newSchemaUtils().getSchemaTypeCode(schema));
					}
				}

				if (metadata.getType() == REFERENCE) {
					secondaryTypes.add(metadata.allowedReferencesBuilder.getSchemaType());
					for (String schema : metadata.allowedReferencesBuilder.getSchemas()) {
						secondaryTypes.add(newSchemaUtils().getSchemaTypeCode(schema));
					}
				}
			}
			primaryTypesDependencies.put(metadataSchemaType.getCode(), primaryTypes);
			secondaryTypesDependencies.put(metadataSchemaType.getCode(), secondaryTypes);
		}
		try {
			MultiMapDependencyResults<String> results = newDependencyUtils().sortTwoLevelOfDependencies(
					primaryTypesDependencies, secondaryTypesDependencies, new DependencyUtilsParams());
			return results.getSortedElements();
		} catch (DependencyUtilsRuntimeException.CyclicDependency e) {
			throw new MetadataSchemaTypesBuilderRuntimeException.CyclicDependenciesInSchemas(e);
		}
	}

	SchemaUtils newSchemaUtils() {
		return new SchemaUtils();
	}

	DependencyUtils<String> newDependencyUtils() {
		return new DependencyUtils<>();
	}

	public Map<String, Set<String>> getTypesDependencies() {
		Map<String, Set<String>> typesDepencencies = new HashMap<>();
		for (MetadataSchemaTypeBuilder type : this.schemaTypes) {
			Set<String> dependencies = getSchemaDependenciesOf(type);
			if (!dependencies.isEmpty()) {
				typesDepencencies.put(type.getCode(), dependencies);
			}
		}
		return typesDepencencies;

	}

	public Set<String> getSchemaDependenciesOf(MetadataSchemaTypeBuilder type) {
		Set<String> otherSchemaTypesReferences = new HashSet<>();
		for (MetadataBuilder metadata : type.getAllMetadatas()) {
			if (metadata.getType() == REFERENCE) {
				otherSchemaTypesReferences.addAll(getSchemaTypeReferences(metadata));
			}
		}
		return otherSchemaTypesReferences;
	}

	private void add(Set<MetadataBuilder> metadatas, MetadataSchemaTypeBuilder schemaType) {
		for (MetadataSchemaBuilder schemaBuilder : schemaType.getAllSchemas()) {
			metadatas.addAll(schemaBuilder.getMetadatas());
		}
	}

	private void validateAutomaticMetadatas() {
		validateCopiedMetadatas();
		validateCalculedMetadatas();
	}

	private void validateCopiedMetadatas() {
		for (MetadataBuilder metadataBuilder : getAllCopiedMetadatas()) {
			if (!metadataBuilder.isMarkedForDeletion()) {
				CopiedDataEntry copiedDataEntry = (CopiedDataEntry) metadataBuilder.getDataEntry();
				String referenceMetadataCode = copiedDataEntry.getReferenceMetadata();
				MetadataBuilder referenceMetadata = getMetadata(referenceMetadataCode);
				String copiedMetadataCode = copiedDataEntry.getCopiedMetadata();
				MetadataBuilder copiedMetadata = getMetadata(copiedMetadataCode);

				referenceMetadata.markAsDependencyOfAutomaticMetadata();
				copiedMetadata.markAsDependencyOfAutomaticMetadata();

				validateCopiedMetadataMultiValues(metadataBuilder, referenceMetadataCode, referenceMetadata, copiedMetadataCode,
						copiedMetadata);
				validateCopiedMetadataType(metadataBuilder, copiedMetadata);
			}
		}
	}

	private void validateCalculedMetadatas() {
		for (MetadataBuilder metadataBuilder : getAllCalculatedMetadatas()) {
			CalculatedDataEntry calculatedDataEntry = (CalculatedDataEntry) metadataBuilder.getDataEntry();
			if (!(calculatedDataEntry.getCalculator() instanceof InitializedMetadataValueCalculator)) {
				validateCalculatedMultivalue(metadataBuilder, calculatedDataEntry);
				MetadataValueType valueTypeMetadataCalculated = calculatedDataEntry.getCalculator().getReturnType();
				List<? extends Dependency> dependencies = calculatedDataEntry.getCalculator().getDependencies();
				boolean needToBeInitialized = calculatedDataEntry.getCalculator() instanceof InitializedMetadataValueCalculator;
				if (!needToBeInitialized && (dependencies == null || dependencies.size() == 0)) {
					//					throw new MetadataSchemaTypesBuilderRuntimeException.NoDependenciesInCalculator(calculatedDataEntry
					//							.getCalculator().getClass().getName());
				}
				if (metadataBuilder.getType() != valueTypeMetadataCalculated) {
					throw new MetadataSchemaTypesBuilderRuntimeException.CannotCalculateDifferentValueTypeInValueMetadata(
							metadataBuilder.getCode(), metadataBuilder.getType(), valueTypeMetadataCalculated);
				}

				metadataBuilder.markAsDependencyOfAutomaticMetadata();
				try {
					validateDependenciesTypes(metadataBuilder, dependencies);
				} catch (MetadataSchemaBuilderRuntimeException.NoSuchMetadata e) {
					throw new MetadataSchemaTypesBuilderRuntimeException.CalculatorHasInvalidMetadataDependency(
							calculatedDataEntry.getCalculator().getClass(), metadataBuilder.getCode(), e.getMetadataCode(), e);
				}
			}
		}
	}

	private void validateCalculatedMultivalue(MetadataBuilder metadataBuilder,
											  CalculatedDataEntry calculatedDataEntry) {
		if (metadataBuilder.isMultivalue() && !calculatedDataEntry.getCalculator().isMultiValue()) {
			throw new MetadataSchemaTypesBuilderRuntimeException.CannotCalculateASingleValueInAMultiValueMetadata(
					metadataBuilder.getCode(), calculatedDataEntry.getCalculator().getClass().getName());
		} else if (!metadataBuilder.isMultivalue() && calculatedDataEntry.getCalculator().isMultiValue()) {
			throw new MetadataSchemaTypesBuilderRuntimeException.CannotCalculateAMultiValueInASingleValueMetadata(
					metadataBuilder.getCode(), calculatedDataEntry.getCalculator().getClass().getName());
		}
	}

	private void validateDependenciesTypes(MetadataBuilder metadataBuilder, List<? extends Dependency> dependencies) {
		for (Dependency dependency : dependencies) {
			if (dependency instanceof ReferenceDependency) {
				validateReferencedDependency(metadataBuilder, dependency);
			} else if (dependency instanceof LocalDependency) {
				validateLocalDependency(metadataBuilder, dependency);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private void validateLocalDependency(MetadataBuilder calculatedMetadataBuilder, Dependency dependency) {
		LocalDependency localDependency = (LocalDependency) dependency;
		String schemaCompleteCode = new SchemaUtils().getSchemaCode(calculatedMetadataBuilder);

		if (!((LocalDependency) dependency).isMetadataCreatedLater()) {
			MetadataBuilder dependencyMetadataBuilder = getMetadata(schemaCompleteCode + "_" + dependency.getLocalMetadataCode());
			dependencyMetadataBuilder.markAsDependencyOfAutomaticMetadata();
			if (dependencyMetadataBuilder.getType() != localDependency.getReturnType()) {
				throw new MetadataSchemaTypesBuilderRuntimeException.CalculatorDependencyHasInvalidValueType(
						calculatedMetadataBuilder.getCode(), dependencyMetadataBuilder.getCode(),
						dependencyMetadataBuilder.getType(),
						localDependency.getReturnType());
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private void validateReferencedDependency(MetadataBuilder calculatedMetadataBuilder, Dependency dependency) {

		ReferenceDependency referenceDependency = (ReferenceDependency) dependency;
		String schemaCompleteCode = new SchemaUtils().getSchemaCode(calculatedMetadataBuilder);

		if (!((ReferenceDependency) dependency).isMetadataCreatedLater()) {
			MetadataBuilder dependencyRefMetadataBuilder = getMetadata(
					schemaCompleteCode + "_" + dependency.getLocalMetadataCode());
			dependencyRefMetadataBuilder.markAsDependencyOfAutomaticMetadata();
			if (dependencyRefMetadataBuilder.getAllowedReferencesBuider() != null) {
				String dependencyMetaCompleteCode = dependencyRefMetadataBuilder.getAllowedReferencesBuider()
						.getMetadataCompleteCode(referenceDependency.getDependentMetadataCode());
				MetadataBuilder dependencyMetadata;
				try {
					dependencyMetadata = getMetadata(dependencyMetaCompleteCode);
					dependencyMetadata.markAsDependencyOfAutomaticMetadata();
				} catch (MetadataSchemaBuilderRuntimeException e) {
					throw new MetadataSchemaTypesBuilderRuntimeException.InvalidDependencyMetadata(dependencyMetaCompleteCode, e);
				}
				if (dependencyMetadata.getType() != referenceDependency.getReturnType()
					|| dependencyRefMetadataBuilder.getType() != MetadataValueType.REFERENCE) {
					throw new MetadataSchemaTypesBuilderRuntimeException.CalculatorDependencyHasInvalidValueType(
							calculatedMetadataBuilder.getCode(), dependencyMetadata.getCode(), dependencyMetadata.getType(),
							referenceDependency.getReturnType());
				} else if (!dependencyMetadata.getCode().contains(DEFAULT)) {
					throw new MetadataSchemaTypesBuilderRuntimeException.CannotUseACustomMetadataForCalculation(
							dependencyMetadata.getCode());
				}
			} else {
				throw new MetadataSchemaTypesBuilderRuntimeException.NoAllowedReferences(
						dependencyRefMetadataBuilder.getCode());
			}
		}
	}

	private void validateCopiedMetadataType(MetadataBuilder metadataBuilder, MetadataBuilder copiedMetadata) {
		if (metadataBuilder.getType() != copiedMetadata.getType()) {
			throw new MetadataSchemaTypesBuilderRuntimeException.CannotCopyADifferentTypeInMetadata(
					metadataBuilder.getCode(), metadataBuilder.getType().name(), copiedMetadata.getCode(),
					copiedMetadata.getType().name());
		} else if (!copiedMetadata.getCode().contains(DEFAULT)) {
			throw new MetadataSchemaTypesBuilderRuntimeException.CannotCopyACustomMetadata(copiedMetadata.getCode());
		}
	}

	private void validateCopiedMetadataMultiValues(MetadataBuilder metadataBuilder, String referenceMetadataCode,
												   MetadataBuilder referenceMetadata, String copiedMetadataCode,
												   MetadataBuilder copiedMetadata) {

		if (!metadataBuilder.isMultivalue() && (referenceMetadata.isMultivalue() || copiedMetadata.isMultivalue())) {
			throw new MetadataSchemaTypesBuilderRuntimeException.CannotCopyMultiValueInSingleValueMetadata(
					metadataBuilder.getCode(), referenceMetadataCode, copiedMetadataCode);
		} else if (metadataBuilder.isMultivalue() && !referenceMetadata.isMultivalue() && !copiedMetadata.isMultivalue()) {
			throw new MetadataSchemaTypesBuilderRuntimeException.CannotCopySingleValueInMultiValueMetadata(
					metadataBuilder.getCode(), referenceMetadataCode, copiedMetadataCode);
		}
	}

	private Set<String> getSchemaTypeReferences(MetadataBuilder metadata) {
		Set<String> schemas = new HashSet<>();
		for (String schemaCode : metadata.allowedReferencesBuilder.getSchemas()) {
			schemas.add(schemaCode.split("_")[0]);
		}
		if (metadata.allowedReferencesBuilder.getSchemaType() != null) {
			schemas.add(metadata.allowedReferencesBuilder.getSchemaType());
		}
		return schemas;
	}

	public String getCollection() {
		return collectionInfo.getCode();
	}

	public void deleteSchemaType(MetadataSchemaType type, SearchServices searchServices) {
		if (searchServices.hasResults(from(type).returnAll())) {
			throw new CannotDeleteSchemaTypeSinceItHasRecords(type.getCode());
		} else {
			try {
				schemaTypes.remove(getSchemaType(type.getCode()));
			} catch (MetadataSchemaTypesBuilderRuntimeException.NoSuchSchemaType e) {
				//OK
			}
		}
	}

	public ClassProvider getClassProvider() {
		return classProvider;
	}

	public void setVersion(int version) {
		this.version = version;
	}


	short nextSchemaTypeId() {
		if (schemasTypeIdSequence == null) {
			schemasTypeIdSequence = new SchemasIdSequence();
			for (MetadataSchemaTypeBuilder schemaTypeBuilder : getTypes()) {
				schemasTypeIdSequence.markAsAssigned(schemaTypeBuilder.getId());
			}
		}
		return schemasTypeIdSequence.getNewId();
	}

	public List<String> getTypesRequiringCacheReload() {
		List<String> typesRequiringCacheReload = new ArrayList<>();

		for(MetadataSchemaTypeBuilder typeBuilder : schemaTypes) {
			if (typeBuilder.isRequiringCacheReload()) {
				typesRequiringCacheReload.add(typeBuilder.getCode());
			}
		}

		return typesRequiringCacheReload;
	}
}
