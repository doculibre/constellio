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
import com.constellio.model.entities.schemas.AbstractMapBasedSeparatedStructureFactory.MapBasedStructure;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.SeparatedStructureFactory;
import com.constellio.model.entities.schemas.StructureFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;

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
			Pair<Metadata, Object> metadataAndValue = iterator.next();
			if (metadataAndValue.getKey().getLocalCode().equals(Schemas.IDENTIFIER.getLocalCode()) && metadataAndValue.getValue() != null) {
				splitInLowerCasedTermsRemovingAccents(words, metadataAndValue.getKey(), StringUtils.stripStart((String) metadataAndValue.getValue(), "0"), autocompleteSplitCriteria);
			}
			splitInLowerCasedTermsRemovingAccents(words, metadataAndValue.getKey(), metadataAndValue.getValue(), autocompleteSplitCriteria);
		}
	}

	public static void splitInLowerCasedTermsRemovingAccents(Set<String> words, Object value,
															 AutocompleteSplitCriteria autocompleteSplitCriteria) {
		splitInLowerCasedTermsRemovingAccents(words, null, value, autocompleteSplitCriteria);
	}

	public static void splitInLowerCasedTermsRemovingAccents(Set<String> words, Metadata metadata, Object value,
															 AutocompleteSplitCriteria autocompleteSplitCriteria) {
		String regex = autocompleteSplitCriteria.getRegex();

		if (valueIsStringList(value)) {
			for (String item : (List<String>) value) {
				splitInLowerCasedTermsRemovingAccents(words, item, regex);
			}
		} else if (value instanceof String) {
			splitInLowerCasedTermsRemovingAccents(words, (String) value, regex);

		} else if (valueIsSeparatedStructure(metadata, value)) {
			SeparatedStructureFactory factory = (SeparatedStructureFactory) metadata.getStructureFactory();
			String displayValue = "" + factory.toFields((ModifiableStructure) value).get(factory.getMainValueFieldName());
			splitInLowerCasedTermsRemovingAccents(words, displayValue, regex);
		}
	}

	private static boolean valueIsStringList(Object value) {
		return (value instanceof List && !((List) value).isEmpty())
			   && ((List) value).get(0) instanceof String;
	}

	private static boolean valueIsSeparatedStructure(Metadata metadata, Object value) {
		return value instanceof MapBasedStructure &&
			   metadata != null && metadata.getStructureFactory() instanceof SeparatedStructureFactory;
	}

	private static void splitInLowerCasedTermsRemovingAccents(Set<String> words, String value, String regex) {
		if (value != null) {
			String cleanedValue = AccentApostropheCleaner.removeAccents(value)
					.replace("(", "").replace(")", "").toLowerCase();
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
			return metadata.isSchemaAutocomplete() &&
				   (metadata.getType().isStringOrText() || (metadata.getType() == MetadataValueType.STRUCTURE &&
															metadata.getStructureFactory() instanceof StructureFactory));
		}

		@Override
		public boolean isIncludingGlobalMetadatas() {
			return true;
		}

	}
}

