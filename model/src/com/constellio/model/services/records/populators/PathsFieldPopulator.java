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
package com.constellio.model.services.records.populators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.FieldsPopulator;

public class PathsFieldPopulator extends SeparatedFieldsPopulator implements FieldsPopulator {

	public PathsFieldPopulator(MetadataSchemaTypes types, boolean fullRewrite) {
		super(types, fullRewrite);
	}

	@Override
	public Map<String, Object> populateCopyfields(Metadata metadata, Object value) {

		if (Schemas.PATH.getLocalCode().equals(metadata.getLocalCode())) {
			Map<String, Object> copyfields = new HashMap<>();
			List<String> paths = (List) value;
			copyfields.put("pathParts_ss", getPathsParts(paths));
			return copyfields;
		} else {
			return Collections.emptyMap();
		}
	}

	private List<String> getPathsParts(List<String> paths) {
		Set<String> pathsParts = new HashSet<>();

		for (String path : paths) {
			String[] splittedPath = path.split("/");
			if (splittedPath.length >= 3) {
				String taxonomyCode = splittedPath[1];
				for (int i = 2; i < splittedPath.length; i++) {
					int level = i - 2;
					pathsParts.add(taxonomyCode + "_" + level + "_" + splittedPath[i]);
				}
			}

		}
		return new ArrayList<>(pathsParts);
	}
}
