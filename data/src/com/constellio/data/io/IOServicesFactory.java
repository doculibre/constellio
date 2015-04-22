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
package com.constellio.data.io;

import java.io.File;

import com.constellio.data.io.services.facades.FileService;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.utils.hashing.HashingService;

public class IOServicesFactory {

	private final File tempFolder;

	public IOServicesFactory(File tempFolder) {
		super();
		this.tempFolder = tempFolder;
	}

	public FileService newFileService() {
		return new FileService(tempFolder);
	}

	public ZipService newZipService() {
		return new ZipService(newIOServices());
	}

	public IOServices newIOServices() {
		return new IOServices(tempFolder);
	}

	public EncodingService newEncodingService() {
		return new EncodingService();
	}

	public HashingService newHashingService() {
		return HashingService.forSHA1(newEncodingService());
	}
}
