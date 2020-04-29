package com.constellio.model.services.records.cache;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.records.cache.LocalCacheConfigs.CollectionTypeLocalCacheConfigs;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;

import static org.apache.ignite.internal.util.lang.GridFunc.asSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LocalCacheConfigsServicesManagerAcceptanceTest extends ConstellioTest {

	@Test
	public void whenAlteringConfigsThenAppliedAndPersisted() {

		File testFile1 = new File(newTempFolder(), "test1.json");
		LocalCacheConfigsServicesManager manager1 = new LocalCacheConfigsServicesManager(testFile1);

		MetadataSchemaType collection1Type1 = mockMetadataSchemaType(1, "collection1", "type1");
		MetadataSchemaType collection1Type2 = mockMetadataSchemaType(2, "collection1", "type2");
		MetadataSchemaType collection2Type1 = mockMetadataSchemaType(3, "collection2", "type1");


		Metadata collection1Type1_m1 = mockMetadata(collection1Type1, "m1");
		Metadata collection1Type1_m2 = mockMetadata(collection1Type1, "m2");
		Metadata collection1Type1_m3 = mockMetadata(collection1Type1, "m3");
		Metadata collection1Type2_m1 = mockMetadata(collection1Type2, "m1");
		Metadata collection1Type2_m2 = mockMetadata(collection1Type2, "m2");
		Metadata collection1Type2_m3 = mockMetadata(collection1Type2, "m3");
		Metadata collection2Type1_m1 = mockMetadata(collection2Type1, "m1");
		Metadata collection2Type1_m2 = mockMetadata(collection2Type1, "m2");
		Metadata collection2Type1_m3 = mockMetadata(collection2Type1, "m3");

		manager1.alter((configs) -> {
			configs.setCollectionTypeConfigs(collection1Type1, new CollectionTypeLocalCacheConfigs(asSet("type1_default_m3")));
			configs.setCollectionTypeConfigs(collection1Type2, new CollectionTypeLocalCacheConfigs(asSet("type2_default_m2")));
		});

		assertThat(manager1.get().excludedDuringLastCacheRebuild(collection1Type1_m1)).isFalse();
		assertThat(manager1.get().excludedDuringLastCacheRebuild(collection1Type1_m2)).isFalse();
		assertThat(manager1.get().excludedDuringLastCacheRebuild(collection1Type1_m3)).isTrue();
		assertThat(manager1.get().excludedDuringLastCacheRebuild(collection1Type2_m1)).isFalse();
		assertThat(manager1.get().excludedDuringLastCacheRebuild(collection1Type2_m2)).isTrue();
		assertThat(manager1.get().excludedDuringLastCacheRebuild(collection1Type2_m3)).isFalse();
		assertThat(manager1.get().excludedDuringLastCacheRebuild(collection2Type1_m1)).isTrue();
		assertThat(manager1.get().isConfigured(collection1Type1)).isTrue();
		assertThat(manager1.get().isConfigured(collection1Type2)).isTrue();
		assertThat(manager1.get().isConfigured(collection2Type1)).isFalse();

		LocalCacheConfigsServicesManager otherManager = new LocalCacheConfigsServicesManager(testFile1);
		assertThat(otherManager.get().excludedDuringLastCacheRebuild(collection1Type1_m1)).isFalse();
		assertThat(otherManager.get().excludedDuringLastCacheRebuild(collection1Type1_m2)).isFalse();
		assertThat(otherManager.get().excludedDuringLastCacheRebuild(collection1Type1_m3)).isTrue();
		assertThat(otherManager.get().excludedDuringLastCacheRebuild(collection1Type2_m1)).isFalse();
		assertThat(otherManager.get().excludedDuringLastCacheRebuild(collection1Type2_m2)).isTrue();
		assertThat(otherManager.get().excludedDuringLastCacheRebuild(collection1Type2_m3)).isFalse();
		assertThat(otherManager.get().excludedDuringLastCacheRebuild(collection2Type1_m1)).isTrue();
		assertThat(otherManager.get().isConfigured(collection1Type1)).isTrue();
		assertThat(otherManager.get().isConfigured(collection1Type2)).isTrue();
		assertThat(otherManager.get().isConfigured(collection2Type1)).isFalse();

		manager1.alter((configs) -> {
			configs.setCollectionTypeConfigs(collection1Type1, new CollectionTypeLocalCacheConfigs(asSet("type1_default_m3")));
			configs.clearTypeConfigs(collection1Type2);
		});

		assertThat(manager1.get().excludedDuringLastCacheRebuild(collection1Type1_m1)).isFalse();
		assertThat(manager1.get().excludedDuringLastCacheRebuild(collection1Type1_m2)).isFalse();
		assertThat(manager1.get().excludedDuringLastCacheRebuild(collection1Type1_m3)).isTrue();
		assertThat(manager1.get().excludedDuringLastCacheRebuild(collection1Type2_m1)).isTrue();
		assertThat(manager1.get().excludedDuringLastCacheRebuild(collection1Type2_m2)).isTrue();
		assertThat(manager1.get().excludedDuringLastCacheRebuild(collection1Type2_m3)).isTrue();
		assertThat(manager1.get().excludedDuringLastCacheRebuild(collection2Type1_m1)).isTrue();
		assertThat(manager1.get().isConfigured(collection1Type2)).isFalse();

		otherManager = new LocalCacheConfigsServicesManager(testFile1);
		assertThat(otherManager.get().excludedDuringLastCacheRebuild(collection1Type1_m1)).isFalse();
		assertThat(otherManager.get().excludedDuringLastCacheRebuild(collection1Type1_m2)).isFalse();
		assertThat(otherManager.get().excludedDuringLastCacheRebuild(collection1Type1_m3)).isTrue();
		assertThat(otherManager.get().excludedDuringLastCacheRebuild(collection1Type2_m1)).isTrue();
		assertThat(otherManager.get().excludedDuringLastCacheRebuild(collection1Type2_m2)).isTrue();
		assertThat(otherManager.get().excludedDuringLastCacheRebuild(collection1Type2_m3)).isTrue();
		assertThat(otherManager.get().excludedDuringLastCacheRebuild(collection2Type1_m1)).isTrue();
		assertThat(otherManager.get().isConfigured(collection1Type2)).isFalse();

	}

	private MetadataSchemaType mockMetadataSchemaType(int uniqueTenantId, String collectionCode, String typeCode) {
		MetadataSchemaType schemaType = mock(MetadataSchemaType.class);
		when(schemaType.getUniqueTenantId()).thenReturn(uniqueTenantId);
		when(schemaType.getCollection()).thenReturn(collectionCode);
		when(schemaType.getCode()).thenReturn(typeCode);

		return schemaType;
	}

	private MetadataSchema mockMetadataSchema(String collectionCode, String typeCode) {
		MetadataSchema schema = mock(MetadataSchema.class);
		when(schema.getCollection()).thenReturn(collectionCode);
		when(schema.getCode()).thenReturn(typeCode);

		return schema;
	}

	private Metadata mockMetadata(MetadataSchemaType schemaType, String localCode) {
		MetadataSchema schema = mockMetadataSchema(schemaType.getCollection(), schemaType.getCode() + "_default");
		String code = schemaType.getCode() + "_default_" + localCode;
		Metadata metadata = Mockito.mock(Metadata.class);
		when(metadata.getSchemaType()).thenReturn(schemaType);
		when(metadata.getLocalCode()).thenReturn(localCode);
		when(metadata.getCode()).thenReturn(code);
		when(metadata.getNoInheritanceCode()).thenReturn(code);
		when(metadata.getSchema()).thenReturn(schema);
		return metadata;
	}
}
