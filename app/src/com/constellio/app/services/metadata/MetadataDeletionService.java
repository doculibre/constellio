package com.constellio.app.services.metadata;

import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.metadata.MetadataDeletionException.MetadataDeletionException_CalculatedMetadataSource;
import com.constellio.app.services.metadata.MetadataDeletionException.MetadataDeletionException_CopiedMetadataReference;
import com.constellio.app.services.metadata.MetadataDeletionException.MetadataDeletionException_CopiedMetadataSource;
import com.constellio.app.services.metadata.MetadataDeletionException.MetadataDeletionException_ExtractedMetadataSource;
import com.constellio.app.services.metadata.MetadataDeletionException.MetadataDeletionException_FacetMetadata;
import com.constellio.app.services.metadata.MetadataDeletionException.MetadataDeletionException_InheritedMetadata;
import com.constellio.app.services.metadata.MetadataDeletionException.MetadataDeletionException_PopulatedMetadata;
import com.constellio.app.services.metadata.MetadataDeletionException.MetadataDeletionException_SystemMetadata;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.encrypt.EncryptionServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.app.services.metadata.DeletionProhibitionReason.CALCULATED_METADATA_SOURCE;
import static com.constellio.app.services.metadata.DeletionProhibitionReason.COPIED_METADATA_REFERENCE;
import static com.constellio.app.services.metadata.DeletionProhibitionReason.COPIED_METADATA_SOURCE;
import static com.constellio.app.services.metadata.DeletionProhibitionReason.EXTRACTED_METADATA_SOURCE;
import static com.constellio.app.services.metadata.DeletionProhibitionReason.FACET_METADATA;
import static com.constellio.app.services.metadata.DeletionProhibitionReason.INHERITED_METADATA;
import static com.constellio.app.services.metadata.DeletionProhibitionReason.POPULATED_METADATA;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class MetadataDeletionService {
	final private MetadataSchemasManager schemasManager;
	final private String collection;
	private final SchemasDisplayManager displayManager;
	private final SearchServices searchServices;
	private final EncryptionServices encryptionServices;
	private final SchemasRecordsServices schemas;

	public MetadataDeletionService(AppLayerFactory appLayerFactory, String collection) {
		schemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		displayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		this.collection = collection;
		searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		encryptionServices = appLayerFactory.getModelLayerFactory().newEncryptionServices();
		schemas = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());
	}

	public boolean isMetadataDeletable(String codeOrLocalCode) {
		if (StringUtils.isBlank(codeOrLocalCode)) {
			throw new RuntimeException("invalid blank code " + codeOrLocalCode);
		}
		String localCode;
		if (codeOrLocalCode.contains("_")) {
			localCode = codeOrLocalCode.split("_")[2];
		} else {
			localCode = codeOrLocalCode;
		}
		return localCode.startsWith("USR") || localCode.startsWith("MAP") || localCode.endsWith("Ref");
	}

	DeletionProhibitionReason canDeleteMetadata(String code) {
		Metadata metadata = getMetadata(code);
		return canDeleteMetadata(metadata);
	}

	private Metadata getMetadata(String code) {
		return schemasManager.getSchemaTypes(collection).getMetadata(code);
	}

	DeletionProhibitionReason canDeleteMetadata(Metadata metadata) {
		if (isInherited(metadata)) {
			return INHERITED_METADATA;
		}
		if (isPopulated(metadata)) {
			return POPULATED_METADATA;
		}
		if (isFacetMetadata(metadata)) {
			return FACET_METADATA;
		}
		MetadataDependency schemaTypeDependencies = computeDependencies(metadata);
		if (!schemaTypeDependencies.getCalculationDependencies().isEmpty()) {
			return CALCULATED_METADATA_SOURCE;
		}
		if (!schemaTypeDependencies.getCopyReferenceDependencies().isEmpty()) {
			return COPIED_METADATA_REFERENCE;
		}
		if (!schemaTypeDependencies.getCopySourceDependencies().isEmpty()) {
			return COPIED_METADATA_SOURCE;
		}
		if (!schemaTypeDependencies.getExtractionDependencies().isEmpty()) {
			return EXTRACTED_METADATA_SOURCE;
		}

		return null;
	}

	private MetadataDependency computeDependencies(Metadata dependOnMetadata) {
		List<Metadata> metadatalist = schemasManager.getSchemaTypes(collection)
				.getAllMetadataIncludingInheritedOnes();
		MetadataDependency metadataDependency = new MetadataDependency(dependOnMetadata.getCode());
		for (Metadata dependentMetadata : metadatalist) {
			computeDependencies(dependOnMetadata, dependentMetadata, metadataDependency);
		}
		return metadataDependency;
	}

	private void computeDependencies(Metadata dependOnMetadata, Metadata dependentMetadata,
									 MetadataDependency metadataDependency) {
		String dependOnMetadataCode = dependOnMetadata.getCode();
		String dependOnMetadataLocalCode = dependOnMetadata.getLocalCode();
		if (dependentMetadata.getDataEntry().getType() == DataEntryType.COPIED) {
			CopiedDataEntry dataEntry = (CopiedDataEntry) dependentMetadata.getDataEntry();
			String referenceMetadata = dataEntry.getReferenceMetadata();
			if (referenceMetadata != null && (referenceMetadata.equals(dependOnMetadataCode) || referenceMetadata
					.equals(dependOnMetadataLocalCode))
			) {
				metadataDependency.addCopyReferenceDependency(dependentMetadata.getCode());
			}
			String sourceMetadata = dataEntry.getCopiedMetadata();
			if (sourceMetadata != null && (sourceMetadata.equals(dependOnMetadataCode)
										   || sourceMetadata.equals(dependOnMetadataLocalCode))) {
				metadataDependency.addCopySourceDependency(dependentMetadata.getCode());
			}
		} else if (dependentMetadata.getDataEntry().getType() == DataEntryType.CALCULATED) {
			CalculatedDataEntry dataEntry = (CalculatedDataEntry) dependentMetadata.getDataEntry();
			for (Dependency dependency : dataEntry.getCalculator().getDependencies()) {
				String dependencyLocalCode = dependency.getLocalMetadataCode();
				if (dependencyLocalCode != null && (dependencyLocalCode.equals(dependOnMetadataLocalCode)
													|| dependencyLocalCode
															.equals(dependOnMetadataCode))) {
					metadataDependency.addCalculationDependency(dependentMetadata.getCode());
				}
				if (dependency instanceof ReferenceDependency) {
					ReferenceDependency referenceDependency = (ReferenceDependency) dependency;
					String dependentMetadataCode = referenceDependency.getDependentMetadataCode();
					if (dependentMetadataCode != null && (dependentMetadataCode.equals(dependOnMetadataLocalCode)
														  || dependentMetadataCode
																  .equals(dependOnMetadataCode))) {
						metadataDependency.addCalculationDependency(dependentMetadata.getCode());
					}
				}
			}
		}
		List<String> properties = (dependentMetadata.getPopulateConfigs() == null) ?
								  new ArrayList<String>() :
								  dependentMetadata.getPopulateConfigs().getProperties();
		if (properties.contains(dependOnMetadataCode) || properties.contains(dependOnMetadataLocalCode)) {
			metadataDependency.addExtractionDependency(dependentMetadata.getCode());
		}
	}

	private boolean isFacetMetadata(Metadata metadata) {
		SchemaTypesDisplayConfig typesConfig = displayManager.getTypes(collection);
		List<String> facetsCodesOrLocalCodes = typesConfig.getFacetMetadataCodes();
		boolean configFacet =
				facetsCodesOrLocalCodes.contains(metadata.getCode()) || facetsCodesOrLocalCodes.contains(metadata.getLocalCode());
		if (configFacet) {
			return true;
		} else {
			return isFieldFacet(metadata);
		}
	}

	private boolean isFieldFacet(Metadata metadata) {
		LogicalSearchCondition query = from(schemas.facetFieldSchema()).where(
				schemas.facetFieldSchema().getMetadata(Facet.FIELD_DATA_STORE_CODE)).is(metadata.getCode());
		return searchServices.hasResults(query);
	}

	private boolean isPopulated(Metadata metadata) {
		MetadataSchemaType schemaType = schemasManager.getSchemaTypes(collection).getSchemaType(
				new SchemaUtils().getSchemaTypeCode(metadata));
		LogicalSearchCondition query;
		Object defaultValue = metadata.getDefaultValue();
		if (defaultValue != null) {
			if (metadata.isEncrypted()) {
				defaultValue = encryptionServices.encryptWithAppKey(defaultValue.toString());
			}
			query = from(schemaType).where(metadata).isNotEqual(defaultValue).andWhere(metadata)
					.isNotNull();
		} else {
			query = from(schemaType).where(metadata).isNotNull();
		}
		return searchServices.hasResults(query);
	}

	private boolean isInherited(Metadata metadata) {
		return metadata.getInheritance() != null;
	}

	public void deleteMetadata(String code)
			throws MetadataDeletionException {
		if (!isMetadataDeletable(code)) {
			throw new MetadataDeletionException_SystemMetadata();
		}
		Metadata metadata = getMetadata(code);
		DeletionProhibitionReason reason = canDeleteMetadata(metadata);
		if (reason != null) {
			switch (reason) {
				case POPULATED_METADATA:
					throw new MetadataDeletionException_PopulatedMetadata();
				case INHERITED_METADATA:
					throw new MetadataDeletionException_InheritedMetadata();
				case COPIED_METADATA_SOURCE:
					throw new MetadataDeletionException_CopiedMetadataSource();
				case COPIED_METADATA_REFERENCE:
					throw new MetadataDeletionException_CopiedMetadataReference();
				case CALCULATED_METADATA_SOURCE:
					throw new MetadataDeletionException_CalculatedMetadataSource();
				case EXTRACTED_METADATA_SOURCE:
					throw new MetadataDeletionException_ExtractedMetadataSource();
				case FACET_METADATA:
					throw new MetadataDeletionException_FacetMetadata();
				default:
					throw new RuntimeException("Unsupported reason " + reason);
			}
		}
		MetadataSchemaTypesBuilder typesBuilder = schemasManager.modify(collection);
		MetadataSchemaBuilder schemaBuilder = typesBuilder
				.getSchema(metadata.getSchemaCode());
		MetadataBuilder metadataBuilder = schemaBuilder.getMetadata(metadata.getLocalCode());
		schemaBuilder.deleteMetadataWithoutValidation(metadataBuilder);
		//typesBuilder.deleteMetadataFromInheretedSchemas(metadata);
		try {
			schemasManager.saveUpdateSchemaTypes(typesBuilder);
		} catch (OptimisticLocking optimistickLocking) {
			throw new RuntimeException(optimistickLocking);
		}
	}

	public static class MetadataDependency {
		final String dependOnMetadataCode;
		Set<String> copyReferenceDependency = new HashSet<>();
		Set<String> copySourceDependency = new HashSet<>();
		Set<String> calculationDependencies = new HashSet<>();
		Set<String> extractionDependencies = new HashSet<>();

		public MetadataDependency(String dependOnMetadataCode) {
			this.dependOnMetadataCode = dependOnMetadataCode;
		}

		public Set<String> getCopyReferenceDependencies() {
			return Collections.unmodifiableSet(copyReferenceDependency);
		}

		public MetadataDependency addCopyReferenceDependency(String copyReferenceDependency) {
			this.copyReferenceDependency.add(copyReferenceDependency);
			return this;
		}

		public Set<String> getCopySourceDependencies() {
			return Collections.unmodifiableSet(copySourceDependency);
		}

		public MetadataDependency addCopySourceDependency(String copySourceDependency) {
			this.copySourceDependency.add(copySourceDependency);
			return this;
		}

		public Set<String> getCalculationDependencies() {
			return Collections.unmodifiableSet(calculationDependencies);
		}

		public MetadataDependency addCalculationDependency(String calculationDependency) {
			this.calculationDependencies.add(calculationDependency);
			return this;
		}

		public Set<String> getExtractionDependencies() {
			return Collections.unmodifiableSet(extractionDependencies);
		}

		public MetadataDependency addExtractionDependency(String extractionDependency) {
			this.extractionDependencies.add(extractionDependency);
			return this;
		}
	}

}
