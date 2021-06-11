package com.constellio.data.io;

import com.constellio.data.conf.HashingEncoding;
import com.constellio.data.utils.ImpossibleRuntimeException;
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

	public String convert(String value, HashingEncoding from, HashingEncoding to) {
		if (from == to) {
			return value;
		}
		byte[] bytes = null;

		switch (from) {
			case BASE32:
				bytes = decodeStringToBase32Bytes(value);
				break;

			case BASE64:
				bytes = decodeStringToBase64Bytes(value);
				break;

			case BASE64_URL_ENCODED:
				bytes = decodeStringToBase64UrlEncodedBytes(value);
			default:
				throw new ImpossibleRuntimeException("Unsupported encoding " + from);

		}
		switch (to) {
			case BASE32:
				return encodeToBase32(bytes);
			case BASE64:
				return encodeToBase64(bytes);
			case BASE64_URL_ENCODED:
				return encodeToBase64UrlEncoded(bytes);

			default:
				throw new ImpossibleRuntimeException("Unsupported encoding " + to);
		}
	}

}
