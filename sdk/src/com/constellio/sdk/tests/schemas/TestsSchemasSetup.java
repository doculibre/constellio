package com.constellio.sdk.tests.schemas;

import com.constellio.model.api.impl.schemas.validation.impl.CreationDateIsBeforeOrEqualToLastModificationDateValidator;
import com.constellio.model.api.impl.schemas.validation.impl.Maximum50CharsRecordMetadataValidator;
import com.constellio.model.api.impl.schemas.validation.impl.Maximum50CharsRecordMultivalueMetadataValidator;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataTransiency;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.testimpl.TestStructureFactory1;
import com.constellio.sdk.tests.setups.SchemaShortcuts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.CONTENT;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE_TIME;
import static com.constellio.model.entities.schemas.MetadataValueType.INTEGER;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.MetadataValueType.STRUCTURE;
import static com.constellio.model.entities.schemas.MetadataValueType.TEXT;
import static com.constellio.sdk.tests.TestUtils.asMap;

public class TestsSchemasSetup extends SchemasSetup {

	public static final String ZE_SCHEMA_TYPE_CODE = "zeSchemaType";
	public static final String ANOTHER_SCHEMA_TYPE_CODE = "anotherSchemaType";
	public static final String A_THIRD_SCHEMA_TYPE_CODE = "aThirdSchemaType";

	public static MetadataBuilderConfigurator limitedTo50Characters = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			builder.defineValidators().add(Maximum50CharsRecordMetadataValidator.class);
		}

	};
	public static MetadataBuilderConfigurator whichIsMultivaluesAndLimitedTo50Characters = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			builder.setMultivalue(true);
			builder.defineValidators().add(Maximum50CharsRecordMultivalueMetadataValidator.class);
		}

	};
	public static MetadataBuilderConfigurator limitedTo50CharactersInCustomSchema = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			getCustomMetadata(builder, schemaTypes).defineValidators().add(Maximum50CharsRecordMetadataValidator.class);
		}

	};
	public static MetadataBuilderConfigurator whichIsMultivaluesAndLimitedTo50CharactersInCustomSchema = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			builder.setMultivalue(true);
			getCustomMetadata(builder, schemaTypes).defineValidators().add(Maximum50CharsRecordMetadataValidator.class);
		}

	};
	public static MetadataBuilderConfigurator whichHasDefaultRequirement = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			builder.setDefaultRequirement(true);
		}

	};

	public static MetadataBuilderConfigurator whichHasInputMask(final String inputMask) {
		return new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setInputMask(inputMask);
			}

		};
	}

	public static MetadataBuilderConfigurator whichIsCalculatedUsing(
			final Class<? extends MetadataValueCalculator<?>> clazz) {
		return new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.defineDataEntry().asCalculated(clazz);
			}

		};
	}

	public static MetadataBuilderConfigurator whichIsScripted(final String script) {
		return new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.defineDataEntry().asJexlScript(script);
			}

		};
	}

	public static MetadataBuilderConfigurator whichHasTransiency(final MetadataTransiency transiency) {
		return new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setTransiency(transiency);
			}

		};
	}

	public static MetadataBuilderConfigurator whichHasDefaultValue(final Object value) {
		return new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setDefaultValue(value);
			}

		};
	}

	public static MetadataBuilderConfigurator whichIncreaseDependencyLevel = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			builder.setIncreasedDependencyLevel(true);
		}

	};

	public static MetadataBuilderConfigurator whichHasCustomAttributes(final String... attributes) {
		return new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				for (String attribute : attributes) {
					builder.addCustomAttribute(attribute);
				}
			}

		};
	}

	public static MetadataBuilderConfigurator whichIsEssentialInSummary = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			builder.setEssentialInSummary(true);
		}

	};


	public static MetadataBuilderConfigurator whichIsEncrypted = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			builder.setEncrypted(true);
		}

	};

	public static MetadataBuilderConfigurator whichIsUnmodifiable = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			builder.setUnmodifiable(true);
		}

	};
	public static MetadataBuilderConfigurator whichIsEssential = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			builder.setEssential(true);
		}

	};

	public static MetadataBuilderConfigurator whichIsMarkedForDeletion = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			builder.setMarkedForDeletion(true);
		}

	};
	public static MetadataBuilderConfigurator whichIsEnabled = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			builder.setEnabled(true);
		}

	};
	public static MetadataBuilderConfigurator whichIsDisabled = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			builder.setEnabled(false);
		}

	};
	public static MetadataBuilderConfigurator whichIsUndeletable = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			builder.setUndeletable(true);
		}

	};
	public static MetadataBuilderConfigurator whichIsMultivalue = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			builder.setMultivalue(true);
		}

	};

	public static MetadataBuilderConfigurator whichIsMultilingual = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			builder.setMultiLingual(true);
		}

	};
	public static MetadataBuilderConfigurator whichIsUnique = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			builder.setUniqueValue(true);
		}

	};

	public static MetadataBuilderConfigurator whichIsSearchable = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			builder.setSearchable(true);
		}

	};
	public static MetadataBuilderConfigurator whichIsNotSearchable = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			builder.setSearchable(false);
		}

	};
	public static MetadataBuilderConfigurator whichIsSortable = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			builder.setSortable(true);
		}

	};

	public static MetadataBuilderConfigurator whichIsSchemaAutocomplete = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			builder.setSchemaAutocomplete(true);
		}

	};
	public static MetadataBuilderConfigurator whichIsEnabledInCustomSchema = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			getCustomMetadata(builder, schemaTypes).setEnabled(true);
		}

	};
	public static MetadataBuilderConfigurator whichIsDisabledInCustomSchema = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			getCustomMetadata(builder, schemaTypes).setEnabled(false);
		}

	};
	public static MetadataBuilderConfigurator whichHasNoDefaultRequirement = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			builder.setDefaultRequirement(false);
		}

	};
	public static MetadataBuilderConfigurator whichIsSystemReserved = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			builder.setSystemReserved(true);
		}

	};
	public static MetadataBuilderConfigurator whichHasDefaultRequirementInCustomSchema = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			getCustomMetadata(builder, schemaTypes).setDefaultRequirement(true);
		}

	};
	public static MetadataBuilderConfigurator whichHasNoDefaultRequirementInCustomSchema = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			getCustomMetadata(builder, schemaTypes).setDefaultRequirement(false);
		}

	};
	public static MetadataBuilderConfigurator whichIsTaxonomyRelationship = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			builder.setTaxonomyRelationship(true);
		}

	};
	public static MetadataBuilderConfigurator whichIsProvidingSecurity = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			builder.setRelationshipProvidingSecurity(true);
		}

	};

	public static MetadataBuilderConfigurator whichIsChildOfRelationship = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			builder.setChildOfRelationship(true);
		}

	};
	public static MetadataBuilderConfigurator whichAllowsThirdSchemaType = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			builder.defineReferences().set(schemaTypes.getSchemaType("aThirdSchemaType"));
		}

	};
	public static MetadataBuilderConfigurator whichDoesNotAllowAnotherSchema = whichAllowsThirdSchemaType;
	public static MetadataBuilderConfigurator whichAllowsAnotherSchemaType = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			builder.defineReferences().set(schemaTypes.getSchemaType("anotherSchemaType"));
		}

	};
	public static MetadataBuilderConfigurator whichAllowsAnotherDefaultSchema = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			builder.defineReferences().add(schemaTypes.getSchema("anotherSchemaType_default"));
		}

	};

	public static MetadataBuilderConfigurator whichAllowsZeSchemaType = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			builder.defineReferences().add(schemaTypes.getSchema("zeSchemaType_default"));
		}

	};

	public static MetadataBuilderConfigurator whichIsCalculatedUsingPattern(final String pattern) {
		return new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.defineDataEntry().asJexlScript(pattern);
			}
		};
	}

	public static MetadataBuilderConfigurator whichHasStructureFactory = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			builder.defineStructureFactory(TestStructureFactory1.class);
		}

	};
	public static MetadataBuilderConfigurator whichIsDuplicable = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			builder.setDuplicable(true);
		}

	};

	protected boolean security = true;
	protected MetadataSchemaTypeBuilder zeSchemaTypeBuilder;
	protected MetadataSchemaBuilder zeDefaultSchemaBuilder;
	protected MetadataSchemaBuilder zeCustomSchemaBuilder;
	protected MetadataSchemaTypeBuilder anOtherSchemaTypeBuilder;
	protected MetadataSchemaBuilder anOtherDefaultSchemaBuilder;
	protected MetadataSchemaTypeBuilder aThirdSchemaTypeBuilder;
	protected MetadataSchemaBuilder athirdDefaultSchemaBuilder;

	public TestsSchemasSetup() {
		this("zeCollection");
	}

	public TestsSchemasSetup(String collection) {
		super(collection);
	}


	public TestsSchemasSetup(String collection, List<String> languages) {
		super(collection, languages);
	}

	public static MetadataBuilderConfigurator whichHasLabel(final String label) {

		return new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.addLabel(Language.French, label);
			}

		};
	}

	public static MetadataBuilderConfigurator whichHasLabelInCustomSchema(final String label) {

		return new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				getCustomMetadata(builder, schemaTypes).addLabel(Language.French, label);
			}

		};
	}

	public static MetadataBuilderConfigurator whichHasFixedSequence(final String fixedSequenceCode) {

		return new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.defineDataEntry().asFixedSequence(fixedSequenceCode);
			}

		};
	}

	public static MetadataBuilderConfigurator whichHasSequenceDefinedByMetadata(final String metadataLocalCode) {

		return new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.defineDataEntry().asSequenceDefinedByMetadata(metadataLocalCode);
			}

		};
	}

	private static MetadataBuilder getCustomMetadata(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
		return schemaTypes.getMetadata(builder.getCode().replace("_default_", "_custom_"));
	}

	public TestsSchemasSetup withSecurityFlag(boolean flag) {
		this.security = flag;
		return this;
	}

	public TestsSchemasSetup withTwoMetadatasCopyingAnotherSchemaValuesUsingTwoDifferentReferenceMetadata(
			boolean multivalue,
			boolean multivalueReferences, boolean required) {
		MetadataBuilder stringReference = zeDefaultSchemaBuilder.create("stringRef").setType(REFERENCE)
				.setMultivalue(multivalueReferences);
		MetadataBuilder copiedStringMetadata = zeDefaultSchemaBuilder.create("copiedStringMeta").setType(STRING)
				.setLabels(asMap(Language.French, "Une métadonnée copiée"))
				.setDefaultRequirement(required).setMultivalue(multivalue || multivalueReferences);
		MetadataBuilder otherSchemaStringMetadata = anOtherSchemaTypeBuilder.getDefaultSchema().create("stringMetadata")
				.setType(STRING).setMultivalue(multivalue);

		MetadataBuilder dateReference = zeDefaultSchemaBuilder.create("dateRef").setType(REFERENCE)
				.setMultivalue(multivalueReferences);
		MetadataBuilder copiedDateMetadata = zeDefaultSchemaBuilder.create("copiedDateMeta").setType(DATE_TIME)
				.setDefaultRequirement(required).setMultivalue(multivalue || multivalueReferences);
		MetadataBuilder otherSchemaDateMetadata = anOtherSchemaTypeBuilder.getDefaultSchema().create("dateMeta").setType(
				DATE_TIME)
				.setMultivalue(multivalue);

		stringReference.defineReferences().set(anOtherSchemaTypeBuilder);
		copiedStringMetadata.defineDataEntry().asCopied(stringReference, otherSchemaStringMetadata);

		dateReference.defineReferences().set(anOtherSchemaTypeBuilder);
		copiedDateMetadata.defineDataEntry().asCopied(dateReference, otherSchemaDateMetadata);

		return this;
	}

	public TestsSchemasSetup withCalculatedDaysBetweenLocalDateAndAnotherSchemaRequiredDate(boolean multivalue)
			throws Exception {
		MetadataBuilder dateReference = zeDefaultSchemaBuilder.create("dateRef").setType(REFERENCE).setMultivalue(multivalue);
		anOtherSchemaTypeBuilder.getDefaultSchema().create("dateMeta").setType(DATE_TIME);
		MetadataBuilder metadata = zeDefaultSchemaBuilder.create("daysBetween").setType(NUMBER);

		if (multivalue) {
			metadata.defineDataEntry().asCalculated(DaysBetweenMultivalueLocalDateAndAnotherSchemaRequiredDateCalculator.class);
		} else {
			metadata.defineDataEntry().asCalculated(DaysBetweenSingleLocalDateAndAnotherSchemaRequiredDateCalculator.class);
		}
		if (multivalue) {
			withADateTimeMetadata(whichIsMultivalue);
		} else {
			withADateTimeMetadata();
		}
		dateReference.defineReferences().set(anOtherSchemaTypeBuilder);

		return this;
	}

	public TestsSchemasSetup withCreationAndModificationDateInZeSchema()
			throws Exception {
		zeDefaultSchemaBuilder.create("creationDate").setType(DATE_TIME);
		zeDefaultSchemaBuilder.create("modificationDate").setType(DATE_TIME);
		zeDefaultSchemaBuilder.defineValidators().add(CreationDateIsBeforeOrEqualToLastModificationDateValidator.class);

		return this;
	}

	public TestsSchemasSetup withCreationAndModificationDateInZeCustomSchema()
			throws Exception {
		zeCustomSchemaBuilder.create("creationDate").setType(DATE_TIME);
		zeCustomSchemaBuilder.create("modificationDate").setType(DATE_TIME);
		zeCustomSchemaBuilder.defineValidators().add(CreationDateIsBeforeOrEqualToLastModificationDateValidator.class);

		return this;
	}

	public TestsSchemasSetup withATitle(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("zetitle").setType(STRING)
				.addLabel(Language.French, "Title");
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withAContent(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("content").setType(STRING)
				.addLabel(Language.French, "Content");
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withAParsedContent(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("parsedContent").setType(STRING)
				.addLabel(Language.French, "Parsed content");
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withAMetadata(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		return withAStringMetadata(builderConfigurators);
	}

	public TestsSchemasSetup withAStructureMetadata(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("structureMetadata").setType(STRUCTURE)
				.defineStructureFactory(TestStructureFactory1.class);
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withAStringMetadata(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("stringMetadata").setType(STRING)
				.addLabel(Language.French, "A toAString metadata");
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withAStringMetadataInAnotherSchema(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = anOtherDefaultSchemaBuilder.create("stringMetadata").setType(STRING)
				.addLabel(Language.French, "A toAString metadata");
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withAContentMetadata(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("contentMetadata").setType(CONTENT);
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withAMultivalueContentMetadata(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("contentMetadata").setType(CONTENT).setMultivalue(true);
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withAContentListMetadata(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("contentListMetadata").setType(CONTENT).setMultivalue(
				true);
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withALargeTextMetadata(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("largeTextMetadata").setType(TEXT);
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withAMultivaluedLargeTextMetadata(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("multivaluedLargeTextMetadata").setType(TEXT);
		metadataBuilder.setMultivalue(true);
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withZeDefaultSchema(MetadataSchemaTypeConfigurator... builderConfigurators)
			throws Exception {
		configureMetadataBuilder(zeSchemaTypeBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withAModifiedStringMetadata(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("stringMetadata").setType(STRING)
				.addLabel(Language.French, "A toAString metadata");
		metadataBuilder.addLabel(Language.French, "A modified label");
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withAnotherStringMetadata(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("anotherStringMetadata").setType(STRING)
				.addLabel(Language.French, "An other toAString metadata");
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withAThirdStringMetadata(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("thirdStringMetadata").setType(STRING)
				.addLabel(Language.French, "A third string metadata");
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withRecordValidator(Class<? extends RecordValidator> validatorClass)
			throws Exception {
		zeDefaultSchemaBuilder.defineValidators().add(validatorClass);
		return this;
	}

	public TestsSchemasSetup withAnotherSchemaStringMetadata(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = anOtherDefaultSchemaBuilder.create("stringMetadata").setType(STRING)
				.addLabel(Language.French, "String metadata");
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withADateTimeMetadata(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("dateTimeMetadata").setType(DATE_TIME)
				.addLabel(Language.French, "a date time metadata");
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withADateMetadata(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("dateMetadata").setType(DATE)
				.addLabel(Language.French, "a date metadata");
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withANumberMetadata(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("numberMetadata").setType(NUMBER)
				.addLabel(Language.French, "A number metadata");
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withANumberMetadataInAnotherSchema(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = anOtherDefaultSchemaBuilder.create("numberMetadata").setType(NUMBER)
				.addLabel(Language.French, "A number metadata");
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withAnIntegerMetadata(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("integerMetadata").setType(INTEGER)
				.addLabel(Language.French, "An integer metadata");
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withABooleanMetadata(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("booleanMetadata").setType(BOOLEAN)
				.addLabel(Language.French, "A boolean metadata");
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withAReferenceMetadata(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("referenceMetadata").setType(REFERENCE)
				.addLabel(Language.French, "A reference metadata");
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withAReferenceMetadataToZeSchema(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("referenceMetadata").setType(REFERENCE)
				.addLabel(Language.French, "A reference metadata");
		metadataBuilder.defineReferencesTo(zeSchemaTypeBuilder);
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withAParentReferenceFromZeSchemaToZeSchema(
			MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("parentReferenceFromZeSchemaToZeSchema")
				.defineChildOfRelationshipToSchemas(Arrays.asList(zeDefaultSchemaBuilder));
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withAParentReferenceFromAnotherSchemaToZeSchema(
			MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = anOtherDefaultSchemaBuilder.create("referenceFromAnotherSchemaToZeSchema")
				.defineChildOfRelationshipToType(zeDefaultSchemaBuilder.getSchemaTypeBuilder());
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withAReferenceFromAnotherSchemaToZeSchema(
			MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = anOtherDefaultSchemaBuilder.create("referenceFromAnotherSchemaToZeSchema")
				.defineReferencesTo(zeDefaultSchemaBuilder.getSchemaTypeBuilder());
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	@Override
	public void setUp() {
		zeSchemaTypeBuilder = typesBuilder.createNewSchemaType(ZE_SCHEMA_TYPE_CODE).setSecurity(security).setLabels(
				asMap(Language.French, "Ze type de schéma", Language.English, "Ze schema type"));
		anOtherSchemaTypeBuilder = typesBuilder.createNewSchemaType(ANOTHER_SCHEMA_TYPE_CODE).setSecurity(security);
		aThirdSchemaTypeBuilder = typesBuilder.createNewSchemaType(A_THIRD_SCHEMA_TYPE_CODE).setSecurity(security);
		zeDefaultSchemaBuilder = zeSchemaTypeBuilder.getDefaultSchema();
		anOtherDefaultSchemaBuilder = anOtherSchemaTypeBuilder.getDefaultSchema();
		athirdDefaultSchemaBuilder = aThirdSchemaTypeBuilder.getDefaultSchema();
	}

	public TestsSchemasSetup andCustomSchema() {
		zeCustomSchemaBuilder = zeSchemaTypeBuilder.createCustomSchema("custom");
		return this;
	}

	public String zeDefaultSchemaCode() {
		return "zeSchemaType_default";
	}

	public String zeCustomSchemaCode() {
		return "zeSchemaType_custom";
	}

	public String anotherDefaultSchemaCode() {
		return "anotherSchemaType_default";
	}

	public String aThirdDefaultSchemaCode() {
		return "aThirdSchemaType_default";
	}

	public String zeCustomSchemaTypeCode() {
		return "zeSchemaType";
	}

	public String anotherSchemaTypeCode() {
		return "anotherSchemaType";
	}

	public String aThirdSchemaTypeCode() {
		return "aThirdSchemaType";
	}

	public MetadataSchema zeDefaultSchema() {
		return getSchema(zeDefaultSchemaCode());
	}

	public MetadataSchemaType zeDefaultSchemaType() {
		return types.getSchemaType("zeSchemaType");
	}

	public MetadataSchema zeCustomSchema() {
		return getSchema(zeCustomSchemaCode());
	}

	public MetadataSchema anotherDefaultSchema() {
		return getSchema(anotherDefaultSchemaCode());
	}

	public MetadataSchema aThirdDefaultSchema() {
		return getSchema(aThirdDefaultSchemaCode());
	}

	public MetadataSchema zeCustomSchemaType() {
		return getSchema("zeSchemaType");
	}

	public MetadataSchema anotherSchemaType() {
		return getSchema("anotherSchemaType");
	}

	public MetadataSchema aThirdSchemaType() {
		return getSchema("aThirdSchemaType");
	}

	public MetadataSchema aThirdOtherSchemaType() {
		return getSchema("aThirdOtherSchemaType");
	}

	public TestsSchemasSetup withSchemaFrenchLabel(String label) {
		zeDefaultSchemaBuilder.addLabel(Language.French, label);
		return this;
	}

	public TestsSchemasSetup withTypeFrenchLabel(String label) {
		zeSchemaTypeBuilder.addLabel(Language.French, label);
		return this;
	}

	public TestsSchemasSetup withCustomSchemaFrenchLabel(String label) {
		zeCustomSchemaBuilder.addLabel(Language.French, label);
		return this;
	}

	public TestsSchemasSetup whichCustomSchemaIsUndeletable()
			throws Exception {
		zeCustomSchemaBuilder.setUndeletable(true);
		return this;
	}

	public TestsSchemasSetup whichCustomSchemaIsDeletable()
			throws Exception {
		zeCustomSchemaBuilder.setUndeletable(false);
		return this;
	}

	public TestsSchemasSetup withADateTimeMetadataInCustomSchema(MetadataBuilderConfigurator... builderConfigurators) {
		MetadataBuilder metadataBuilder = zeCustomSchemaBuilder.create("customDate").setType(DATE_TIME);
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withAStringMetadataInCustomSchema(MetadataBuilderConfigurator... builderConfigurators) {
		MetadataBuilder metadataBuilder = zeCustomSchemaBuilder.create("customString").setType(STRING);
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withAnEnumMetadata(Class enumClass,
												MetadataBuilderConfigurator... builderConfigurators) {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("withAnEnumMetadata").defineAsEnum(enumClass);
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public static MetadataBuilderConfigurator whichIsReferencing(SchemaShortcuts schemaShortcuts) {
		String schemaCode = schemaShortcuts.code();
		String typeCode = new SchemaUtils().getSchemaTypeCode(schemaCode);
		return whichIsReferencing(typeCode);
	}

	public static MetadataBuilderConfigurator whichIsReferencing(final String typeCode) {
		return new MetadataBuilderConfigurator() {
			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder type = schemaTypes.getSchemaType(typeCode);
				builder.defineReferencesTo(type);
			}
		};
	}

	public TestsSchemasSetup withAFixedSequence(MetadataBuilderConfigurator... builderConfigurators) {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("fixedSequenceMetadata").defineDataEntry()
				.asFixedSequence("zeSequence");
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withADynamicSequence() {
		zeDefaultSchemaBuilder.create("metadataDefiningSequenceNumber").setType(STRING);
		zeDefaultSchemaBuilder.create("dynamicSequenceMetadata").defineDataEntry()
				.asSequenceDefinedByMetadata("metadataDefiningSequenceNumber");
		return this;
	}

	public TestsSchemasSetup whichIsIsStoredInDataStore(String dataStore) {
		zeSchemaTypeBuilder.setDataStore(dataStore);
		return this;
	}

	public static class ZeSchemaMetadatasAdapter implements SchemaShortcuts {

		ZeSchemaMetadatas zeSchemaMetadatas;

		public ZeSchemaMetadatasAdapter(ZeSchemaMetadatas zeSchemaMetadatas) {
			this.zeSchemaMetadatas = zeSchemaMetadatas;
		}

		public Metadata metadataWithCode(String code) {
			return zeSchemaMetadatas.metadataWithCode(code);
		}

		@Override
		public String code() {
			return zeSchemaMetadatas.code();
		}

		@Override
		public String collection() {
			return zeSchemaMetadatas.collection();
		}

		public Metadata stringCopiedFromFirstReferenceStringMeta() {
			return zeSchemaMetadatas.stringCopiedFromFirstReferenceStringMeta();
		}

		public Metadata dateCopiedFromSecondReferenceDateMeta() {
			return zeSchemaMetadatas.dateCopiedFromSecondReferenceDateMeta();
		}

		public String firstReferenceToAnotherSchemaCompleteCode() {
			return zeSchemaMetadatas.firstReferenceToAnotherSchemaCompleteCode();
		}

		public Metadata dynamicSequenceMetadata() {
			return zeSchemaMetadatas.dynamicSequenceMetadata();
		}

		public Metadata fixedSequenceMetadata() {
			return zeSchemaMetadatas.fixedSequenceMetadata();
		}

		public Metadata metadataDefiningSequenceNumber() {
			return zeSchemaMetadatas.metadataDefiningSequenceNumber();
		}

		public Metadata firstReferenceToAnotherSchema() {
			return zeSchemaMetadatas.firstReferenceToAnotherSchema();
		}

		public Metadata secondReferenceToAnotherSchema() {
			return zeSchemaMetadatas.secondReferenceToAnotherSchema();
		}

		public Metadata stringMetadata() {
			return zeSchemaMetadatas.stringMetadata();
		}

		public Metadata enumMetadata() {
			return zeSchemaMetadatas.enumMetadata();
		}

		public Metadata contentMetadata() {
			return zeSchemaMetadatas.contentMetadata();
		}

		public Metadata contentListMetadata() {
			return zeSchemaMetadatas.contentListMetadata();
		}

		public Metadata parentReferenceFromZeSchemaToZeSchema() {
			return zeSchemaMetadatas.parentReferenceFromZeSchemaToZeSchema();
		}

		public Metadata metadata() {
			return zeSchemaMetadatas.metadata();
		}

		public Metadata anotherStringMetadata() {
			return zeSchemaMetadatas.anotherStringMetadata();
		}

		public Metadata dateTimeMetadata() {
			return zeSchemaMetadatas.dateTimeMetadata();
		}

		public Metadata dateMetadata() {
			return zeSchemaMetadatas.dateMetadata();
		}

		public Metadata numberMetadata() {
			return zeSchemaMetadatas.numberMetadata();
		}

		public Metadata integerMetadata() {
			return zeSchemaMetadatas.integerMetadata();
		}

		public Metadata booleanMetadata() {
			return zeSchemaMetadatas.booleanMetadata();
		}

		public Metadata referenceMetadata() {
			return zeSchemaMetadatas.referenceMetadata();
		}

		public Metadata creationDate() {
			return zeSchemaMetadatas.creationDate();
		}

		public Metadata modificationDate() {
			return zeSchemaMetadatas.modificationDate();
		}

		public Metadata title() {
			return zeSchemaMetadatas.title();
		}

		public Metadata content() {
			return zeSchemaMetadatas.content();
		}

		public Metadata parsedContent() {
			return zeSchemaMetadatas.parsedContent();
		}

		public Metadata calculatedDaysBetween() {
			return zeSchemaMetadatas.calculatedDaysBetween();
		}

		public List<Metadata> allFieldsList() {
			return zeSchemaMetadatas.allFieldsList();
		}

		public MetadataSchema instance() {
			return zeSchemaMetadatas.instance();
		}

		public Metadata metadata(String code) {
			return zeSchemaMetadatas.metadata(code);
		}

		public Metadata path() {
			return zeSchemaMetadatas.path();
		}

		public Metadata parentpath() {
			return zeSchemaMetadatas.parentpath();
		}

		public Metadata largeTextMetadata() {
			return zeSchemaMetadatas.largeTextMetadata();
		}

		public Metadata multivaluedLargeTextMetadata() {
			return zeSchemaMetadatas.multivaluedLargeTextMetadata();
		}

		public String typeCode() {
			return zeSchemaMetadatas.typeCode();
		}

		public MetadataSchemaType type() {
			return zeSchemaMetadatas.type();
		}
	}

	public class ZeSchemaMetadatas implements SchemaShortcuts {

		public Metadata dynamicSequenceMetadata() {
			return getMetadata(code() + "_" + "dynamicSequenceMetadata");
		}

		public Metadata fixedSequenceMetadata() {
			return getMetadata(code() + "_" + "fixedSequenceMetadata");
		}

		public Metadata metadataDefiningSequenceNumber() {
			return getMetadata(code() + "_" + "metadataDefiningSequenceNumber");
		}

		public Metadata metadataWithCode(String code) {
			return getMetadata(code);
		}

		public String code() {
			return "zeSchemaType_default";
		}

		public String collection() {
			return collection;
		}

		public Metadata stringCopiedFromFirstReferenceStringMeta() {
			return getMetadata(code() + "_" + "copiedStringMeta");
		}

		public Metadata dateCopiedFromSecondReferenceDateMeta() {
			return getMetadata(code() + "_" + "copiedDateMeta");
		}

		public String firstReferenceToAnotherSchemaCompleteCode() {
			return code() + "_" + "stringRef";
		}

		public Metadata firstReferenceToAnotherSchema() {
			return getMetadata(firstReferenceToAnotherSchemaCompleteCode());
		}

		public Metadata secondReferenceToAnotherSchema() {
			return getMetadata(code() + "_" + "dateRef");
		}

		public Metadata stringMetadata() {
			return getMetadata(code() + "_" + "stringMetadata");
		}

		public Metadata enumMetadata() {
			return getMetadata(code() + "_" + "withAnEnumMetadata");
		}

		public Metadata contentMetadata() {
			return getMetadata(code() + "_" + "contentMetadata");
		}

		public Metadata contentListMetadata() {
			return getMetadata(code() + "_" + "contentListMetadata");
		}

		public Metadata parentReferenceFromZeSchemaToZeSchema() {
			return getMetadata(code() + "_" + "parentReferenceFromZeSchemaToZeSchema");
		}

		public Metadata metadata() {
			return stringMetadata();
		}

		public Metadata anotherStringMetadata() {
			return getMetadata(code() + "_" + "anotherStringMetadata");
		}

		public Metadata dateTimeMetadata() {
			return getMetadata(code() + "_" + "dateTimeMetadata");
		}

		public Metadata dateMetadata() {
			return getMetadata(code() + "_" + "dateMetadata");
		}

		public Metadata numberMetadata() {
			return getMetadata(code() + "_" + "numberMetadata");
		}

		public Metadata integerMetadata() {
			return getMetadata(code() + "_" + "integerMetadata");
		}

		public Metadata booleanMetadata() {
			return getMetadata(code() + "_" + "booleanMetadata");
		}

		public Metadata referenceMetadata() {
			return getMetadata(code() + "_" + "referenceMetadata");
		}

		public Metadata creationDate() {
			return getMetadata(code() + "_" + "creationDate");
		}

		public Metadata modificationDate() {
			return getMetadata(code() + "_" + "modificationDate");
		}

		public Metadata title() {
			return getMetadata(code() + "_" + "zetitle");
		}

		public Metadata content() {
			return getMetadata(code() + "_" + "content");
		}

		public Metadata parsedContent() {
			return getMetadata(code() + "_" + "parsedContent");
		}

		public Metadata calculatedDaysBetween() {
			return getMetadata(code() + "_" + "daysBetween");
		}

		public List<Metadata> allFieldsList() {
			List<Metadata> metadatas = new ArrayList<>();
			metadatas.addAll(getSchema(code()).getMetadatas());
			return Collections.unmodifiableList(metadatas);
		}

		public MetadataSchema instance() {
			return getSchema(code());
		}

		public Metadata metadata(String code) {
			return getMetadata(code() + "_" + code);
		}

		public Metadata path() {
			return getMetadata(code() + "_path");
		}

		public Metadata parentpath() {
			return getMetadata(code() + "_parentpath");
		}

		public Metadata largeTextMetadata() {
			return getMetadata(code() + "_largeTextMetadata");
		}

		public Metadata multivaluedLargeTextMetadata() {
			return getMetadata(code() + "_multivaluedLargeTextMetadata");
		}

		public String typeCode() {
			return "zeSchemaType";
		}

		public MetadataSchemaType type() {
			return get(code().split("_")[0]);
		}
	}

	public class ZeCustomSchemaMetadatas extends ZeSchemaMetadatas {

		@Override
		public String code() {
			return "zeSchemaType_custom";
		}

		public Metadata customStringMetadata() {
			return getMetadata(code() + "_" + "customString");
		}

		public Metadata customDateMetadata() {
			return getMetadata(code() + "_" + "customDate");
		}

	}

	public class AnotherSchemaMetadatas implements SchemaShortcuts {

		public Metadata metadataWithCode(String code) {
			return getMetadata(code);
		}

		public String stringMetadataCompleteCode = code() + "_" + "stringMetadata";

		public String code() {
			return "anotherSchemaType_default";
		}

		public String collection() {
			return collection;
		}

		public Metadata stringMetadata() {
			return getMetadata(stringMetadataCompleteCode);
		}

		public Metadata dateMetadata() {
			return getMetadata(code() + "_" + "dateMeta");
		}

		public MetadataSchema instance() {
			return getSchema(code());
		}

		public MetadataSchemaType type() {
			return get(code().split("_")[0]);
		}

		public Metadata metadata(String code) {
			return getMetadata(this.code() + "_" + code);
		}

		public Metadata referenceFromAnotherSchemaToZeSchema() {
			return getMetadata(code() + "_" + "referenceFromAnotherSchemaToZeSchema");
		}

		public String typeCode() {
			return "anotherSchemaType";
		}
	}

	public class ThirdSchemaMetadatas implements SchemaShortcuts {

		public String code() {
			return "aThirdSchemaType_default";
		}

		public String collection() {
			return collection;
		}

		public MetadataSchema instance() {
			return getSchema(code());
		}

		public MetadataSchemaType type() {
			return get(code().split("_")[0]);
		}

		public Metadata metadata(String code) {
			return getMetadata(this.code() + "_" + code);
		}

		public String typeCode() {
			return "aThirdSchemaType";
		}
	}

}
