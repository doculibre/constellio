package com.constellio.model.entities.schemas;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE_TIME;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.MetadataValueType.TEXT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.constellio.data.dao.services.solr.SolrDataStoreTypesUtils;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;

public class Schemas {

	private static List<Metadata> allGlobalMetadatas = new ArrayList<>();

	public static final String TITLE_CODE = "title";
	public static final String GLOBAL_SCHEMA_TYPE = "global";

	public static final Metadata CREATED_BY = add(new Metadata("createdById_s", MetadataValueType.REFERENCE, false));
	public static final Metadata MODIFIED_BY = add(new Metadata("modifiedById_s", MetadataValueType.REFERENCE, false));
	public static final Metadata IDENTIFIER = add(new Metadata("id", STRING, false));
	public static final Metadata VERSION = add(new Metadata("_version_", MetadataValueType.NUMBER, false));
	public static final Metadata SCHEMA = add(new Metadata("schema_s", STRING, false));
	public static final Metadata TITLE = add(new Metadata("title_s", STRING, false));
	public static final Metadata PATH = add(new Metadata("path_ss", STRING, true));
	// Double-check this, principal path wasn't merged yet
	public static final Metadata PATH_PARTS = add(new Metadata("pathParts_ss", STRING, true));
	public static final Metadata PRINCIPAL_PATH = add(new Metadata("principalpath_s", STRING, false));
	public static final Metadata PARENT_PATH = add(new Metadata("parentpath_ss", STRING, true));
	public static final Metadata AUTHORIZATIONS = add(new Metadata("authorizations_ss", STRING, true));
	public static final Metadata REMOVED_AUTHORIZATIONS = add(new Metadata("removedauthorizations_ss", STRING, true));
	public static final Metadata INHERITED_AUTHORIZATIONS = add(new Metadata("inheritedauthorizations_ss", STRING, true));
	public static final Metadata ALL_AUTHORIZATIONS = add(new Metadata("allauthorizations_ss", STRING, true));
	public static final Metadata ALL_REMOVED_AUTHS = add(new Metadata("allRemovedAuths_ss", STRING, true));
	public static final Metadata IS_DETACHED_AUTHORIZATIONS = add(new Metadata("detachedauthorizations_s", STRING, false));
	public static final Metadata TOKENS = add(new Metadata("tokens_ss", STRING, true));
	public static final Metadata MANUAL_TOKENS = add(new Metadata("manualTokens_ss", STRING, true));
	public static final Metadata DENY_TOKENS = add(new Metadata("denyTokens_ss", STRING, true));
	public static final Metadata SHARE_TOKENS = add(new Metadata("shareTokens_ss", STRING, true));
	public static final Metadata SHARE_DENY_TOKENS = add(new Metadata("shareDenyTokens_ss", STRING, true));
	public static final Metadata COLLECTION = add(new Metadata("collection_s", STRING, false));
	public static final Metadata LOGICALLY_DELETED_STATUS = add(new Metadata("deleted_s", BOOLEAN, false));

	public static final String CREATED_ON_CODE = "createdOn_dt";
	public static final Metadata CREATED_ON = add(new Metadata(CREATED_ON_CODE, DATE_TIME, false));
	public static final Metadata MODIFIED_ON = add(new Metadata("modifiedOn_dt", DATE_TIME, false));
	public static final Metadata LOGICALLY_DELETED_ON = add(new Metadata("logicallyDeletedOn_dt", DATE_TIME, false));
	public static final Metadata ERROR_ON_PHYSICAL_DELETION = add(new Metadata("errorOnPhysicalDeletion_s", BOOLEAN, false));

	public static final Metadata SEARCH_FIELD = add(new Metadata("search_txt", TEXT, true));
	public static final Metadata FRENCH_SEARCH_FIELD = add(new Metadata("search_txt_fr", TEXT, true));
	public static final Metadata ENGLISH_SEARCH_FIELD = add(new Metadata("search_txt_en", TEXT, true));
	public static final Metadata SPELL_CHECK_FIELD = add(new Metadata("_spell_text", TEXT, true));

	//TODO : Remove
	public static final Metadata FOLLOWERS = add(new Metadata("followers_ss", STRING, true));

	public static final Metadata CODE = new Metadata("code_s", STRING, false);
	public static final Metadata DESCRIPTION_TEXT = new Metadata("description_t", TEXT, false);
	public static final Metadata DESCRIPTION_STRING = new Metadata("description_s", STRING, false);

	public static final Metadata LINKABLE = new Metadata("linkable_s", BOOLEAN, false);

	public static final Metadata LEGACY_ID = add(
			new Metadata("legacyIdentifier_s", STRING, false));

	public static final Metadata MARKED_FOR_PREVIEW_CONVERSION = add(
			new Metadata("markedForPreviewConversion_s", BOOLEAN, false));
	public static final Metadata VISIBLE_IN_TREES = add(new Metadata("visibleInTrees_s", BOOLEAN, false));
	public static final Metadata SEARCHABLE = add(new Metadata("searchable_s", BOOLEAN, false));

	public static final Metadata URL = add(new Metadata("url_s", STRING, false));
	public static final Metadata FETCHED = new Metadata("fetched_s", BOOLEAN, false);
	public static final Metadata LINKED_SCHEMA = new Metadata("linkedSchema_s", STRING, false);
	public static final Metadata ALL_REFERENCES = add(new Metadata("allReferences_ss", STRING, true));
	public static final Metadata MARKED_FOR_REINDEXING = add(new Metadata("markedForReindexing_s", BOOLEAN, false));
	public static final Metadata ATTACHED_ANCESTORS = add(new Metadata("attachedAncestors_ss", STRING, true));

	public static Metadata add(Metadata metadata) {
		String localCode = metadata.getLocalCode();
		if (localCode.startsWith("USR") || localCode.startsWith("MAP")) {
			throw new RuntimeException("Invalid local code for global metadata : " + localCode);
		}
		allGlobalMetadatas.add(metadata);
		return metadata;
	}

	public static final Metadata SCHEMA_AUTOCOMPLETE_FIELD = new Metadata("autocomplete_ss", STRING, false);

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

	public static Metadata getSearchFieldForLanguage(String languageCode) {
		if (languageCode.equals("fr")) {
			return FRENCH_SEARCH_FIELD;
		} else {
			return ENGLISH_SEARCH_FIELD;
		}
	}

	public static List<Metadata> getAllSearchFields() {
		return Arrays.asList(FRENCH_SEARCH_FIELD, ENGLISH_SEARCH_FIELD);
	}

	public static Metadata dummyMultiValueMetadata(Metadata metadata) {
		String dataStoreCode = SolrDataStoreTypesUtils.getMultivalueFieldCode(metadata.getDataStoreCode());
		return new Metadata(dataStoreCode, metadata.getType(), true);
	}

	public static Metadata dummySingleValueMetadata(Metadata metadata) {
		String dataStoreCode = SolrDataStoreTypesUtils.getSinglevalueFieldCode(metadata.getDataStoreCode());
		return new Metadata(dataStoreCode, metadata.getType(), false);
	}

	public static Metadata getSortMetadata(Metadata metadata) {

		String dataStoreCode = metadata.getDataStoreCode().replace("_s", "_sort_s");
		String schemaCode = metadata.getCode().replace("_" + metadata.getLocalCode(), "");
		return new Metadata(schemaCode, dataStoreCode, STRING, metadata.isMultivalue(),
				metadata.isMultiLingual());
	}

	public static Metadata getSearchableMetadata(Metadata metadata, String languageCode) {

		String dataStoreCode = metadata.getDataStoreCode();

		if (metadata.isMultivalue()) {
			dataStoreCode = dataStoreCode.replace("_txt", "_txt_" + languageCode);
			dataStoreCode = dataStoreCode.replace("_ss", "_txt_" + languageCode);
		} else {
			dataStoreCode = dataStoreCode.replace("_t", "_t_" + languageCode);
			if (metadata.getType() == MetadataValueType.CONTENT) {
				dataStoreCode = dataStoreCode.replace("_s", "_txt_" + languageCode);
			} else {
				dataStoreCode = dataStoreCode.replace("_s", "_t_" + languageCode);
			}
		}

		String schemaCode = metadata.getCode().replace("_" + metadata.getLocalCode(), "");
		return new Metadata(schemaCode, dataStoreCode, TEXT, metadata.isMultivalue(),
				metadata.isMultiLingual());
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
