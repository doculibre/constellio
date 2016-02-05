package com.constellio.data.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import com.constellio.data.io.services.zip.ZipServiceException;

public class TimeoutTaskTest {

	@Test
	public void givenFastEnoughWhenExecutingThenCancelNotCalledReturnValue()
			throws Exception {
		final AtomicBoolean cancelCalled = new AtomicBoolean();

		TimeoutTask<String, ZipServiceException> task = new TimeoutTask<String, ZipServiceException>(100) {

			@Override
			protected String doExecute()
					throws ZipServiceException {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				return "ok";
			}

			@Override
			protected void onCancel() {
				cancelCalled.set(true);
			}
		};
		assertThat(task.execute()).isEqualTo("ok");
		assertThat(cancelCalled.get()).isFalse();
	}

	@Test()
	public void givenNotFastEnoughWhenExecutingThenThrowTimeoutException()
			throws Exception {
		final AtomicBoolean cancelCalled = new AtomicBoolean();

		TimeoutTask<String, ZipServiceException> task = new TimeoutTask<String, ZipServiceException>(100) {

			@Override
			protected String doExecute()
					throws ZipServiceException {
				boolean working = true;
				while (working) {
					working = true;
				}
				throw new RuntimeException();
			}

			@Override
			protected void onCancel() {
				cancelCalled.set(true);
			}
		};
		try {
			task.execute();
			fail("An exception was expected");
		} catch (Exception e) {
			assertThat(cancelCalled.get()).isTrue();
			assertThat(e).isInstanceOf(TimeoutException.class);
		}
	}

	@Test()
	public void givenCheckedExceptionWhenExecutingThenCancelCalledAndExceptionThrown()
			throws Exception {
		final AtomicBoolean cancelCalled = new AtomicBoolean();

		TimeoutTask<String, ZipServiceException> task = new TimeoutTask<String, ZipServiceException>(100) {

			@Override
			protected String doExecute()
					throws ZipServiceException {
				throw new ZipServiceException("ze message");
			}

			@Override
			protected void onCancel() {
				cancelCalled.set(true);
			}
		};
		try {
			task.execute();
			fail("An exception was expected");
		} catch (Exception e) {
			assertThat(cancelCalled.get()).isTrue();
			assertThat(e).hasMessage("ze message").isInstanceOf(ZipServiceException.class);
		}
	}

	@Test
	public void givenRuntimeExceptionWhenExecutingThenCancelCalledAndRuntimeExceptionThrown()
			throws Exception {
		final AtomicBoolean cancelCalled = new AtomicBoolean();

		TimeoutTask<String, ZipServiceException> task = new TimeoutTask<String, ZipServiceException>(100) {

			@Override
			protected String doExecute()
					throws ZipServiceException {
				throw new RuntimeException("ze message");
			}

			@Override
			protected void onCancel() {
				cancelCalled.set(true);
			}
		};

		try {
			task.execute();
			fail("An exception was expected");
		} catch (Exception e) {
			assertThat(cancelCalled.get()).isTrue();
			assertThat(e).hasMessage("ze message").isInstanceOf(RuntimeException.class);
		}
	}

}
