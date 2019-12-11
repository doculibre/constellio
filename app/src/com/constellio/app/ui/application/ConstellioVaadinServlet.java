package com.constellio.app.ui.application;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.dao.services.Stats;
import com.constellio.data.dao.services.Stats.CallStatCompiler;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.CustomizedSystemMessages;
import com.vaadin.server.SystemMessages;
import com.vaadin.server.SystemMessagesInfo;
import com.vaadin.server.SystemMessagesProvider;
import com.vaadin.server.VaadinServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.constellio.app.ui.i18n.i18n.$;

@SuppressWarnings("serial")
@WebServlet(value = "/*", asyncSupported = true)
@VaadinServletConfiguration(productionMode = false, ui = ConstellioUI.class)
public class ConstellioVaadinServlet extends VaadinServlet {

	boolean initialized = false;
	Thread initThread;

	@Override
	public void init(ServletConfig servletConfig)
			throws ServletException {
		super.init(servletConfig);
		initThread = new Thread() {
			@Override
			public void run() {
				ConstellioFactories.getInstance();
				initialized = true;
			}
		};
		initThread.start();
	}


	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (handleContextRootWithoutSlash(request, response)) {
			return;
		}
		boolean staticResourceRequest = isStaticResourceRequest(request);
		if (!staticResourceRequest) {
			if (!initialized) {
				try {
					initThread.join();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			ConstellioFactories.getInstance().onRequestStarted();
		}

		CallStatCompiler statCompiler = Stats.compilerFor("Unknown");
		long start = statCompiler.start();
		try {
			super.service(request, response);
		} finally {
			statCompiler.stop(start);
			if (!staticResourceRequest) {
				ConstellioFactories.getInstance().onRequestEnded();
			}
		}
	}

	/**
	 * Adapted to support responsive design.
	 * <p>
	 * See https://vaadin.com/forum#!/thread/1676923
	 *
	 * @see com.vaadin.server.VaadinServlet#servletInitialized()
	 */

	@Override
	protected final void servletInitialized()
			throws ServletException {
		super.servletInitialized();
		getService().addSessionInitListener(new ConstellioSessionInitListener());

		final CustomizedSystemMessages messages = new CustomizedSystemMessages() {
			@Override
			public String getSessionExpiredCaption() {
				return $("ConstellioVaadinServlet.internalErrorCaption");
			}

			@Override
			public String getSessionExpiredMessage() {
				return $("ConstellioVaadinServlet.internalErrorMessage");
			}

			@Override
			public String getInternalErrorCaption() {
				return $("ConstellioVaadinServlet.internalErrorCaption");
			}

			@Override
			public String getInternalErrorMessage() {
				return $("ConstellioVaadinServlet.internalErrorMessage");
			}
		};
		getService().setSystemMessagesProvider(new SystemMessagesProvider() {
			@Override
			public SystemMessages getSystemMessages(SystemMessagesInfo systemMessagesInfo) {
				return messages;
			}
		});
	}


	public static ConstellioVaadinServlet getCurrent() {
		return (ConstellioVaadinServlet) VaadinServlet.getCurrent();
	}

}
