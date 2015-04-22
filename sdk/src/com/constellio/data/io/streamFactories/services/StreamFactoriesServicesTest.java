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
package com.constellio.data.io.streamFactories.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.data.io.streamFactories.CloseableStreamFactory;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.io.streamFactories.services.one.StreamOperation;
import com.constellio.data.io.streamFactories.services.one.StreamOperationReturningValue;
import com.constellio.data.io.streamFactories.services.one.StreamOperationReturningValueOrThrowingException;
import com.constellio.data.io.streamFactories.services.one.StreamOperationThrowingException;
import com.constellio.data.io.streamFactories.services.two.TwoStreamsOperation;
import com.constellio.data.io.streamFactories.services.two.TwoStreamsOperationReturningValue;
import com.constellio.data.io.streamFactories.services.two.TwoStreamsOperationReturningValueOrThrowingException;
import com.constellio.data.io.streamFactories.services.two.TwoStreamsOperationThrowingException;
import com.constellio.data.io.streams.factories.StreamsServices;
import com.constellio.sdk.tests.ConstellioTest;

public class StreamFactoriesServicesTest extends ConstellioTest {

	StreamFactoriesServices streamFactoriesServices;

	@Mock StreamsServices streamsServices;

	@Mock StreamOperation<Closeable> streamOperation;
	@Mock StreamOperationReturningValue<Closeable, String> streamOperationReturningValue;
	@Mock StreamOperationReturningValueOrThrowingException<Closeable, String, IllegalArgumentException> streamOperationReturningValueOrThrowingException;
	@Mock StreamOperationThrowingException<Closeable, IllegalArgumentException> streamOperationThrowingException;
	@Mock TwoStreamsOperation<Closeable, Closeable> twoStreamsOperation;
	@Mock TwoStreamsOperationReturningValue<Closeable, Closeable, String> twoStreamsOperationReturningValue;
	@Mock TwoStreamsOperationReturningValueOrThrowingException<Closeable, Closeable, String, IllegalArgumentException> twoStreamsOperationReturningValueOrThrowingException;
	@Mock TwoStreamsOperationThrowingException<Closeable, Closeable, IllegalArgumentException> twoStreamsOperationThrowingException;

	@Mock StreamFactory<Closeable> closeableStreamFactory;
	@Mock StreamFactory<Closeable> secondCloseableStreamFactory;

	@Mock Closeable closeable;
	@Mock Closeable secondCloseable;

	@Mock CloseableStreamFactory<InputStream> inputStreamFactory;

	@Mock InputStream inputStream;

	@Before
	public void setUp() {
		streamFactoriesServices = new StreamFactoriesServices(streamsServices);
	}

	@Test
	public void whenExecuteOperationThenStreamClosedQuietlyAndOperationExecuteCloseable()
			throws IOException {
		when(closeableStreamFactory.create(anyString())).thenReturn(closeable);

		streamFactoriesServices.execute(streamOperation, closeableStreamFactory);

		verify(streamsServices).closeQuietly(closeable);
		verify(streamOperation).execute(closeable);
	}

	@Test(expected = IOException.class)
	public void givenExceptionWhenExecuteOperationThenStreamClosedQuietlyAndOperationExecuteCloseable()
			throws IOException {
		when(closeableStreamFactory.create(anyString())).thenReturn(closeable);
		doThrow(IOException.class).when(streamOperation).execute(closeable);
		try {
			streamFactoriesServices.execute(streamOperation, closeableStreamFactory);
		} finally {
			verify(streamsServices).closeQuietly(closeable);
			verify(streamOperation).execute(closeable);
		}
	}

	@Test(expected = IOException.class)
	public void givenExceptionWhenExecuteOperationReturningValueThenReturnOperationAndServiceAlwaysClosed()
			throws IOException {
		when(closeableStreamFactory.create(anyString())).thenReturn(closeable);
		doThrow(IOException.class).when(streamOperationReturningValue).execute(closeable);
		try {
			assertThat(streamFactoriesServices.execute(streamOperationReturningValue, closeableStreamFactory)).isEqualTo(
					streamOperationReturningValue.execute(closeable));
		} finally {
			verify(streamsServices).closeQuietly(closeable);
		}
	}

	@Test
	public void whenExecuteOperationReturningValueWithStreamFactoryThenReturnOperationAndServicesAlwaysClosed()
			throws IOException {
		when(closeableStreamFactory.create(anyString())).thenReturn(closeable);

		assertThat(streamFactoriesServices.execute(streamOperationReturningValue, closeableStreamFactory)).isEqualTo(
				streamOperationReturningValue.execute(closeable));

		verify(streamsServices).closeQuietly(closeable);
	}

	@Test
	public void whenExecuteOperationReturningValueOrThrowingExceptionThenReturnOperationAndServicesAlwaysClosed()
			throws IOException {
		when(closeableStreamFactory.create(anyString())).thenReturn(closeable);

		assertThat(streamFactoriesServices.execute(streamOperationReturningValueOrThrowingException, closeableStreamFactory))
				.isEqualTo(streamOperationReturningValueOrThrowingException.execute(closeable));

		verify(streamsServices).closeQuietly(closeable);
	}

	@Test(expected = IOException.class)
	public void givenExceptionWhenExecuteOperationReturningValueOrThrowingExceptionThenReturnOperationAndServicesAlwaysClosed()
			throws IOException {
		when(closeableStreamFactory.create(anyString())).thenReturn(closeable);
		doThrow(IOException.class).when(streamOperationReturningValueOrThrowingException).execute(closeable);
		try {
			assertThat(streamFactoriesServices.execute(streamOperationReturningValueOrThrowingException, closeableStreamFactory))
					.isEqualTo(streamOperationReturningValueOrThrowingException.execute(closeable));
		} finally {
			verify(streamsServices).closeQuietly(closeable);
		}
	}

	@Test
	public void whenExecuteOperationThrowingExceptionThenExecuteOperationAndServicesAlwaysClosed()
			throws Exception {
		when(closeableStreamFactory.create(anyString())).thenReturn(closeable);

		streamFactoriesServices.execute(streamOperationThrowingException, closeableStreamFactory);

		verify(streamsServices).closeQuietly(closeable);
		verify(streamOperationThrowingException).execute(closeable);
	}

	@Test(expected = IOException.class)
	public void givenExceptionWhenExecuteOperationThrowingExceptionThenExecuteOperationAndServicesAlwaysClosed()
			throws Exception {
		when(closeableStreamFactory.create(anyString())).thenReturn(closeable);
		doThrow(IOException.class).when(streamOperationThrowingException).execute(closeable);

		try {
			streamFactoriesServices.execute(streamOperationThrowingException, closeableStreamFactory);
		} finally {
			verify(streamsServices).closeQuietly(closeable);
			verify(streamOperationThrowingException).execute(closeable);
		}
	}

	@Test
	public void whenExecuteTwoStreamsOperationThenExecuteOperationAndCloseQuietly()
			throws Exception {
		when(closeableStreamFactory.create(anyString())).thenReturn(closeable);
		when(secondCloseableStreamFactory.create(anyString())).thenReturn(secondCloseable);

		streamFactoriesServices.execute(twoStreamsOperation, closeableStreamFactory, secondCloseableStreamFactory);
		verify(streamsServices).closeQuietly(closeable);
		verify(streamsServices).closeQuietly(secondCloseable);
	}

	@Test(expected = IOException.class)
	public void givenExceptionWhenExecuteTwoStreamsOperationThenExecuteOperationAndCloseQuietly()
			throws IOException {
		when(closeableStreamFactory.create(anyString())).thenReturn(closeable);
		when(secondCloseableStreamFactory.create(anyString())).thenReturn(secondCloseable);
		doThrow(IOException.class).when(twoStreamsOperation).execute(closeable, secondCloseable);

		try {
			streamFactoriesServices.execute(twoStreamsOperation, closeableStreamFactory, secondCloseableStreamFactory);
		} finally {
			verify(streamsServices).closeQuietly(closeable);
			verify(streamsServices).closeQuietly(secondCloseable);
		}
	}

	@Test
	public void whenExecuteTwoStreamsOperationReturningValueThenReturnOperationAndServicesClosedQuietly()
			throws IOException {
		when(closeableStreamFactory.create(anyString())).thenReturn(closeable);
		when(secondCloseableStreamFactory.create(anyString())).thenReturn(secondCloseable);

		assertThat(
				streamFactoriesServices.execute(twoStreamsOperationReturningValue, closeableStreamFactory,
						secondCloseableStreamFactory)).isEqualTo(
				twoStreamsOperationReturningValue.execute(closeable, secondCloseable));

		verify(streamsServices).closeQuietly(closeable);
		verify(streamsServices).closeQuietly(secondCloseable);
	}

	@Test(expected = IOException.class)
	public void givenExceptionWhenExecuteTwoStreamsOperationReturningValueThenReturnOperationAndServicesClosedQuietly()
			throws IOException {
		when(closeableStreamFactory.create(anyString())).thenReturn(closeable);
		when(secondCloseableStreamFactory.create(anyString())).thenReturn(secondCloseable);
		doThrow(IOException.class).when(twoStreamsOperationReturningValue).execute(closeable, secondCloseable);

		try {
			assertThat(
					streamFactoriesServices.execute(twoStreamsOperationReturningValue, closeableStreamFactory,
							secondCloseableStreamFactory)).isEqualTo(
					twoStreamsOperationReturningValue.execute(closeable, secondCloseable));
		} finally {
			verify(streamsServices).closeQuietly(closeable);
			verify(streamsServices).closeQuietly(secondCloseable);
		}
	}

	@Test
	public void whenExecuteTwoStreamsOperationReturningValueOrThrowingExceptionThenReturnOperationAndServicesClosedQuietly()
			throws IOException {
		when(closeableStreamFactory.create(anyString())).thenReturn(closeable);
		when(secondCloseableStreamFactory.create(anyString())).thenReturn(secondCloseable);

		assertThat(
				streamFactoriesServices.execute(twoStreamsOperationReturningValue, closeableStreamFactory,
						secondCloseableStreamFactory)).isEqualTo(
				twoStreamsOperationReturningValue.execute(closeable, secondCloseable));

		verify(streamsServices).closeQuietly(closeable);
		verify(streamsServices).closeQuietly(secondCloseable);
	}

	@Test(expected = IOException.class)
	public void givenExceptionWhenExecuteTwoStreamsOperationReturningValueOrThrowingExceptionThenReturnOperationAndServicesClosedQuietly()
			throws IOException {
		when(closeableStreamFactory.create(anyString())).thenReturn(closeable);
		when(secondCloseableStreamFactory.create(anyString())).thenReturn(secondCloseable);
		doThrow(IOException.class).when(twoStreamsOperationReturningValueOrThrowingException).execute(closeable, secondCloseable);

		try {
			assertThat(
					streamFactoriesServices.execute(twoStreamsOperationReturningValueOrThrowingException, closeableStreamFactory,
							secondCloseableStreamFactory)).isEqualTo(
					twoStreamsOperationReturningValueOrThrowingException.execute(closeable, secondCloseable));
		} finally {
			verify(streamsServices).closeQuietly(closeable);
			verify(streamsServices).closeQuietly(secondCloseable);
		}
	}

	@Test
	public void whenExecuteTwoStreamsOperationThrowingExceptionThenExecuteOperationAndServicesClosedQuietly()
			throws IOException {
		when(closeableStreamFactory.create(anyString())).thenReturn(closeable);
		when(secondCloseableStreamFactory.create(anyString())).thenReturn(secondCloseable);

		streamFactoriesServices.execute(twoStreamsOperationThrowingException, closeableStreamFactory,
				secondCloseableStreamFactory);

		verify(streamsServices).closeQuietly(closeable);
		verify(streamsServices).closeQuietly(secondCloseable);
	}

	@Test(expected = IOException.class)
	public void givenExceptionWhenExecuteTwoStreamsOperationThrowingExceptionThenExecuteOperationAndServicesClosedQuietly()
			throws IOException {
		when(closeableStreamFactory.create(anyString())).thenReturn(closeable);
		when(secondCloseableStreamFactory.create(anyString())).thenReturn(secondCloseable);
		doThrow(IOException.class).when(twoStreamsOperationThrowingException).execute(closeable, secondCloseable);

		try {
			streamFactoriesServices.execute(twoStreamsOperationThrowingException, closeableStreamFactory,
					secondCloseableStreamFactory);
		} finally {
			verify(streamsServices).closeQuietly(closeable);
			verify(streamsServices).closeQuietly(secondCloseable);
		}
	}

	@Test
	public void whenCloseQuietlyThenInputStreamFactoryClosed()
			throws IOException {
		streamFactoriesServices.closeQuietly(inputStreamFactory);

		verify(inputStreamFactory).close();
	}

}
