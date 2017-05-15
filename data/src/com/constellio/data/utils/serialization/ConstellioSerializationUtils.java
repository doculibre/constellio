package com.constellio.data.utils.serialization;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.Serializable;

import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.SerializationUtils;

import com.constellio.data.utils.serialization.SerializableChecker.ConstellioNotSerializableException;

public class ConstellioSerializationUtils {
	
	public static void validateSerializable(Serializable serializable) {
//		validateSerializable(serializable, (Class<Object>) null);
	}
	
	public static void validateSerializable(Serializable serializable, Class<?>...ignoredClasses) {
		try {
			// Test if the view is serializable
			SerializationUtils.serialize(serializable);
		} catch (SerializationException e) {
			// Not serializable, the SerializableChecker will produce a nice stack trace leading to the culprit
			NotSerializableException nse = (NotSerializableException) e.getCause();
			SerializableChecker checker = null;
			try {
				checker = new SerializableChecker(nse);
				checker.writeObject(serializable);
			} catch (IOException e1) {
				throw new RuntimeException(e1);
			} catch (ConstellioNotSerializableException e1) {
				if (ignoredClasses != null) {
					try {
						Class<?> notSerializableClass = Class.forName(nse.getMessage());
						// Is that an ignored class?
						boolean throwException = true;
						for (Class<?> ignoredClass : ignoredClasses) {
							if (ignoredClass != null && ignoredClass.isAssignableFrom(notSerializableClass)) {
								throwException = false;
								break;
							}
						}
						if (throwException) {
							throw e1;
						}
					} catch (ClassNotFoundException e2) {
						throw new RuntimeException(e2);
					}
				} else {
					throw e1;
				}
			} finally {
				if (checker != null) {
					try {
						checker.close();
					} catch (Exception e1) {
						// Ignore
					}
				}
			}
		}
	}

}
