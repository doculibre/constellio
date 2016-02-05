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
