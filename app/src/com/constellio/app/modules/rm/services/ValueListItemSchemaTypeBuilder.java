package com.constellio.app.modules.rm.services;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.modules.rm.wrappers.structures.CommentFactory;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.HierarchicalValueListItem;
import com.constellio.model.entities.records.wrappers.ValueListItem;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class ValueListItemSchemaTypeBuilder {

	public enum ValueListItemSchemaTypeCodeMode {REQUIRED_AND_UNIQUE, FACULTATIVE, DISABLED}

	MetadataSchemaTypesBuilder metadataSchemaTypesBuilder;

	public ValueListItemSchemaTypeBuilder(
			MetadataSchemaTypesBuilder metadataSchemaTypesBuilder) {
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

		defaultSchemaBuilder.getMetadata(Schemas.TITLE_CODE).setUniqueValue(options.titleUnique)
				.setDefaultRequirement(true).setMultiLingual(options.isMultilingual());

		MetadataBuilder codeMetadata = defaultSchemaBuilder.create(ValueListItem.CODE).setType(
				MetadataValueType.STRING).setSearchable(true).setUndeletable(true).setSchemaAutocomplete(true);

		List<Language> languages = new ArrayList<>(labels.keySet());
		for (Language language : languages) {
			codeMetadata.addLabel(language, $("init.valuelist.default.code"));
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
			descriptionMetadata.addLabel(language, $("init.valuelist.default.description"));
		}

		defaultSchemaBuilder.create(ValueListItem.COMMENTS).setMultivalue(true)
				.setType(MetadataValueType.ENUM).defineStructureFactory(CommentFactory.class);

		MetadataBuilder titleMetadata = defaultSchemaBuilder.getMetadata(Schemas.TITLE.getLocalCode()).setSearchable(true);
		for (Language language : languages) {
			if (labels.containsKey(language))
				titleMetadata.addLabel(language, $("init.valuelist.default.title", language.getLocale()));
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
