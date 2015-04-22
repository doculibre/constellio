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

import java.io.UnsupportedEncodingException;

public class StringView implements DataWrapper<String>{
	private String data; 

	@Override
	public void init(byte[] data) {
		try {
			this.data = new String(data, "utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte[] toBytes() {
		return data.getBytes();
	}

	@Override
	public String getData() {
		return data;
	}

	@Override
	public StringView setData(String data) {
		this.data = data;
		return this;
	}

}
