package com.constellio.model.services.schemas.builders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.sdk.tests.ConstellioTest;

public class AllowedReferencesBuilderTest extends ConstellioTest {

	AllowedReferencesBuilder allowedReferencesBuilder;

	@Mock MetadataSchemaTypeBuilder zeType;
	@Mock MetadataSchemaTypeBuilder otherType;

	@Mock MetadataSchemaBuilder zeTypeDefaultSchema;
	@Mock MetadataSchemaBuilder zeTypeCustomSchema;
	@Mock MetadataSchemaBuilder otherTypeDefaultSchema;

	@Before
	public void setUp()
			throws Exception {
		allowedReferencesBuilder = new AllowedReferencesBuilder();

		when(zeType.getCode()).thenReturn("zeType");
		when(otherType.getCode()).thenReturn("zeType");
		when(zeTypeDefaultSchema.getCode()).thenReturn("zeType_default");
		when(zeTypeCustomSchema.getCode()).thenReturn("zeType_custom");
		when(otherTypeDefaultSchema.getCode()).thenReturn("otherType_default");
	}

	@Test
	public void whenSettingSchemaTypeThenSchemaTypeSet() {
		allowedReferencesBuilder.set(zeType);

		assertThat(allowedReferencesBuilder.getSchemaType()).isEqualTo("zeType");
	}

	@Test(expected = AllowedReferencesBuilderRuntimeException.SchemaTypeAlreadySet.class)
	public void whenSettingSchemaTypeTwiceThenExceptionThrown() {
		allowedReferencesBuilder.set(zeType).set(zeType);
	}

	@Test
	public void whenAddingSchemasThenSchemasAdded() {
		allowedReferencesBuilder.add(zeTypeDefaultSchema).add(zeTypeCustomSchema);

		assertThat(allowedReferencesBuilder.getSchemas()).containsOnly("zeType_default", "zeType_custom");
	}

	@Test(expected = AllowedReferencesBuilderRuntimeException.CannotHaveBothATypeAndSchemas.class)
	public void givenAllowedSchemasWhenSettingSchemaTypeThenExceptionThrown() {
		allowedReferencesBuilder.add(zeTypeDefaultSchema).add(zeTypeCustomSchema);

		allowedReferencesBuilder.set(zeType);
	}

	@Test(expected = AllowedReferencesBuilderRuntimeException.CannotHaveBothATypeAndSchemas.class)
	public void givenAllowedSchemaTypeWhenAddingSchemaThenExceptionThrown() {
		allowedReferencesBuilder.set(zeType).add(zeTypeDefaultSchema);
	}

	@Test(expected = AllowedReferencesBuilderRuntimeException.AllSchemasMustBeOfSameType.class)
	public void givenSchemasOfOneTypeWhenAddingSchemaOfOtherTypeThenExceptionThrown() {
		allowedReferencesBuilder.add(zeTypeDefaultSchema).add(otherTypeDefaultSchema);
	}

}
