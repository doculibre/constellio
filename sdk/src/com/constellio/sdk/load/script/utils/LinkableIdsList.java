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

import com.constellio.model.entities.records.wrappers.RecordWrapper;

public class LinkableIdsList {

	private int current = 0;
	private List<String> ids = new ArrayList<>();

	public LinkableIdsList() {
	}

	public LinkableIdsList(List<String> ids) {
		this.ids.addAll(ids);
		Collections.shuffle(this.ids);
	}

	public static LinkableIdsList forRecords(List<RecordWrapper> recordWrappers) {
		List<String> ids = new ArrayList<>();
		for (RecordWrapper recordWrapper : recordWrappers) {
			ids.add(recordWrapper.getId());
		}
		return new LinkableIdsList(ids);
	}

	public <T extends RecordWrapper> T attach(T wrapper) {
		ids.add(wrapper.getId());
		return wrapper;
	}

	public synchronized String next() {
		if (current + 1 >= ids.size()) {
			current = 0;
		} else {
			current++;
		}
		return ids.get(current);
	}
}




