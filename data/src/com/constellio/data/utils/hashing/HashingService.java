/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.data.utils.hashing;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.IOUtils;

import com.constellio.data.io.EncodingService;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.utils.Factory;

public class HashingService {

	private final MessageDigest messageDigest;

	public HashingService(String algorithm, EncodingService encodingService)
			throws NoSuchAlgorithmException {
		messageDigest = MessageDigest.getInstance(algorithm);
	}

	public static HashingService forMD5(EncodingService encodingService) {
		try {
			return new HashingService("MD5", encodingService);
		} catch (NoSuchAlgorithmException e) {
			throw new HashingServiceRuntimeException.NoSuchAlgorithm("MD5", e);
		}
	}

	public static HashingService forSHA1(EncodingService encodingService) {
		try {
			return new HashingService("SHA1", encodingService);
		} catch (NoSuchAlgorithmException e) {
			throw new HashingServiceRuntimeException.NoSuchAlgorithm("SHA1", e);
		}
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
		return getHashFromBytes(new Factory<byte[]>() {

			@Override
			public byte[] get() {
				try {
					return IOUtils.toByteArray(stream);

				} catch (IOException e) {
					throw new HashingServiceRuntimeException.CannotGetHashFromStream(e);

				}

			}

		});
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
		throw new UnsupportedOperationException("TODO");
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
		byte[] digestBytes = messageDigest.digest(bytes);
		return new EncodingService().encodeToBase64(digestBytes);
	}

}
