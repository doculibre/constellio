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
package com.constellio.sdk.load.script.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LinkableRecordsList<T> {

	private int current = 0;
	private List<T> records = new ArrayList<>();

	public LinkableRecordsList() {
	}

	public LinkableRecordsList(List<T> records) {
		this.records.addAll(records);
		Collections.shuffle(this.records);
	}

	public T attach(T wrapper) {
		records.add(wrapper);
		return wrapper;
	}

	public synchronized T next() {
		if (current + 1 >= records.size()) {
			current = 0;
		} else {
			current++;
		}
		return records.get(current);
	}
}




