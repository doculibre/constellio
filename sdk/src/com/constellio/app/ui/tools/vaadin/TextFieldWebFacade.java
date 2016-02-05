package com.constellio.app.ui.tools.vaadin;

import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class TextFieldWebFacade {

	ConstellioWebElement wrappedElement;

	public TextFieldWebFacade(ConstellioWebElement wrappedElement) {
		this.wrappedElement = wrappedElement;
	}

	public String getValue() {
		return wrappedElement.getAttribute("value");
	}

	public void setValue(final String value) {
		final String enteredValue = value == null ? "" : value;
		wrappedElement.changeValueTo(enteredValue);

		int attempts = 0;
		while (!enteredValue.equals(getValue()) && attempts < 10) {
			attempts++;
			wrappedElement.changeValueTo(enteredValue);
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

}