package com.constellio.data.io;

import org.apache.commons.codec.binary.Base64;

public class EncodingService {

	public String encodeToBase64(byte[] bytes) {
		return new String(Base64.encodeBase64(bytes));
	}

	public byte[] decodeStringToBase64Bytes(String contentString) {
		return Base64.decodeBase64(contentString.getBytes());
	}

}
