package com.constellio.app.ui.framework.buttons.toggle;

import com.constellio.app.events.EventArgs;
import com.constellio.app.events.EventListener;
import com.constellio.app.events.EventObservable;
import com.vaadin.ui.Component;


public interface Toggleable extends Component {

	void toggle();

	boolean isToggled();

	Component getDefaultView();

	Component getToggledView();

	void addToggleToDefaultViewListener(ToggleToDefaultViewListener listener);

	void removeToggleToDefaultViewListener(ToggleToDefaultViewListener listener);

	void addToggleToToggledViewListener(ToggleToToggledViewListener listener);

	void removeToggleToToggledViewListener(ToggleToToggledViewListener listener);

	void addToggledListener(ToggledListener listener);

	void removeToggledListener(ToggledListener listener);

	class ToggleEventArgs extends EventArgs<Toggleable> {
		public ToggleEventArgs(Toggleable sender) {
			super(sender);
		}

		public boolean isToggled() {
			Toggleable sender = getSender();
			return sender != null && sender.isToggled();
		}
	}

	interface ToggleToDefaultViewListener extends EventListener<ToggleEventArgs> {
	}

	interface ToggleToToggledViewListener extends EventListener<ToggleEventArgs> {
	}

	interface ToggledListener extends EventListener<ToggleEventArgs> {
	}

	class ToggleEventObservable extends EventObservable<ToggleEventArgs> {
	}
}
