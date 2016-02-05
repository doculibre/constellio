package com.constellio.app.modules.tasks.model.calculators;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollower;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

public class TaskFollowersCalculator implements MetadataValueCalculator<List<String>> {
    LocalDependency<List<TaskFollower>> taskFollowers = LocalDependency
            .toAStructure(Task.TASK_FOLLOWERS).whichIsMultivalue();

    @Override
    public List<String> calculate(CalculatorParameters parameters) {
        Set<String> ids = new HashSet<>();
        List<TaskFollower> taskFollowerList = parameters.get(taskFollowers);
        if(taskFollowerList != null) {
            for (TaskFollower taskFollower : taskFollowerList) {
                ids.add(taskFollower.getFollowerId());
            }
        }

        return new ArrayList<>(ids);
    }

    @Override
    public List<String> getDefaultValue() {
        return new ArrayList<>();
    }

    @Override
    public MetadataValueType getReturnType() {
        return MetadataValueType.REFERENCE;
    }

    @Override
    public boolean isMultiValue() {
        return true;
    }

    @Override
    public List<? extends Dependency> getDependencies() {
        return asList(taskFollowers);
    }
}
