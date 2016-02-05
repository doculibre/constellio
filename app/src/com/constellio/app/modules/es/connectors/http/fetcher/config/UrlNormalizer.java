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

import java.net.MalformedURLException;
import java.net.URISyntaxException;

/**
 * Interface for url normalizers.
 *
 * @author Nicolas Bélisle (nicolas.belisle@doculibre.com)
 */
public interface UrlNormalizer {

	public String normalize(String url)
			throws MalformedURLException, URISyntaxException;
}