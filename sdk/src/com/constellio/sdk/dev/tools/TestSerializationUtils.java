package com.constellio.sdk.dev.tools;

import java.io.Serializable;

import org.mockito.internal.creation.DelegatingMethod;

import com.constellio.app.utils.ConstellioSerializationUtils;
import com.constellio.sdk.tests.ConstellioTest;

public class TestSerializationUtils {
	
	public static void validateSerializable(Serializable serializable) {
		validateSerializable(serializable, ConstellioTest.class, DelegatingMethod.class);
	}
	
	public static void validateSerializable(Serializable serializable, Class<?>...ignoredClasses) {
		ConstellioSerializationUtils.validateSerializable(serializable, ignoredClasses);
	}

}
