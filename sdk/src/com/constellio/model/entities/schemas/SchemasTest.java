package com.constellio.model.entities.schemas;

import org.assertj.core.api.Condition;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
	public void validateRemovedAuthorizations() {
		assertThat(Schemas.REMOVED_AUTHORIZATIONS).has(code("removedauthorizations"));
		assertThat(Schemas.REMOVED_AUTHORIZATIONS.getDataStoreCode()).isEqualTo("removedauthorizations_ss");
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
	public void validateGetFrenchAnalyzedfield() {

		Metadata textSingleValue = new Metadata((short)0,"parsedContent_t", MetadataValueType.TEXT, false);
		assertThat(textSingleValue.getAnalyzedField("fr").getDataStoreCode()).isEqualTo("parsedContent_t_fr");

		Metadata textMultiValue = new Metadata((short)0,"parsedContents_txt", MetadataValueType.TEXT, true);
		assertThat(textMultiValue.getAnalyzedField("fr").getDataStoreCode()).isEqualTo("parsedContents_txt_fr");

		Metadata stringSingleValue = new Metadata((short)0,"meta_s", MetadataValueType.TEXT, false);
		assertThat(stringSingleValue.getAnalyzedField("fr").getDataStoreCode()).isEqualTo("meta_t_fr");

		Metadata stringMultivalue = new Metadata((short)0,"meta_ss", MetadataValueType.TEXT, true);
		assertThat(stringMultivalue.getAnalyzedField("fr").getDataStoreCode()).isEqualTo("meta_txt_fr");

		Metadata contentSingleValue = new Metadata((short)0,"content_s", MetadataValueType.CONTENT, false);
		assertThat(contentSingleValue.getAnalyzedField("fr").getDataStoreCode()).isEqualTo("content_txt_fr");

		Metadata contentMultiValue = new Metadata((short)0,"contents_ss", MetadataValueType.CONTENT, true);
		assertThat(contentMultiValue.getAnalyzedField("fr").getDataStoreCode()).isEqualTo("contents_txt_fr");

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
