package com.constellio.model.services.search;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;

public class SearchServiceAcceptanceTestSchemas extends TestsSchemasSetup {

	public SearchServiceAcceptanceTestSchemas() {
	}

	public SearchServiceAcceptanceTestSchemas(String collection) {
		super(collection);
	}

	public SearchServiceAcceptanceTestSchemas withStringMetadatasInZeSchemaAndAnotherSchema() {
		zeDefaultSchemaBuilder.create("textMetadata").setType(MetadataValueType.STRING);
		anOtherDefaultSchemaBuilder.create("textMetadata").setType(MetadataValueType.STRING);
		return this;
	}

	public SearchServiceAcceptanceTestSchemas withCodeInZeSchema() {
		zeDefaultSchemaBuilder.create("code").setType(MetadataValueType.STRING);
		return this;
	}

	public class ZeSchemaMetadatas extends TestsSchemasSetup.ZeSchemaMetadatas {

		public Metadata textMetadata() {
			return getMetadata(code() + "_textMetadata");
		}

	}

	public class AnotherSchemaMetadatas extends TestsSchemasSetup.AnotherSchemaMetadatas {

		public Metadata textMetadata() {
			return getMetadata(code() + "_textMetadata");
		}

	}

	public class OtherSchemaMetadatasInCollection2 extends TestsSchemasSetup.ZeSchemaMetadatas {

		public Metadata textMetadata() {
			return getMetadata(code() + "_textMetadata");
		}

		@Override
		public String collection() {
			return "collection2";
		}
	}
}
