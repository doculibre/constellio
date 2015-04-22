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
package com.constellio.model.services.records;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.HierarchyDependencyValue;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.TransactionRecordsReindexation;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.taxonomies.TaxonomiesManager;

public class RecordAutomaticMetadataServices {

	private final MetadataSchemasManager schemasManager;
	private final TaxonomiesManager taxonomiesManager;
	private final SystemConfigurationsManager systemConfigurationsManager;

	public RecordAutomaticMetadataServices(MetadataSchemasManager schemasManager, TaxonomiesManager taxonomiesManager,
			SystemConfigurationsManager systemConfigurationsManager) {
		super();
		this.schemasManager = schemasManager;
		this.taxonomiesManager = taxonomiesManager;
		this.systemConfigurationsManager = systemConfigurationsManager;
	}

	public void updateAutomaticMetadatas(RecordImpl record, RecordProvider recordProvider,
			TransactionRecordsReindexation reindexation) {

		MetadataSchema schema = schemasManager.getSchemaTypes(record.getCollection()).getSchema(record.getSchemaCode());
		for (Metadata automaticMetadata : schema.getAutomaticMetadatas()) {
			updateAutomaticMetadata(record, recordProvider, automaticMetadata, reindexation);
		}

	}

	void updateAutomaticMetadata(RecordImpl record, RecordProvider recordProvider, Metadata metadata,
			TransactionRecordsReindexation reindexation) {
		if (metadata.getDataEntry().getType() == DataEntryType.COPIED) {
			setCopiedValuesInRecords(record, metadata, recordProvider, reindexation);
		} else if (metadata.getDataEntry().getType() == DataEntryType.CALCULATED) {
			setCalculatedValuesInRecords(record, metadata, recordProvider, reindexation);
		}
	}

	void setCopiedValuesInRecords(RecordImpl record, Metadata metadataWithCopyDataEntry, RecordProvider recordProvider,
			TransactionRecordsReindexation reindexation) {

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
			copyValueInRecord(record, metadataWithCopyDataEntry, recordProvider, referenceMetadata, copiedMetadata);
		}
	}

	boolean calculatorDependencyModified(RecordImpl record, MetadataValueCalculator<?> calculator) {
		boolean calculatorDependencyModified = !record.isSaved();
		for (Dependency dependency : calculator.getDependencies()) {
			if (dependency == SpecialDependencies.HIERARCHY) {
				calculatorDependencyModified = true;
			} else if (dependency == SpecialDependencies.IDENTIFIER) {
				calculatorDependencyModified = true;
			} else if (dependency == SpecialDependencies.PRINCIPAL_TAXONOMY_CODE) {
				calculatorDependencyModified = true;
			} else if (!(dependency instanceof ConfigDependency)) {
				Metadata localMetadata = getMetadataFromDependency(record, dependency);
				if (record.isModified(localMetadata)) {
					calculatorDependencyModified = true;
				}
			}
		}
		return calculatorDependencyModified;
	}

	void calculateValueInRecord(RecordImpl record, Metadata metadataWithCalculatedDataEntry, RecordProvider recordProvider) {
		MetadataValueCalculator<?> calculator = getCalculatorFrom(metadataWithCalculatedDataEntry);
		Map<Dependency, Object> values = new HashMap<>();
		boolean requiredValuesDefined = addValuesFromDependencies(record, recordProvider, calculator, values);

		Object calculatedValue;
		if (requiredValuesDefined) {
			calculatedValue = calculator.calculate(new CalculatorParameters(values, record.getCollection()));
		} else {
			calculatedValue = calculator.getDefaultValue();
		}
		record.updateAutomaticValue(metadataWithCalculatedDataEntry, calculatedValue);
	}

	MetadataValueCalculator<?> getCalculatorFrom(Metadata metadataWithCalculatedDataEntry) {
		CalculatedDataEntry calculatedDataEntry = (CalculatedDataEntry) metadataWithCalculatedDataEntry.getDataEntry();
		return calculatedDataEntry.getCalculator();
	}

	boolean addValuesFromDependencies(RecordImpl record, RecordProvider recordProvider, MetadataValueCalculator<?> calculator,
			Map<Dependency, Object> values) {
		for (Dependency dependency : calculator.getDependencies()) {
			if (dependency instanceof LocalDependency<?>) {
				if (!addValueForLocalDependency(record, values, dependency)) {
					return false;
				}

			} else if (dependency instanceof ReferenceDependency<?>) {
				if (!addValueForReferenceDependency(record, recordProvider, values, dependency)) {
					return false;
				}

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

	void addValuesFromSpecialDependencies(RecordImpl record, RecordProvider recordProvider,
			Map<Dependency, Object> values, Dependency dependency) {
		if (dependency == SpecialDependencies.HIERARCHY) {
			addValueForTaxonomyDependency(record, recordProvider, values, dependency);

		} else if (dependency == SpecialDependencies.IDENTIFIER) {
			values.put(dependency, record.getId());
		} else if (dependency == SpecialDependencies.PRINCIPAL_TAXONOMY_CODE) {
			Taxonomy principalTaxonomy = taxonomiesManager.getPrincipalTaxonomy(record.getCollection());
			if (principalTaxonomy != null) {
				values.put(dependency, principalTaxonomy.getCode());
			}
		}
	}

	boolean addValueForReferenceDependency(RecordImpl record, RecordProvider recordProvider, Map<Dependency, Object> values,
			Dependency dependency) {
		ReferenceDependency<?> referenceDependency = (ReferenceDependency<?>) dependency;
		Metadata referenceMetadata = getMetadataFromDependency(record, referenceDependency);
		if (!referenceMetadata.isMultivalue()) {
			return addSingleValueReference(record, recordProvider, values, referenceDependency, referenceMetadata);
		} else {
			return addMultivalueReference(record, recordProvider, values, referenceDependency, referenceMetadata);
		}
	}

	boolean addValueForTaxonomyDependency(RecordImpl record, RecordProvider recordProvider, Map<Dependency, Object> values,
			Dependency dependency) {

		String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
		Taxonomy taxonomy = taxonomiesManager.getTaxonomyFor(record.getCollection(), schemaTypeCode);

		List<String> paths = new ArrayList<>();
		List<String> authorizations = new ArrayList<>();
		MetadataSchema recordSchema = schemasManager.getSchemaTypes(record.getCollection()).getSchema(record.getSchemaCode());
		List<Metadata> parentReferences = recordSchema.getParentReferences();
		for (Metadata metadata : parentReferences) {
			String referenceValue = record.get(metadata);
			if (referenceValue != null) {
				Record referencedRecord = recordProvider.getRecord(referenceValue);
				List<String> parentPaths = referencedRecord.getList(Schemas.PATH);
				paths.addAll(parentPaths);
				List<String> parentAuthorizations = referencedRecord.getList(Schemas.ALL_AUTHORIZATIONS);
				authorizations.addAll(parentAuthorizations);
			}
		}
		List<Metadata> metadataReferencingTaxonomy = recordSchema
				.getTaxonomyRelationshipReferences(taxonomiesManager.getEnabledTaxonomies(record.getCollection()));
		for (Metadata metadata : metadataReferencingTaxonomy) {
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
				Record referencedRecord = recordProvider.getRecord(referenceValue);
				List<String> parentPaths = referencedRecord.getList(Schemas.PATH);
				paths.addAll(parentPaths);
				List<String> parentAuthorizations = referencedRecord.getList(Schemas.ALL_AUTHORIZATIONS);
				authorizations.addAll(parentAuthorizations);
			}
		}
		HierarchyDependencyValue value = new HierarchyDependencyValue(taxonomy, paths, authorizations);
		values.put(dependency, value);
		return true;
	}

	@SuppressWarnings("unchecked")
	private boolean addMultivalueReference(RecordImpl record, RecordProvider recordProvider, Map<Dependency, Object> values,
			ReferenceDependency<?> referenceDependency, Metadata referenceMetadata) {
		List<String> referencesValues = (List<String>) record.get(referenceMetadata);
		List<Record> referencedRecords = new ArrayList<>();
		for (String referenceValue : referencesValues) {
			referencedRecords.add(recordProvider.getRecord(referenceValue));
		}
		List<Object> referencedValues = new ArrayList<>();
		for (Record referencedRecord : referencedRecords) {
			Metadata dependentMetadata = getDependentMetadataFromDependency(referenceDependency, referencedRecord);
			Object dependencyValue = referencedRecord.get(dependentMetadata);
			if (referenceDependency.isRequired() && dependencyValue == null) {
				return false;
			} else if (dependencyValue instanceof List) {
				referencedValues.addAll((List) dependencyValue);
			} else {
				referencedValues.add(dependencyValue);
			}
		}
		values.put(referenceDependency, referencedValues);
		return true;
	}

	private boolean addSingleValueReference(RecordImpl record, RecordProvider recordProvider, Map<Dependency, Object> values,
			ReferenceDependency<?> dependency, Metadata referenceMetadata) {
		String referenceValue = (String) record.get(referenceMetadata);
		Record referencedRecord;
		if (dependency.isRequired() && referenceValue == null) {
			return false;
		} else {
			referencedRecord = referenceValue == null ? null : recordProvider.getRecord(referenceValue);
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
		return schemasManager.getSchemaTypes(referencedRecord.getCollection()).getMetadata(
				referencedRecord.getSchemaCode() + "_" + referenceDependency.getDependentMetadataCode());
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
		return schemasManager.getSchemaTypes(record.getCollection())
				.getMetadata(record.getSchemaCode() + "_" + dependency.getLocalMetadataCode());
	}

	void copyValueInRecord(RecordImpl record, Metadata metadataWithCopyDataEntry, RecordProvider recordProvider,
			Metadata referenceMetadata, Metadata copiedMetadata) {
		if (referenceMetadata.isMultivalue()) {
			List<String> referencedRecordIds = record.getList(referenceMetadata);
			if (referencedRecordIds == null || referencedRecordIds.isEmpty()) {
				record.updateAutomaticValue(metadataWithCopyDataEntry, Collections.emptyList());
			} else {
				copyReferenceValueInRecord(record, metadataWithCopyDataEntry, recordProvider, copiedMetadata,
						referencedRecordIds);
			}
		} else {
			String referencedRecordId = record.get(referenceMetadata);
			if (referencedRecordId == null) {
				record.updateAutomaticValue(metadataWithCopyDataEntry, null);
			} else {
				copyReferenceValueInRecord(record, metadataWithCopyDataEntry, recordProvider, copiedMetadata, referencedRecordId);
			}
		}

	}

	void copyReferenceValueInRecord(RecordImpl record, Metadata metadataWithCopyDataEntry, RecordProvider recordProvider,
			Metadata copiedMetadata, String referencedRecordId) {
		Record referencedRecord = recordProvider.getRecord(referencedRecordId);
		Object copiedValue = referencedRecord.get(copiedMetadata);
		record.updateAutomaticValue(metadataWithCopyDataEntry, copiedValue);
	}

	void copyReferenceValueInRecord(RecordImpl record, Metadata metadataWithCopyDataEntry, RecordProvider recordProvider,
			Metadata copiedMetadata, List<String> referencedRecordIds) {
		List<Object> values = new ArrayList<>();
		for (String referencedRecordId : referencedRecordIds) {
			Record referencedRecord = recordProvider.getRecord(referencedRecordId);
			if (copiedMetadata.isMultivalue()) {
				values.addAll(referencedRecord.getList(copiedMetadata));
			} else {
				Object value = referencedRecord.get(copiedMetadata);
				if (value != null) {
					values.add(value);
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
			TransactionRecordsReindexation reindexation) {

		MetadataValueCalculator<?> calculator = getCalculatorFrom(metadataWithCalculatedDataEntry);

		if (calculatorDependencyModified(record, calculator) || reindexation.isReindexed(metadataWithCalculatedDataEntry)) {
			calculateValueInRecord(record, metadataWithCalculatedDataEntry, recordProvider);
		}
	}

}
