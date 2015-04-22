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
package com.constellio.model.services.records.bulkImport;

import com.constellio.model.entities.schemas.Schemas;

public class Resolver {

	String metadata;

	String value;

	public String getMetadata() {
		return metadata;
	}

	public String getValue() {
		return value;
	}

	public static Resolver toResolver(String strValue) {
		Resolver resolver = new Resolver();
		int indexOfFirstColon = strValue.indexOf(":");
		if (indexOfFirstColon != -1) {
			resolver.metadata = strValue.substring(0, indexOfFirstColon);
			resolver.value = strValue.substring(indexOfFirstColon + 1);
		} else {
			resolver.metadata = Schemas.LEGACY_ID.getLocalCode();
			resolver.value = strValue;
		}
		return resolver;
	}

}
