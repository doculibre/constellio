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
package com.constellio.data.frameworks.extensions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VaultBehaviorsList<T> {

	List<OrderedExtension<T>> extensions = new ArrayList<>();

	public void add(T extension) {
		add(100, extension);
	}

	public void add(int priority, T extension) {
		extensions.add(new OrderedExtension<>(extension, priority));
		Collections.sort(extensions);
	}

	public List<T> getBehaviors() {
		List<T> returnedBehaviors = new ArrayList<>();
		for (OrderedExtension<T> extension : extensions) {
			returnedBehaviors.add(extension.behavior);
		}

		return returnedBehaviors;
	}

}
