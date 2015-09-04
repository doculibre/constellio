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
package com.constellio.data.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.constellio.data.io.services.facades.IOServices;

public class BigFileFolderIterator extends LazyIterator<BigFileEntry> {

	private String resourceName;
	private Iterator<File> filesIterator;
	private BigFileIterator currentIterator;
	private InputStream currentIteratorInputStream;
	private IOServices ioServices;

	public BigFileFolderIterator(List<File> files, IOServices ioServices, String resourceName) {
		this.filesIterator = files.iterator();
		this.resourceName = resourceName;
		this.ioServices = ioServices;
	}

	public BigFileFolderIterator(File folder, IOServices ioServices, String resourceName) {
		this(getBigFilesIn(folder), ioServices, resourceName);
	}

	private static List<File> getBigFilesIn(File folder) {
		List<File> files = new ArrayList<>();
		for (File file : folder.listFiles()) {
			if (file.getName().endsWith(".bigf")) {
				files.add(file);
			}
		}
		return files;
	}

	@Override
	protected synchronized BigFileEntry getNextOrNull() {
		if (currentIterator == null || !currentIterator.hasNext()) {
			if (!filesIterator.hasNext()) {
				return null;
			} else {
				if (currentIteratorInputStream != null) {
					ioServices.closeQuietly(currentIteratorInputStream);
				}
				File nextFile = filesIterator.next();
				try {
					currentIteratorInputStream = ioServices.newBufferedFileInputStream(nextFile, resourceName);
					currentIterator = new BigFileIterator(currentIteratorInputStream);
				} catch (FileNotFoundException e) {
					throw new RuntimeException(e);
				}

				return getNextOrNull();
			}
		} else {
			return currentIterator.next();
		}

	}

	public void close() {
		if (currentIteratorInputStream != null) {
			ioServices.closeQuietly(currentIteratorInputStream);
		}
	}
}
