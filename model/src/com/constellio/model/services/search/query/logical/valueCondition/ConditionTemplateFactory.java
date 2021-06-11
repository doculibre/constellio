package com.constellio.model.services.search.query.logical.valueCondition;

import com.constellio.data.utils.AccentApostropheCleaner;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.parser.LanguageDetectionManager;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;
import com.constellio.model.services.search.query.logical.condition.ConditionTemplate;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchConditionBuilder;
import com.constellio.model.services.search.query.logical.ongoing.OngoingLogicalSearchCondition;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.allConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.any;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.not;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.query;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.whereAny;
import static java.util.Arrays.asList;

public class ConditionTemplateFactory {

	private static final Pattern NUMBER_REGEX_PATTERN = Pattern.compile("\\d+");
	private String collection;
	private ModelLayerFactory modelLayerFactory;

	public ConditionTemplateFactory(ModelLayerFactory modelLayerFactory, String collection) {
		this.modelLayerFactory = modelLayerFactory;
		this.collection = collection;
	}

	public static ConditionTemplate schemaTypeIs(MetadataSchemaType type) {
		return schemaTypeIs(type.getCode());
	}

	public static ConditionTemplate schemaTypeIs(String type) {
		return schemaTypeIsIn(asList(type));
	}

	public static ConditionTemplate schemaTypeIsIn(List<String> types) {
		List<LogicalSearchValueCondition> schemaTypesCriteria = new ArrayList<>();
		for (String type : types) {
			schemaTypesCriteria.add(LogicalSearchQueryOperators.startingWithText(type));
		}
		return ConditionTemplate.field(Schemas.SCHEMA, any(schemaTypesCriteria));
	}

	public static ConditionTemplate schemaTypeIsNotIn(List<String> types) {
		List<LogicalSearchValueCondition> schemaTypesCriteria = new ArrayList<>();
		for (String type : types) {
			schemaTypesCriteria.add(LogicalSearchQueryOperators.startingWithText(type));
		}
		return ConditionTemplate.field(Schemas.SCHEMA, not(any(schemaTypesCriteria)));
	}

	public static LogicalSearchConditionBuilder autocompleteFieldMatching(String text) {
		return autocompleteFieldMatchingInMetadatas(text, asList(Schemas.SCHEMA_AUTOCOMPLETE_FIELD));
	}

	public static LogicalSearchConditionBuilder autocompleteFieldMatchingInMetadatas(String text,
																					 List<Metadata> metadatas) {
		if (StringUtils.isBlank(text)) {
			return new LogicalSearchConditionBuilder() {
				@Override
				public LogicalSearchCondition build(OngoingLogicalSearchCondition ongoing) {
					return ongoing.where(Schemas.IDENTIFIER).isEqualTo("a38");
				}
			};
		}

		final LogicalSearchCondition logicalSearchCondition;
		List<LogicalSearchCondition> conditions = new ArrayList<>();

		String[] splittedTextWords = text.split(" ");
		List<String> cleanedForAutocompleteExtraFieldsWordsList = new ArrayList<>();
		List<String> cleanedForAutocompleteFieldsWordsList = new ArrayList<>();
		String lastWord = "";

		for (int i = 0; i < splittedTextWords.length; i++) {
			String splittedTextWord = splittedTextWords[i];
			boolean containsNumber = NUMBER_REGEX_PATTERN.matcher(splittedTextWord).find();
			if (!containsNumber && !splittedTextWord.contains("-") && !splittedTextWord.contains("_") && !splittedTextWord.contains(".")) {
				cleanedForAutocompleteExtraFieldsWordsList.add(cleanText(splittedTextWord));
			} else {
				cleanedForAutocompleteFieldsWordsList.add(cleanText(splittedTextWord));
			}
			if (i == splittedTextWords.length - 1) {
				lastWord = cleanText(splittedTextWord);
			}
		}

		conditions.add(buildCondition(cleanedForAutocompleteFieldsWordsList, asList(Schemas.SCHEMA_AUTOCOMPLETE_FIELD), text.endsWith(" "),
				lastWord));
		List<Metadata> queryMetadatas = getListWithAutocompleteFieldMetadata(metadatas);
		conditions.add(buildCondition(cleanedForAutocompleteExtraFieldsWordsList, queryMetadatas, text.endsWith(" "), lastWord));
		conditions.removeIf(condition -> condition == null);
		logicalSearchCondition = allConditions(conditions);

		return ongoing -> ongoing.where(logicalSearchCondition);
	}

	private static List<Metadata> getListWithAutocompleteFieldMetadata(List<Metadata> metadatas) {
		boolean schemaAutocompleteAlreadyIncluded = false;
		for (Metadata metadata : metadatas) {
			if (metadata.getLocalCode().equals(Schemas.SCHEMA_AUTOCOMPLETE_FIELD.getLocalCode())) {
				schemaAutocompleteAlreadyIncluded = true;
			}
		}
		List<Metadata> queryMetadatas = new ArrayList<>(metadatas);
		if (!schemaAutocompleteAlreadyIncluded) {
			queryMetadatas.add(Schemas.SCHEMA_AUTOCOMPLETE_FIELD);
		}
		return queryMetadatas;
	}

	private static String cleanText(String text) {
		return AccentApostropheCleaner.removeAccents(text).replace("(", "").replace(")", "").toLowerCase();
	}

	private static LogicalSearchCondition buildCondition(List<String> cleanedTextWords, List<Metadata> metadatas,
														 boolean endsWithSpace, String lastWord) {
		LogicalSearchCondition condition;
		if (cleanedTextWords.size() >= 1) {
			List<LogicalSearchCondition> conditions = new ArrayList<>();
			for (int i = 0; i < cleanedTextWords.size(); i++) {
				if (lastWord.equals(cleanedTextWords.get(i))) {
					if (endsWithSpace) {
						conditions.add(whereAny(metadatas).isEqualTo(cleanedTextWords.get(i)));
					} else {
						conditions.add(whereAny(metadatas).isStartingWithText(cleanedTextWords.get(i)));
					}
				} else {
					conditions.add(whereAny(metadatas).isEqualTo(cleanedTextWords.get(i)));
				}
			}
			condition = allConditions(conditions);
		} else {
			condition = null;
		}
		return condition;
	}

	public ConditionTemplate metadatasHasAnalyzedValue(String value, Metadata... metadatas) {
		return metadatasHasAnalyzedValue(value, asList(metadatas));
	}

	public ConditionTemplate metadatasHasAnalyzedValue(String value, List<Metadata> metadatas) {

		List<String> availableLanguages = modelLayerFactory.getCollectionsListManager().getCollectionLanguages(collection);
		LanguageDetectionManager languageDetectionManager = modelLayerFactory.getLanguageDetectionManager();

		String language =
				availableLanguages.size() == 1 ? availableLanguages.get(0) : languageDetectionManager.tryDetectLanguage(value);
		List<Metadata> searchedMetadatas = new ArrayList<>();
		for (Metadata metadata : metadatas) {
			if (!availableLanguages.contains(language) || language == null) {
				for (String availableLanguage : availableLanguages) {
					searchedMetadatas.add(metadata.getSearchableMetadataWithLanguage(availableLanguage));
				}
			} else {
				searchedMetadatas.add(metadata.getSearchableMetadataWithLanguage(language));
			}
		}

		return ConditionTemplate.anyField(searchedMetadatas, query(value));
	}

	public ConditionTemplate titleHasAnalyzedValue(String value) {
		return metadatasHasAnalyzedValue(value, asList(Schemas.TITLE));
	}

}
