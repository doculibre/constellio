package com.constellio.model.services.schemas.builders;

import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.sdk.tests.TestUtils;
import org.assertj.core.api.ThrowableAssert;
import org.assertj.core.internal.Failures;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;

public class MetadataBuilder_MoveToDefaultSchemasTest extends MetadataBuilderTest {

	private static final String CUSTOM_METADATA_CODE = "USRmetadata";

	private MetadataSchemaBuilder customSchemaBuilder;
	private MetadataBuilder metadataBuilderInCustomSchema;

	@Before
	@Override
	public void setup() {
		super.setup();

		customSchemaBuilder = schemaTypeBuilder.createCustomSchema("customSchemaType");

		metadataBuilderInCustomSchema = customSchemaBuilder
				.create(CUSTOM_METADATA_CODE)
				.setType(MetadataValueType.NUMBER)
				.setDefaultValue(Arrays.asList(1, 2, 3))
				.setMultivalue(true)
				.setLabels(
						TestUtils.MapBuilder
								.with(Language.French, "Libellé en français")
								.andWith(Language.English, "Label in english")
								.build());
	}

	@Test
	public void whenMoveToDefaultSchemasMetadataIsCreatedInDefaultSchemaAndIsDisabled() {
		assertThatExceptionIsThrown(MetadataSchemaBuilderRuntimeException.NoSuchMetadata.class, () -> schemaBuilder.getMetadata(schemaBuilder.getCode() + "_" + CUSTOM_METADATA_CODE));

		customSchemaBuilder.getMetadata(CUSTOM_METADATA_CODE).moveToDefaultSchemas();

		assertThat(schemaBuilder.getMetadata(CUSTOM_METADATA_CODE)).isNotNull();

		MetadataBuilder metadataBuilderInDefaultSchema = schemaBuilder.getMetadata(CUSTOM_METADATA_CODE);

		assertThat(metadataBuilderInDefaultSchema.getEnabled()).isFalse();
		assertThat(metadataBuilderInCustomSchema.getEnabled()).isTrue();
	}

	@Test
	public void whenMoveToDefaultSchemasSomeMetadataPropertiesAreCopie() {
		customSchemaBuilder.getMetadata(CUSTOM_METADATA_CODE).moveToDefaultSchemas();

		MetadataBuilder metadataBuilderInDefaultSchema = schemaBuilder.getMetadata(CUSTOM_METADATA_CODE);

		assertThat(metadataBuilderInDefaultSchema.getLocalCode()).isEqualTo(metadataBuilderInCustomSchema.getLocalCode());
		assertThat(metadataBuilderInDefaultSchema.isMultivalue()).isEqualTo(metadataBuilderInCustomSchema.isMultivalue());
		assertThat(metadataBuilderInDefaultSchema.getType()).isEqualTo(metadataBuilderInCustomSchema.getType());
		assertThatMapsAreEqual(metadataBuilderInDefaultSchema.getLabels(), metadataBuilderInCustomSchema.getLabels());
	}

	private static <K, V> void assertThatMapsAreEqual(Map<K, V> actual, Map<K, V> expected) {
		assertThat(actual.entrySet().stream().count()).isEqualTo(expected.entrySet().stream().count());
		actual.forEach((key, value) -> assertThat(value).isEqualTo(expected.get(key)));
	}

	private static <TException extends Exception> ThrowableAssert assertThatExceptionIsThrown(
			@NotNull Class<TException> expectedException, @NotNull final Callable action) {
		try {
			action.call();

			throw Failures.instance().failure("The action has completed it's task when an exception was expected." + System.lineSeparator() +
											  "Exception expected: " + expectedException.toString());
		} catch (Exception e) {
			return assertThat(e).isInstanceOf(expectedException);
		}
	}
}
