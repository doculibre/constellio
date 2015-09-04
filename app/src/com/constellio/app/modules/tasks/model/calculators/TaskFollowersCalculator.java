/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
