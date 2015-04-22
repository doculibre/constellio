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
package com.constellio.app.modules.rm.reports.factories.labels;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// TODO: Implement this
public class LabelConfiguration implements Serializable {
	private final String key;
	private final int columns;
	private final int lines;
	private final List<LabelField> fields;

	public static List<LabelConfiguration> getSupportedConfigurations() {
		List<LabelConfiguration> result = new ArrayList<>();
		result.add(new LabelConfiguration("left", 0, 0, null));
		result.add(new LabelConfiguration("right", 0, 0, null));
		return result;
	}

	public LabelConfiguration(
			String key, int columns, int lines, List<LabelField> fields) {
		this.key = key;
		this.columns = columns;
		this.lines = lines;
		this.fields = fields;
	}

	public String getKey() {
		return key;
	}
}
