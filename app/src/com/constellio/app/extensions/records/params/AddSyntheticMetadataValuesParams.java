package com.constellio.app.extensions.records.params;

import com.constellio.app.entities.schemasDisplay.enums.MetadataSortingType;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.model.entities.records.Record;

import java.util.EnumSet;

public class AddSyntheticMetadataValuesParams {
	public enum WhereToAddMetadata {
		DISPLAY, FORM;

		public static final EnumSet<WhereToAddMetadata> Everywhere = EnumSet.allOf(WhereToAddMetadata.class);
	}

	private final Record record;
	private final SyntheticMetadataVOBuilder syntheticMetadataVOBuilder;

	public AddSyntheticMetadataValuesParams(Record record,
											SyntheticMetadataVOBuilder syntheticMetadataVOBuilder) {
		this.record = record;
		this.syntheticMetadataVOBuilder = syntheticMetadataVOBuilder;
	}

	public Record getRecord() {
		return record;
	}

	public SyntheticMetadataVOBuilder getSyntheticMetadataVOBuilder() {
		return syntheticMetadataVOBuilder;
	}

	public interface SyntheticMetadataVOBuilder {
		MetadataVO build(SyntheticMetadataVOBuildingArgs syntheticMetadataVOBuildingArgs);
	}

	public static class SyntheticMetadataVOBuildingArgs {
		private final String metadataCode;
		private final String referencedSchemaType;
		private final String referencedSchema;
		private final String label;
		private final MetadataSortingType metadataSortingType;

		public SyntheticMetadataVOBuildingArgs(String metadataCode, String referencedSchemaType,
											   String referencedSchema, String label,
											   MetadataSortingType metadataSortingType) {
			this.metadataCode = metadataCode;
			this.referencedSchemaType = referencedSchemaType;
			this.referencedSchema = referencedSchema;
			this.label = label;
			this.metadataSortingType = metadataSortingType;
		}

		public String getMetadataCode() {
			return metadataCode;
		}

		public String getReferencedSchemaType() {
			return referencedSchemaType;
		}

		public String getReferencedSchema() {
			return referencedSchema;
		}

		public String getLabel() {
			return label;
		}

		public String getInsertBeforeThisMetadataInForm() {
			return null;
		}

		public String getInsertBeforeThisMetadataInDisplay() {
			return null;
		}

		public EnumSet<WhereToAddMetadata> getWhereToAddMetadata() {
			return WhereToAddMetadata.Everywhere;
		}

		public boolean isReadOnly() {
			return false;
		}

		public MetadataSortingType getMetadataSortingType() {
			return metadataSortingType;
		}
	}
}
