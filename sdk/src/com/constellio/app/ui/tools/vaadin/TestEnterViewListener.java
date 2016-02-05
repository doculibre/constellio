package com.constellio.app.ui.tools.vaadin;

import java.util.Date;
import java.util.List;

import com.constellio.app.ui.pages.base.EnterViewListener;
import com.constellio.app.ui.tools.pageloadtime.PageLoadTimeWriter;
import com.constellio.app.ui.util.ComponentTreeUtils;
import com.constellio.sdk.dev.tools.TestSerializationUtils;
import com.vaadin.navigator.View;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;

@SuppressWarnings("serial")
public class TestEnterViewListener implements EnterViewListener {

	@Override
	public void enterView(View view) {
		addClickListeners(view);
		
		// Put a javascript variable in the view to know that the view is done loading.
		// This variable will be updated whenever an event happens (e.g.: a button is clicked)
		writePageLoadTime(view);
	}

	private void addClickListeners(final View view) {
		List<Button> buttons = ComponentTreeUtils.getChildren((Component) view, Button.class);
		for (Button button : buttons) {
			if (button.isEnabled()) {
				button.addClickListener(new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						// Whenever a click happens, we update the page load time
						writePageLoadTime(view);
					}
				});
			}
		}
	}
	
	private void writePageLoadTime(View view) {
		new PageLoadTimeWriter().write(new Date());
//		validateSerializable(view);
	}
	
	private void validateSerializable(View view) {
		TestSerializationUtils.validateSerializable(view);
	}

}
