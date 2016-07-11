package com.constellio.data.io;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;

public class EncodingService {

	public String encodeToBase64(byte[] bytes) {
		return new String(Base64.encodeBase64(bytes));
	}

	public byte[] decodeStringToBase64Bytes(String contentString) {
		return Base64.decodeBase64(contentString.getBytes());
	}

	public String encodeToBase64UrlEncoded(byte[] bytes) {
		return new String(Base64.encodeBase64(bytes)).replace("/", "_").replace("+", "-");
	}

	public byte[] decodeStringToBase64UrlEncodedBytes(String contentString) {
		return Base64.decodeBase64(contentString.replace("_", "/").replace("-", "+").getBytes());
	}

	public String encodeToBase32(byte[] bytes) {
		return new String(new Base32().encode(bytes));
	}

	public byte[] decodeStringToBase32Bytes(String contentString) {
		return new Base32().decode(contentString.getBytes());
	}

}
