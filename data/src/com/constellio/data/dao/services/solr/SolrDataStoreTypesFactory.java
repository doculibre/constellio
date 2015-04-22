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

import com.constellio.data.dao.services.DataStoreTypesFactory;

public class SolrDataStoreTypesFactory implements DataStoreTypesFactory {

	@Override
	public String forString(boolean multivalue) {
		return multivalue ? "ss" : "s";
	}

	@Override
	public String forText(boolean multivalue) {
		return multivalue ? "txt" : "t";
	}

	@Override
	public String forDouble(boolean multivalue) {
		return multivalue ? "ds" : "d";
	}

	@Override
	public String forDate(boolean multivalue) {
		return multivalue ? "das" : "da";
	}

	@Override
	public String forDateTime(boolean multivalue) {
		return multivalue ? "dts" : "dt";
	}

	// We use a string Solr type for boolean fields, because null is not possible in a Solr boolean.
	@Override
	public String forBoolean(boolean multivalue) {
		return multivalue ? "ss" : "s";
	}

}
