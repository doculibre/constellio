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
package com.constellio.data.io.services.facades;

import java.util.HashMap;
import java.util.Map;

import com.constellio.data.utils.ImpossibleRuntimeException;

public class OpenedResourcesWatcher {

	static Map<String, Object> openedResources = new HashMap<String, Object>();

	public static Map<String, Object> getOpenedResources() {
		return new HashMap<>(openedResources);
	}

	public static synchronized <T> T onOpen(T resource) {
		if (openedResources.containsKey(resource.toString())) {
			throw new ImpossibleRuntimeException("Resource named " + resource.toString() + " is alredy opened");
		}
		openedResources.put(resource.toString(), resource);
		return resource;
	}

	public static synchronized void onClose(Object resource) {
		openedResources.remove(resource.toString());
	}

	public static void clear() {
		openedResources.clear();
	}
}
