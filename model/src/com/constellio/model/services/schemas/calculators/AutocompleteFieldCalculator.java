package com.constellio.model.services.schemas.calculators;

import com.constellio.data.utils.AccentApostropheCleaner;
import com.constellio.data.utils.Pair;
import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.DynamicLocalDependency;
import com.constellio.model.entities.enums.AutocompleteSplitCriteria;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

public class AutocompleteFieldCalculator extends AbstractMetadataValueCalculator<List<String>> {

	DynamicLocalDependency autocompleteMetadatasDependency = new LocalAutocompleteMetadatasDependency();
	ConfigDependency<AutocompleteSplitCriteria> autocompletSplitCriteriaConfigDependency
			= ConstellioEIMConfigs.AUTOCOMPLETE_SPLIT_CRITERIA.dependency();

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		Set<String> words = new HashSet<>();
		splitInLowerCasedTermsRemovingAccents(words, parameters.get(autocompleteMetadatasDependency),
				parameters.get(autocompletSplitCriteriaConfigDependency));
		List<String> returnedWords = new ArrayList<>(words);
		Collections.sort(returnedWords);
		return returnedWords;
	}

	public static void splitInLowerCasedTermsRemovingAccents(Set<String> words,
															 DynamicDependencyValues autocompleteMetadatasValues,
															 AutocompleteSplitCriteria autocompleteSplitCriteria) {

		Iterator<Pair<Metadata, Object>> iterator = autocompleteMetadatasValues.iterateWithValues();

		while (iterator.hasNext()) {
			splitInLowerCasedTermsRemovingAccents(words, iterator.next().getValue(), autocompleteSplitCriteria);
		}
	}

	public static void splitInLowerCasedTermsRemovingAccents(Set<String> words, Object value,
															 AutocompleteSplitCriteria autocompleteSplitCriteria) {
		String regex = autocompleteSplitCriteria.getRegex();

		if (value instanceof List) {
			for (String item : (List<String>) value) {
				splitInLowerCasedTermsRemovingAccents(words, item, regex);
			}
		} else if (value instanceof String) {
			splitInLowerCasedTermsRemovingAccents(words, (String) value, regex);

		}
	}

	private static void splitInLowerCasedTermsRemovingAccents(Set<String> words, String value, String regex) {
		if (value != null) {
			String cleanedValue = AccentApostropheCleaner.removeAccents(value).toLowerCase();
			for (String word : cleanedValue.split(regex)) {
				if (!Strings.isNullOrEmpty(word)) {
					words.add(word);
				}
			}
		}
	}

	@Override
	public List<String> getDefaultValue() {
		return new ArrayList<>();
	}

	@Override
	public MetadataValueType getReturnType() {
		return MetadataValueType.STRING;
	}

	@Override
	public boolean isMultiValue() {
		return true;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(autocompleteMetadatasDependency, autocompletSplitCriteriaConfigDependency);
	}

	public static class LocalAutocompleteMetadatasDependency extends DynamicLocalDependency {

		@Override
		public boolean isDependentOf(Metadata metadata, Metadata caclulatedMetadata) {
			return metadata.isSchemaAutocomplete() && metadata.getType().isStringOrText();
		}

		@Override
		public boolean isIncludingGlobalMetadatas() {
			return true;
		}

	}
}

