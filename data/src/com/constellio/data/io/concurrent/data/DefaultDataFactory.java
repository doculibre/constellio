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
package com.constellio.data.io.concurrent.data;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class DefaultDataFactory<T extends DataWrapper<?>> implements DataFactory<T>{
	private Class<T> type;

	public DefaultDataFactory(Class<T> type) {
		this.type = type;
	}

	@Override
	public T makeInstance() {
		try {
			Constructor<T> ctor = type.getDeclaredConstructor();
			T instance = ctor.newInstance();
			return instance;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException("Could not instantiate with default constructor", e);
		}
	}
}
