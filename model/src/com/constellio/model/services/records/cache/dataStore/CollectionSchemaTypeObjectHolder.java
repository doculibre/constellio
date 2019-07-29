package com.constellio.model.services.records.cache.dataStore;

import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.strobel.functions.Suppliers;

import java.util.function.Supplier;

import static com.constellio.model.entities.schemas.MetadataSchemaTypes.LIMIT_OF_TYPES_IN_COLLECTION;

public class CollectionSchemaTypeObjectHolder<T> {

	Object[][] indexes = new Object[256][];

	Supplier<T> createEmptyValueToPersistSupplier, createEmptyValueToReturn;

	public CollectionSchemaTypeObjectHolder(Supplier<T> createEmptyValueToPersistSupplier,
											Supplier<T> createEmptyValueToReturn) {

		this.createEmptyValueToPersistSupplier = createEmptyValueToPersistSupplier;
		this.createEmptyValueToReturn = createEmptyValueToReturn;
	}

	public CollectionSchemaTypeObjectHolder(Supplier<T> createEmptyValue) {

		this.createEmptyValueToPersistSupplier = createEmptyValue;
		this.createEmptyValueToReturn = createEmptyValue;
	}

	public CollectionSchemaTypeObjectHolder() {

		this.createEmptyValueToPersistSupplier = (Supplier<T>) Suppliers.forValue(null);
		this.createEmptyValueToReturn = (Supplier<T>) Suppliers.forValue(null);
	}

	public void set(MetadataSchemaType schemaType, T value) {
		set(schemaType.getCollectionInfo().getCollectionIndex(), schemaType.getId(), value);
	}

	public void set(byte collectionId, short typeId, T value) {
		int collectionIndex = collectionId - Byte.MIN_VALUE;
		set(collectionIndex, typeId, value);
	}

	public void set(int collectionIndex, short typeId, T value) {
		Object[] collectionTypesValues = indexes[collectionIndex];
		if (collectionTypesValues == null) {
			collectionTypesValues = synchronizedGetOrCreateCollectionTypesValuesArray(collectionIndex);
		}

		collectionTypesValues[typeId] = value;
	}

	private synchronized Object[] synchronizedGetOrCreateCollectionTypesValuesArray(int collectionIndex) {
		Object[] collectionTypesValues = indexes[collectionIndex];
		if (collectionTypesValues == null) {
			collectionTypesValues = indexes[collectionIndex] = new Object[LIMIT_OF_TYPES_IN_COLLECTION];
		}
		return collectionTypesValues;
	}

	public T get(MetadataSchemaType schemaType, boolean createIfNull) {
		return get(schemaType.getCollectionInfo().getCollectionIndex(), schemaType.getId(), createIfNull);
	}

	public T get(byte collectionId, short typeId, boolean createIfNull) {
		int collectionIndex = collectionId - Byte.MIN_VALUE;
		return get(collectionIndex, typeId, createIfNull);
	}

	public T get(int collectionIndex, short typeId, boolean createIfNull) {
		Object[] collectionTypesValues = indexes[collectionIndex];
		if (collectionTypesValues == null) {
			if (createIfNull) {
				collectionTypesValues = synchronizedGetOrCreateCollectionTypesValuesArray(collectionIndex);

			} else {
				return createEmptyValueToReturn.get();
			}
		}

		Object typeValue = collectionTypesValues[typeId];
		if (typeValue == null) {
			if (createIfNull) {
				synchronized (this) {
					typeValue = collectionTypesValues[typeId];
					if (typeValue == null) {
						typeValue = indexes[collectionIndex][typeId] = createEmptyValueToPersistSupplier.get();
					}
				}
			} else {
				return createEmptyValueToReturn.get();
			}
		}
		return (T) typeValue;
	}

}
