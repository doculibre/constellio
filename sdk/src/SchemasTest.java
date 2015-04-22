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
import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Condition;
import org.junit.Test;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;

public class SchemasTest {

	@Test
	public void validateIdentifier() {
		assertThat(Schemas.IDENTIFIER).has(code("id"));
		assertThat(Schemas.IDENTIFIER.getDataStoreCode()).isEqualTo("id");
	}

	@Test
	public void validateVersion() {
		assertThat(Schemas.VERSION).has(code("_version_"));
		assertThat(Schemas.VERSION.getDataStoreCode()).isEqualTo("_version_");
	}

	@Test
	public void validateSchema() {
		assertThat(Schemas.SCHEMA).has(code("schema"));
		assertThat(Schemas.SCHEMA.getDataStoreCode()).isEqualTo("schema_s");
	}

	@Test
	public void validateTitle() {
		assertThat(Schemas.TITLE).has(code("title"));
		assertThat(Schemas.TITLE.getDataStoreCode()).isEqualTo("title_s");
	}

	@Test
	public void validatePath() {
		assertThat(Schemas.PATH).has(code("path"));
		assertThat(Schemas.PATH.getDataStoreCode()).isEqualTo("path_ss");
	}

	@Test
	public void validatePrincipalPath() {
		assertThat(Schemas.PRINCIPAL_PATH).has(code("principalpath"));
		assertThat(Schemas.PRINCIPAL_PATH.getDataStoreCode()).isEqualTo("principalpath_s");
	}

	@Test
	public void validateParentPath() {
		assertThat(Schemas.PARENT_PATH).has(code("parentpath"));
		assertThat(Schemas.PARENT_PATH.getDataStoreCode()).isEqualTo("parentpath_ss");
	}

	@Test
	public void validateAuthorizations() {
		assertThat(Schemas.AUTHORIZATIONS).has(code("authorizations"));
		assertThat(Schemas.AUTHORIZATIONS.getDataStoreCode()).isEqualTo("authorizations_ss");
	}

	@Test
	public void validateRemovedAuthorizations() {
		assertThat(Schemas.REMOVED_AUTHORIZATIONS).has(code("removedauthorizations"));
		assertThat(Schemas.REMOVED_AUTHORIZATIONS.getDataStoreCode()).isEqualTo("removedauthorizations_ss");
	}

	@Test
	public void validateInheritedAuthorizations() {
		assertThat(Schemas.INHERITED_AUTHORIZATIONS).has(code("inheritedauthorizations"));
		assertThat(Schemas.INHERITED_AUTHORIZATIONS.getDataStoreCode()).isEqualTo("inheritedauthorizations_ss");
	}

	@Test
	public void validateAllAuthorizations() {
		assertThat(Schemas.ALL_AUTHORIZATIONS).has(code("allauthorizations"));
		assertThat(Schemas.ALL_AUTHORIZATIONS.getDataStoreCode()).isEqualTo("allauthorizations_ss");
	}

	@Test
	public void validateIsDetachedAuthorizations() {
		assertThat(Schemas.IS_DETACHED_AUTHORIZATIONS).has(code("detachedauthorizations"));
		assertThat(Schemas.IS_DETACHED_AUTHORIZATIONS.getDataStoreCode()).isEqualTo("detachedauthorizations_s");
	}

	@Test
	public void validateTokens() {
		assertThat(Schemas.TOKENS).has(code("tokens"));
		assertThat(Schemas.TOKENS.getDataStoreCode()).isEqualTo("tokens_ss");
	}

	@Test
	public void validateCollection() {
		assertThat(Schemas.COLLECTION).has(code("collection"));
		assertThat(Schemas.COLLECTION.getDataStoreCode()).isEqualTo("collection_s");
	}

	@Test
	public void validateLogicallyDeletedStatus() {
		assertThat(Schemas.LOGICALLY_DELETED_STATUS).has(code("deleted"));
		assertThat(Schemas.LOGICALLY_DELETED_STATUS.getDataStoreCode()).isEqualTo("deleted_s");
	}

	@Test
	public void validateCreatedBy() {
		assertThat(Schemas.CREATED_BY).has(code("createdBy"));
		assertThat(Schemas.CREATED_BY.getDataStoreCode()).isEqualTo("createdById_s");
	}

	@Test
	public void validateModifiedBy() {
		assertThat(Schemas.MODIFIED_BY).has(code("modifiedBy"));
		assertThat(Schemas.MODIFIED_BY.getDataStoreCode()).isEqualTo("modifiedById_s");
	}

	@Test
	public void validateCreatedOn() {
		assertThat(Schemas.CREATED_ON).has(code("createdOn"));
		assertThat(Schemas.CREATED_ON.getDataStoreCode()).isEqualTo("createdOn_dt");
	}

	@Test
	public void validateModifiedOn() {
		assertThat(Schemas.MODIFIED_ON).has(code("modifiedOn"));
		assertThat(Schemas.MODIFIED_ON.getDataStoreCode()).isEqualTo("modifiedOn_dt");
	}

	@Test
	public void validateFrenchSearchField() {
		assertThat(Schemas.FRENCH_SEARCH_FIELD).has(code("search"));
		assertThat(Schemas.FRENCH_SEARCH_FIELD.getDataStoreCode()).isEqualTo("search_txt_fr");
	}

	@Test
	public void validateEnglishSearchField() {
		assertThat(Schemas.ENGLISH_SEARCH_FIELD).has(code("search"));
		assertThat(Schemas.ENGLISH_SEARCH_FIELD.getDataStoreCode()).isEqualTo("search_txt_en");
	}

	private Condition<? super Metadata> code(final String code) {
		return new Condition<Metadata>() {
			@Override
			public boolean matches(Metadata metadata) {
				assertThat(metadata.getCode()).isEqualTo("global_default_" + code);
				assertThat(metadata.getLocalCode()).isEqualTo(code);
				return true;
			}
		};
	}

}
