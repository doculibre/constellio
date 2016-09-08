package com.constellio.data.utils.hashing;

import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.constellio.data.conf.HashingEncoding;
import com.constellio.data.io.EncodingService;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.utils.Factory;
import org.apache.commons.io.output.NullOutputStream;

public class HashingService {

	private HashingEncoding hashingEncoding;
	private String algorithm;

	public HashingService(String algorithm, EncodingService encodingService, HashingEncoding hashingEncoding) {
		this.hashingEncoding = hashingEncoding;
		this.algorithm = algorithm;
	}

	public static HashingService forMD5(EncodingService encodingService, HashingEncoding hashingEncoding) {
		return new HashingService("MD5", encodingService, hashingEncoding);
	}

	public static HashingService forSHA1(EncodingService encodingService, HashingEncoding hashingEncoding) {
		return new HashingService("SHA1", encodingService, hashingEncoding);
	}

	public String getHashFromStream(StreamFactory<InputStream> streamFactory)
			throws HashingServiceException {
		InputStream inputStream = null;
		try {
			inputStream = streamFactory.create("getHashFromStream");
		} catch (IOException e) {
			throw new HashingServiceException.CannotReadContent(e);
		}

		try {
			return getHashFromStream(inputStream);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	public String getHashFromStream(final InputStream stream)
			throws HashingServiceException {

		try {
			MessageDigest messageDigest = this.getDigest();
			DigestInputStream dis = new DigestInputStream(stream, messageDigest);
			IOUtils.copy(dis, new NullOutputStream());

			return this.encodeDigest(messageDigest.digest());
		} catch (IOException e) {
			throw new HashingServiceException.CannotReadContent(e);
		} catch (RuntimeException e) {
			throw new HashingServiceException.CannotHashContent(e);
		}
	}

	public String getHashFromReader(final StreamFactory<Reader> readerFactory)
			throws HashingServiceException {

		Reader reader = null;
		try {
			reader = readerFactory.create("getHashFromReader");
		} catch (IOException e) {
			throw new HashingServiceException.CannotReadContent(e);
		}

		try {
			return getHashFromReader(reader);
		} finally {
			IOUtils.closeQuietly(reader);
		}

	}

	public String getHashFromReader(final Reader reader)
			throws HashingServiceException {
		return getHashFromBytes(new Factory<byte[]>() {

			@Override
			public byte[] get() {
				try {
					return IOUtils.toByteArray(reader);
				} catch (IOException e) {
					throw new HashingServiceRuntimeException.CannotGetHashFromReader(e);
				}
			}
		});
	}

	public String getHashFromFile(File file)
			throws HashingServiceException {
		try (FileInputStream fis = new FileInputStream(file)) {
			return getHashFromStream(fis);
		} catch (IOException e) {
			throw new HashingServiceRuntimeException.CannotGetHashFromStream(e);
		}
	}

	public String getHashFromBytes(final byte[] bytes)
			throws HashingServiceException {
		return getHashFromBytes(new Factory<byte[]>() {

			@Override
			public byte[] get() {
				return bytes;
			}

		});
	}

	public String getHashFromBytes(final Factory<byte[]> bytesFactory)
			throws HashingServiceException {

		try {
			byte[] bytes;
			try {
				bytes = bytesFactory.get();
			} catch (RuntimeException e) {
				throw new HashingServiceException.CannotReadContent(e);
			}
			return doHash(bytes);
		} catch (RuntimeException e) {
			throw new HashingServiceException.CannotHashContent(e);
		}
	}

	public String getHashFromString(String string)
			throws HashingServiceException {
		return getHashFromBytes(string.getBytes());
	}

	String doHash(byte[] bytes) {
		MessageDigest messageDigest = getDigest();
		byte[] digestBytes = messageDigest.digest(bytes);

		return encodeDigest(digestBytes);
	}

	private MessageDigest getDigest() {
		try {
			return MessageDigest.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new HashingServiceRuntimeException.NoSuchAlgorithm(algorithm, e);
		}
	}

	String encodeDigest(byte[] digestBytes) {
		String encoded;
		if (hashingEncoding == HashingEncoding.BASE64_URL_ENCODED) {
			encoded = new EncodingService().encodeToBase64UrlEncoded(digestBytes);

		} else if (hashingEncoding == HashingEncoding.BASE32) {
			encoded = new EncodingService().encodeToBase32(digestBytes);

		} else {
			encoded = new EncodingService().encodeToBase64(digestBytes);
		}

		return encoded;
	}
}
