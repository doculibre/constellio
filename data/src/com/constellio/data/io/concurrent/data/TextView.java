package com.constellio.data.io.concurrent.data;

import java.nio.charset.StandardCharsets;

public class TextView extends AbstractDataWithVersion<String>{
	private String data; 

	@Override
	public void init(byte[] data) {
		this.data = new String(data, StandardCharsets.UTF_8);
	}

	@Override
	public byte[] toBytes() {
		return data.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public String getData() {
		return data;
	}

	@Override
	public TextView setData(String data) {
		this.data = data;
		return this;
	}

}
