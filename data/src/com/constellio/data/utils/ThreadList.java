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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ThreadList<T extends Thread> {

	private final List<T> threads = new ArrayList<T>();

	public void add(T thread) {
		threads.add(thread);
	}

	public void addAndStart(T thread) {
		threads.add(thread);
		thread.start();
	}

	public void startAll()
			throws InterruptedException {
		for (Thread thread : threads) {
			thread.start();
		}
	}

	public void joinAll()
			throws InterruptedException {
		for (Thread thread : threads) {
			while (thread.isAlive()) {
				thread.join();
			}
		}
	}

	public List<T> getThreads() {
		return Collections.unmodifiableList(threads);
	}

	public int size() {
		return threads.size();
	}
}
