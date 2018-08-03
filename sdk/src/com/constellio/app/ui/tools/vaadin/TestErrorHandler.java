package com.constellio.app.ui.tools.vaadin;

import com.constellio.app.ui.handlers.ConstellioErrorHandler;
import com.constellio.app.ui.tools.ServerThrowableContext;
import com.constellio.app.ui.tools.pageloadtime.PageLoadTimeWriter;
import com.vaadin.server.ErrorEvent;

import java.util.Date;

@SuppressWarnings("serial")
public class TestErrorHandler extends ConstellioErrorHandler {

	@Override
	public void error(ErrorEvent event) {
		Throwable throwable = event.getThrowable();
		ServerThrowableContext.LAST_THROWABLE.set(throwable);
		//		throwable.printStackTrace();

		new PageLoadTimeWriter().write(new Date());

		super.error(event);
	}

}
