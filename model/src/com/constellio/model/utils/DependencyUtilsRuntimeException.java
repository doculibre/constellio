package com.constellio.model.utils;

import java.util.Map;
import java.util.Set;

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

		private final Map cyclicDependenciesMap;

		public CyclicDependency(Map cyclicDependenciesMap) {
			super(toMessage(cyclicDependenciesMap));
			this.cyclicDependenciesMap = cyclicDependenciesMap;
		}

		public CyclicDependency(Map cyclicDependenciesMap, String graph) {
			super("There is a cyclic dependency : \n" + graph);
			this.cyclicDependenciesMap = cyclicDependenciesMap;
		}

		private static String toMessage(Map<Object, Set<Object>> cyclicDependencies) {
			StringBuilder sb = new StringBuilder("There is a cyclic dependency : ");
			for (Map.Entry<Object, Set<Object>> cyclicDependency : cyclicDependencies.entrySet()) {
				sb.append(cyclicDependency.getKey() + "<-" + cyclicDependency.getValue() + ",  ");
			}
			return sb.toString();
		}

		@SuppressWarnings("unchecked")
		public <T> Map<T, Set<T>> getCyclicDependencies() {
			return (Map<T, Set<T>>) cyclicDependenciesMap;
		}

	}

}
