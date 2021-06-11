package com.constellio.data.io.streamFactories.services;

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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

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
			StreamOperationReturningValueOrThrowingException<F, R, E> operation,
			StreamFactory<F> closeableStreamFactory)
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
																   StreamFactory<F> firstCloseableStreamFactory,
																   StreamFactory<S> secondCloseableStreamFactory)
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
																   StreamFactory<F> firstCloseableStreamFactory,
																   StreamFactory<S> secondCloseableStreamFactory)
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
		if (inputStreamFactory != null) {
			try {
				inputStreamFactory.close();
			} catch (IOException e) {
				return;
			}
		}
	}

}
