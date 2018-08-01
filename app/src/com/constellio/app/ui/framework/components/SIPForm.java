package com.constellio.app.ui.framework.components;

import com.vaadin.ui.*;

public class SIPForm extends CustomComponent {
	private Layout mainLayout;

	protected Layout header;
	protected Layout central;
	protected HorizontalLayout footer;

	public SIPForm(FormLayout formLayout) {
		init(new HorizontalLayout(), formLayout, new HorizontalLayout());
	}

	protected void init(Layout header, Layout central, HorizontalLayout footer) {
		this.footer = footer;
		this.header = header;
		this.central = central;
		this.footer.setSpacing(true);

		mainLayout = new VerticalLayout();
		mainLayout.addComponent(header);
		mainLayout.addComponent(central);
		mainLayout.addComponent(footer);

		setCompositionRoot(mainLayout);
		setSizeFull();
	}

	public Layout getHeader() {
		return header;
	}

	public Layout getCentral() {
		return central;
	}

	public HorizontalLayout getFooter() {
		return footer;
	}
}