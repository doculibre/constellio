package com.constellio.model.entities.schemas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.constellio.data.dao.services.solr.SolrDataStoreTypesUtils;
import com.constellio.model.services.schemas.SchemaUtils;

public class Schemas {

	private static List<Metadata> allGlobalMetadatas = new ArrayList<>();

	public static final String TITLE_CODE = "title";
	public static final String GLOBAL_SCHEMA_TYPE = "global";

	public static final Metadata CREATED_BY = add(new Metadata("createdById_s", MetadataValueType.REFERENCE, false));
	public static final Metadata MODIFIED_BY = add(new Metadata("modifiedById_s", MetadataValueType.REFERENCE, false));
	public static final Metadata IDENTIFIER = add(new Metadata("id", MetadataValueType.STRING, false));
	public static final Metadata VERSION = add(new Metadata("_version_", MetadataValueType.NUMBER, false));
	public static final Metadata SCHEMA = add(new Metadata("schema_s", MetadataValueType.STRING, false));
	public static final Metadata TITLE = add(new Metadata("title_s", MetadataValueType.STRING, false));
	public static final Metadata PATH = add(new Metadata("path_ss", MetadataValueType.STRING, true));
	// Double-check this, principal path wasn't merged yet
	public static final Metadata PATH_PARTS = add(new Metadata("pathParts_ss", MetadataValueType.STRING, true));
	public static final Metadata PRINCIPAL_PATH = add(new Metadata("principalpath_s", MetadataValueType.STRING, false));
	public static final Metadata PARENT_PATH = add(new Metadata("parentpath_ss", MetadataValueType.STRING, true));
	public static final Metadata AUTHORIZATIONS = add(new Metadata("authorizations_ss", MetadataValueType.STRING, true));
	public static final Metadata REMOVED_AUTHORIZATIONS = add(new Metadata("removedauthorizations_ss", MetadataValueType.STRING,
			true));
	public static final Metadata INHERITED_AUTHORIZATIONS = add(
			new Metadata("inheritedauthorizations_ss", MetadataValueType.STRING,
					true));
	public static final Metadata ALL_AUTHORIZATIONS = add(new Metadata("allauthorizations_ss", MetadataValueType.STRING, true));
	public static final Metadata IS_DETACHED_AUTHORIZATIONS = add(
			new Metadata("detachedauthorizations_s", MetadataValueType.STRING,
					false));
	public static final Metadata TOKENS = add(new Metadata("tokens_ss", MetadataValueType.STRING, true));
	public static final Metadata MANUAL_TOKENS = add(new Metadata("manualTokens_ss", MetadataValueType.STRING, true));
	public static final Metadata DENY_TOKENS = add(new Metadata("denyTokens_ss", MetadataValueType.STRING, true));
	public static final Metadata SHARE_TOKENS = add(new Metadata("shareTokens_ss", MetadataValueType.STRING, true));
	public static final Metadata SHARE_DENY_TOKENS = add(new Metadata("shareDenyTokens_ss", MetadataValueType.STRING, true));
	public static final Metadata COLLECTION = add(new Metadata("collection_s", MetadataValueType.STRING, false));
	public static final Metadata LOGICALLY_DELETED_STATUS = add(new Metadata("deleted_s", MetadataValueType.BOOLEAN, false));

	private static final String CREATED_ON_CODE = "createdOn_dt";
	public static final Metadata CREATED_ON = add(new Metadata(CREATED_ON_CODE, MetadataValueType.DATE_TIME, false));
	public static final Metadata MODIFIED_ON = add(new Metadata("modifiedOn_dt", MetadataValueType.DATE_TIME, false));

	public static final Metadata SEARCH_FIELD = add(new Metadata("search_txt", MetadataValueType.TEXT, true));
	public static final Metadata FRENCH_SEARCH_FIELD = add(new Metadata("search_txt_fr", MetadataValueType.TEXT, true));
	public static final Metadata ENGLISH_SEARCH_FIELD = add(new Metadata("search_txt_en", MetadataValueType.TEXT, true));
	public static final Metadata SPELL_CHECK_FIELD = add(new Metadata("_spell_text", MetadataValueType.TEXT, true));
	public static final Metadata FOLLOWERS = add(new Metadata("followers_ss", MetadataValueType.STRING, true));

	public static final Metadata CODE = new Metadata("code_s", MetadataValueType.STRING, false);
	public static final Metadata DESCRIPTION_TEXT = new Metadata("description_t", MetadataValueType.TEXT, false);
	public static final Metadata DESCRIPTION_STRING = new Metadata("description_s", MetadataValueType.STRING, false);

	public static final Metadata LINKABLE = new Metadata("linkable_s", MetadataValueType.BOOLEAN, false);

	public static final Metadata LEGACY_ID = add(
			new Metadata("legacyIdentifier_s", MetadataValueType.STRING, false));

	public static final Metadata MARKED_FOR_PREVIEW_CONVERSION = add(
			new Metadata("markedForPreviewConversion_s", MetadataValueType.BOOLEAN, false));
	public static final Metadata VISIBLE_IN_TREES = add(new Metadata("visibleInTrees_s", MetadataValueType.BOOLEAN, false));
	public static final Metadata SEARCHABLE = add(new Metadata("searchable_s", MetadataValueType.BOOLEAN, false));

	public static final Metadata URL = add(new Metadata("url_s", MetadataValueType.STRING, false));
	public static final Metadata FETCHED = new Metadata("fetched_s", MetadataValueType.BOOLEAN, false);

	public static Metadata add(Metadata metadata) {
		String localCode = metadata.getLocalCode();
		if (localCode.startsWith("USR") || localCode.startsWith("MAP")) {
			throw new RuntimeException("Invalid local code for global metadata : " + localCode);
		}
		allGlobalMetadatas.add(metadata);
		return metadata;
	}

	public static final Metadata SCHEMA_AUTOCOMPLETE_FIELD = new Metadata("autocomplete_ss", MetadataValueType.STRING, false);

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
		return new Metadata(schemaCode, dataStoreCode, MetadataValueType.TEXT, metadata.isMultivalue());
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
}
