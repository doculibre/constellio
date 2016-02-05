package com.constellio.model.entities.schemas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.sdk.tests.ConstellioTest;

public class AllowedReferencesTest extends ConstellioTest {

	AllowedReferences allowedReferences;

	@Mock MetadataSchemaType zeType;
	@Mock MetadataSchemaType anotherType;
	@Mock MetadataSchema zeTypeDefaultSchema;
	@Mock MetadataSchema zeTypeCustomSchema;
	@Mock MetadataSchema anotherTypeDefaultSchema;
	@Mock MetadataSchema anotherTypeCustomSchema;

	Set<String> allowedSchemasCodes;
	String allowedTypeCode;

	List<MetadataSchema> allowedSchemas;

	@Before
	public void setUp() {
		allowedSchemasCodes = new HashSet<>();
		allowedSchemasCodes.add("zeType_default");
		allowedSchemasCodes.add("zeType_custom");

		allowedSchemas = new ArrayList<>();
		allowedSchemas.add(zeTypeCustomSchema);

		allowedTypeCode = "zeType";

		when(zeType.getCode()).thenReturn(allowedTypeCode);
		when(zeType.getDefaultSchema()).thenReturn(zeTypeDefaultSchema);
		when(zeType.getSchemas()).thenReturn(allowedSchemas);
		when(anotherType.getCode()).thenReturn("anotherType");

		when(zeTypeDefaultSchema.getCode()).thenReturn("zeType_default");
		when(zeTypeCustomSchema.getCode()).thenReturn("zeType_custom");
		when(anotherTypeDefaultSchema.getCode()).thenReturn("anotherType_default");
		when(anotherTypeCustomSchema.getCode()).thenReturn("anotherType_custom");
	}

	@Test
	public void givenSchemaTypeIsAllowedWhenCheckingIfAllowedThenReturnTrue() {
		allowedReferences = new AllowedReferences(allowedTypeCode, null);

		assertThat(allowedReferences.isAllowed(zeType)).isTrue();
	}

	@Test
	public void givenSchemaTypeIsNotAllowedWhenCheckingIfAllowedThenReturnFalse() {
		allowedReferences = new AllowedReferences(allowedTypeCode, null);

		assertThat(allowedReferences.isAllowed(anotherType)).isFalse();
	}

	@Test
	public void givenSchemaIsAllowedWhenCheckingIfAllowedThenReturnTrue() {
		allowedReferences = new AllowedReferences(null, allowedSchemasCodes);

		assertThat(allowedReferences.isAllowed(zeTypeDefaultSchema)).isTrue();
		assertThat(allowedReferences.isAllowed(zeTypeCustomSchema)).isTrue();
	}

	@Test
	public void givenSchemaIsNotAllowedWhenCheckingIfAllowedThenReturnFalse() {
		allowedReferences = new AllowedReferences(null, allowedSchemasCodes);

		assertThat(allowedReferences.isAllowed(anotherTypeDefaultSchema)).isFalse();
		assertThat(allowedReferences.isAllowed(anotherTypeCustomSchema)).isFalse();
	}

	@Test
	public void givenAtLeastOneSchemaAllowedWhenCheckingIfAtLeastOneSchemaAllowedThenReturnTrue() {
		allowedReferences = new AllowedReferences(null, allowedSchemasCodes);

		assertThat(allowedReferences.isAtLeastOneSchemaAllowed(zeType)).isTrue();
	}

	@Test
	public void givenNoSchemaAllowedWhenCheckingIfAtLeastOneSchemaAllowedThenReturnFalse() {
		allowedReferences = new AllowedReferences("anotherType", null);

		assertThat(allowedReferences.isAtLeastOneSchemaAllowed(zeType)).isFalse();
	}

	@Test
	public void givenAllSchemasAllowedWhenCheckingIfAllSchemasAllowedThenReturnTrue() {
		allowedReferences = new AllowedReferences(null, allowedSchemasCodes);

		assertThat(allowedReferences.isAllSchemasAllowed(zeType)).isTrue();
	}

	@Test
	public void givenSchemaTypeAllowedWhenCheckingIfAllSchemasAllowedThenReturnTrue() {
		allowedReferences = new AllowedReferences(allowedTypeCode, null);

		assertThat(allowedReferences.isAllSchemasAllowed(zeType)).isTrue();

	}

	@Test
	public void givenSchemaTypeAllowedWhenCheckingIfASchemaOfTheSchemaTypeIsAllowedThenReturnTrue() {
		allowedReferences = new AllowedReferences(zeType.getCode(), null);

		assertThat(allowedReferences.isAllowed(zeTypeDefaultSchema)).isTrue();
		assertThat(allowedReferences.isAllowed(zeTypeCustomSchema)).isTrue();

	}

	@Test
	public void givenSchemaTypeAllowedAndSchemasEmptyWhenCheckingIfASchemaOfTheSchemaTypeIsAllowedThenReturnTrue() {
		allowedReferences = new AllowedReferences(zeType.getCode(), new HashSet<String>());

		assertThat(allowedReferences.isAllowed(zeTypeDefaultSchema)).isTrue();
		assertThat(allowedReferences.isAllowed(zeTypeCustomSchema)).isTrue();

	}

	@Test
	public void givenSchemaTypeAllowedhenGettingSchemaTypeWithAllowedSchemaThenRightSchemaReturned() {
		allowedReferences = new AllowedReferences(zeType.getCode(), new HashSet<String>());

		assertThat(allowedReferences.getTypeWithAllowedSchemas()).isEqualTo("zeType");
	}

	@Test
	public void givenSchemasAllowedhenGettingSchemaTypeWithAllowedSchemaThenRightSchemaReturned() {
		allowedReferences = new AllowedReferences(null, allowedSchemasCodes);

		assertThat(allowedReferences.getTypeWithAllowedSchemas()).isEqualTo("zeType");
	}

}
