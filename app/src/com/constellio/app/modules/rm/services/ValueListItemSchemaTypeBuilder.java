package com.constellio.app.modules.rm.services;

import com.constellio.app.modules.rm.wrappers.structures.CommentFactory;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.HierarchicalValueListItem;
import com.constellio.model.entities.records.wrappers.ValueListItem;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.behaviors.SchemaExtension;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.validators.metadatas.IllegalCharactersValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class ValueListItemSchemaTypeBuilder {

	public enum ValueListItemSchemaTypeCodeMode {REQUIRED_AND_UNIQUE, FACULTATIVE, DISABLED}

	MetadataSchemaTypesBuilder metadataSchemaTypesBuilder;
	List<SchemaExtension> extensions;

	public ValueListItemSchemaTypeBuilder(MetadataSchemaTypesBuilder metadataSchemaTypesBuilder) {
		this.metadataSchemaTypesBuilder = metadataSchemaTypesBuilder;
	}

	public MetadataSchemaTypeBuilder createValueListItemSchema(String code, Map<Language, String> labels,
															   ValueListItemSchemaTypeBuilderOptions options) {

		if (labels == null) {
			labels = new HashMap<>();
		}

		if (labels == null || labels.size() == 0) {
			for (Language language : metadataSchemaTypesBuilder.getLanguages()) {
				labels.put(language, $("init." + code));
			}
		}

		MetadataSchemaTypeBuilder typeBuilder = metadataSchemaTypesBuilder.createNewSchemaType(code);
		typeBuilder.setLabels(labels);
		typeBuilder.setSecurity(false);

		MetadataSchemaBuilder defaultSchemaBuilder = typeBuilder.getDefaultSchema().setLabels(labels);

		defaultSchemaBuilder.create(ValueListItem.COMMENTS).setMultivalue(true)
				.setType(MetadataValueType.STRUCTURE).defineStructureFactory(CommentFactory.class);

		List<Language> languages = new ArrayList<>(labels.keySet());

		for (MetadataBuilder metadataBuilder : defaultSchemaBuilder.getMetadatas()) {
			for (Language language : languages) {
				if (metadataBuilder.getLabel(language) == null
					|| metadataBuilder.getLabel(language).equals(metadataBuilder.getLocalCode())) {
					String labelValue = $("init.allTypes.allSchemas." + metadataBuilder.getLocalCode(), language.getLocale());
					if (labelValue != null) {
						metadataBuilder.addLabel(language, labelValue);
					}
				}
			}
		}

		defaultSchemaBuilder.getMetadata(Schemas.TITLE_CODE).setUniqueValue(options.titleUnique)
				.setDefaultRequirement(true).setMultiLingual(options.isMultilingual());

		MetadataBuilder codeMetadata = defaultSchemaBuilder.create(ValueListItem.CODE);
		codeMetadata.setType(MetadataValueType.STRING);
		codeMetadata.setSearchable(true);
		codeMetadata.setUndeletable(true);
		codeMetadata.setSchemaAutocomplete(true);
		codeMetadata.addValidator(IllegalCharactersValidator.class);

		for (Language language : languages) {
			codeMetadata.addLabel(language, $("init.valuelist.default.code", language.getLocale()));
		}

		codeMetadata.setSchemaAutocomplete(true);
		if (options.codeMode == ValueListItemSchemaTypeCodeMode.REQUIRED_AND_UNIQUE) {
			codeMetadata.setUniqueValue(true).setDefaultRequirement(true);

		} else if (options.codeMode == ValueListItemSchemaTypeCodeMode.DISABLED) {
			codeMetadata.setEnabled(false);
		}

		MetadataBuilder descriptionMetadata = defaultSchemaBuilder.create(ValueListItem.DESCRIPTION)
				.setType(MetadataValueType.TEXT).setSearchable(true)
				.setUndeletable(true)
				.setMultiLingual(options.isMultilingual());

		for (Language language : languages) {
			descriptionMetadata.addLabel(language, $("init.valuelist.default.description", language.getLocale()));
		}

		MetadataBuilder abbreviationMetadata = defaultSchemaBuilder.create(ValueListItem.ABBREVIATION);
		abbreviationMetadata.setType(MetadataValueType.STRING);
		abbreviationMetadata.setSearchable(true);
		abbreviationMetadata.setUndeletable(true);
		abbreviationMetadata.setMultiLingual(options.isMultilingual());
		abbreviationMetadata.addValidator(IllegalCharactersValidator.class);

		for (Language language : languages) {
			abbreviationMetadata.addLabel(language, $("init.valuelist.default.abbreviation", language.getLocale()));
		}

		MetadataBuilder titleMetadata = defaultSchemaBuilder.getMetadata(Schemas.TITLE.getLocalCode());
		titleMetadata.setSearchable(true);
		titleMetadata.addValidator(IllegalCharactersValidator.class);

		for (Language language : languages) {
			if (labels.containsKey(language)) {
				titleMetadata.addLabel(language, $("init.valuelist.default.title", language.getLocale()));
			}
		}

		return typeBuilder;
	}

	public MetadataSchemaTypeBuilder createHierarchicalValueListItemSchema(String code, Map<Language, String> label,
																		   ValueListItemSchemaTypeBuilderOptions options) {
		List<Language> languages = metadataSchemaTypesBuilder.getLanguages();
		MetadataSchemaTypeBuilder typeBuilder = createValueListItemSchema(code, label, options);
		MetadataSchemaBuilder defaultSchemaBuilder = typeBuilder.getDefaultSchema();
		MetadataBuilder parentMetadata = defaultSchemaBuilder.create(HierarchicalValueListItem.PARENT)
				.defineChildOfRelationshipToType(typeBuilder)
				.setUndeletable(true);

		for (Language language : languages) {
			parentMetadata.addLabel(language, $("init.valuelist.default.parent"));
		}

		return typeBuilder;
	}

	public static class ValueListItemSchemaTypeBuilderOptions {
		ValueListItemSchemaTypeCodeMode codeMode;
		boolean titleUnique = true;
		boolean multilingual = true;

		private ValueListItemSchemaTypeBuilderOptions(ValueListItemSchemaTypeCodeMode codeMode) {
			this.codeMode = codeMode;
		}

		public static ValueListItemSchemaTypeBuilderOptions codeMetadataRequiredAndUnique() {
			return new ValueListItemSchemaTypeBuilderOptions(ValueListItemSchemaTypeCodeMode.REQUIRED_AND_UNIQUE);
		}

		public static ValueListItemSchemaTypeBuilderOptions codeMetadataDisabled() {
			return new ValueListItemSchemaTypeBuilderOptions(ValueListItemSchemaTypeCodeMode.DISABLED);
		}

		public static ValueListItemSchemaTypeBuilderOptions codeMetadataFacultative() {
			return new ValueListItemSchemaTypeBuilderOptions(ValueListItemSchemaTypeCodeMode.FACULTATIVE);
		}

		public static ValueListItemSchemaTypeBuilderOptions codeMode(ValueListItemSchemaTypeCodeMode codeMode) {
			return new ValueListItemSchemaTypeBuilderOptions(codeMode);
		}

		public boolean isMultilingual() {
			return multilingual;
		}

		public ValueListItemSchemaTypeBuilderOptions setMultilingual(boolean multilingual) {
			this.multilingual = multilingual;
			return this;
		}

		public ValueListItemSchemaTypeBuilderOptions titleUnique(boolean titleUnique) {
			this.titleUnique = titleUnique;
			return this;
		}
	}

}
