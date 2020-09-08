package com.constellio.model.services.records;

import com.constellio.data.dao.services.records.DataStore;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.util.Arrays;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class GetRecordOptions {

	int value;


	public static GetRecordOptions DO_NOT_CALL_EXTENSIONS = new GetRecordOptions(0);

	public static GetRecordOptions USE_DATASTORE(String datastore) {
		return new DataStoreRecordServicesGetOptions(1, datastore);
	}

	public static GetRecordOptions SILENT_IF_DOES_NOT_EXIST = new GetRecordOptions(2);

	public static GetRecordOptions WARN_IF_DOES_NOT_EXIST = new GetRecordOptions(3);

	public static GetRecordOptions DO_NOT_INSERT_IN_CACHE = new GetRecordOptions(4);

	public static GetRecordOptions RETURNING_SUMMARY = new GetRecordOptions(5);

	public static GetRecordOptions GET_BY_QUERY = new GetRecordOptions(6);

	public static GetRecordOptions IN_COLLECTION(String collection) {
		return new CollectionRecordServicesGetOptions(7, collection);
	}

	public static GetRecordOptions IN_COLLECTION(CollectionInfo collection) {
		return new CollectionInfoRecordServicesGetOptions(8, collection);
	}

	public static GetRecordOptions IN_SCHEMA_TYPE(String schemaType) {
		return new SchemaTypeCodeRecordServicesGetOptions(9, schemaType);
	}

	public static GetRecordOptions IN_SCHEMA_TYPE(MetadataSchemaType schemaType) {
		return new SchemaTypeRecordServicesGetOptions(10, schemaType);
	}

	public static GetRecordOptions EXPECTING_VERSION_HIGHER_OR_EQUAL_TO(Long version) {
		return new ExpectingVersionHigherOrEqualRecordServicesGetOptions(11, version);
	}

	public static class DataStoreRecordServicesGetOptions extends GetRecordOptions {
		String dataStore;

		private DataStoreRecordServicesGetOptions(int value, String dataStore) {
			super(value);
			this.dataStore = dataStore;
		}
	}

	public static class CollectionRecordServicesGetOptions extends GetRecordOptions {
		String collection;

		private CollectionRecordServicesGetOptions(int value, String collection) {
			super(value);
			this.collection = collection;
		}
	}

	public static class SchemaTypeCodeRecordServicesGetOptions extends GetRecordOptions {
		String schemaTypeCode;

		private SchemaTypeCodeRecordServicesGetOptions(int value, String schemaTypeCode) {
			super(value);
			this.schemaTypeCode = schemaTypeCode;
		}
	}

	public static class SchemaTypeRecordServicesGetOptions extends GetRecordOptions {
		MetadataSchemaType schemaType;

		private SchemaTypeRecordServicesGetOptions(int value, MetadataSchemaType schemaType) {
			super(value);
			this.schemaType = schemaType;
		}
	}

	public static class CollectionInfoRecordServicesGetOptions extends GetRecordOptions {
		CollectionInfo collectionInfo;

		private CollectionInfoRecordServicesGetOptions(int value, CollectionInfo collectionInfo) {
			super(value);
			this.collectionInfo = collectionInfo;
		}
	}

	public static boolean isCallingExtensions(GetRecordOptions... options) {
		return options == null || Arrays.stream(options).noneMatch((o) -> o == DO_NOT_CALL_EXTENSIONS);
	}

	public static boolean isThrowingExceptionIfDoesNotExist(GetRecordOptions... options) {
		return options == null || Arrays.stream(options).noneMatch((o) -> o == SILENT_IF_DOES_NOT_EXIST || o == WARN_IF_DOES_NOT_EXIST);
	}

	public static boolean isWarningIfDoesNotExist(GetRecordOptions... options) {
		return options != null && Arrays.stream(options).anyMatch((o) -> o == WARN_IF_DOES_NOT_EXIST);
	}


	public static boolean isSilentIfDoesNotExist(GetRecordOptions... options) {
		return options != null && Arrays.stream(options).anyMatch((o) -> o == SILENT_IF_DOES_NOT_EXIST);
	}

	public static boolean isInsertingInCache(GetRecordOptions... options) {
		return options == null || Arrays.stream(options).noneMatch((o) -> o == RETURNING_SUMMARY || o == DO_NOT_INSERT_IN_CACHE);
	}

	public static boolean isReturningSummary(GetRecordOptions... options) {
		return options != null && Arrays.stream(options).anyMatch((o) -> o == RETURNING_SUMMARY);
	}

	public static boolean isRealtimeGet(GetRecordOptions... options) {
		return options == null || Arrays.stream(options).noneMatch((o) -> o == GET_BY_QUERY);
	}

	public static Long getExpectedVersionHigherOrEqual(GetRecordOptions... options) {
		Long version = null;
		if (options != null) {
			for (GetRecordOptions option : options) {
				if (option instanceof ExpectingVersionHigherOrEqualRecordServicesGetOptions) {
					version = ((ExpectingVersionHigherOrEqualRecordServicesGetOptions) option).version;
				}
			}
		}
		return version;
	}

	public static String getCollection(GetRecordOptions... options) {
		String collection = null;
		if (options != null) {
			for (GetRecordOptions option : options) {
				if (option instanceof CollectionRecordServicesGetOptions) {
					collection = ((CollectionRecordServicesGetOptions) option).collection;
				}

				if (option instanceof SchemaTypeRecordServicesGetOptions) {
					collection = ((SchemaTypeRecordServicesGetOptions) option).schemaType.getCollection();
				}

				if (option instanceof CollectionInfoRecordServicesGetOptions) {
					collection = ((CollectionInfoRecordServicesGetOptions) option).collectionInfo.getCode();
				}
			}
		}
		return collection;
	}

	public static CollectionInfo getCollectionInfo(GetRecordOptions... options) {
		CollectionInfo collection = null;
		if (options != null) {
			for (GetRecordOptions option : options) {
				if (option instanceof SchemaTypeRecordServicesGetOptions) {
					collection = ((SchemaTypeRecordServicesGetOptions) option).schemaType.getCollectionInfo();
				}
				if (option instanceof CollectionInfoRecordServicesGetOptions) {
					collection = ((CollectionInfoRecordServicesGetOptions) option).collectionInfo;
				}
			}
		}
		return collection;
	}

	public static MetadataSchemaType getSchemaType(GetRecordOptions... options) {
		MetadataSchemaType schemaType = null;
		if (options != null) {
			for (GetRecordOptions option : options) {
				if (option instanceof SchemaTypeRecordServicesGetOptions) {
					schemaType = ((SchemaTypeRecordServicesGetOptions) option).schemaType;
				}
			}
		}
		return schemaType;
	}

	public static String getSchemaTypeCode(GetRecordOptions... options) {
		String schemaTypeCode = null;
		if (options != null) {
			for (GetRecordOptions option : options) {
				if (option instanceof SchemaTypeRecordServicesGetOptions) {
					schemaTypeCode = ((SchemaTypeRecordServicesGetOptions) option).schemaType.getCode();
				}
				if (option instanceof SchemaTypeCodeRecordServicesGetOptions) {
					schemaTypeCode = ((SchemaTypeCodeRecordServicesGetOptions) option).schemaTypeCode;
				}
			}
		}
		return schemaTypeCode;
	}


	public static String getDataStore(GetRecordOptions... options) {
		String dataStore = DataStore.RECORDS;
		if (options != null) {
			for (GetRecordOptions option : options) {
				if (option instanceof DataStoreRecordServicesGetOptions) {
					dataStore = ((DataStoreRecordServicesGetOptions) option).dataStore;
				}
			}
		}
		return dataStore;
	}

	public static class ExpectingVersionHigherOrEqualRecordServicesGetOptions extends GetRecordOptions {
		Long version;

		private ExpectingVersionHigherOrEqualRecordServicesGetOptions(int value, Long version) {
			super(value);
			this.version = version;
		}
	}


}
