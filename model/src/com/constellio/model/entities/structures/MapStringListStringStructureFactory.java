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
package com.constellio.model.entities.structures;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.entities.schemas.StructureFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class MapStringListStringStructureFactory implements StructureFactory {

	private transient GsonBuilder gsonBuilder;
	private transient Gson gson;

	public MapStringListStringStructureFactory() {
		gsonBuilder = new GsonBuilder();
		gson = gsonBuilder.create();
	}

	@Override
	public String toString(ModifiableStructure structure) {
		return gson.toJson(structure);
	}

	@Override
	public ModifiableStructure build(String structure) {
		MapStringListStringStructure mapStringListStringStructure = new MapStringListStringStructure();
		if (StringUtils.isNotBlank(structure)) {
			TypeToken<MapStringListStringStructure> listTypeToken = new TypeToken<MapStringListStringStructure>() {
			};

			mapStringListStringStructure = gson.fromJson(structure, listTypeToken.getType());
			mapStringListStringStructure.dirty = false;
		}
		return mapStringListStringStructure;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

}
