package com.constellio.sdk.tests;

public class TestClassFinder {

	private TestClassFinder() {

	}

	@SuppressWarnings("unchecked")
	public static Class<? extends ConstellioTest> findCurrentTest() {

		String lastConstellioClassName = null;
		for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
			String className = stackTraceElement.getClassName();

			if ((className.startsWith("com.constellio") || className.startsWith("sct.services")) && !className.contains("$")) {
				lastConstellioClassName = className;
			}
		}

		if (lastConstellioClassName.contains(".ConstellioTest")) {
			throw new RuntimeException("Cannot use this class from ConstellioTest");
		}

		try {
			return (Class<? extends ConstellioTest>) Class.forName(lastConstellioClassName);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

	}

}
