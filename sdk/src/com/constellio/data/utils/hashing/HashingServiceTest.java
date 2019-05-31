package com.constellio.data.utils.hashing;

import com.constellio.data.io.EncodingService;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.utils.Factory;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.commons.io.input.ReaderInputStream;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.constellio.data.conf.HashingEncoding.BASE32;
import static com.constellio.data.conf.HashingEncoding.BASE64;
import static com.constellio.data.conf.HashingEncoding.BASE64_URL_ENCODED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HashingServiceTest extends ConstellioTest {

	String stringContent = "This a message";
	byte[] bytesContent = stringContent.getBytes();
	String expectedMD5Hash = "pjOAZQLkFvZxSS1u9mXnDQ==";
	String expectedSHA1Hash = "NlWk4A0G3n9XSi43FGoWLwHXq50=";
	String stringContentGeneratingSHA1WithSlash = "This a message 40";
	String stringContentGeneratingSHA1WithPlus = "This a message 41";
	EncodingService encodingService;
	HashingService md5HashingService, md5HashingServiceUsingBase32, sha1HashingService, sha1HashingServiceUsingBase32;

	HashingService md5HashingServiceUsingUrlEncodedBase64;
	HashingService sha1HashingServiceUsingUrlEncodedBase64;

	@Before
	public void setUp() {
		encodingService = new EncodingService();
		md5HashingService = spy(HashingService.forMD5(encodingService, BASE64));
		sha1HashingService = spy(HashingService.forSHA1(encodingService, BASE64));

		md5HashingServiceUsingBase32 = spy(HashingService.forMD5(encodingService, BASE32));
		sha1HashingServiceUsingBase32 = spy(HashingService.forSHA1(encodingService, BASE32));

		md5HashingServiceUsingUrlEncodedBase64 = spy(HashingService.forMD5(encodingService, BASE64_URL_ENCODED));
		sha1HashingServiceUsingUrlEncodedBase64 = spy(HashingService.forSHA1(encodingService, BASE64_URL_ENCODED));
	}

	@Test
	public void whenHashingContentThenReceivedCorrectHash()
			throws Exception {

		assertThat(md5HashingService.getHashFromString(stringContent)).isEqualTo(expectedMD5Hash);
		assertThat(sha1HashingService.getHashFromString(stringContent)).isEqualTo(expectedSHA1Hash);
	}

	@Test
	public void givenBase64URLEnabledWhenHashingThenReplacePlusAndSlashesWithMinusAndUnderlines()
			throws Exception {

		assertThat(sha1HashingService.getHashFromString(stringContentGeneratingSHA1WithPlus))
				.isEqualTo("9Pr0wDV0Dp6K7q+Q9yAPgozZ5Vg=");

		assertThat(sha1HashingService.getHashFromString(stringContentGeneratingSHA1WithSlash))
				.isEqualTo("8IPj/NxfvfS59bJO6yjLlU/AbSw=");

		assertThat(sha1HashingServiceUsingUrlEncodedBase64.getHashFromString(stringContentGeneratingSHA1WithPlus))
				.isEqualTo("9Pr0wDV0Dp6K7q-Q9yAPgozZ5Vg=");

		assertThat(sha1HashingServiceUsingUrlEncodedBase64.getHashFromString(stringContentGeneratingSHA1WithSlash))
				.isEqualTo("8IPj_NxfvfS59bJO6yjLlU_AbSw=");

		assertThat(sha1HashingServiceUsingBase32.getHashFromString(stringContentGeneratingSHA1WithPlus))
				.isEqualTo("6T5PJQBVOQHJ5CXOV6IPOIAPQKGNTZKY");

		assertThat(sha1HashingServiceUsingBase32.getHashFromString(stringContentGeneratingSHA1WithSlash))
				.isEqualTo("6CB6H7G4L667JOPVWJHOWKGLSVH4A3JM");

	}

	@Test
	public void whenHashingBytesThenReceivedCorrectHash()
			throws Exception {
		assertThat(md5HashingService.getHashFromBytes(bytesContent)).isEqualTo(expectedMD5Hash);
		assertThat(sha1HashingService.getHashFromBytes(bytesContent)).isEqualTo(expectedSHA1Hash);
		assertThat(sha1HashingService.getHashFromString("")).isEqualTo("2jmj7l5rSw0yVb/vlWAYkK/YBwk=");
	}

	@Test
	public void whenHashingBytesFactoryThenReceivedCorrectHash()
			throws Exception {
		Factory<byte[]> bytesFactory = new Factory<byte[]>() {

			@Override
			public byte[] get() {
				return bytesContent;
			}

		};

		assertThat(md5HashingService.getHashFromBytes(bytesFactory)).isEqualTo(expectedMD5Hash);
		assertThat(sha1HashingService.getHashFromBytes(bytesFactory)).isEqualTo(expectedSHA1Hash);
	}

	@Test
	public void whenHashingReaderThenReceivedCorrectHashAndReaderNotClosed()
			throws Exception {
		StringReader reader1 = spy(new StringReader(stringContent));
		StringReader reader2 = spy(new StringReader(stringContent));

		assertThat(md5HashingService.getHashFromReader(reader1)).isEqualTo(expectedMD5Hash);
		assertThat(sha1HashingService.getHashFromReader(reader2)).isEqualTo(expectedSHA1Hash);
		verify(reader1, never()).close();
		verify(reader2, never()).close();
	}

	@Test
	public void whenHashingReaderFactoryThenReceivedCorrectHashAndOpenedReadersClosed()
			throws Exception {
		final List<Reader> returnReaders = new ArrayList<Reader>();

		StreamFactory<Reader> readerFactory = new StreamFactory<Reader>() {

			@Override
			public Reader create(String name)
					throws IOException {
				Reader reader = spy(new StringReader(stringContent));
				returnReaders.add(reader);
				return reader;
			}
		};

		assertThat(md5HashingService.getHashFromReader(readerFactory)).isEqualTo(expectedMD5Hash);
		assertThat(sha1HashingService.getHashFromReader(readerFactory)).isEqualTo(expectedSHA1Hash);
		verify(returnReaders.get(0)).close();
		verify(returnReaders.get(1)).close();
	}

	@Test
	public void givenReadExceptionWhenHashingReaderFactoryThenReceivedCorrectHashAndOpenedReadersClosed()
			throws Exception {
		final AtomicReference<Reader> readerRef = new AtomicReference<Reader>();

		StreamFactory<Reader> readerFactory = new StreamFactory<Reader>() {

			@Override
			public Reader create(String name)
					throws IOException {
				Reader reader = spy(new StringReader(stringContent) {
					@Override
					public int read()
							throws IOException {
						throw new IOException();
					}

					@Override
					public int read(char[] cbuf)
							throws IOException {
						throw new IOException();
					}

					@Override
					public int read(char[] cbuf, int off, int len)
							throws IOException {
						throw new IOException();
					}

					@Override
					public int read(CharBuffer target)
							throws IOException {
						throw new IOException();
					}
				});
				readerRef.set(reader);
				return reader;
			}
		};

		try {
			md5HashingService.getHashFromReader(readerFactory);
			fail("Exception expected");
		} catch (HashingServiceException.CannotReadContent e) {

		}
		verify(readerRef.get()).close();
	}

	@Test(expected = HashingServiceException.CannotReadContent.class)
	public void givenStreamReaderWhenGetHashFromReaderAndIOExceptionThenHashingServiceExceptionThrown()
			throws Exception {
		final StreamFactory<Reader> readerFactory = Mockito.mock(StreamFactory.class, "IOExceptionReader");
		when(readerFactory.create(SDK_STREAM)).thenThrow(new IOException());

		md5HashingService.getHashFromReader(readerFactory);
	}

	@Test
	public void givenHashExceptionWhenHashingReaderFactoryThenReceivedCorrectHashAndOpenedReadersClosed()
			throws Exception {
		final Reader reader = spy(new StringReader(stringContent));
		StreamFactory<Reader> readerFactory = new StreamFactory<Reader>() {

			@Override
			public Reader create(String name)
					throws IOException {
				return reader;
			}
		};

		when(md5HashingService.doHash(stringContent.getBytes())).thenThrow(new RuntimeException());

		try {
			md5HashingService.getHashFromReader(readerFactory);
			fail("Exception expected");
		} catch (HashingServiceException.CannotHashContent e) {

		}
		verify(reader).close();
	}

	@Test
	public void whenHashingStreamThenReceivedCorrectHashAndStreamNotClosed()
			throws Exception {
		InputStream reader1 = spy(new ReaderInputStream(new StringReader(stringContent)));
		InputStream reader2 = spy(new ReaderInputStream(new StringReader(stringContent)));

		assertThat(md5HashingService.getHashFromStream(reader1)).isEqualTo(expectedMD5Hash);
		assertThat(sha1HashingService.getHashFromStream(reader2)).isEqualTo(expectedSHA1Hash);
		verify(reader1, never()).close();
		verify(reader2, never()).close();
	}

	@Test
	public void whenHashingStreamFactoryThenReceivedCorrectHashAndOpenedStreamsClosed()
			throws Exception {
		final List<InputStream> returnReaders = new ArrayList<>();

		StreamFactory<InputStream> readerFactory = new StreamFactory<InputStream>() {

			@Override
			public InputStream create(String name)
					throws IOException {
				InputStream reader = spy(new ReaderInputStream(new StringReader(stringContent)));
				returnReaders.add(reader);
				return reader;
			}
		};

		assertThat(md5HashingService.getHashFromStream(readerFactory)).isEqualTo(expectedMD5Hash);
		assertThat(sha1HashingService.getHashFromStream(readerFactory)).isEqualTo(expectedSHA1Hash);
		verify(returnReaders.get(0)).close();
		verify(returnReaders.get(1)).close();
	}

	@Test
	public void givenReadExceptionWhenHashingStreamFactoryThenReceivedCorrectHashAndOpenedStreamsClosed()
			throws Exception {
		final AtomicReference<InputStream> readerRef = new AtomicReference<InputStream>();

		StreamFactory<InputStream> readerFactory = new StreamFactory<InputStream>() {

			@Override
			public InputStream create(String name)
					throws IOException {
				InputStream reader = spy(new ReaderInputStream(new StringReader(stringContent) {
					@Override
					public int read()
							throws IOException {
						throw new IOException();
					}

					@Override
					public int read(char[] cbuf)
							throws IOException {
						throw new IOException();
					}

					@Override
					public int read(char[] cbuf, int off, int len)
							throws IOException {
						throw new IOException();
					}

					@Override
					public int read(CharBuffer target)
							throws IOException {
						throw new IOException();
					}
				}));
				readerRef.set(reader);
				return reader;
			}
		};

		try {
			md5HashingService.getHashFromStream(readerFactory);
			fail("Exception expected");
		} catch (HashingServiceException.CannotReadContent e) {

		}
		verify(readerRef.get()).close();
	}

	@Test(expected = HashingServiceException.CannotReadContent.class)
	public void givenStreamFactoryWhenGetHashFromStreamAndIOExceptionThenHashingServiceExceptionThrown()
			throws Exception {
		final StreamFactory<InputStream> readerFactory = Mockito.mock(StreamFactory.class, "IOExceptionReader");
		doThrow(new IOException()).when(readerFactory).create(any(String.class));

		md5HashingService.getHashFromStream(readerFactory);
	}

	@Test
	public void givenHashExceptionWhenHashingStreamFactoryThenReceivedCorrectHashAndOpenedStreamsClosed()
			throws Exception {
		final InputStream reader = spy(new ReaderInputStream(new StringReader(stringContent)));
		StreamFactory<InputStream> readerFactory = new StreamFactory<InputStream>() {

			@Override
			public InputStream create(String name)
					throws IOException {
				return reader;
			}
		};

		doThrow(new RuntimeException()).when(md5HashingService).encodeDigest(any(byte[].class));

		try {
			md5HashingService.getHashFromStream(readerFactory);
			fail("Exception expected");
		} catch (HashingServiceException.CannotHashContent e) {

		}
		verify(reader).close();
	}

}
