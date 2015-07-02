/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.sdk.tests.schemas;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.constellio.model.api.impl.schemas.validation.impl.CreationDateIsBeforeOrEqualToLastModificationDateValidator;
import com.constellio.model.api.impl.schemas.validation.impl.Maximum50CharsRecordMetadataValidator;
import com.constellio.model.api.impl.schemas.validation.impl.Maximum50CharsRecordMultivalueMetadataValidator;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.testimpl.TestStructureFactory1;
import com.constellio.sdk.tests.setups.SchemaShortcuts;

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

	public static MetadataBuilderConfigurator whichHasDefaultValue(final Object value) {
		return new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setDefaultValue(value);
			}

		};
	}

	public static MetadataBuilderConfigurator whichNullValuesAreNotWritten = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			builder.setWriteNullValues(false);
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

	public static MetadataBuilderConfigurator whichHasStructureFactory = new MetadataBuilderConfigurator() {

		@Override
		public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
			builder.defineStructureFactory(TestStructureFactory1.class);
		}

	};

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

	public static MetadataBuilderConfigurator whichHasLabel(final String label) {

		return new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setLabel(label);
			}

		};
	}

	public static MetadataBuilderConfigurator whichHasLabelInCustomSchema(final String label) {

		return new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				getCustomMetadata(builder, schemaTypes).setLabel(label);
			}

		};
	}

	private static MetadataBuilder getCustomMetadata(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
		return schemaTypes.getMetadata(builder.getCode().replace("_default_", "_custom_"));
	}

	public TestsSchemasSetup withTwoMetadatasCopyingAnotherSchemaValuesUsingTwoDifferentReferenceMetadata(boolean multivalue,
			boolean multivalueReferences, boolean required) {
		MetadataBuilder stringReference = zeDefaultSchemaBuilder.create("stringRef").setType(REFERENCE)
				.setMultivalue(multivalueReferences);
		MetadataBuilder copiedStringMetadata = zeDefaultSchemaBuilder.create("copiedStringMeta").setType(STRING)
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
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("zetitle").setType(STRING).setLabel("Title");
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withAContent(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("content").setType(STRING).setLabel("Content");
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withAParsedContent(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("parsedContent").setType(STRING)
				.setLabel("Parsed content");
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
				.setLabel("A toAString metadata");
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
				.setLabel("A toAString metadata");
		metadataBuilder.setLabel("A modified label");
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withAnotherStringMetadata(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("anotherStringMetadata").setType(STRING)
				.setLabel("An other toAString metadata");
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withAnotherSchemaStringMetadata(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = anOtherDefaultSchemaBuilder.create("stringMetadata").setType(STRING)
				.setLabel("String metadata");
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withADateTimeMetadata(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("dateTimeMetadata").setType(DATE_TIME)
				.setLabel("a date time metadata");
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withADateMetadata(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("dateMetadata").setType(DATE)
				.setLabel("a date metadata");
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withANumberMetadata(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("numberMetadata").setType(NUMBER)
				.setLabel("A number metadata");
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withAnIntegerMetadata(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("integerMetadata").setType(INTEGER)
				.setLabel("An integer metadata");
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withABooleanMetadata(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("booleanMetadata").setType(BOOLEAN)
				.setLabel("A boolean metadata");
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withAReferenceMetadata(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("referenceMetadata").setType(REFERENCE)
				.setLabel("A reference metadata");
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withAReferenceMetadataToZeSchema(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("referenceMetadata").setType(REFERENCE)
				.setLabel("A reference metadata");
		metadataBuilder.defineReferencesTo(zeSchemaTypeBuilder);
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withAParentReferenceFromZeSchemaToZeSchema(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = zeDefaultSchemaBuilder.create("parentReferenceFromZeSchemaToZeSchema")
				.defineChildOfRelationshipToSchemas(Arrays.asList(zeDefaultSchemaBuilder));
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	public TestsSchemasSetup withAReferenceFromAnotherSchemaToZeSchema(MetadataBuilderConfigurator... builderConfigurators)
			throws Exception {
		MetadataBuilder metadataBuilder = anOtherDefaultSchemaBuilder.create("referenceFromAnotherSchemaToZeSchema")
				.defineChildOfRelationshipToType(zeDefaultSchemaBuilder.getSchemaTypeBuilder());
		configureMetadataBuilder(metadataBuilder, typesBuilder, builderConfigurators);
		return this;
	}

	@Override
	public void setUp() {
		zeSchemaTypeBuilder = typesBuilder.createNewSchemaType(ZE_SCHEMA_TYPE_CODE);
		anOtherSchemaTypeBuilder = typesBuilder.createNewSchemaType(ANOTHER_SCHEMA_TYPE_CODE);
		aThirdSchemaTypeBuilder = typesBuilder.createNewSchemaType(A_THIRD_SCHEMA_TYPE_CODE);
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

	public TestsSchemasSetup withSchemaLabel(String label) {
		zeDefaultSchemaBuilder.setLabel(label);
		return this;
	}

	public TestsSchemasSetup withCustomSchemaLabel(String label) {
		zeCustomSchemaBuilder.setLabel(label);
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

	public class ZeSchemaMetadatas implements SchemaShortcuts {

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
			return "zeCollection";
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
			return "zeCollection";
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
