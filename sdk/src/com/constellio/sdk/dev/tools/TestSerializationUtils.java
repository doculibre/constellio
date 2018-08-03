package com.constellio.sdk.dev.tools;

import com.constellio.data.utils.serialization.ConstellioSerializationUtils;
import com.constellio.sdk.tests.ConstellioTest;
import org.mockito.internal.creation.DelegatingMethod;

import java.io.Serializable;

public class TestSerializationUtils {

	public static void validateSerializable(Serializable serializable) {
		validateSerializable(serializable, ConstellioTest.class, DelegatingMethod.class);
	}

	public static void validateSerializable(Serializable serializable, Class<?>... ignoredClasses) {
		ConstellioSerializationUtils.validateSerializable(serializable, ignoredClasses);
	}

}
