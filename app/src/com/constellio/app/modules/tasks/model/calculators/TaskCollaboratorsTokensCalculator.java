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
	LocalDependency<List<String>> collaboratorsGroupsParam = LocalDependency.toAReferenceList(Task.TASK_COLLABORATORS_GROUPS);
	LocalDependency<List<Boolean>> authorizationTypeParam = LocalDependency.toABooleanList(Task.TASK_COLLABORATORS_WRITE_AUTHORIZATIONS);
	LocalDependency<List<Boolean>> authorizationGroupTypeParam = LocalDependency.toABooleanList(Task.TASK_COLLABORATORS_GROUPS_WRITE_AUTHORIZATIONS);

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		List<String> tokens = new ArrayList<>(parameters.get(manualTokensParam));
		List<String> collaborators = parameters.get(collaboratorsParam);
		List<Boolean> writeAuthorizations = parameters.get(authorizationTypeParam);
		List<String> collaboratorsGroups = parameters.get(collaboratorsGroupsParam);
		List<Boolean> writeGroupAuthorizations = parameters.get(authorizationGroupTypeParam);

		List<String> collaboratorsWithWriteAuthorizations = new ArrayList<>();
		List<String> collaboratorsReadAuthorizations = new ArrayList<>();
		List<String> collaboratorsGroupsWithWriteAuthorizations = new ArrayList<>();
		List<String> collaboratorsGroupsReadAuthorizations = new ArrayList<>();

		for (int i = 0; i < collaborators.size(); i++) {
			if (writeAuthorizations.get(i)) {
				collaboratorsWithWriteAuthorizations.add(collaborators.get(i));
			} else {
				collaboratorsReadAuthorizations.add(collaborators.get(i));
			}
		}

		for (int i = 0; i < collaboratorsGroups.size(); i++) {
			if (writeGroupAuthorizations.get(i)) {
				collaboratorsGroupsWithWriteAuthorizations.add(collaboratorsGroups.get(i));
			} else {
				collaboratorsGroupsReadAuthorizations.add(collaboratorsGroups.get(i));
			}
		}

		if (!collaboratorsWithWriteAuthorizations.isEmpty()) {
			for (String user : collaboratorsWithWriteAuthorizations) {
				tokens.add("r_" + user);
				tokens.add("w_" + user);
			}
		}

		if (!collaboratorsReadAuthorizations.isEmpty()) {
			for (String user : collaboratorsReadAuthorizations) {
				tokens.add("r_" + user);
			}
		}

		if (!collaboratorsGroupsWithWriteAuthorizations.isEmpty()) {
			for (String user : collaboratorsGroupsWithWriteAuthorizations) {
				tokens.add("r_" + user);
				tokens.add("w_" + user);
			}
		}

		if (!collaboratorsGroupsReadAuthorizations.isEmpty()) {
			for (String user : collaboratorsGroupsReadAuthorizations) {
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
		return Arrays.asList(manualTokensParam, collaboratorsParam, authorizationTypeParam, collaboratorsGroupsParam, authorizationGroupTypeParam);
	}
}
