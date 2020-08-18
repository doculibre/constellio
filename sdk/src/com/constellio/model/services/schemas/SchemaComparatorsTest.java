package com.constellio.model.services.schemas;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SchemaComparatorsTest extends ConstellioTest {

	@Mock MetadataSchemaTypes types;

	@Mock Record zeFirstSchemaTypeDefaultRecord;

	@Mock Record zeFirstSchemaTypeCustomRecord;

	@Mock Record anotherSchemaTypeCustomRecord;

	@Mock Record anotherSchemaTypeDefaultRecord;

	@Before
	public void setUp()
			throws Exception {
		when(types.getSchemaTypesCodesSortedByDependency()).thenReturn(Arrays.asList("zeFirstSchemaType", "anotherSchemaType"));

	}

	@Test
	public void whenSortingRecordsBySchemaTypes()
			throws Exception {

		when(zeFirstSchemaTypeDefaultRecord.getSchemaCode()).thenReturn("zeFirstSchemaType_default");
		when(zeFirstSchemaTypeCustomRecord.getSchemaCode()).thenReturn("zeFirstSchemaType_custom");
		when(anotherSchemaTypeCustomRecord.getSchemaCode()).thenReturn("anotherSchemaType_custom");
		when(anotherSchemaTypeDefaultRecord.getSchemaCode()).thenReturn("anotherSchemaType_default");

		List<Record> records = Arrays.asList(anotherSchemaTypeCustomRecord, zeFirstSchemaTypeDefaultRecord,
				zeFirstSchemaTypeCustomRecord,
				anotherSchemaTypeDefaultRecord);

		Collections.sort(records, SchemaComparators.sortRecordsBySchemasDependencies(types));

		assertThat(records)
				.containsExactly(zeFirstSchemaTypeDefaultRecord, zeFirstSchemaTypeCustomRecord, anotherSchemaTypeCustomRecord,
						anotherSchemaTypeDefaultRecord);

	}

	@Test
	public void whenSortingMetadataByAscCodeThenCorrect()
			throws Exception {

		Metadata metadataABC = mock(Metadata.class);
		when(metadataABC.getLocalCode()).thenReturn("abc");

		Metadata metadataDEF = mock(Metadata.class);
		when(metadataDEF.getLocalCode()).thenReturn("def");

		Metadata metadataGHI = mock(Metadata.class);
		when(metadataGHI.getLocalCode()).thenReturn("ghi");

		List<Metadata> metadatas = new ArrayList<>(Arrays.asList(metadataGHI, metadataABC, metadataDEF));

		Collections.sort(metadatas, SchemaComparators.METADATA_COMPARATOR_BY_ASC_LOCAL_CODE);

		assertThat(metadatas).containsExactly(metadataABC, metadataDEF, metadataGHI);

	}

	@Test
	public void whenSortingMetadataSchemaByAscCodeThenCorrect()
			throws Exception {

		MetadataSchema metadataSchemaABC = mock(MetadataSchema.class);
		when(metadataSchemaABC.getLocalCode()).thenReturn("abc");

		MetadataSchema metadataSchemaDEF = mock(MetadataSchema.class);
		when(metadataSchemaDEF.getLocalCode()).thenReturn("def");

		MetadataSchema metadataSchemaGHI = mock(MetadataSchema.class);
		when(metadataSchemaGHI.getLocalCode()).thenReturn("ghi");

		List<MetadataSchema> metadatas = new ArrayList<>(
				Arrays.asList(metadataSchemaGHI, metadataSchemaABC, metadataSchemaDEF));

		Collections.sort(metadatas, SchemaComparators.SCHEMA_COMPARATOR_BY_ASC_LOCAL_CODE);

		assertThat(metadatas).containsExactly(metadataSchemaABC, metadataSchemaDEF, metadataSchemaGHI);

	}

	@Test
	public void whenSortingMetadataSchemaTypeByAscCodeThenCorrect()
			throws Exception {

		MetadataSchemaType metadataSchemaTypeABC = mock(MetadataSchemaType.class);
		when(metadataSchemaTypeABC.getCode()).thenReturn("abc");

		MetadataSchemaType metadataSchemaTypeDEF = mock(MetadataSchemaType.class);
		when(metadataSchemaTypeDEF.getCode()).thenReturn("def");

		MetadataSchemaType metadataSchemaTypeGHI = mock(MetadataSchemaType.class);
		when(metadataSchemaTypeGHI.getCode()).thenReturn("ghi");

		List<MetadataSchemaType> metadatas = new ArrayList<>(
				Arrays.asList(metadataSchemaTypeGHI, metadataSchemaTypeABC, metadataSchemaTypeDEF));

		Collections.sort(metadatas, SchemaComparators.SCHEMA_TYPE_COMPARATOR_BY_ASC_CODE);

		assertThat(metadatas).containsExactly(metadataSchemaTypeABC, metadataSchemaTypeDEF, metadataSchemaTypeGHI);

	}
}
