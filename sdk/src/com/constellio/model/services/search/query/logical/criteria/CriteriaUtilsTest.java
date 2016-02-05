package com.constellio.model.services.search.query.logical.criteria;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE_TIME;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.sdk.tests.ConstellioTest;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class CriteriaUtilsTest extends ConstellioTest {

	@Mock Metadata metadata;

	@Before
	public void setUp()
			throws Exception {

	}

	@Test
	public void whenGettingBooleanValueThenRightStringReturned()
			throws Exception {
		assertThat(CriteriaUtils.getBooleanStringValue(true)).isEqualTo("__TRUE__");
		assertThat(CriteriaUtils.getBooleanStringValue(false)).isEqualTo("__FALSE__");
	}

	@Test
	public void whenGettingNullStringValueThenRightStringReturned()
			throws Exception {
		assertThat(CriteriaUtils.getNullStringValue()).isEqualTo("__NULL__");
	}

	@Test
	public void whenGettingNullDateValueThenRightValueReturned()
			throws Exception {
		assertThat(CriteriaUtils.getNullDateValue()).isEqualTo(CriteriaUtils.getNullDateValue());
	}

	@Test
	public void whenGettingNullNumberValueThenRightValueReturned()
			throws Exception {
		assertThat(CriteriaUtils.getNullNumberValue()).isEqualTo("" + Integer.MIN_VALUE);
	}

	@Test
	public void givenTextMetadataWhenGettingNullValueForMetadataThenRightValueReturned()
			throws Exception {
		Mockito.when(metadata.getType()).thenReturn(STRING);

		assertThat(CriteriaUtils.getNullValueForDataStoreField(metadata)).isEqualTo("__NULL__");
	}

	@Test
	public void givenBooleanMetadataWhenGettingNullValueForMetadataThenRightValueReturned()
			throws Exception {
		Mockito.when(metadata.getType()).thenReturn(BOOLEAN);

		assertThat(CriteriaUtils.getNullValueForDataStoreField(metadata)).isEqualTo("__NULL__");
	}

	@Test
	public void givenDateMetadataWhenGettingNullValueForMetadataThenRightValueReturned()
			throws Exception {
		Mockito.when(metadata.getType()).thenReturn(DATE_TIME);

		assertThat(CriteriaUtils.getNullValueForDataStoreField(metadata)).isEqualTo(CriteriaUtils.getNullDateValue());
	}

	@Test
	public void givenNumberMetadataWhenGettingNullValueForMetadataThenRightValueReturned()
			throws Exception {
		Mockito.when(metadata.getType()).thenReturn(NUMBER);

		assertThat(CriteriaUtils.getNullValueForDataStoreField(metadata)).isEqualTo("" + Integer.MIN_VALUE);
	}

	@Test
	public void givenReferenceMetadataWhenGettingNullValueForMetadataThenRightValueReturned()
			throws Exception {
		Mockito.when(metadata.getType()).thenReturn(REFERENCE);

		assertThat(CriteriaUtils.getNullValueForDataStoreField(metadata)).isEqualTo("__NULL__");
	}
}
