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
package com.constellio.app.ui.tools.vaadin;

import java.util.Date;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.pages.base.InitUIListener;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.tools.pageloadtime.PageLoadTimeWriter;
import com.constellio.sdk.dev.tools.TestSerializationUtils;
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
		new PageLoadTimeWriter().write(new Date());
		validateSerializable(ConstellioUI.getCurrent());
	}

	private void validateSerializable(ConstellioUI ui) {
		TestSerializationUtils.validateSerializable(ui);
	}

	@Override
	public void afterInitialize(final ConstellioUI ui) {
	}

}
