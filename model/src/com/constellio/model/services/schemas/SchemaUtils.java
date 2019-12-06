package com.constellio.model.services.schemas;

import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.DynamicLocalDependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.CannotGetMetadatasOfAnotherSchema;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.CannotGetMetadatasOfAnotherSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.schemas.SchemaUtilsRuntimeException.SchemaUtilsRuntimeException_NoMetadataWithDatastoreCode;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilderRuntimeException.NoSuchMetadata;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.Schemas.LEGACY_ID;
import static com.constellio.model.entities.schemas.Schemas.MIGRATION_DATA_VERSION;
import static com.constellio.model.entities.schemas.Schemas.TITLE;

public class SchemaUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(SchemaUtils.class);

	public static Map<String, String[]> underscoreSplitCache = new HashMap<>();

	public static String[] underscoreSplitWithCache(String text) {
		String[] cached = underscoreSplitCache.get(text);
		if (cached == null) {
			cached = text.split("_");
			underscoreSplitCache.put(text, cached);
		}
		return cached;
	}

	public List<String> toMetadataLocalCodes(List<Metadata> metadatas) {
		List<String> localCodes = new ArrayList<>();
		for (Metadata metadata : metadatas) {
			localCodes.add(this.toLocalMetadataCode(metadata.getCode()));
		}
		return localCodes;
	}

	public String toLocalMetadataCode(String codeOrLocalCode) {
		String simpleCode = codeOrLocalCode;
		if (codeOrLocalCode != null) {
			String[] parts = underscoreSplitWithCache(codeOrLocalCode);
			if (parts.length == 3) {
				simpleCode = parts[2];
			}
		}

		return simpleCode;
	}

	public Set<String> getLocalDependencies(Metadata metadata, List<Metadata> allMetadatas) {
		Set<String> localDependencies = new HashSet<>();
		if (metadata.getDataEntry().getType() == DataEntryType.COPIED) {
			CopiedDataEntry dataEntry = (CopiedDataEntry) metadata.getDataEntry();
			localDependencies.add(toLocalMetadataCode(dataEntry.getReferenceMetadata()));

		} else if (metadata.getDataEntry().getType() == DataEntryType.CALCULATED) {
			CalculatedDataEntry dataEntry = (CalculatedDataEntry) metadata.getDataEntry();
			for (Dependency dependency : dataEntry.getCalculator().getDependencies()) {
				if (dependency instanceof DynamicLocalDependency) {

					for (Metadata aMetadata : allMetadatas) {
						if (isDependentMetadata(metadata, aMetadata, (DynamicLocalDependency) dependency)) {
							localDependencies.add(aMetadata.getLocalCode());
						}
					}

				} else {
					localDependencies.add(toLocalMetadataCode(dependency.getLocalMetadataCode()));
				}
			}
		}
		return localDependencies;
	}

	public Map<String, Set<String>> calculatedMetadataDependencies(List<Metadata> metadatas) {
		Map<String, Set<String>> dependenciesMap = new HashMap<>();

		for (Metadata metadata : metadatas) {
			Set<String> localDependencies = getLocalDependencies(metadata, metadatas);
			if (!localDependencies.isEmpty()) {
				dependenciesMap.put(metadata.getLocalCode(), localDependencies);
			}
		}

		return dependenciesMap;
	}

	public String getSchemaTypeCode(Metadata metadata) {
		return underscoreSplitWithCache(metadata.getCode())[0];
	}

	public static String getSchemaTypeCode(String schema) {
		return underscoreSplitWithCache(schema)[0];
	}

	public String getSchemaLocalCode(String schema) {
		return schema.split("_")[1];
	}

	public String getSchemaCode(Metadata metadata) {
		return getSchemaCode(metadata.getCode());
	}

	public String getSchemaCode(String metadataCompleteCode) {
		String[] parts = underscoreSplitWithCache(metadataCompleteCode);
		return parts[0] + "_" + parts[1];
	}

	public String getSchemaCode(MetadataBuilder builder) {
		String[] parts = underscoreSplitWithCache(builder.getCode());
		return parts[0] + "_" + parts[1];
	}

	public String getReferenceCode(Metadata automaticMetadata, ReferenceDependency<?> referenceDependency) {
		String referenceCode = referenceDependency.getLocalMetadataCode();
		if (!referenceCode.contains("_")) {
			referenceCode = automaticMetadata.getCode().replace(automaticMetadata.getLocalCode(), referenceCode);
		}
		return referenceCode;
	}

	public String getDependencyCode(ReferenceDependency<?> referenceDependency, Metadata reference) {
		String dependencyCode = referenceDependency.getDependentMetadataCode();
		if (!dependencyCode.contains("_")) {
			String schemaType = reference.getAllowedReferences().getAllowedSchemaType();
			if (schemaType == null) {
				schemaType = underscoreSplitWithCache(
						reference.getAllowedReferences().getAllowedSchemas().iterator().next())[0];
			}

			dependencyCode = schemaType + "_default_" + dependencyCode;
		}
		return dependencyCode;
	}

	public Metadata getMetadataFromDataStoreCode(String metadataDataStoreCode, MetadataSchemaType schemaType) {
		String code = getLocalCodeFromDataStoreCode(metadataDataStoreCode);
		try {
			return schemaType.getDefaultSchema().getMetadata(code);
		} catch (NoSuchMetadata e) {
			LOGGER.debug("Metadata not found in default schema, searching in ");
			for (MetadataSchema customSchema : schemaType.getCustomSchemas()) {
				Metadata customMetadata = customSchema.getMetadata(code);
				if (customMetadata != null) {
					return customMetadata;
				}
			}
		}

		throw new SchemaUtilsRuntimeException_NoMetadataWithDatastoreCode(metadataDataStoreCode);
	}

	public static String getLocalCodeFromDataStoreCode(String metadataDataStoreCode) {
		int indexOfUnderscore = metadataDataStoreCode.indexOf("_");
		String firstPart;
		if (indexOfUnderscore == -1) {
			firstPart = metadataDataStoreCode;
		} else {
			firstPart = metadataDataStoreCode.substring(0, indexOfUnderscore);
		}

		if (firstPart.endsWith("PId")) {
			return firstPart.substring(0, firstPart.length() - 3);

		} else if (firstPart.endsWith("Id")) {
			return firstPart.substring(0, firstPart.length() - 2);
		} else {
			return StringUtils.substringBefore(firstPart, ".");
		}
	}

	public Map<String, Metadata> buildMetadataByLocalCodeIndex(List<MetadataSchema> customSchemas,
															   MetadataSchema defaultSchema) {
		//TODO Test that default schema metadata are returned instead of an inheritance in a custom schema
		Map<String, Metadata> index = new HashMap<>();
		for (MetadataSchema customSchema : customSchemas) {
			index.putAll(customSchema.getIndexByLocalCode());
		}
		index.putAll(defaultSchema.getIndexByLocalCode());
		return index;
	}

	public Map<String, Metadata> buildIndexByCode(List<Metadata> metadatas) {
		Map<String, Metadata> index = new HashMap<>();
		for (Metadata metadata : metadatas) {
			index.put(metadata.getCode(), metadata);
			if (metadata.isGlobal() || "code".equals(metadata.getLocalCode())) {
				index.put("global_default_" + metadata.getLocalCode(), metadata);
			}
			if (metadata.getInheritance() != null) {
				index.put(metadata.getInheritanceCode(), metadata);
			}
		}
		return index;
	}


	public Map<String, Metadata> buildIndexByDatastoreCode(List<Metadata> metadatas) {
		Map<String, Metadata> index = new HashMap<>();
		for (Metadata metadata : metadatas) {
			index.put(metadata.getDataStoreCode(), metadata);
		}
		return index;
	}


	public Map<Short, Metadata> buildIndexById(List<Metadata> metadatas) {
		Map<Short, Metadata> index = new HashMap<>();
		for (Metadata metadata : metadatas) {
			index.put(metadata.getId(), metadata);
		}
		return index;
	}

	public Map<String, Metadata> buildIndexByLocalCode(List<Metadata> metadatas) {
		Map<String, Metadata> index = new HashMap<>();
		for (Metadata metadata : metadatas) {
			index.put(metadata.getLocalCode(), metadata);
		}
		return index;
	}


	public List<Metadata> buildListOfCacheIndexMetadatas(List<Metadata> metadatas) {

		List<Metadata> cacheIndexMetadatas = new ArrayList<>();

		for (Metadata metadata : metadatas) {
			boolean cacheIndex = isCacheIndex(metadata);

			if (cacheIndex) {
				cacheIndexMetadatas.add(metadata);
			}
		}

		return cacheIndexMetadatas;
	}

	public List<Metadata> buildListOfSummaryMetadatas(List<Metadata> metadatas) {

		List<Metadata> summaryMetadatas = new ArrayList<>();

		for (Metadata metadata : metadatas) {
			boolean summary = isSummary(metadata);

			if (summary) {
				summaryMetadatas.add(metadata);
			}
		}

		return summaryMetadatas;
	}

	public static boolean areCacheIndex(Collection<Metadata> metadatas) {
		boolean allSummary = true;
		for (Metadata metadata : metadatas) {
			allSummary &= metadata.isCacheIndex();
		}
		return allSummary;
	}

	public static boolean areSummary(Collection<Metadata> metadatas) {
		boolean allSummary = true;
		for (Metadata metadata : metadatas) {
			allSummary &= isSummary(metadata);
		}
		return allSummary;
	}


	public static boolean isCacheIndex(Metadata metadata) {
		return (metadata.isCacheIndex() || (metadata.isUniqueValue())
										   && (metadata.getType() == MetadataValueType.STRING || metadata.getType() == REFERENCE)
										   && !Schemas.IDENTIFIER.isSameLocalCode(metadata));
	}


	public static boolean isSummary(Metadata metadata) {
		boolean summary;
		switch (metadata.getType()) {
			case DATE:
			case DATE_TIME:
			case STRING:
				summary = metadata.isEssentialInSummary() || metadata.isUniqueValue()
						  || LEGACY_ID.isSameLocalCode(metadata)
						  || TITLE.isSameLocalCode(metadata) || metadata.isEssentialInSummary()
						  || metadata.isCacheIndex() || Schemas.TOKENS.getLocalCode().equals(metadata.getLocalCode())
						  || Schemas.ALL_REMOVED_AUTHS.getLocalCode().equals(metadata.getLocalCode())
						  || Schemas.ATTACHED_ANCESTORS.getLocalCode().equals(metadata.getLocalCode());
				;
				break;

			case STRUCTURE:
			case CONTENT:
				//TODO Based on summary flag, support these typestype
				summary = metadata.isEssentialInSummary();
				break;

			case TEXT:
				summary = metadata.isEssentialInSummary();
				break;

			case INTEGER:
			case NUMBER:
			case BOOLEAN:
			case REFERENCE:
			case ENUM:
				summary = true;
				break;
			default:
				throw new ImpossibleRuntimeException("Unsupported type : " + metadata.getType());

		}

		if (summary && metadata.hasSameCode(MIGRATION_DATA_VERSION)) {
			summary = false;
		}
		return summary;
	}

	public String getLocalCodeFromMetadataCode(String metadataCode) {
		if (!metadataCode.contains("_")) {
			return metadataCode;
		}
		String schemaCode = getSchemaCode(metadataCode);
		return getLocalCode(metadataCode, schemaCode);
	}

	public String getLocalCode(String codeOrLocalCode, String schemaCode) {
		String partialCode;
		String[] codeOrLocalCodeSplitted = underscoreSplitWithCache(codeOrLocalCode);
		if (codeOrLocalCodeSplitted.length == 3) {
			partialCode = codeOrLocalCodeSplitted[2];
			String requestedSchemaType = codeOrLocalCodeSplitted[0];

			if (!Schemas.GLOBAL_SCHEMA_TYPE.equals(requestedSchemaType) && !schemaCode.startsWith(requestedSchemaType)) {
				throw new CannotGetMetadatasOfAnotherSchemaType(requestedSchemaType, schemaCode);
			}
			String requestedSchema = codeOrLocalCodeSplitted[1];
			String schemaLocalCode = underscoreSplitWithCache(schemaCode)[1];
			if (!requestedSchema.equals(MetadataSchemaType.DEFAULT) && !requestedSchema.equals(schemaLocalCode)) {
				throw new CannotGetMetadatasOfAnotherSchema(requestedSchema, schemaLocalCode);
			}

			if (codeOrLocalCodeSplitted.length != 3) {
				throw new MetadataSchemasRuntimeException.InvalidCode(schemaCode);
			}
		} else {
			partialCode = codeOrLocalCode;
		}

		if (partialCode.endsWith("PId")) {
			partialCode = partialCode.substring(0, partialCode.length() - 3);
		}

		if (partialCode.endsWith("Id")) {
			partialCode = partialCode.substring(0, partialCode.length() - 2);
		}
		return partialCode;
	}

	public List<String> toMetadataCodes(List<Metadata> metadatas) {
		List<String> codes = new ArrayList<>();
		for (Metadata metadata : metadatas) {
			codes.add(metadata.getCode());
		}
		return codes;
	}

	public List<String> toSchemaTypeCodes(List<MetadataSchemaType> types) {
		List<String> codes = new ArrayList<>();
		for (MetadataSchemaType type : types) {
			codes.add(type.getCode());
		}
		return codes;
	}

	public boolean hasSameTypeAndLocalCode(String code1, String code2) {
		String typeCode1 = getSchemaTypeCode(code1);
		String typeCode2 = getSchemaTypeCode(code2);
		String localCode1 = getLocalCodeFromMetadataCode(code1);
		String localCode2 = getLocalCodeFromMetadataCode(code2);

		return typeCode1.equals(typeCode2) && localCode1.equals(localCode2);
	}

	public boolean isDependentMetadata(Metadata calculatedMetadata, Metadata otherMetadata,
									   DynamicLocalDependency dependency) {
		return !calculatedMetadata.getLocalCode().equals(otherMetadata.getLocalCode())
			   && (dependency.isIncludingGlobalMetadatas() || !otherMetadata.isGlobal())
			   && dependency.isDependentOf(otherMetadata, calculatedMetadata);
	}

	public static String getMetadataLocalCodeWithoutPrefix(Metadata metadata) {

		//USR
		//MAP
		//USRMAP
		//MAPUSR

		String code = metadata.getLocalCode();
		if (code.startsWith("MAP")) {
			code = code.substring(3);

		}
		if (code.startsWith("USR")) {
			code = code.substring(3);

		}
		if (code.startsWith("MAP")) {
			code = code.substring(3);

		}

		return code;
	}

	static Set<String> validMetadataLocalCodes = new HashSet<>();

	public static boolean isValidMetadataCodeWithCache(String localCode) {
		if (localCode == null) {
			return false;
		}
		if (validMetadataLocalCodes.contains(localCode)) {
			return true;
		}
		String pattern = "([a-zA-Z0-9])+";
		boolean valid = !localCode.matches(pattern) || (localCode.toLowerCase().endsWith("id") && !localCode.equals("id"));

		if (valid) {
			validMetadataLocalCodes.add(localCode);
		}

		return valid;
	}

	static Set<String> validSchemaLocalCodes = new HashSet<>();

	public static boolean isValidSchemaCodeWithCache(String localCode) {
		if (localCode == null) {
			return false;
		}
		if (validMetadataLocalCodes.contains(localCode)) {
			return true;
		}
		String pattern = "([a-zA-Z0-9])+";
		boolean valid = localCode.matches(pattern);

		if (valid) {
			validSchemaLocalCodes.add(localCode);
		}

		return valid;
	}

	public static Metadata getMetadataUsedByCalculatedReferenceWithTaxonomyRelationship(MetadataSchema schema,
																						Metadata metadata) {

		CalculatedDataEntry calculatedDataEntry = ((CalculatedDataEntry) metadata.getDataEntry());
		for (Dependency calculatorDependency : calculatedDataEntry.getCalculator().getDependencies()) {
			if (calculatorDependency instanceof LocalDependency) {
				LocalDependency calculatorLocalDependency = (LocalDependency) calculatorDependency;
				if (calculatorLocalDependency.getReturnType() == REFERENCE) {
					Metadata otherMetadata = schema.get(calculatorLocalDependency.getLocalMetadataCode());
					if (otherMetadata.getAllowedReferences().getTypeWithAllowedSchemas()
							.equals(metadata.getAllowedReferences().getTypeWithAllowedSchemas())) {
						return otherMetadata;
					}
				}
			}
		}

		throw new ImpossibleRuntimeException("getMetadataUsedByCalculatedReferenceWithTaxonomyRelationship - No such metadata!");

	}

	public static List<String> localCodes(List<String> codes) {
		List<String> localCodes = new ArrayList<>();
		for (String code : codes) {
			localCodes.add(SchemaUtils.toLocalCode(code));
		}
		return localCodes;
	}

	private static String toLocalCode(String code) {
		String[] parts = new SchemaUtils().underscoreSplitWithCache(code);
		return parts[2];
	}

	public static List<String> getSchemaTypesInHierarchyOf(String schemaTypeCode, MetadataSchemaTypes allSchemaTypes,
														   boolean isDocumentSchemaType) {
		Set<String> schemaTypesInHierarchy = new HashSet<>();

		if (!isDocumentSchemaType) {
			schemaTypesInHierarchy.add(schemaTypeCode);
		}

		schemaTypesInHierarchy.addAll(getSchemaTypesInHierarchyOf(schemaTypeCode, allSchemaTypes, schemaTypesInHierarchy));

		return new ArrayList<>(schemaTypesInHierarchy);
	}

	private static Set<String> getSchemaTypesInHierarchyOf(String schemaTypeCode, MetadataSchemaTypes allSchemaTypes,
														   Set<String> schemaTypesInHierarchy) {
		MetadataSchemaType schemaType = allSchemaTypes.getSchemaType(schemaTypeCode);
		for (Metadata metadata : schemaType.getAllMetadatas()) {
			if (metadata.isChildOfRelationship() || metadata.isTaxonomyRelationship()) {
				String referencedSchemaType = metadata.getReferencedSchemaType();
				if (schemaTypesInHierarchy.add(referencedSchemaType)) {
					schemaTypesInHierarchy
							.addAll(getSchemaTypesInHierarchyOf(referencedSchemaType, allSchemaTypes, schemaTypesInHierarchy));
				}
			}
		}

		return schemaTypesInHierarchy;
	}
}
