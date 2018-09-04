package com.constellio.app.modules.restapi.signature;

import com.constellio.app.modules.restapi.core.util.HashingUtils;

import static com.constellio.app.modules.restapi.core.util.Algorithms.HMAC_SHA_256;

public class SignatureService {

	public String sign(String key, String data) throws Exception {
		return sign(key, data, HMAC_SHA_256);
	}

	public String sign(String key, String data, String algorithm) throws Exception {

		if (algorithm.equals(HMAC_SHA_256)) {
			return HashingUtils.hmacSha256Base64UrlEncoded(key, data);
		} else {
			throw new UnsupportedOperationException(String.format("Unsupported algorithm : %s", algorithm));
		}
	}

}
