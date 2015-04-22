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
package com.constellio.data.io.concurrent.data;

/*
 * A wrapper around a type. This wrapper is responsible to convert the type to a byte array and 
 * construct the type from an array of byte.
 * @author Majid
 *
 * @param <T> The type that wrapper wraps it.
 */
public interface DataWrapper<T> {
	/*
	 * Constructs the <T> form the given data.
	 * @param data
	 */
	public void init(final byte[] data);

	/*
	 * Converts the inner type to the array of bytes;
	 * @return
	 */
	public byte[] toBytes();

	/*
	 * Give the inner data. Changing the output data should not change inner data. 
	 * @return
	 */
	public T getData();

	/*
	 * Set the internal data and return the wrapper
	 * @param data the value of internal data
	 * @return itself
	 */
	public DataWrapper<T> setData(final T data);
}
