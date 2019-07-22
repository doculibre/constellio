package com.constellio.model.services.records;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class RecordUtilsUnitTest extends ConstellioTest {

	RecordUtils recordUtils;

	@Mock Record recordOfCustomSchemaOfType1;
	@Mock Record recordOfDefaultSchemaOfType1;
	@Mock Record recordOfCustomSchemaOfType2;
	@Mock Record recordOfDefaultSchemaOfType2;

	@Before
	public void setUp()
			throws Exception {
		recordUtils = new RecordUtils();

		when(recordOfCustomSchemaOfType1.getSchemaCode()).thenReturn("type1_custom");
		when(recordOfDefaultSchemaOfType1.getSchemaCode()).thenReturn("type1_default");
		when(recordOfCustomSchemaOfType2.getSchemaCode()).thenReturn("type2_custom");
		when(recordOfDefaultSchemaOfType2.getSchemaCode()).thenReturn("type2_default");

	}

	@Test
	public void whenSplitRecordBySchemaTypeThenPutDefaultAndCustomSchemaOfSameTypesTogether()
			throws Exception {

		List<Record> records = Arrays.asList(recordOfCustomSchemaOfType1, recordOfDefaultSchemaOfType1,
				recordOfCustomSchemaOfType2, recordOfDefaultSchemaOfType2);

		Map<String, List<Record>> results = recordUtils.splitRecordsBySchemaTypes(records);
		assertThat(results).hasSize(2);
		assertThat(results)
				.containsEntry("type1", Arrays.asList(recordOfCustomSchemaOfType1, recordOfDefaultSchemaOfType1));
		assertThat(results)
				.containsEntry("type2", Arrays.asList(recordOfCustomSchemaOfType2, recordOfDefaultSchemaOfType2));
	}

	@Test
	public void whenGetMetadataLocalCodeWithoutPrefixThenGetValidValue()
			throws Exception {

		assertThat(SchemaUtils.getMetadataLocalCodeWithoutPrefix(
				Metadata.newDummyMetadata((short) 1, "zeSchema_default", "patate", MetadataValueType.STRING, true, false)))
				.isEqualTo("patate");

		assertThat(SchemaUtils.getMetadataLocalCodeWithoutPrefix(
				Metadata.newDummyMetadata((short) 1, "zeSchema_default", "USRpatate", MetadataValueType.STRING, true, false)))
				.isEqualTo("patate");

		assertThat(SchemaUtils.getMetadataLocalCodeWithoutPrefix(
				Metadata.newDummyMetadata((short) 1, "zeSchema_default", "MAPpatate", MetadataValueType.STRING, true, false)))
				.isEqualTo("patate");

		assertThat(SchemaUtils.getMetadataLocalCodeWithoutPrefix(
				Metadata.newDummyMetadata((short) 1, "zeSchema_default", "USRMAPpatate", MetadataValueType.STRING, true, false)))
				.isEqualTo("patate");

		assertThat(SchemaUtils.getMetadataLocalCodeWithoutPrefix(
				Metadata.newDummyMetadata((short) 1, "zeSchema_default", "MAPUSRpatate", MetadataValueType.STRING, true, false)))
				.isEqualTo("patate");

	}

	@Test
	public void whenGetIdWithoutZerosThenOk()
			throws Exception {

		assertThat(RecordUtils.removeZerosInId("0000042")).isEqualTo("42");
		assertThat(RecordUtils.removeZerosInId("000000")).isEqualTo("000000");
		assertThat(RecordUtils.removeZerosInId("0")).isEqualTo("0");
		assertThat(RecordUtils.removeZerosInId("101")).isEqualTo("101");
		assertThat(RecordUtils.removeZerosInId("00000420")).isEqualTo("420");
		assertThat(RecordUtils.removeZerosInId("hohoho")).isEqualTo("hohoho");

	}
}
