package com.constellio.model.services.records;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.DynamicLocalDependency;
import com.constellio.model.entities.calculators.dependencies.HierarchyDependencyValue;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.TransactionRecordsReindexation;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataTransiency;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.AggregatedDataEntry;
import com.constellio.model.entities.schemas.entries.AggregationType;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerLogger;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.taxonomies.TaxonomiesManager;

public class RecordAutomaticMetadataServices {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecordAutomaticMetadataServices.class);

	private final ModelLayerLogger modelLayerLogger;
	private final MetadataSchemasManager schemasManager;
	private final TaxonomiesManager taxonomiesManager;
	private final SystemConfigurationsManager systemConfigurationsManager;
	private final SearchServices searchServices;

	public RecordAutomaticMetadataServices(MetadataSchemasManager schemasManager, TaxonomiesManager taxonomiesManager,
			SystemConfigurationsManager systemConfigurationsManager, ModelLayerLogger modelLayerLogger,
			SearchServices searchServices) {
		super();
		this.modelLayerLogger = modelLayerLogger;
		this.schemasManager = schemasManager;
		this.taxonomiesManager = taxonomiesManager;
		this.systemConfigurationsManager = systemConfigurationsManager;
		this.searchServices = searchServices;
	}

	public void updateAutomaticMetadatas(RecordImpl record, RecordProvider recordProvider,
			TransactionRecordsReindexation reindexation, RecordUpdateOptions options) {
		MetadataSchemaTypes types = schemasManager.getSchemaTypes(record.getCollection());
		MetadataSchema schema = types.getSchema(record.getSchemaCode());
		for (Metadata automaticMetadata : schema.getAutomaticMetadatas()) {
			updateAutomaticMetadata(record, recordProvider, automaticMetadata, reindexation, types, options);
		}

	}

	public void loadTransientEagerMetadatas(RecordImpl record, RecordProvider recordProvider, RecordUpdateOptions options) {
		TransactionRecordsReindexation reindexation = TransactionRecordsReindexation.ALL();
		MetadataSchemaTypes types = schemasManager.getSchemaTypes(record.getCollection());
		MetadataSchema schema = types.getSchema(record.getSchemaCode());
		for (Metadata automaticMetadata : schema.getEagerTransientMetadatas()) {
			updateAutomaticMetadata(record, recordProvider, automaticMetadata, reindexation, types, options);
		}

	}

	public void loadTransientLazyMetadatas(RecordImpl record, RecordProvider recordProvider, RecordUpdateOptions options) {
		TransactionRecordsReindexation reindexation = TransactionRecordsReindexation.ALL();
		MetadataSchemaTypes types = schemasManager.getSchemaTypes(record.getCollection());
		MetadataSchema schema = types.getSchema(record.getSchemaCode());
		for (Metadata automaticMetadata : schema.getLazyTransientMetadatas()) {
			updateAutomaticMetadata(record, recordProvider, automaticMetadata, reindexation, types, options);
		}

	}

	void updateAutomaticMetadata(RecordImpl record, RecordProvider recordProvider, Metadata metadata,
			TransactionRecordsReindexation reindexation, MetadataSchemaTypes types, RecordUpdateOptions options) {
		if (metadata.isMarkedForDeletion()) {
			record.updateAutomaticValue(metadata, null);

		} else if (metadata.getDataEntry().getType() == DataEntryType.COPIED) {
			setCopiedValuesInRecords(record, metadata, recordProvider, reindexation, options);

		} else if (metadata.getDataEntry().getType() == DataEntryType.CALCULATED) {
			setCalculatedValuesInRecords(record, metadata, recordProvider, reindexation, types, options);

		} else if (metadata.getDataEntry().getType() == DataEntryType.AGGREGATED) {
			//We don't want to calculate this metadata during record imports
			if (record.get(Schemas.LEGACY_ID) == null || record.isSaved()) {
				setAggregatedValuesInRecords(record, metadata, recordProvider, reindexation, types);
			}

		}
	}

	private void setAggregatedValuesInRecords(RecordImpl record, Metadata metadata, RecordProvider recordProvider,
			TransactionRecordsReindexation reindexation, MetadataSchemaTypes types) {

		AggregatedDataEntry aggregatedDataEntry = (AggregatedDataEntry) metadata.getDataEntry();

		Metadata referenceMetadata = types.getMetadata(aggregatedDataEntry.getReferenceMetadata());
		MetadataSchemaType schemaType = types.getSchemaType(new SchemaUtils().getSchemaTypeCode(referenceMetadata));
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(from(schemaType).where(referenceMetadata).isEqualTo(record));

		if (aggregatedDataEntry.getAgregationType() == AggregationType.SUM) {

			Metadata inputMetadata = types.getMetadata(aggregatedDataEntry.getInputMetadata());
			query.computeStatsOnField(inputMetadata);
			query.setNumberOfRows(0);
			SPEQueryResponse response = searchServices.query(query);

			if (aggregatedDataEntry.getAgregationType() == AggregationType.SUM) {
				Map<String, Object> statsValues = response.getStatValues(inputMetadata);
				Double sum = statsValues == null ? 0.0 : (Double) response.getStatValues(inputMetadata).get("sum");
				((RecordImpl) record).updateAutomaticValue(metadata, sum);

			} else {
				throw new ImpossibleRuntimeException("Unsupported aggregation type : " + aggregatedDataEntry.getAgregationType());
			}
		} else if (aggregatedDataEntry.getAgregationType() == AggregationType.REFERENCE_COUNT) {
			Double childrenCount = new Double(searchServices.getResultsCount(query));
			((RecordImpl) record).updateAutomaticValue(metadata, childrenCount);
		}

	}

	void setCopiedValuesInRecords(RecordImpl record, Metadata metadataWithCopyDataEntry, RecordProvider recordProvider,
			TransactionRecordsReindexation reindexation, RecordUpdateOptions options) {

		CopiedDataEntry copiedDataEntry = (CopiedDataEntry) metadataWithCopyDataEntry.getDataEntry();
		Metadata referenceMetadata = schemasManager.getSchemaTypes(record.getCollection())
				.getMetadata(copiedDataEntry.getReferenceMetadata());
		Object referenceValue = record.get(referenceMetadata);
		Map<String, Object> modifiedValues = record.getModifiedValues();
		boolean isReferenceModified = modifiedValues.containsKey(referenceMetadata.getDataStoreCode());
		boolean forcedReindexation = reindexation.isReindexed(metadataWithCopyDataEntry);
		boolean inTransaction = recordProvider.hasRecordInMemoryList(referenceValue);
		if (isReferenceModified || forcedReindexation || inTransaction) {
			Metadata copiedMetadata = schemasManager.getSchemaTypes(record.getCollection())
					.getMetadata(copiedDataEntry.getCopiedMetadata());

			copyValueInRecord(record, metadataWithCopyDataEntry, recordProvider, referenceMetadata, copiedMetadata, options);
		}
	}

	boolean calculatorDependencyModified(RecordImpl record, MetadataValueCalculator<?> calculator, MetadataSchemaTypes types,
			Metadata calculatedMetadata) {
		boolean calculatorDependencyModified = !record.isSaved();
		for (Dependency dependency : calculator.getDependencies()) {
			if (SpecialDependencies.HIERARCHY.equals(dependency)) {
				calculatorDependencyModified = true;

			} else if (SpecialDependencies.IDENTIFIER.equals(dependency)) {
				calculatorDependencyModified = true;

			} else if (SpecialDependencies.PRINCIPAL_TAXONOMY_CODE.equals(dependency)) {
				calculatorDependencyModified = true;

			} else if (dependency instanceof DynamicLocalDependency) {
				DynamicLocalDependency dynamicLocalDependency = (DynamicLocalDependency) dependency;
				for (Metadata metadata : record.getModifiedMetadatas(types)) {
					if (new SchemaUtils().isDependentMetadata(calculatedMetadata, metadata, dynamicLocalDependency)) {
						calculatorDependencyModified = true;
						break;
					}
				}

			} else if (!(dependency instanceof ConfigDependency)) {
				Metadata localMetadata = getMetadataFromDependency(record, dependency);
				if (record.isModified(localMetadata)) {
					calculatorDependencyModified = true;
				}
			}
		}
		return calculatorDependencyModified;
	}

	void calculateValueInRecord(RecordImpl record, Metadata metadataWithCalculatedDataEntry, RecordProvider recordProvider,
			MetadataSchemaTypes types, RecordUpdateOptions options) {
		MetadataValueCalculator<?> calculator = getCalculatorFrom(metadataWithCalculatedDataEntry);
		Map<Dependency, Object> values = new HashMap<>();
		boolean requiredValuesDefined = addValuesFromDependencies(record, metadataWithCalculatedDataEntry, recordProvider,
				calculator, values, types, options);

		Object calculatedValue;
		if (requiredValuesDefined) {
			modelLayerLogger.logCalculatedValue(record, calculator, values);
			calculatedValue = calculator.calculate(
					new CalculatorParameters(values, record.getId(), record.<String>get(Schemas.LEGACY_ID),
							types.getSchemaType(record.getTypeCode()), record.getCollection()));
		} else {
			calculatedValue = calculator.getDefaultValue();
		}
		record.updateAutomaticValue(metadataWithCalculatedDataEntry, calculatedValue);
	}

	MetadataValueCalculator<?> getCalculatorFrom(Metadata metadataWithCalculatedDataEntry) {
		CalculatedDataEntry calculatedDataEntry = (CalculatedDataEntry) metadataWithCalculatedDataEntry.getDataEntry();
		return calculatedDataEntry.getCalculator();
	}

	boolean addValuesFromDependencies(RecordImpl record, Metadata metadata, RecordProvider recordProvider,
			MetadataValueCalculator<?> calculator,
			Map<Dependency, Object> values, MetadataSchemaTypes types, RecordUpdateOptions options) {
		for (Dependency dependency : calculator.getDependencies()) {
			if (dependency instanceof LocalDependency<?>) {
				if (!addValueForLocalDependency(record, values, dependency)) {
					return false;
				}

			} else if (dependency instanceof ReferenceDependency<?>) {
				if (!addValueForReferenceDependency(record, recordProvider, values, dependency, options)) {
					return false;
				}

			} else if (dependency instanceof DynamicLocalDependency) {
				addValueForDynamicLocalDependency(record, metadata, values, (DynamicLocalDependency) dependency, types,
						recordProvider, options);

			} else if (dependency instanceof ConfigDependency<?>) {
				ConfigDependency<?> configDependency = (ConfigDependency<?>) dependency;
				Object configValue = systemConfigurationsManager.getValue(configDependency.getConfiguration());
				values.put(dependency, configValue);

			} else if (dependency instanceof SpecialDependency<?>) {
				addValuesFromSpecialDependencies(record, recordProvider, values, dependency);
			}
		}
		return true;
	}

	private void addValueForDynamicLocalDependency(RecordImpl record, Metadata calculatedMetadata,
			Map<Dependency, Object> values, DynamicLocalDependency dependency, MetadataSchemaTypes types,
			RecordProvider recordProvider, RecordUpdateOptions options) {

		Map<String, Object> dynamicDependencyValues = new HashMap<>();

		MetadataList availableMetadatas = new MetadataList();
		MetadataList availableMetadatasWithValue = new MetadataList();
		for (Metadata metadata : types.getSchema(record.getSchemaCode()).getMetadatas()) {

			if (metadata.getTransiency() == MetadataTransiency.TRANSIENT_LAZY
					&& record.getLazyTransientValues().isEmpty()) {
				loadTransientLazyMetadatas(record, recordProvider, options);
			}

			if (new SchemaUtils().isDependentMetadata(calculatedMetadata, metadata, dependency)) {
				availableMetadatas.add(metadata);
				if (metadata.isMultivalue()) {
					List<?> metadataValues = record.getList(metadata);
					dynamicDependencyValues.put(metadata.getLocalCode(), metadataValues);
					if (!metadataValues.isEmpty()) {
						availableMetadatasWithValue.add(metadata);
					}
				} else {
					Object metadataValue = record.get(metadata);
					dynamicDependencyValues.put(metadata.getLocalCode(), metadataValue);
					if (metadataValue != null) {
						availableMetadatasWithValue.add(metadata);
					}
				}
			}
		}
		MetadataValueCalculator<?> calculator = ((CalculatedDataEntry) calculatedMetadata.getDataEntry()).getCalculator();
		values.put(dependency, new DynamicDependencyValues(calculator, dynamicDependencyValues, availableMetadatas.unModifiable(),
				availableMetadatasWithValue.unModifiable()));

	}

	void addValuesFromSpecialDependencies(RecordImpl record, RecordProvider recordProvider,
			Map<Dependency, Object> values, Dependency dependency) {
		if (SpecialDependencies.HIERARCHY.equals(dependency)) {
			addValueForTaxonomyDependency(record, recordProvider, values, dependency);

		} else if (SpecialDependencies.IDENTIFIER.equals(dependency)) {
			values.put(dependency, record.getId());
		} else if (SpecialDependencies.PRINCIPAL_TAXONOMY_CODE.equals(dependency)) {
			Taxonomy principalTaxonomy = taxonomiesManager.getPrincipalTaxonomy(record.getCollection());
			if (principalTaxonomy != null) {
				values.put(dependency, principalTaxonomy.getCode());
			}
		}
	}

	boolean addValueForReferenceDependency(RecordImpl record, RecordProvider recordProvider, Map<Dependency, Object> values,
			Dependency dependency, RecordUpdateOptions options) {
		ReferenceDependency<?> referenceDependency = (ReferenceDependency<?>) dependency;
		Metadata referenceMetadata = getMetadataFromDependency(record, referenceDependency);

		if (!referenceMetadata.isMultivalue()) {
			return addSingleValueReference(record, recordProvider, values, referenceDependency, referenceMetadata, options);
		} else {
			return addMultivalueReference(record, recordProvider, values, referenceDependency, referenceMetadata, options);
		}
	}

	boolean addValueForTaxonomyDependency(RecordImpl record, RecordProvider recordProvider, Map<Dependency, Object> values,
			Dependency dependency) {

		String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
		Taxonomy taxonomy = taxonomiesManager.getTaxonomyFor(record.getCollection(), schemaTypeCode);

		List<String> paths = new ArrayList<>();
		List<String> removedAuthorizations = new ArrayList<>();
		List<String> attachedAncestors = new ArrayList<>();
		MetadataSchema recordSchema = schemasManager.getSchemaTypes(record.getCollection()).getSchema(record.getSchemaCode());
		List<Metadata> parentReferences = recordSchema.getParentReferences();
		for (Metadata metadata : parentReferences) {
			String referenceValue = record.get(metadata);
			if (referenceValue != null) {
				Record referencedRecord = recordProvider.getRecord(referenceValue);
				List<String> parentPaths = referencedRecord.getList(Schemas.PATH);
				paths.addAll(parentPaths);
				removedAuthorizations.addAll(referencedRecord.<String>getList(Schemas.ALL_REMOVED_AUTHS));
				attachedAncestors.addAll(referencedRecord.<String>getList(Schemas.ATTACHED_ANCESTORS));
			}
		}
		for (Taxonomy aTaxonomy : taxonomiesManager.getEnabledTaxonomies(record.getCollection())) {
			for (Metadata metadata : recordSchema.getTaxonomyRelationshipReferences(aTaxonomy)) {
				List<String> referencesValues = new ArrayList<>();
				if (metadata.isMultivalue()) {
					referencesValues.addAll(record.<String>getList(metadata));
				} else {
					String referenceValue = record.get(metadata);
					if (referenceValue != null) {
						referencesValues.add(referenceValue);
					}
				}
				for (String referenceValue : referencesValues) {
					if (referenceValue != null) {
						try {
							Record referencedRecord = recordProvider.getRecord(referenceValue);
							List<String> parentPaths = referencedRecord.getList(Schemas.PATH);
							paths.addAll(parentPaths);
							removedAuthorizations.addAll(referencedRecord.<String>getList(Schemas.ALL_REMOVED_AUTHS));
							if (aTaxonomy.hasSameCode(taxonomiesManager.getPrincipalTaxonomy(record.getCollection()))) {
								attachedAncestors.addAll(referencedRecord.<String>getList(Schemas.ATTACHED_ANCESTORS));
							}
						} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		HierarchyDependencyValue value = new HierarchyDependencyValue(taxonomy, paths, removedAuthorizations,
				attachedAncestors);
		values.put(dependency, value);
		return true;
	}

	@SuppressWarnings("unchecked")
	private boolean addMultivalueReference(RecordImpl record, RecordProvider recordProvider, Map<Dependency, Object> values,
			ReferenceDependency<?> referenceDependency, Metadata referenceMetadata, RecordUpdateOptions options) {
		List<String> referencesValues = record.<String>getList(referenceMetadata);
		List<Record> referencedRecords = new ArrayList<>();
		for (String referenceValue : referencesValues) {
			if (referenceValue != null) {
				try {
					referencedRecords.add(recordProvider.getRecord(referenceValue));
				} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
					RuntimeException brokenReferenceException = new RecordServicesRuntimeException.BrokenReference(
							record.getId(), referenceValue, referenceMetadata, e);
					if (options.isCatchBrokenReferenceErrors()) {
						LOGGER.warn("Broken reference while calculating automatic metadata", brokenReferenceException);
					} else {
						throw brokenReferenceException;
					}
				}
			}
		}
		List<Object> referencedValues = new ArrayList<>();
		SortedMap<String, Object> referencedValuesMap = new TreeMap<>();
		for (Record referencedRecord : referencedRecords) {
			Metadata dependentMetadata = getDependentMetadataFromDependency(referenceDependency, referencedRecord);
			Object dependencyValue = referencedRecord.get(dependentMetadata);
			if (referenceDependency.isRequired() && dependencyValue == null) {
				return false;

			} else if (referenceDependency.isGroupedByReference()) {
				referencedValuesMap.put(referencedRecord.getId(), dependencyValue);

			} else if (dependencyValue instanceof List) {
				referencedValues.addAll((List) dependencyValue);

			} else {
				referencedValues.add(dependencyValue);
			}
		}

		if (referenceDependency.isGroupedByReference()) {
			values.put(referenceDependency, referencedValuesMap);
		} else {
			values.put(referenceDependency, referencedValues);
		}

		return true;
	}

	private boolean addSingleValueReference(RecordImpl record, RecordProvider recordProvider, Map<Dependency, Object> values,
			ReferenceDependency<?> dependency, Metadata referenceMetadata, RecordUpdateOptions options) {
		String referenceValue = (String) record.get(referenceMetadata);
		Record referencedRecord;
		if (dependency.isRequired() && referenceValue == null) {
			return false;
		} else {
			try {
				referencedRecord = referenceValue == null ? null : recordProvider.getRecord(referenceValue);
			} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
				RuntimeException brokenReferenceException = new RecordServicesRuntimeException.BrokenReference(
						record.getId(), referenceValue, referenceMetadata, e);
				if (options.isCatchBrokenReferenceErrors()) {
					LOGGER.warn("Broken reference while calculating automatic metadata", brokenReferenceException);
					referencedRecord = null;
				} else {
					throw brokenReferenceException;
				}
			}
		}

		Object dependencyValue;
		if (referencedRecord != null) {
			Metadata dependentMetadata = getDependentMetadataFromDependency(dependency, referencedRecord);
			dependencyValue = referencedRecord.get(dependentMetadata);
		} else if (dependency.isMultivalue()) {
			dependencyValue = new ArrayList<>();
		} else {
			dependencyValue = null;
		}
		if (dependency.isRequired() && dependencyValue == null) {
			return false;
		} else {
			values.put(dependency, dependencyValue);
		}
		return true;
	}

	Metadata getDependentMetadataFromDependency(ReferenceDependency<?> referenceDependency, Record referencedRecord) {
		MetadataSchema schema = schemasManager.getSchemaTypes(referencedRecord.getCollection())
				.getSchema(referencedRecord.getSchemaCode());
		return schema.get(referenceDependency.getDependentMetadataCode());
	}

	boolean addValueForLocalDependency(RecordImpl record, Map<Dependency, Object> values, Dependency dependency) {
		Metadata metadata = getMetadataFromDependency(record, dependency);
		Object dependencyValue = record.get(metadata);
		if (dependency.isRequired() && dependencyValue == null) {
			return false;
		} else {
			values.put(dependency, dependencyValue);
		}
		return true;
	}

	Metadata getMetadataFromDependency(RecordImpl record, Dependency dependency) {
		MetadataSchema schema = schemasManager.getSchemaTypes(record.getCollection()).getSchema(record.getSchemaCode());
		return schema.get(dependency.getLocalMetadataCode());
	}

	void copyValueInRecord(RecordImpl record, Metadata metadataWithCopyDataEntry, RecordProvider recordProvider,
			Metadata referenceMetadata, Metadata copiedMetadata, RecordUpdateOptions options) {

		if (referenceMetadata.isMultivalue()) {
			List<String> referencedRecordIds = record.getList(referenceMetadata);
			if (referencedRecordIds == null || referencedRecordIds.isEmpty()) {
				record.updateAutomaticValue(metadataWithCopyDataEntry, Collections.emptyList());
			} else {
				copyReferenceValueInRecord(record, metadataWithCopyDataEntry, recordProvider, copiedMetadata,
						referencedRecordIds, referenceMetadata, options);
			}
		} else {
			String referencedRecordId = record.get(referenceMetadata);
			if (referencedRecordId == null) {
				record.updateAutomaticValue(metadataWithCopyDataEntry, null);
			} else {
				copyReferenceValueInRecord(record, metadataWithCopyDataEntry, recordProvider, copiedMetadata, referencedRecordId,
						referenceMetadata, options);
			}
		}

	}

	void copyReferenceValueInRecord(RecordImpl record, Metadata metadataWithCopyDataEntry, RecordProvider recordProvider,
			Metadata copiedMetadata, String referencedRecordId, Metadata referenceMetadata, RecordUpdateOptions options) {
		Object copiedValue;
		try {
			Record referencedRecord = recordProvider.getRecord(referencedRecordId);
			copiedValue = referencedRecord.get(copiedMetadata);

		} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
			RuntimeException brokenReferenceException = new RecordServicesRuntimeException.BrokenReference(
					record.getId(), referencedRecordId, referenceMetadata, e);
			if (options.isCatchBrokenReferenceErrors()) {
				LOGGER.warn("Broken reference while calculating automatic metadata", brokenReferenceException);
				copiedValue = null;
			} else {
				throw brokenReferenceException;
			}
		}
		record.updateAutomaticValue(metadataWithCopyDataEntry, copiedValue);
	}

	void copyReferenceValueInRecord(RecordImpl record, Metadata metadataWithCopyDataEntry, RecordProvider recordProvider,
			Metadata copiedMetadata, List<String> referencedRecordIds, Metadata referenceMetadata, RecordUpdateOptions options) {
		List<Object> values = new ArrayList<>();
		for (String referencedRecordId : referencedRecordIds) {
			if (referencedRecordId != null) {
				try {
					RecordImpl referencedRecord = (RecordImpl) recordProvider.getRecord(referencedRecordId);

					if (copiedMetadata.isMultivalue()) {
						values.addAll(referencedRecord.getList(copiedMetadata));
					} else {
						Object value = referencedRecord.get(copiedMetadata);
						if (value != null) {
							values.add(value);
						}
					}
				} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
					RuntimeException brokenReferenceException = new RecordServicesRuntimeException.BrokenReference(
							record.getId(), referencedRecordId, referenceMetadata, e);
					if (options.isCatchBrokenReferenceErrors()) {
						LOGGER.warn("Broken reference while calculating automatic metadata", brokenReferenceException);
					} else {
						throw brokenReferenceException;
					}
				}
			}

		}
		record.updateAutomaticValue(metadataWithCopyDataEntry, values);
	}

	public List<Metadata> sortMetadatasUsingLocalDependencies(Map<Metadata, Set<String>> metadatasWithLocalCodeDependencies) {
		List<Metadata> sortedMetadatas = new ArrayList<>();
		Map<Metadata, Set<String>> metadatas = copyInModifiableMap(metadatasWithLocalCodeDependencies);
		while (!metadatas.isEmpty()) {
			Metadata nextMetadata = getAMetadataWithoutDependencies(metadatas);
			if (nextMetadata == null) {
				throw new ImpossibleRuntimeException("Cyclic dependency");
			}
			metadatas.remove(nextMetadata);
			for (Map.Entry<Metadata, Set<String>> otherMetadataEntry : metadatas.entrySet()) {
				otherMetadataEntry.getValue().remove(nextMetadata.getLocalCode());
			}
			sortedMetadatas.add(nextMetadata);
		}
		return sortedMetadatas;
	}

	private Map<Metadata, Set<String>> copyInModifiableMap(Map<Metadata, Set<String>> metadatasWithLocalCodeDependencies) {
		Map<Metadata, Set<String>> metadatas = new HashMap<>();
		for (Map.Entry<Metadata, Set<String>> entry : metadatasWithLocalCodeDependencies.entrySet()) {
			metadatas.put(entry.getKey(), new HashSet<String>(entry.getValue()));
		}
		return metadatas;
	}

	private Metadata getAMetadataWithoutDependencies(Map<Metadata, Set<String>> metadatas) {
		Metadata nextMetadata = null;
		for (Map.Entry<Metadata, Set<String>> entry : metadatas.entrySet()) {
			if (entry.getValue().isEmpty()) {
				nextMetadata = entry.getKey();
				break;
			}
		}
		return nextMetadata;
	}

	void setCalculatedValuesInRecords(RecordImpl record, Metadata metadataWithCalculatedDataEntry, RecordProvider recordProvider,
			TransactionRecordsReindexation reindexation, MetadataSchemaTypes types, RecordUpdateOptions options) {

		MetadataValueCalculator<?> calculator = getCalculatorFrom(metadataWithCalculatedDataEntry);

		boolean lazyTransientMetadataToLoad = metadataWithCalculatedDataEntry.getTransiency() == MetadataTransiency.TRANSIENT_LAZY
				&& !record.getLazyTransientValues().containsKey(metadataWithCalculatedDataEntry.getDataStoreCode());

		if (calculatorDependencyModified(record, calculator, types, metadataWithCalculatedDataEntry)
				|| reindexation.isReindexed(metadataWithCalculatedDataEntry)
				|| lazyTransientMetadataToLoad) {
			calculateValueInRecord(record, metadataWithCalculatedDataEntry, recordProvider, types, options);
		}
	}

}
