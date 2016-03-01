package com.constellio.data.io.concurrent.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * This class deals with encoding. It wraps a {@link Reader} over byte[] with {@link StandardCharsets#UTF_8} encoding.
 * It also uses {@link Writer} for the conversion of output to the byte[].
 * @author Majid Laali
 *
 * @param <T>
 */
public abstract class ReaderWriterDataWrapper<T> implements DataWrapper<T>{
	public void init(final byte[] data){
		Reader inputStreamReader = getReader(data);
		init(inputStreamReader);
	}

	protected Reader getReader(final byte[] data) {
		InputStreamReader inputStreamReader = new InputStreamReader(new ByteArrayInputStream(data), StandardCharsets.UTF_8);
		return inputStreamReader;
	}
	
	protected abstract void init(Reader reader);
	
	public byte[] toBytes() {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		OutputStreamWriter output = new OutputStreamWriter(buffer, StandardCharsets.UTF_8);
		toBytes(output);
		return buffer.toByteArray();
	}
	
	protected abstract void toBytes(Writer writer);
	
}
