package com.constellio.data.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class RetryUtil {

	public void tryThreeTimes(Callable func) {
		AtomicInteger atomicTries = new AtomicInteger(0);
		do {
			try {
				func.call();
				break;
			} catch (Exception e) {
				atomicTries.getAndIncrement();
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) {
					throw new RuntimeException(e);
				}
				if (atomicTries.get() > 2) {
					throw new RuntimeException(e);
				}
			}
		}
		while (true);
	}
}
