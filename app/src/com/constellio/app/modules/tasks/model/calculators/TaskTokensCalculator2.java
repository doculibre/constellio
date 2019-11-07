package com.constellio.app.modules.tasks.model.calculators;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TaskTokensCalculator2 extends TaskTokensCalculator {
	LocalDependency<List<String>> collaboratorsParam = LocalDependency.toAReferenceList(Task.TASK_COLLABORATORS);
	LocalDependency<List<String>> collaboratorsGroupsParam = LocalDependency.toAReferenceList(Task.TASK_COLLABORATORS_GROUPS);
	LocalDependency<List<Boolean>> authorizationTypeParam = LocalDependency.toABooleanList(Task.TASK_COLLABORATORS_WRITE_AUTHORIZATIONS);
	LocalDependency<List<Boolean>> authorizationGroupTypeParam = LocalDependency.toABooleanList(Task.TASK_COLLABORATORS_GROUPS_WRITE_AUTHORIZATIONS);

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		Set<String> tokens = new HashSet<>();
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

		List<String> tokensList = new ArrayList<>();
		tokens.addAll(super.calculate(parameters));
		tokensList.addAll(tokens);

		return tokensList;
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return Arrays.asList(manualTokensParam, collaboratorsParam, authorizationTypeParam, collaboratorsGroupsParam, authorizationGroupTypeParam, manualTokensParam, assigneeParam, assigneeGroupsParam, assigneeUsersParam,
				followersParam, createdByParam, parentTokensParam);
	}
}
