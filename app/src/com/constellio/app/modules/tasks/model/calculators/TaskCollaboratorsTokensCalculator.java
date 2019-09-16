package com.constellio.app.modules.tasks.model.calculators;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

public class TaskCollaboratorsTokensCalculator extends AbstractMetadataValueCalculator<List<String>> {
	LocalDependency<List<String>> manualTokensParam = LocalDependency.toAStringList(CommonMetadataBuilder.MANUAL_TOKENS);
	LocalDependency<List<String>> collaboratorsParam = LocalDependency.toAReferenceList(Task.TASK_COLLABORATORS);
	LocalDependency<List<Boolean>> authorizationTypeParam = LocalDependency.toABooleanList(Task.TASK_COLLABORATORS_WRITE_AUTHORIZATIONS);

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		List<String> tokens = new ArrayList<>(parameters.get(manualTokensParam));
		List<String> collaborators = parameters.get(collaboratorsParam);
		List<Boolean> writeAuthorization = parameters.get(authorizationTypeParam);

		List<String> collaboratorsWithWriteAuthorizations = new ArrayList<>();
		List<String> collaboratorsReadAuthorizations = new ArrayList<>();

		for (int i = 0; i < collaborators.size(); i++) {
			if (writeAuthorization.get(i)) {
				collaboratorsWithWriteAuthorizations.add(collaborators.get(i));
			} else {
				collaboratorsReadAuthorizations.add(collaborators.get(i));
			}
		}

		if (!collaboratorsWithWriteAuthorizations.isEmpty()) {
			for (String user : collaboratorsWithWriteAuthorizations) {
				tokens.add("r_" + user);
				tokens.add("w_" + user);
			}
		}

		if (collaboratorsReadAuthorizations.isEmpty()) {
			for (String user : collaboratorsReadAuthorizations) {
				tokens.add("r_" + user);
			}
		}

		return tokens;
	}

	@Override
	public List<String> getDefaultValue() {
		return Collections.emptyList();
	}

	@Override
	public MetadataValueType getReturnType() {
		return STRING;
	}

	@Override
	public boolean isMultiValue() {
		return true;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Arrays.asList(manualTokensParam, collaboratorsParam, authorizationTypeParam);
	}
}
