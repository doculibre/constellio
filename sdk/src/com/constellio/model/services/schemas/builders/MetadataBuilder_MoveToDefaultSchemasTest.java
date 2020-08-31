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
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public class MetadataBuilder_MoveToDefaultSchemasTest extends MetadataBuilderTest {

	private static final String CUSTOM_METADATA_CODE = "USRmetadata";
	private static final MetadataValueType CUSTOM_METADATA_VALUE_TYPE = MetadataValueType.NUMBER;
	private static final Object CUSTOM_METADATA_DEFAULT_VALUE = Arrays.asList(1, 2, 3);
	private static final boolean CUSTOM_METADATA_IS_MULTIVALUE = true;
	private static final Map<Language, String> CUSTOM_METADATA_LABELS = TestUtils.MapBuilder
			.with(Language.French, "Libellé en français")
			.andWith(Language.English, "Label in english")
			.build();

	private MetadataSchemaBuilder customSchemaBuilder;
	private MetadataBuilder metadataBuilderInCustomSchema;

	@Before
	@Override
	public void setup() {
		super.setup();

		customSchemaBuilder = schemaTypeBuilder.createCustomSchema("customSchemaType");

		metadataBuilderInCustomSchema = customSchemaBuilder
				.create(CUSTOM_METADATA_CODE)
				.setType(CUSTOM_METADATA_VALUE_TYPE)
				.setDefaultValue(CUSTOM_METADATA_DEFAULT_VALUE)
				.setMultivalue(CUSTOM_METADATA_IS_MULTIVALUE)
				.setLabels(CUSTOM_METADATA_LABELS);
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

		assertThatValueAreEqualOnMultipleInstance(CUSTOM_METADATA_CODE, MetadataBuilder::getLocalCode, metadataBuilderInDefaultSchema, metadataBuilderInCustomSchema);
		assertThatValueAreEqualOnMultipleInstance(CUSTOM_METADATA_IS_MULTIVALUE, MetadataBuilder::isMultivalue, metadataBuilderInDefaultSchema, metadataBuilderInCustomSchema);
		assertThatValueAreEqualOnMultipleInstance(CUSTOM_METADATA_VALUE_TYPE, MetadataBuilder::getType, metadataBuilderInDefaultSchema, metadataBuilderInCustomSchema);
		executeOnAllInstances(builder -> assertThatMapsAreEqual(builder.getLabels(), CUSTOM_METADATA_LABELS), metadataBuilderInDefaultSchema, metadataBuilderInCustomSchema);

	}

	private static <TInstance> void executeOnAllInstances(Consumer<TInstance> whatToDo, TInstance... instances) {
		Arrays.stream(instances)
				.forEach(whatToDo);
	}

	private static <TValue, TInstance> void assertThatValueAreEqualOnMultipleInstance(TValue value,
																					  Function<TInstance, TValue> valueProvider,
																					  TInstance... instances) {
		Arrays.stream(instances)
				.map(valueProvider)
				.forEach(valueGotten -> assertThat(valueGotten).isEqualTo(value));
	}

	private static <K, V> void assertThatMapsAreEqual(Map<K, V> actual, Map<K, V> expected) {
		assertThat(actual.entrySet().size()).isEqualTo(expected.entrySet().size());
		actual.forEach((key, value) -> assertThat(value).isEqualTo(expected.get(key)));
	}

	private static <TException extends Exception> ThrowableAssert assertThatExceptionIsThrown(
			@NotNull Class<TException> expectedException, @NotNull final Callable<?> action) {
		try {
			action.call();

			throw Failures.instance().failure("The action has completed it's task when an exception was expected." + System.lineSeparator() +
											  "Exception expected: " + expectedException.toString());
		} catch (Exception e) {
			return assertThat(e).isInstanceOf(expectedException);
		}
	}
}
