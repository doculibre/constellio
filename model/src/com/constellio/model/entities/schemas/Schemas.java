package com.constellio.model.entities.schemas;

import com.constellio.data.dao.services.solr.SolrDataStoreTypesUtils;
import com.constellio.model.services.schemas.SchemaUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.constellio.data.dao.services.bigVault.BigVaultRecordDao.DATE_SEARCH_FIELD;
import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE_TIME;
import static com.constellio.model.entities.schemas.MetadataValueType.INTEGER;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.MetadataValueType.TEXT;
import static java.util.Arrays.asList;

public class
Schemas {

	private static List<Metadata> allGlobalMetadatas = new ArrayList<>();

	public static final String TITLE_CODE = "title";
	public static final String GLOBAL_SCHEMA_TYPE = "global";

	public static final Metadata MIGRATION_DATA_VERSION = add(
			new Metadata(-1, "migrationDataVersion_d", MetadataValueType.NUMBER, false));
	public static final Metadata CREATED_BY = add(new Metadata(-2, "createdById_s", MetadataValueType.REFERENCE, false));
	public static final Metadata MODIFIED_BY = add(new Metadata(-3, "modifiedById_s", MetadataValueType.REFERENCE, false));
	public static final Metadata IDENTIFIER = add(new Metadata(-4, "id", STRING, false));
	public static final Metadata VERSION = add(new Metadata(-5, "_version_", MetadataValueType.NUMBER, false));
	public static final Metadata SCHEMA = add(new Metadata(-6, "schema_s", STRING, false));
	public static final Metadata TITLE = add(new Metadata(-7, "title_s", STRING, false));
	public static final Metadata PATH = add(new Metadata(-8, "path_ss", STRING, true));
	public static final Metadata PATH_PARTS = add(new Metadata(-9, "pathParts_ss", STRING, true));
	public static final Metadata PRINCIPAL_PATH = add(new Metadata(-10, "principalpath_s", STRING, false));
	public static final Metadata REMOVED_AUTHORIZATIONS = add(new Metadata(-11, "removedauthorizations_ss", STRING, true));
	public static final Metadata ALL_REMOVED_AUTHS = add(new Metadata(-12, "allRemovedAuths_ss", STRING, true));
	public static final Metadata IS_DETACHED_AUTHORIZATIONS = add(new Metadata(-13, "detachedauthorizations_s", STRING, false));
	public static final Metadata TOKENS = add(new Metadata(-14, "tokens_ss", STRING, true));
	public static final Metadata TOKENS_OF_HIERARCHY = add(new Metadata(-15, "tokensHierarchy_ss", STRING, true));
	public static final Metadata ATTACHED_ANCESTORS = add(new Metadata(-16, "attachedAncestors_ss", STRING, true));
	public static final Metadata ESTIMATED_SIZE = add(new Metadata(-17, "estimatedSize_i", INTEGER, false));

	public static final Metadata COLLECTION = add(new Metadata(-18, "collection_s", STRING, false));
	public static final Metadata LOGICALLY_DELETED_STATUS = add(new Metadata(-19, "deleted_s", BOOLEAN, false));

	public static final String CREATED_ON_CODE = "createdOn_dt";
	public static final Metadata CREATED_ON = add(new Metadata(-20, CREATED_ON_CODE, DATE_TIME, false));
	public static final Metadata MODIFIED_ON = add(new Metadata(-21, "modifiedOn_dt", DATE_TIME, false));
	public static final Metadata LOGICALLY_DELETED_ON = add(new Metadata(-22, "logicallyDeletedOn_dt", DATE_TIME, false));
	public static final Metadata ERROR_ON_PHYSICAL_DELETION = add(new Metadata(-23, "errorOnPhysicalDeletion_s", BOOLEAN, false));

	public static final Metadata CAPTION = add(new Metadata(-24, "caption_s", STRING, false));

	public static final Metadata SEARCH_FIELD = add(new Metadata(-25, "search_txt", TEXT, true));
	public static final Metadata FRENCH_SEARCH_FIELD = add(new Metadata(-26, "search_txt_fr", TEXT, true));
	public static final Metadata ENGLISH_SEARCH_FIELD = add(new Metadata(-27, "search_txt_en", TEXT, true));
	public static final Metadata SPELL_CHECK_FIELD = add(new Metadata(-28, "_spell_text", TEXT, true));

	public static final Metadata MANUAL_TOKENS = add(new Metadata(-29, "manualTokens_ss", STRING, true));


	//Move in SMB documents/folders:
	public static final Metadata DENY_TOKENS = add(new Metadata(-30, "denyTokens_ss", STRING, true));
	public static final Metadata SHARE_TOKENS = add(new Metadata(-31, "shareTokens_ss", STRING, true));
	public static final Metadata SHARE_DENY_TOKENS = add(new Metadata(-32, "shareDenyTokens_ss", STRING, true));
	public static final Metadata URL = add(new Metadata(-33, "url_s", STRING, false));
	public static final Metadata FETCHED = new Metadata(-34, "fetched_s", BOOLEAN, false);


	public static final Metadata CODE = new Metadata(-35, "code_s", STRING, false);
	public static final Metadata DESCRIPTION_TEXT = new Metadata(-36, "description_t", TEXT, false);
	public static final Metadata DESCRIPTION_STRING = new Metadata(-37, "description_s", STRING, false);
	public static final Metadata LINKABLE = new Metadata(-38, "linkable_s", BOOLEAN, false);

	public static final Metadata LEGACY_ID = add(
			new Metadata(-39, "legacyIdentifier_s", STRING, false));


	public static final Metadata VISIBLE_IN_TREES = add(new Metadata(-40, "visibleInTrees_s", BOOLEAN, false));

	public static final Metadata LINKED_SCHEMA = new Metadata(-41, "linkedSchema_s", STRING, false);
	public static final Metadata ALL_REFERENCES = add(new Metadata(-42, "allReferences_ss", STRING, true));
	public static final Metadata MARKED_FOR_PREVIEW_CONVERSION = add(
			new Metadata(-43, "markedForPreviewConversion_s", BOOLEAN, false));
	public static final Metadata MARKED_FOR_REINDEXING = add(new Metadata(-44, "markedForReindexing_s", BOOLEAN, false));
	public static final Metadata MARKED_FOR_PARSING = add(new Metadata(-45, "markedForParsing_s", BOOLEAN, false));
	public static final Metadata SCHEMA_AUTOCOMPLETE_FIELD = add(new Metadata(-46, "autocomplete_ss", STRING, false));


	public static Metadata add(Metadata metadata) {
		String localCode = metadata.getLocalCode();
		if (localCode.startsWith("USR") || localCode.startsWith("MAP")) {
			throw new RuntimeException("Invalid local code for global metadata : " + localCode);
		}
		allGlobalMetadatas.add(metadata);
		return metadata;
	}

	public static Metadata getGlobalMetadata(String code) {
		//folder_default_createdOn_dt
		//global_default_createdOn_dt
		String cratedOnCodeWithoutType = StringUtils.substringBeforeLast(CREATED_ON_CODE, "_");
		if (code.contains(cratedOnCodeWithoutType)) {
			return CREATED_ON;
		} else {
			String metadataLocalCode = new SchemaUtils().toLocalMetadataCode(code);
			for (Metadata globalMetadata : getAllGlobalMetadatas()) {
				if (globalMetadata.getLocalCode().equals(metadataLocalCode)) {
					return globalMetadata;
				}
			}
			return null;
		}
	}

	public static List<Metadata> getAllGlobalMetadatas() {
		return Collections.unmodifiableList(allGlobalMetadatas);
	}

	public static Metadata dummyMultiValueMetadata(Metadata metadata) {
		String dataStoreCode = SolrDataStoreTypesUtils.getMultivalueFieldCode(metadata.getDataStoreCode());
		return new Metadata(metadata.id, dataStoreCode, metadata.getType(), true);
	}

	public static Metadata dummySingleValueMetadata(Metadata metadata) {
		String dataStoreCode = SolrDataStoreTypesUtils.getSinglevalueFieldCode(metadata.getDataStoreCode());
		return new Metadata(metadata.id, dataStoreCode, metadata.getType(), false);
	}

	public static Metadata dummy(Metadata metadata) {
		String dataStoreCode = metadata.getDataStoreCode();
		return new Metadata(metadata.id, dataStoreCode, metadata.getType(), metadata.isMultivalue());
	}

	public static Metadata getSortMetadata(Metadata metadata) {

		String dataStoreCode = metadata.getDataStoreCode().replace("_s", "_sort_s");
		String schemaCode = metadata.getCode().replace("_" + metadata.getLocalCode(), "");
		return new Metadata(metadata.id, schemaCode, dataStoreCode, STRING, metadata.isMultivalue(),
				metadata.isMultiLingual());
	}

	public static Metadata getSearchableMetadata(Metadata metadata, String languageCode) {

		String dataStoreCode = metadata.getDataStoreCode();

		if (isValueTypeSearchable(metadata)) {
			if (metadata.isMultivalue()) {
				dataStoreCode = dataStoreCode.replace("_txt", "_txt_" + languageCode);
				dataStoreCode = dataStoreCode.replace("_ss", "_txt_" + languageCode);
				dataStoreCode = dataStoreCode.replace("_das", DATE_SEARCH_FIELD);

			} else if (metadata.getLocalCode().equals("id")) {
				dataStoreCode = "id_txt_" + languageCode;

			} else {
				dataStoreCode = dataStoreCode.replace("_t", "_t_" + languageCode);
				if (metadata.getType() == MetadataValueType.CONTENT) {
					dataStoreCode = dataStoreCode.replace("_s", "_txt_" + languageCode);
				} else {
					dataStoreCode = dataStoreCode.replace("_s", "_t_" + languageCode);
					dataStoreCode = dataStoreCode.replace("_da", DATE_SEARCH_FIELD);
				}
			}
		}

		String schemaCode = metadata.getCode().replace("_" + metadata.getLocalCode(), "");
		return new Metadata(metadata.id, schemaCode, dataStoreCode, TEXT, metadata.isMultivalue(),
				metadata.isMultiLingual());
	}

	public static boolean isValueTypeSearchable(Metadata metadata) {
		return !asList(MetadataValueType.ENUM, MetadataValueType.BOOLEAN).contains(metadata.getType());
	}

	public static Metadata getSecondaryLanguageMetadata(Metadata metadata, String language) {

		String dataStoreCode = getSecondaryLanguageDataStoreCode(metadata.getDataStoreCode(), language);

		String schemaCode = metadata.getCode().replace("_" + metadata.getLocalCode(), "");
		return new Metadata(metadata.id, schemaCode, dataStoreCode, TEXT, metadata.isMultivalue(),
				metadata.isMultiLingual());
	}

	public static String getSecondaryLanguageDataStoreCode(String dataStoreCode, String language) {

		String beforeUnderscore = StringUtils.substringBefore(dataStoreCode, "_");
		String afterUnderscore = StringUtils.substringAfter(dataStoreCode, "_");

		return beforeUnderscore + "." + language + "_" + afterUnderscore;
	}

	private static String replaceLast(String string, String expressionToReplace, String replacement) {
		int index = string.lastIndexOf(expressionToReplace);
		if (index == -1) {
			return string;
		}
		return string.substring(0, index) + replacement + string.substring(index + expressionToReplace.length());
	}

	public static boolean isGlobalMetadata(String metadata) {
		String metadataLocalCode = new SchemaUtils().toLocalMetadataCode(metadata);
		for (Metadata globalMetadata : getAllGlobalMetadatas()) {

			if (globalMetadata.getLocalCode().equals(metadataLocalCode)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isGlobalMetadataExceptTitle(String metadata) {
		String metadataLocalCode = new SchemaUtils().toLocalMetadataCode(metadata);
		return !TITLE_CODE.equals(metadataLocalCode) && isGlobalMetadata(metadata);
	}
}
