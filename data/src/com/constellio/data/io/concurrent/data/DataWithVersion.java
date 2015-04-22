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

import org.apache.commons.lang.builder.EqualsBuilder;


public class DataWithVersion{
	private byte[] data;
	private Object version;
	
		public DataWithVersion(byte[] binaryData, Object version) {
		this.data = binaryData;
		this.version = version;
	}


	public Object getVersion() {
		return version;
	}
	
	public byte[] getData() {
		return data;
	}

	public DataWithVersion setData(byte[] data) {
		this.data = data;
		return this;
	}
	
	public <T> DataWithVersion setDataFromView(DataWrapper<T> aView){
		return setData(aView.toBytes());
	}
	
	public <T extends DataWrapper<?>> T getView(T toFill){
		toFill.init(data);
		return toFill;
	}
	
	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}
}
