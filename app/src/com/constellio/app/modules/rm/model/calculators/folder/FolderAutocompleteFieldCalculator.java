package com.constellio.app.modules.rm.model.calculators.folder;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.DynamicLocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.enums.AutocompleteSplitCriteria;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.schemas.calculators.AutocompleteFieldCalculator.LocalAutocompleteMetadatasDependency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.model.services.schemas.calculators.AutocompleteFieldCalculator.splitInLowerCasedTermsRemovingAccents;
import static java.util.Arrays.asList;

public class FolderAutocompleteFieldCalculator extends AbstractMetadataValueCalculator<List<String>> {

	DynamicLocalDependency autocompleteMetadatasDependency = new LocalAutocompleteMetadatasDependency();
	ReferenceDependency<List<String>> parentAutocompleteTermsDependency = ReferenceDependency
			.toAString(Folder.PARENT_FOLDER, Schemas.SCHEMA_AUTOCOMPLETE_FIELD.getLocalCode()).whichIsMultivalue();
	ConfigDependency<AutocompleteSplitCriteria> autocompletSplitCriteriaConfigDependency
			= ConstellioEIMConfigs.AUTOCOMPLETE_SPLIT_CRITERIA.dependency();

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		Set<String> words = new HashSet<>();
		AutocompleteSplitCriteria autocompleteSplitCriteria = parameters.get(autocompletSplitCriteriaConfigDependency);
		splitInLowerCasedTermsRemovingAccents(words, parameters.get(autocompleteMetadatasDependency), autocompleteSplitCriteria);
		splitInLowerCasedTermsRemovingAccents(words, parameters.get(parentAutocompleteTermsDependency), autocompleteSplitCriteria);
		List<String> returnedWords = new ArrayList<>(words);
		Collections.sort(returnedWords);
		return returnedWords;
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
		return asList(autocompleteMetadatasDependency, parentAutocompleteTermsDependency, autocompletSplitCriteriaConfigDependency);
	}

}
