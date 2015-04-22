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

import com.constellio.data.utils.DelayedRuntimeException.DelayedRuntimeException_AlreadyDefined;
import com.constellio.data.utils.DelayedRuntimeException.DelayedRuntimeException_NotYetDefined;

public class Delayed<T> {

	boolean defined;

	T value;

	public Delayed(T value) {
		this.defined = true;
		this.value = value;
	}

	public Delayed() {
	}

	public T get() {
		if (!defined) {
			throw new DelayedRuntimeException_NotYetDefined();
		}
		return value;
	}

	public void set(T value) {
		if (defined) {
			throw new DelayedRuntimeException_AlreadyDefined();
		}
		defined = true;
		this.value = value;
	}

}
