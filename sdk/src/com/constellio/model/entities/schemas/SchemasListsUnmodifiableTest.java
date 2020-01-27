package com.constellio.model.entities.schemas;

import com.constellio.data.dao.services.records.DataStore;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.when;

public class SchemasListsUnmodifiableTest extends ConstellioTest {

	@Mock ConstellioEIMConfigs constellioEIMConfigs;
	@Mock Metadata metadata1;
	@Mock Metadata metadata2;
	@Mock MetadataSchema schema1;
	@Mock MetadataSchema schema2;
	@Mock MetadataSchema defaultSchema;

	Map<Language, String> labels;

	@Before
	public void setUp()
			throws Exception {
		labels = new HashMap<>();
		labels.put(Language.French, "aLabel");

	}

	@Test(expected = UnsupportedOperationException.class)
	public void whenClearingMetadatasListInSchemaThenExceptionThrown() {
		List<Metadata> metadatas = Arrays.asList(metadata1, metadata2);
		when(metadata1.getType()).thenReturn(MetadataValueType.BOOLEAN);
		when(metadata2.getType()).thenReturn(MetadataValueType.ENUM);
		when(metadata1.getId()).thenReturn((short) 1);
		when(metadata2.getId()).thenReturn((short) 2);
		Set<RecordValidator> validators = new HashSet<RecordValidator>();
		CollectionInfo zeCollectionInfo = new CollectionInfo((byte) 0, zeCollection, "fr", Arrays.asList("fr"));
		MetadataSchema schema = new MetadataSchema((short) 1, "aCode", "aCode", zeCollectionInfo, labels, metadatas, false, true, validators,
				null, DataStore.RECORDS, true, constellioEIMConfigs);

		schema.getMetadatas().clear();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void whenClearingSchemasListInSchemaTypeThenExceptionThrown() {
		List<MetadataSchema> schemas = Arrays.asList(schema1, schema2);
		CollectionInfo zeCollectionInfo = new CollectionInfo((byte) 0, zeCollection, "fr", Arrays.asList("fr"));
		when(defaultSchema.getMetadatas()).thenReturn(new MetadataList());
		when(schema1.getMetadatas()).thenReturn(new MetadataList());
		when(schema2.getMetadatas()).thenReturn(new MetadataList());
		MetadataSchemaType schemaType = new MetadataSchemaType((short) 0, "aCode", null, zeCollectionInfo, labels, schemas,
				defaultSchema, false, true, RecordCacheType.NOT_CACHED, true, false, "records");

		schemaType.getCustomSchemas().clear();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void whenClearingAllowedSchemasListsInAllowedReferencesThenExceptionThrown() {
		Set<String> schemas = new HashSet<String>(Arrays.asList("aSchema", "anotherSchema"));
		AllowedReferences allowedReferences = new AllowedReferences(null, schemas);
		allowedReferences.getAllowedSchemas().add("yetAnotherSchema");
	}
}
