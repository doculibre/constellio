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
package com.constellio.model.entities.calculators;

import java.util.Map;

import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;

public class CalculatorParameters {

	final Map<Dependency, Object> values;
	final String collection;

	public CalculatorParameters(Map<Dependency, Object> values, String collection) {
		super();
		this.values = values;
		this.collection = collection;
	}

	@SuppressWarnings("unchecked")
	public <T> T get(LocalDependency<T> dependency) {
		return (T) values.get(dependency);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(ReferenceDependency<T> dependency) {
		return (T) values.get(dependency);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(SpecialDependency<T> dependency) {
		return (T) values.get(dependency);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(ConfigDependency<T> dependency) {
		return (T) values.get(dependency);
	}

	public String getCollection() {
		return collection;
	}
}
