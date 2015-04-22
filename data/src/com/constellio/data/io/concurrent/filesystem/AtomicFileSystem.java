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
package com.constellio.data.io.concurrent.filesystem;

import java.util.List;

import com.constellio.data.io.concurrent.data.DataWithVersion;

/*
 * This class represents a file system in the Constellio environment. 
 * @author Majid
 *
 */
public interface AtomicFileSystem {
	/*
	 * Atomic read data at the
	 * @param path
	 * @return a DataWithVersion if a file exists in the given path.
	 * @throws {@link ConcurrencyIOException} if no file exists in the given path.
	 */
	public DataWithVersion readData(String path);

	/*
	 * Write data to a specific path and return the a DataWithVersion with the updated version of the data. If the input version is null, the method does
	 * not check for optimistic locking. 
	 * @param path 
	 * @param dataWithVersion 
	 * @return An updated dataWithVersion with same data of the input but a new version
	 * @throws throws OptimisticLockingException if the version of file is different from the given input
	 */
	public DataWithVersion writeData(String path, DataWithVersion dataWithVersion);

	public void delete(String path, Object version);

	/*
	 * list all files in the given path
	 * @return list of all files in the given path 
	 */
	public List<String> list(String path);

	public boolean exists(String path);

	public boolean isDirectory(String path);

	public boolean mkdirs(String path);
}
