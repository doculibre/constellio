package com.constellio.model.services.schemas.calculators;

import com.constellio.data.utils.AccentApostropheCleaner;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.DynamicLocalDependency;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.*;

import static java.util.Arrays.asList;

public class AutocompleteFieldCalculator implements MetadataValueCalculator<List<String>> {

	DynamicLocalDependency autocompleteMetadatasDependency = new LocalAutocompleteMetadatasDependency();

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		Set<String> words = new HashSet<>();
		splitInLowerCasedTermsRemovingAccents(words, parameters.get(autocompleteMetadatasDependency));
		List<String> returnedWords = new ArrayList<>(words);
		Collections.sort(returnedWords);
		return returnedWords;
	}

	public static void splitInLowerCasedTermsRemovingAccents(Set<String> words,
															 DynamicDependencyValues autocompleteMetadatasValues) {
		for (Metadata metadata : autocompleteMetadatasValues.getAvailableMetadatasWithAValue().onlySchemaAutocomplete()) {
			splitInLowerCasedTermsRemovingAccents(words, autocompleteMetadatasValues.<Object>getValue(metadata));
		}
	}

	public static void splitInLowerCasedTermsRemovingAccents(Set<String> words, Object value) {
		if (value instanceof List) {
			for (String item : (List<String>) value) {
				splitInLowerCasedTermsRemovingAccents(words, item);
			}
		} else if (value instanceof String) {
			splitInLowerCasedTermsRemovingAccents(words, (String) value);

		}
	}

	public static void splitInLowerCasedTermsRemovingAccents(Set<String> words, String value) {
		if (value != null) {
			String cleanedValue = AccentApostropheCleaner.removeAccents(value).toLowerCase();
			for (String word : cleanedValue.split(" ")) {
				words.add(word);
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
		return asList(autocompleteMetadatasDependency);
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

