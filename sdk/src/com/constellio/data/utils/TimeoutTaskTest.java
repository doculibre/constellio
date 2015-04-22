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
