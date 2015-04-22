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

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;

public class RecordServicesTestSchemaSetup extends TestsSchemasSetup {

	public RecordServicesTestSchemaSetup withAMetadataCopiedInAnotherSchema() {
		MetadataBuilder copiedMeta = zeDefaultSchemaBuilder.create("copiedMeta").setType(MetadataValueType.STRING);
		MetadataBuilder referenceToZeSchema = anOtherDefaultSchemaBuilder.create("referenceToZeSchema")
				.defineReferencesTo(zeSchemaTypeBuilder);
		MetadataBuilder copiedDataInAnotherSchema = anOtherDefaultSchemaBuilder.create("metadataWithCopiedEntry")
				.setType(MetadataValueType.STRING)
				.defineDataEntry().asCopied(referenceToZeSchema, copiedMeta);
		anOtherDefaultSchemaBuilder.create("manualMeta").setType(MetadataValueType.STRING);
		MetadataBuilder referenceToAnotherSchema = aThirdSchemaTypeBuilder.getDefaultSchema()
				.create("referenceToAnotherSchema")
				.defineReferencesTo(anOtherSchemaTypeBuilder);
		athirdDefaultSchemaBuilder.create("metadataWithCopiedEntry").setType(MetadataValueType.STRING)
				.defineDataEntry().asCopied(referenceToAnotherSchema, copiedDataInAnotherSchema);
		athirdDefaultSchemaBuilder.create("manualMeta").setType(MetadataValueType.STRING);
		return this;
	}

	public class ZeSchemaMetadatas extends TestsSchemasSetup.ZeSchemaMetadatas {

		public Metadata getCopiedMeta() {
			return getMetadata(code() + "_copiedMeta");
		}

	}

	public class AnotherSchemaMetadatas extends TestsSchemasSetup.AnotherSchemaMetadatas {

		public Metadata referenceToZeSchema() {
			return getMetadata(code() + "_referenceToZeSchema");
		}

		public Metadata manualMeta() {
			return getMetadata(code() + "_manualMeta");
		}

		public Metadata metadataWithCopiedEntry() {
			return getMetadata(code() + "_metadataWithCopiedEntry");
		}
	}

	public class ThirdSchemaMetadatas extends TestsSchemasSetup.ThirdSchemaMetadatas {

		public Metadata referenceToAnotherSchema() {
			return getMetadata(code() + "_referenceToAnotherSchema");
		}

		public Metadata manualMeta() {
			return getMetadata(code() + "_manualMeta");
		}

		public Metadata metadataWithCopiedEntry() {
			return getMetadata(code() + "_metadataWithCopiedEntry");
		}
	}
}
