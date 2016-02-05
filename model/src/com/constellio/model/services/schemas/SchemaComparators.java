package com.constellio.model.services.schemas;

import java.util.Comparator;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class SchemaComparators {

	public static final Comparator<Metadata> METADATA_COMPARATOR_BY_ASC_LOCAL_CODE = new Comparator<Metadata>() {
		@Override
		public int compare(Metadata o1, Metadata o2) {
			return o1.getLocalCode().compareTo(o2.getLocalCode());
		}
	};

	public static final Comparator<MetadataSchema> SCHEMA_COMPARATOR_BY_ASC_LOCAL_CODE = new Comparator<MetadataSchema>() {
		@Override
		public int compare(MetadataSchema o1, MetadataSchema o2) {
			return o1.getLocalCode().compareTo(o2.getLocalCode());
		}
	};

	public static final Comparator<MetadataSchemaType> SCHEMA_TYPE_COMPARATOR_BY_ASC_CODE = new Comparator<MetadataSchemaType>() {
		@Override
		public int compare(MetadataSchemaType o1, MetadataSchemaType o2) {
			return o1.getCode().compareTo(o2.getCode());
		}
	};

	public static final Comparator<Record> sortRecordsBySchemasDependencies(final MetadataSchemaTypes schemaTypes) {
		return new Comparator<Record>() {
			@Override
			public int compare(Record r1, Record r2) {
				SchemaUtils schemaUtils = new SchemaUtils();
				String schemaType1 = schemaUtils.getSchemaTypeCode(r1.getSchemaCode());
				String schemaType2 = schemaUtils.getSchemaTypeCode(r2.getSchemaCode());
				Integer index1 = schemaTypes.getSchemaTypesSortedByDependency().indexOf(schemaType1);
				Integer index2 = schemaTypes.getSchemaTypesSortedByDependency().indexOf(schemaType2);
				return index1.compareTo(index2);
			}
		};
	}
}
