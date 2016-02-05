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
