package com.constellio.data.io.concurrent.data;

import org.apache.commons.lang.builder.EqualsBuilder;

import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;

/**
 * This class is low level access to the data. It is better to use {@link DataWrapper} such as {@link TextView}
 * to work with data. A typical approach could be:
 * <li> {@link AtomicFileSystem#readData(String)} and then call {@link DataWithVersion#getView(DataWrapper)} to convert the date to a proper view. </li>
 * <li> Manipulate data with {@link DataWrapper} class and finally convert it to {@link DataWithVersion} by calling 
 * {@link DataWrapper#toDataWithVersion()} method. Note that using this method the version of data will be preserved </li>
 * 
 * @author Majid Laali
 *
 */
public class DataWithVersion{
	private byte[] data;
	private Object version;

	public DataWithVersion(final byte[] binaryData, final Object version) {
		this.data = binaryData;
		this.version = version;
	}


	public Object getVersion() {
		return version;
	}

	public byte[] getData() {
		return data;
	}

	public DataWithVersion setData(final byte[] data) {
		this.data = data;
		return this;
	}

	public <T> DataWithVersion setDataFromView(final DataWrapper<T> aView){
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
