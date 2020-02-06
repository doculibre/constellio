package com.constellio.model.services.records.cache;

import com.constellio.data.dao.dto.records.RecordDTOMode;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache.ByteArrayRecordDTO.ByteArrayRecordDTOWithIntegerId;
import com.constellio.model.services.schemas.MetadataSchemaProvider;
import com.constellio.model.services.schemas.MetadataSchemasManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ByteArrayRecordDTOSerializerService {

	ModelLayerFactory modelLayerFactory;

	public ByteArrayRecordDTOSerializerService(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
	}

	public byte[] serialize(ByteArrayRecordDTOWithIntegerId recordDTO) {

		int id = recordDTO.getIntId();
		long version = recordDTO.getVersion();
		byte collectionId = recordDTO.getCollectionId();
		boolean summary = recordDTO.getLoadingMode() == RecordDTOMode.SUMMARY;
		short schemaId = recordDTO.getSchemaId();
		short typeId = recordDTO.getTypeId();
		int sortValueId = recordDTO.getMainSortValue();

		byte[] memoryBytes = recordDTO.data;
		byte[] persistedBytes = recordDTO.get();


		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(
				Integer.BYTES + //id
				Long.BYTES + //version
				Byte.BYTES + //collectionId
				Byte.BYTES + //summary
				Short.BYTES + // schemaId
				Short.BYTES + //typeId
				Integer.BYTES + //titleSortValue
				Integer.BYTES + //memoryBytesLength
				Integer.BYTES + //persistedBytesLength
				memoryBytes.length + persistedBytes.length);

		try (ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream)) {
			outputStream.writeInt(id);
			outputStream.writeLong(version);
			outputStream.writeByte(collectionId);
			outputStream.writeByte(summary ? 1 : 0);
			outputStream.writeShort(schemaId);
			outputStream.writeShort(typeId);
			outputStream.writeInt(sortValueId);
			outputStream.writeInt(memoryBytes.length);
			outputStream.write(memoryBytes);
			outputStream.writeInt(persistedBytes.length);
			outputStream.write(persistedBytes);
			outputStream.flush();
			return byteArrayOutputStream.toByteArray();
		} catch (IOException ioException) {
			throw new ImpossibleRuntimeException(ioException);
		}

	}

	public ByteArrayRecordDTOWithIntegerId deserialize(byte[] bytes) throws IOException {

		try (ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
			int id = inputStream.readInt();
			long version = inputStream.readLong();
			byte collectionId = inputStream.readByte();
			boolean summary = inputStream.readByte() == 1;
			short schemaId = inputStream.readShort();
			short typeId = inputStream.readShort();
			int mainSortValue = inputStream.readInt();
			int memoryBytesLength = inputStream.readInt();
			byte[] memoryBytes = new byte[memoryBytesLength];
			inputStream.read(memoryBytes);

			int persistedBytesLength = inputStream.readInt();
			byte[] persistedBytes = new byte[persistedBytesLength];
			inputStream.read(persistedBytes);

			MetadataSchemaProvider schemaProvider = modelLayerFactory.getMetadataSchemasManager();
			short tenantId = modelLayerFactory.getInstanceId();

			String collectionCode = modelLayerFactory.getCollectionsListManager().getCollectionCode(collectionId);
			MetadataSchemaType schemaType = ((MetadataSchemasManager) schemaProvider).getSchemaTypes(collectionId).getSchemaType(typeId);
			String typeCode = schemaType.getCode();
			String schemaCode = schemaType.getSchema(schemaId).getCode();

			return new ByteArrayRecordDTOWithIntegerId(id, schemaProvider, version, summary, tenantId, collectionCode,
					collectionId, typeCode, typeId, schemaCode, schemaId, memoryBytes, mainSortValue) {
				@Override
				public byte[] get() {
					return persistedBytes;
				}
			};
		}

	}

}
