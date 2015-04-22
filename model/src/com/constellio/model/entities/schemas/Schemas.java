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
package com.constellio.model.entities.schemas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

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

	public static final Metadata LINKABLE = new Metadata("linkable_s", MetadataValueType.BOOLEAN, false);

	public static final Metadata LEGACY_ID = add(
			new Metadata("legacyIdentifier_s", MetadataValueType.STRING, false));

	public static Metadata add(Metadata metadata) {
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
			//TODO
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

}
