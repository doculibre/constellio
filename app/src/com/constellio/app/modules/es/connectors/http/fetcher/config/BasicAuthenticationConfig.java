/**
 * Copyright © 2010 DocuLibre inc.
 *
 * This file is part of Constellio.
 *
 * Constellio is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Constellio is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Constellio.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.constellio.app.modules.es.connectors.http.fetcher.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores authentication information for the HTTP crawler.
 *
 * @author Nicolas Bélisle (nicolas.belisle@doculibre.com)
 */
public class BasicAuthenticationConfig {

	private final String username;

	private final String password;

	private List<String> filters = Collections
			.synchronizedList(new ArrayList<String>());

	public BasicAuthenticationConfig(String username, String password,
			List<String> filters) {
		super();
		this.username = username;
		this.password = password;
		this.filters = filters;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public void addFilter(String filter) {
		filters.add(filter);
	}

	public void addFilter(int index, String filter)
			throws IndexOutOfBoundsException {
		filters.add(index, filter);
	}

	public void removeFilter(int index)
			throws IndexOutOfBoundsException {
		filters.remove(index);
	}

	public String[] getFilters() {
		return this.filters.toArray(new String[0]);
	}
}
