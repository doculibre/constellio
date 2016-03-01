package com.constellio.data.io.concurrent.data;

import java.nio.charset.StandardCharsets;


/**
 * A wrapper around a type. This wrapper is responsible to convert the type to a byte array and 
 * construct the type from an array of byte. Note that DataWrapper should take care of encoding. 
 * Therefore any conversion from byte[] to String and vice versa should use {@link StandardCharsets#UTF_8} as 
 * the encoding, otherwise the system default encoding will be used. 
 * @author Majid
 *
 * @param <T> The type that wrapper wraps it.
 */
public interface DataWrapper<T> {
	public void initWithDataWithVersion(DataWithVersion dataWithVersion);
	public DataWithVersion toDataWithVersion();
	
	/**
	 * Constructs the <T> form the given data.
	 * @param data
	 */
	public void init(final byte[] data);

	/**
	 * Converts the inner type to the array of bytes;
	 * @return
	 */
	public byte[] toBytes();

	/**
	 * Give the inner data. Changing the output data should not change inner data. 
	 * @return
	 */
	public T getData();

	/**
	 * Set the internal data and return the wrapper
	 * @param data the value of internal data
	 * @return itself
	 */
	public DataWrapper<T> setData(final T data);
}
