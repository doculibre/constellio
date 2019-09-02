package com.constellio.model.services.records;

import com.constellio.data.utils.Factory;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.constellio.model.services.records.RecordUtils.changeSchemaTypeAccordingToTypeLinkedSchema;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class BaseSchemasRecordsServices implements Serializable {

	protected String collection;

	protected transient ModelLayerFactory modelLayerFactory;

	private Factory<ModelLayerFactory> modelLayerFactoryFactory;

	protected Locale locale;

	public BaseSchemasRecordsServices(String collection, Factory<ModelLayerFactory> modelLayerFactoryFactory) {
		this(collection, modelLayerFactoryFactory, null);
	}

	public BaseSchemasRecordsServices(String collection, Factory<ModelLayerFactory> modelLayerFactoryFactory,
									  Locale locale) {
		this.collection = collection;
		this.modelLayerFactory = modelLayerFactoryFactory.get();
		this.modelLayerFactoryFactory = modelLayerFactoryFactory;
		this.locale = locale;
		if (this.locale == null) {
			String mainDataLanguage = modelLayerFactory.getConfiguration().getMainDataLanguage();
			this.locale = mainDataLanguage == null ? Locale.ENGLISH : Language.withCode(mainDataLanguage).getLocale();
		}
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		modelLayerFactory = modelLayerFactoryFactory.get();
	}

	public String getCollection() {
		return collection;
	}

	//

	public Metadata getRecordTypeMetadataOf(MetadataSchemaType schemaType) {
		return schemaType.getDefaultSchema().getMetadata("type");

	}

	public Metadata getRecordTypeMetadataOf(Record record) {
		String recordSchemaType = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
		RecordProvider recordProvider = new RecordProvider(modelLayerFactory.newRecordServices());
		return getTypes().getDefaultSchema(recordSchemaType).getMetadata("type");

	}

	public String getLinkedSchemaOf(RecordWrapper recordWrapper) {
		return getLinkedSchemaOf(recordWrapper.getWrappedRecord());
	}

	//TODO Francis : Test
	public String getLinkedSchemaOf(Record record) {
		MetadataSchemaTypes types = getTypes();
		String recordSchemaType = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
		MetadataSchema recordSchema = types.getSchema(record.getSchemaCode());

		//The case where the record is a type
		if (recordSchemaType.toLowerCase().contains("type")) {

			if (!recordSchema.hasMetadataWithCode("linkedSchema")) {
				throw new IllegalArgumentException(
						"The record's schema '" + recordSchemaType + "' does not have 'linkedSchema' metadata");
			}

			Metadata linkedSchemaMetadata = recordSchema.getMetadata("linkedSchema");

			String linkedSchema = record.get(linkedSchemaMetadata);
			if (linkedSchema != null && linkedSchema.contains("_")) {
				return linkedSchema;
			} else {

				String linkedSchemaType = null;
				for (MetadataSchemaType type : types.getSchemaTypes()) {
					if (type.getDefaultSchema().hasMetadataWithCode("type")) {
						Metadata metadata = type.getDefaultSchema().getMetadata("type");
						if (metadata.getType() == MetadataValueType.REFERENCE && recordSchemaType
								.equals(metadata.getReferencedSchemaType())) {
							linkedSchemaType = new SchemaUtils().getSchemaTypeCode(metadata.getSchemaCode());
						}
					}
				}

				if (linkedSchemaType == null) {
					throw new IllegalArgumentException("No Schematype has a type referencing '" + recordSchemaType + "'");
				}

				return linkedSchemaType + "_" + (linkedSchema == null ? "default" : linkedSchema);
			}

		} else {
			//The case where the record is an object with a type

			RecordProvider recordProvider = new RecordProvider(modelLayerFactory.newRecordServices());
			Metadata typeMetadata = getRecordTypeMetadataOf(record);
			return RecordUtils.getSchemaAccordingToTypeLinkedSchema(record, types, recordProvider, typeMetadata);
		}

	}

	public void setType(Record record, Record type) {
		MetadataSchemaTypes types = getTypes();
		MetadataSchema currentRecordSchema = types.getSchema(record.getSchemaCode());

		Metadata recordTypeMetadata = getRecordTypeMetadataOf(record);

		record.set(recordTypeMetadata, type);
		RecordProvider recordProvider = new RecordProvider(getModelLayerFactory().newRecordServices());
		changeSchemaTypeAccordingToTypeLinkedSchema(record, types, recordProvider, recordTypeMetadata);
	}

	public ModelLayerFactory getModelLayerFactory() {
		return modelLayerFactory;
	}

	public MetadataSchemaTypes getTypes() {
		return modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
	}

	public MetadataSchema defaultSchema(String code) {
		return getTypes().getSchema(code + "_default");
	}

	public MetadataSchema schema(String code) {
		return getTypes().getSchema(code);
	}

	public MetadataSchemaType schemaType(String code) {
		return getTypes().getSchemaType(code);
	}

	public Record get(String id) {
		return modelLayerFactory.newRecordServices().getDocumentById(id);
	}

	public Record get(MetadataSchemaType schemaType, String id) {
		return modelLayerFactory.newRecordServices().getById(schemaType, id);
	}

	public Record getByLegacyId(MetadataSchemaType schemaType, String id) {
		LogicalSearchCondition condition = from(schemaType).where(Schemas.LEGACY_ID).isEqualTo(id);
		return modelLayerFactory.newSearchServices().searchSingleResult(condition);
	}

	public Record getByLegacyId(String schemaTypeCode, String id) {
		LogicalSearchCondition condition = from(schemaType(schemaTypeCode)).where(Schemas.LEGACY_ID).isEqualTo(id);
		return modelLayerFactory.newSearchServices().searchSingleResult(condition);
	}

	public List<Record> get(List<String> ids) {
		List<Record> records = new ArrayList<>();

		for (String id : ids) {
			records.add(get(id));
		}

		return records;
	}

	public List<Record> get(MetadataSchemaType metadataSchemaType, List<String> ids) {
		List<Record> records = new ArrayList<>();

		for (String id : ids) {
			records.add(get(metadataSchemaType, id));
		}

		return records;
	}

	public Record create(MetadataSchema schema) {
		return modelLayerFactory.newRecordServices().newRecordWithSchema(schema);
	}

	public Record create(MetadataSchema schema, String id) {
		if (id == null) {
			return modelLayerFactory.newRecordServices().newRecordWithSchema(schema);
		} else {
			return modelLayerFactory.newRecordServices().newRecordWithSchema(schema, id);
		}
	}

	public Record getByCode(MetadataSchemaType schemaType, String code) {
		Metadata metadata = schemaType.getDefaultSchema().getMetadata(Schemas.CODE.getLocalCode());
		return modelLayerFactory.newRecordServices().getRecordByMetadata(metadata, code);
	}

	public static abstract class AbstractSchemaTypeShortcuts {

		String schemaTypeCode;
		String schemaCode;

		protected AbstractSchemaTypeShortcuts(String schemaCode) {
			this.schemaCode = schemaCode;
			this.schemaTypeCode = new SchemaUtils().getSchemaTypeCode(schemaCode);
		}

		public MetadataSchemaType schemaType() {
			return types().getSchemaType(schemaTypeCode);
		}

		public MetadataSchema schema() {
			return types().getSchema(schemaCode);
		}

		protected abstract MetadataSchemaTypes types();

		public Metadata title() {
			return metadata(Schemas.TITLE.getLocalCode());
		}

		public Metadata createdOn() {
			return metadata(Schemas.CREATED_ON.getLocalCode());
		}

		public Metadata createdBy() {
			return metadata(Schemas.CREATED_BY.getLocalCode());
		}

		public Metadata modifiedOn() {
			return metadata(Schemas.MODIFIED_ON.getLocalCode());
		}

		public Metadata modifiedBy() {
			return metadata(Schemas.MODIFIED_BY.getLocalCode());
		}

		public Metadata legacyId() {
			return metadata(Schemas.LEGACY_ID.getLocalCode());
		}

		protected Metadata metadata(String code) {
			return schema().getMetadata(schemaCode + "_" + code);
		}

/*		MetadataSchemaTypes types() {
			return modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
		}*/

	}

	public class SchemaTypeShortcuts extends AbstractSchemaTypeShortcuts {

		protected SchemaTypeShortcuts(String schemaCode) {
			super(schemaCode);
		}

		@Override
		protected MetadataSchemaTypes types() {
			return modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
		}
	}

}
