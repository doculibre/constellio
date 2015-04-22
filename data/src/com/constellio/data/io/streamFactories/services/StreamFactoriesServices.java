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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

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

public class StreamFactoriesServices {

	private static String EXECUTOR_THREAD_NAME() {
		return "executor_" + UUID.randomUUID().toString();
	}

	StreamsServices streamsServices;

	public StreamFactoriesServices(StreamsServices streamsServices) {
		this.streamsServices = streamsServices;
	}

	public <F extends Closeable> void execute(StreamOperation<F> operation, StreamFactory<F> closeableStreamFactory)
			throws IOException {
		F closeableStream = closeableStreamFactory.create(EXECUTOR_THREAD_NAME());
		try {
			operation.execute(closeableStream);
		} finally {
			streamsServices.closeQuietly(closeableStream);
		}
	}

	public <F extends Closeable, R> R execute(StreamOperationReturningValue<F, R> operation,
			StreamFactory<F> closeableStreamFactory)
			throws IOException {
		F closeableStream = closeableStreamFactory.create(EXECUTOR_THREAD_NAME());
		try {
			return operation.execute(closeableStream);
		} finally {
			streamsServices.closeQuietly(closeableStream);
		}
	}

	public <F extends Closeable, R, E extends Exception> R execute(
			StreamOperationReturningValueOrThrowingException<F, R, E> operation, StreamFactory<F> closeableStreamFactory)
			throws E, IOException {
		F closeableStream = closeableStreamFactory.create(EXECUTOR_THREAD_NAME());
		try {
			return operation.execute(closeableStream);
		} finally {
			streamsServices.closeQuietly(closeableStream);
		}
	}

	public <F extends Closeable, E extends Exception> void execute(StreamOperationThrowingException<F, E> operation,
			StreamFactory<F> closeableStreamFactory)
			throws E, IOException {
		F closeableStream = closeableStreamFactory.create(EXECUTOR_THREAD_NAME());
		try {
			operation.execute(closeableStream);
		} finally {
			streamsServices.closeQuietly(closeableStream);
		}
	}

	public <F extends Closeable, S extends Closeable> void execute(TwoStreamsOperation<F, S> operation,
			StreamFactory<F> firstCloseableStreamFactory, StreamFactory<S> secondCloseableStreamFactory)
			throws IOException {
		F firstStream = null;
		S secondStream = null;
		try {
			firstStream = firstCloseableStreamFactory.create(EXECUTOR_THREAD_NAME());
			secondStream = secondCloseableStreamFactory.create(EXECUTOR_THREAD_NAME());
			operation.execute(firstStream, secondStream);
		} finally {
			streamsServices.closeQuietly(firstStream);
			streamsServices.closeQuietly(secondStream);
		}
	}

	public <F extends Closeable, S extends Closeable, R> R execute(TwoStreamsOperationReturningValue<F, S, R> operation,
			StreamFactory<F> firstCloseableStreamFactory, StreamFactory<S> secondCloseableStreamFactory)
			throws IOException {
		F firstStream = null;
		S secondStream = null;
		try {
			firstStream = firstCloseableStreamFactory.create(EXECUTOR_THREAD_NAME());
			secondStream = secondCloseableStreamFactory.create(EXECUTOR_THREAD_NAME());
			return operation.execute(firstStream, secondStream);
		} finally {
			streamsServices.closeQuietly(firstStream);
			streamsServices.closeQuietly(secondStream);
		}
	}

	public <F extends Closeable, S extends Closeable, R, E extends Exception> R execute(
			TwoStreamsOperationReturningValueOrThrowingException<F, S, R, E> operation,
			StreamFactory<F> firstCloseableStreamFactory, StreamFactory<S> secondCloseableStreamFactory)
			throws E, IOException {
		F firstStream = null;
		S secondStream = null;
		try {
			firstStream = firstCloseableStreamFactory.create(EXECUTOR_THREAD_NAME());
			secondStream = secondCloseableStreamFactory.create(EXECUTOR_THREAD_NAME());
			return operation.execute(firstStream, secondStream);
		} finally {
			streamsServices.closeQuietly(firstStream);
			streamsServices.closeQuietly(secondStream);
		}
	}

	public <F extends Closeable, S extends Closeable, E extends Exception> void execute(
			TwoStreamsOperationThrowingException<F, S, E> operation, StreamFactory<F> firstCloseableStreamFactory,
			StreamFactory<S> secondCloseableStreamFactory)
			throws E, IOException {
		F firstStream = null;
		S secondStream = null;
		try {
			firstStream = firstCloseableStreamFactory.create(EXECUTOR_THREAD_NAME());
			secondStream = secondCloseableStreamFactory.create(EXECUTOR_THREAD_NAME());
			operation.execute(firstStream, secondStream);
		} finally {
			streamsServices.closeQuietly(firstStream);
			streamsServices.closeQuietly(secondStream);
		}
	}

	public void closeQuietly(CloseableStreamFactory<InputStream> inputStreamFactory) {
		try {
			inputStreamFactory.close();
		} catch (IOException e) {
			return;
		}
	}

}
