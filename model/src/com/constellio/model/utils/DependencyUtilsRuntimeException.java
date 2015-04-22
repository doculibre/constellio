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
package com.constellio.model.utils;

import java.util.List;

@SuppressWarnings("serial")
public class DependencyUtilsRuntimeException extends RuntimeException {

	public DependencyUtilsRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public DependencyUtilsRuntimeException(String message) {
		super(message);
	}

	public DependencyUtilsRuntimeException(Throwable cause) {
		super(cause);
	}

	@SuppressWarnings("rawtypes")
	public static class CyclicDependency extends DependencyUtilsRuntimeException {

		private final List cyclicDependencies;

		public CyclicDependency(List cyclicDependencies) {
			super(toMessage(cyclicDependencies));
			this.cyclicDependencies = cyclicDependencies;
		}

		private static String toMessage(List cyclicDependencies) {
			StringBuilder sb = new StringBuilder("There is a cyclic dependency : ");
			for (Object cyclicDependency : cyclicDependencies) {
				sb.append(cyclicDependency.toString());
				sb.append("->");
			}
			sb.append(cyclicDependencies.get(0).toString());
			return sb.toString();
		}

		@SuppressWarnings("unchecked")
		public <T> List<T> getCyclicDependencies() {
			return cyclicDependencies;
		}

	}

}
