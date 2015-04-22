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
package com.constellio.model.services.schemas.builders;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassListBuilder<T> {

	Class<?> implementedClass;

	Set<String> implementationsClassname = new HashSet<>();

	public ClassListBuilder(Class<?> implementedClass) {
		this.implementedClass = implementedClass;
	}

	public ClassListBuilder(Class<?> implementedClass, Set<T> implementations) {
		this.implementedClass = implementedClass;
		for (T implementation : implementations) {
			this.implementationsClassname.add(implementation.getClass().getName());
		}
	}

	public ClassListBuilder<T> add(String name) {
		this.implementationsClassname.add(name);
		return this;
	}

	public <I> ClassListBuilder<T> add(Class<I> implementationClass) {
		this.implementationsClassname.add(implementationClass.getName());
		return this;
	}

	public Set<T> build() {
		return build(new HashSet<T>());
	}

	@SuppressWarnings("unchecked")
	public Set<T> build(Set<T> otherIncludedImplementations) {
		Set<T> instanciatedImplementations = new HashSet<>();
		Set<String> allClassNames = new HashSet<>();
		for (T otherIncludedImplementation : otherIncludedImplementations) {
			allClassNames.add(otherIncludedImplementation.getClass().getName());
		}
		allClassNames.addAll(implementationsClassname);

		for (String implementationClassname : allClassNames) {
			try {
				Class<T> implementationClass = (Class<T>) Class.forName(implementationClassname);
				if (!implementedClass.isAssignableFrom(implementationClass)) {
					throw new ClassListBuilderRuntimeException.ClassDoesntImplementInterface(implementationClass.getName(),
							implementedClass);
				}
				instanciatedImplementations.add(implementationClass.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				throw new ClassListBuilderRuntimeException.CannotInstanciate(implementationClassname, e);
			} catch (ClassNotFoundException e) {
				throw new ClassListBuilderRuntimeException.ClassNotFound(implementationClassname, e);
			}
		}
		return instanciatedImplementations;
	}

	public <I> ClassListBuilder<T> remove(Class<I> implementationClass) {
		this.implementationsClassname.remove(implementationClass.getName());
		return this;
	}

	public ClassListBuilder<T> remove(String name) {
		this.implementationsClassname.remove(name);
		return this;
	}

	public void set(List<String> validators) {
		this.implementationsClassname.clear();
		this.implementationsClassname.addAll(validators);
	}
}
