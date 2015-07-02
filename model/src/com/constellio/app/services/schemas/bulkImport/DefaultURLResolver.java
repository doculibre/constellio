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
package com.constellio.app.services.schemas.bulkImport;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.streamFactories.StreamFactory;

public class DefaultURLResolver implements URLResolver {

	IOServices ioServices;

	public DefaultURLResolver(IOServices ioServices) {
		this.ioServices = ioServices;
	}

	@Override
	public StreamFactory<InputStream> resolve(final URL url, final String fileName) {
		return new StreamFactory<InputStream>() {

			@Override
			public InputStream create(String name)
					throws IOException {
				return ioServices.newBufferedInputStream(url.openStream(), name);
			}
		};
	}
}
