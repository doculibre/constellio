package com.constellio.data.threads;

import com.constellio.data.io.services.facades.OpenedResourcesWatcher;

public abstract class ConstellioThread extends Thread {

	private String resourceName;

	public ConstellioThread(String resourceName) {
		this.resourceName = resourceName;
		setName(resourceName);
	}

	@Override
	public final void run() {
		try {
			execute();
		} finally {
			OpenedResourcesWatcher.onClose(this);
		}
	}

	@Override
	public synchronized void start() {
		OpenedResourcesWatcher.onOpen(this);
		super.start();
	}

	protected abstract void execute();

	@Override
	public final String toString() {
		return resourceName;
	}
}
