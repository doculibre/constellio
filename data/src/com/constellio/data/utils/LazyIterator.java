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

import java.util.Iterator;

import com.constellio.data.utils.LazyIteratorRuntimeException.LazyIteratorRuntimeException_IncorrectUsage;
import com.constellio.data.utils.LazyIteratorRuntimeException.LazyIteratorRuntimeException_RemoveNotAvailable;

public abstract class LazyIterator<T> implements Iterator<T> {

	boolean consumed = true;
	T next;

	@Override
	public final boolean hasNext() {
		if (consumed) {
			next = getNextOrNull();
			consumed = false;
		}
		return next != null;
	}

	@Override
	public final T next() {
		if (consumed) {
			next = getNextOrNull();
			consumed = false;
		}
		if (next == null) {
			throw new LazyIteratorRuntimeException_IncorrectUsage();
		}

		consumed = true;
		return next;
	}

	@Override
	public final void remove() {
		throw new LazyIteratorRuntimeException_RemoveNotAvailable();
	}

	protected abstract T getNextOrNull();
}
