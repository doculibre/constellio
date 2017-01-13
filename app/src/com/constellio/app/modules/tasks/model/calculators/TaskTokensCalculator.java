package com.constellio.app.modules.tasks.model.calculators;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.schemas.calculators.TokensCalculator2;

public class TaskTokensCalculator implements MetadataValueCalculator<List<String>> {

	LocalDependency<List<String>> allAuthorizationsParam = LocalDependency
			.toAStringList(CommonMetadataBuilder.ALL_AUTHORIZATIONS);
	LocalDependency<List<String>> manualTokensParam = LocalDependency.toAStringList(CommonMetadataBuilder.MANUAL_TOKENS);
	LocalDependency<String> assigneeParam = LocalDependency.toAReference(Task.ASSIGNEE);
	LocalDependency<List<String>> assigneeUsersParam = LocalDependency.toAReference(Task.ASSIGNEE_USERS_CANDIDATES)
			.whichIsMultivalue();
	LocalDependency<List<String>> assigneeGroupsParam = LocalDependency.toAReference(Task.ASSIGNEE_GROUPS_CANDIDATES)
			.whichIsMultivalue();
	LocalDependency<List<String>> followersParam = LocalDependency.toAReference(Task.FOLLOWERS_IDS).whichIsMultivalue();
	LocalDependency<String> createdByParam = LocalDependency.toAReference(CommonMetadataBuilder.CREATED_BY);
	ReferenceDependency<List<String>> parentTokensParam = ReferenceDependency
			.toAString(Task.PARENT_TASK, Schemas.TOKENS.getLocalCode()).whichIsMultivalue();

	@Override
	public List<String> calculate(CalculatorParameters parameters) {
		List<String> tokens = new ArrayList<>(parameters.get(manualTokensParam));

		String assignee = parameters.get(assigneeParam);
		List<String> assigneeUsers = parameters.get(assigneeUsersParam);
		List<String> assigneeGroups = parameters.get(assigneeGroupsParam);
		List<String> followers = parameters.get(followersParam);
		String createdBy = parameters.get(createdByParam);
		List<String> parentTokens = parameters.get(parentTokensParam);

		tokens.add("r_" + createdBy);
		tokens.add("w_" + createdBy);
		tokens.add("d_" + createdBy);

		tokens.add("r_" + assignee);
		tokens.add("w_" + assignee);

		if (assigneeUsers != null) {
			for (String user : assigneeUsers) {
				tokens.add("r_" + user);
				tokens.add("w_" + user);
			}
		}

		if (assigneeGroups != null) {
			for (String group : assigneeGroups) {
				tokens.add("r_" + group);
				tokens.add("w_" + group);
			}
		}

		if (followers != null) {
			for (String follower : followers) {
				tokens.add("r_" + follower);
			}
		}

		if (parentTokens != null) {
			tokens.addAll(parentTokens);
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
		return Arrays.asList(allAuthorizationsParam, manualTokensParam, assigneeParam, assigneeGroupsParam, assigneeUsersParam,
				followersParam, createdByParam, parentTokensParam);
	}

}