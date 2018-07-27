package com.constellio.app.modules.restapi.core.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static com.constellio.app.modules.restapi.core.util.Algorithms.HMAC_SHA_256;
import static com.constellio.app.modules.restapi.core.util.Algorithms.MD5;

@UtilityClass
public final class HashingUtils {

    public static String hmacSha256Base64UrlEncoded(String key, String data) throws Exception {
        Mac hmacSha256 = Mac.getInstance(HMAC_SHA_256);
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA_256);
        hmacSha256.init(secretKey);

        return Base64.encodeBase64URLSafeString(hmacSha256.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    public static String md5(String data) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance(MD5);
        return Hex.encodeHexString(md5.digest(data.getBytes(StandardCharsets.UTF_8)));
    }

    public static String md5(byte[] data) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance(MD5);
        return Hex.encodeHexString(md5.digest(data));
    }

}
