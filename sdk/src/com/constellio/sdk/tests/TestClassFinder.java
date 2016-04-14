package com.constellio.sdk.tests;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

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

	public static String getTestClassName() {
		StackTraceElement[] elements = new Throwable().fillInStackTrace().getStackTrace();

		for (int i = 0; i < elements.length; i++) {
			StackTraceElement element = elements[i];
			try {
				Class clz = Class.forName(element.getClassName());
				Method method = clz.getMethod(element.getMethodName(), new Class[0]);
				for (Annotation annotation : method.getAnnotations()) {
					if (annotation.annotationType() == org.junit.Test.class
							|| annotation.annotationType() == org.junit.Before.class
							|| annotation.annotationType() == org.junit.After.class) {
						return element.getClassName();
					}
				}
			} catch (NoSuchMethodException ex) {
			} catch (SecurityException ex) {
			} catch (ClassNotFoundException classNotFoundException) {
			}

		}

		throw new RuntimeException("Test class not found");

	}

}
