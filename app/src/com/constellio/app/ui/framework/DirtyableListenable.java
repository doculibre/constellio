package com.constellio.app.ui.framework;

import com.constellio.app.events.EventArgs;
import com.constellio.app.events.EventListener;
import com.constellio.app.events.EventObservable;
import com.constellio.app.ui.framework.DirtyableListenable.DirtiedArgs.DirtiedListener;
import com.constellio.app.ui.framework.components.Dirtyable;

public interface DirtyableListenable extends Dirtyable {
	void addComponentDirtiedListener(DirtiedListener listener);

	void removeComponentDirtiedListener(DirtiedListener listener);

	class DirtiedArgs extends EventArgs<DirtyableListenable> {

		public DirtiedArgs(DirtyableListenable sender) {
			super(sender);
		}

		public interface DirtiedListener extends EventListener<DirtiedArgs> {
		}

		public static class DirtiedObservable extends EventObservable<DirtiedArgs> {
		}
	}
}
