package com.constellio.data.utils.hashing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.input.ReaderInputStream;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.constellio.data.io.EncodingService;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.utils.Factory;
import com.constellio.sdk.tests.ConstellioTest;

public class HashingServiceTest extends ConstellioTest {

	String stringContent = "This a message";
	byte[] bytesContent = stringContent.getBytes();
	String expectedMD5Hash = "pjOAZQLkFvZxSS1u9mXnDQ==";
	String expectedSHA1Hash = "NlWk4A0G3n9XSi43FGoWLwHXq50=";
	String stringContentGeneratingSHA1WithSlash = "This a message 40";
	String stringContentGeneratingSHA1WithPlus = "This a message 41";
	EncodingService encodingService;
	HashingService md5HashingService;
	HashingService sha1HashingService;

	HashingService md5HashingServiceUsingUrlEncodedBase64;
	HashingService sha1HashingServiceUsingUrlEncodedBase64;

	@Before
	public void setUp() {
		encodingService = new EncodingService();
		md5HashingService = spy(HashingService.forMD5(encodingService, false));
		sha1HashingService = spy(HashingService.forSHA1(encodingService, false));

		md5HashingServiceUsingUrlEncodedBase64 = spy(HashingService.forMD5(encodingService, true));
		sha1HashingServiceUsingUrlEncodedBase64 = spy(HashingService.forSHA1(encodingService, true));
	}

	@Test
	public void whenHashingContentThenReceivedCorrectHash()
			throws Exception {

		for (int i = 0; i < 100000; i++) {
			String hash = sha1HashingService.getHashFromString(stringContent + " " + i);
			if (hash.contains("/")) {
				System.out.println("/ : " + i);
			}

			if (hash.contains("+")) {
				System.out.println("+ : " + i);
			}

		}

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

	}

	@Test
	public void whenHashingBytesThenReceivedCorrectHash()
			throws Exception {
		assertThat(md5HashingService.getHashFromBytes(bytesContent)).isEqualTo(expectedMD5Hash);
		assertThat(sha1HashingService.getHashFromBytes(bytesContent)).isEqualTo(expectedSHA1Hash);
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
		when(readerFactory.create(SDK_STREAM)).thenThrow(new IOException());

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

		when(md5HashingService.doHash(stringContent.getBytes())).thenThrow(new RuntimeException());

		try {
			md5HashingService.getHashFromStream(readerFactory);
			fail("Exception expected");
		} catch (HashingServiceException.CannotHashContent e) {

		}
		verify(reader).close();
	}

}
