package com.constellio.app.ui.tools.vaadin;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.pages.base.InitUIListener;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.VaadinSessionContext;
import com.vaadin.ui.HasComponents.ComponentAttachEvent;
import com.vaadin.ui.HasComponents.ComponentAttachListener;
import com.vaadin.ui.HasComponents.ComponentDetachEvent;
import com.vaadin.ui.HasComponents.ComponentDetachListener;

@SuppressWarnings("serial")
public class TestInitUIListener implements InitUIListener {

	private SessionContext sessionContext;

	public TestInitUIListener(SessionContext testSessionContext) {
		this.sessionContext = testSessionContext;
	}

	@Override
	public void beforeInitialize(ConstellioUI ui) {
		if (sessionContext != null) {
			ui.setSessionContext(sessionContext);
			ui.getSession().getSession().setAttribute(VaadinSessionContext.CURRENT_USER_ATTRIBUTE, sessionContext.getCurrentUser());
			ui.getSession().getSession().setAttribute(VaadinSessionContext.CURRENT_COLLECTION_ATTRIBUTE, sessionContext.getCurrentCollection());
		}

		ui.setErrorHandler(new TestErrorHandler());

		//		ui.addAttachListener(new AttachListener() {
		//			@Override
		//			public void attach(AttachEvent event) {
		//				writePageLoadTime();
		//			}
		//		});
		//
		//		ui.addDetachListener(new DetachListener() {
		//			@Override
		//			public void detach(DetachEvent event) {
		//				writePageLoadTime();
		//			}
		//		});

		ui.addComponentAttachListener(new ComponentAttachListener() {
			@Override
			public void componentAttachedToContainer(ComponentAttachEvent event) {
				writePageLoadTime();
			}
		});

		ui.addComponentDetachListener(new ComponentDetachListener() {
			@Override
			public void componentDetachedFromContainer(ComponentDetachEvent event) {
				writePageLoadTime();
			}
		});
	}

	private void writePageLoadTime() {
//		new PageLoadTimeWriter().write(new Date());
//		validateSerializable(ConstellioUI.getCurrent());
	}

	private void validateSerializable(ConstellioUI ui) {
//		TestSerializationUtils.validateSerializable(ui);
	}

	@Override
	public void afterInitialize(final ConstellioUI ui) {
	}

}
