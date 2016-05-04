package com.constellio.model.entities.schemas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.sdk.tests.ConstellioTest;

public class SchemasListsUnmodifiableTest extends ConstellioTest {

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
		Set<RecordValidator> validators = new HashSet<RecordValidator>();
		MetadataSchema schema = new MetadataSchema("aCode", "aCode", "zeCollection", labels, metadatas, false, true, validators,
				new ArrayList<Metadata>());

		schema.getMetadatas().clear();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void whenClearingSchemasListInSchemaTypeThenExceptionThrown() {
		List<MetadataSchema> schemas = Arrays.asList(schema1, schema2);
		MetadataSchemaType schemaType = new MetadataSchemaType("aCode", "zeCollection", labels, schemas,
				defaultSchema, false,
				true, true);

		schemaType.getSchemas().clear();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void whenClearingAllowedSchemasListsInAllowedReferencesThenExceptionThrown() {
		Set<String> schemas = new HashSet<String>(Arrays.asList("aSchema", "anotherSchema"));
		AllowedReferences allowedReferences = new AllowedReferences(null, schemas);
		allowedReferences.getAllowedSchemas().add("yetAnotherSchema");
	}
}
