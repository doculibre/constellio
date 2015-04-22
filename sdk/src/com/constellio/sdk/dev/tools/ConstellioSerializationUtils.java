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
package com.constellio.sdk.dev.tools;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.Serializable;

import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.SerializationUtils;

import com.constellio.app.utils.SerializableChecker;
import com.constellio.app.utils.SerializableChecker.ConstellioNotSerializableException;

public class ConstellioSerializationUtils {

	public static void validateSerializable(Serializable serializable) {
		//validateSerializable(serializable, ConstellioTest.class, DelegatingMethod.class);
	}

	public static void validateSerializable(Serializable serializable, Class<?>... ignoredClasses) {
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
