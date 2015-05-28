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
package com.constellio.data.dao.services.solr;

import com.constellio.data.utils.ImpossibleRuntimeException;

public class SolrDataStoreTypesUtils {

	static SolrDataStoreTypesFactory factory = new SolrDataStoreTypesFactory();

	public static String getMultivalueFieldCode(String code) {
		if (code.endsWith(factory.forText(false))) {
			return code.replace("_" + factory.forText(false), "_" + factory.forText(true));
		}
		if (code.endsWith(factory.forBoolean(false))) {
			return code.replace("_" + factory.forBoolean(false), "_" + factory.forBoolean(true));
		}
		if (code.endsWith(factory.forDate(false))) {
			return code.replace("_" + factory.forDate(false), "_" + factory.forDate(true));
		}
		if (code.endsWith(factory.forDateTime(false))) {
			return code.replace("_" + factory.forDateTime(false), "_" + factory.forDateTime(true));
		}
		if (code.endsWith(factory.forDouble(false))) {
			return code.replace("_" + factory.forDouble(false), "_" + factory.forDouble(true));
		}
		if (code.endsWith(factory.forString(false))) {
			return code.replace("_" + factory.forString(false), "_" + factory.forString(true));
		}

		throw new ImpossibleRuntimeException("Invalid code '" + code + "'");
	}

	public static String getSinglevalueFieldCode(String code) {
		if (code.endsWith(factory.forText(true))) {
			return code.replace("_" + factory.forText(true), "_" + factory.forText(false));
		}
		if (code.endsWith(factory.forBoolean(true))) {
			return code.replace("_" + factory.forBoolean(true), "_" + factory.forBoolean(false));
		}
		if (code.endsWith(factory.forDate(true))) {
			return code.replace("_" + factory.forDate(true), "_" + factory.forDate(false));
		}
		if (code.endsWith(factory.forDateTime(true))) {
			return code.replace("_" + factory.forDateTime(true), "_" + factory.forDateTime(false));
		}
		if (code.endsWith(factory.forDouble(true))) {
			return code.replace("_" + factory.forDouble(true), "_" + factory.forDouble(false));
		}
		if (code.endsWith(factory.forString(true))) {
			return code.replace("_" + factory.forString(true), "_" + factory.forString(false));
		}

		throw new ImpossibleRuntimeException("Invalid code '" + code + "'");
	}

}
