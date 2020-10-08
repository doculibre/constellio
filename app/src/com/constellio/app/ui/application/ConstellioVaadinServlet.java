package com.constellio.app.ui.application;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.data.utils.TenantUtils.EMPTY_TENANT_ID;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.dao.services.Stats;
import com.constellio.data.dao.services.Stats.CallStatCompiler;
import com.constellio.data.services.tenant.TenantProperties;
import com.constellio.data.services.tenant.TenantService;
import com.constellio.data.utils.TenantUtils;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.CustomizedSystemMessages;
import com.vaadin.server.SystemMessages;
import com.vaadin.server.SystemMessagesInfo;
import com.vaadin.server.SystemMessagesProvider;
import com.vaadin.server.VaadinServlet;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("serial")
@WebServlet(value = "/*", asyncSupported = true)
@VaadinServletConfiguration(productionMode = false, ui = ConstellioUI.class)
@Slf4j
public class ConstellioVaadinServlet extends VaadinServlet {

	private TenantService tenantService = TenantService.getInstance();
	private Set<String> initializedTenantIds = new HashSet<>();
	private Map<String, Thread> initThreadByTenantId = new HashMap<>();
	private static ThreadLocal<HttpServletRequest> threadRequest = new ThreadLocal<>();

	@Override
	public void init(ServletConfig servletConfig)
			throws ServletException {
		super.init(servletConfig);

		if (tenantService.isSupportingTenants()) {
			// TODO executor service to prevent launching too many threads
			tenantService.getTenants().forEach(tenant -> {
				String tenantId = "" + tenant.getId();
				Thread initThread = new Thread(() -> {
					TenantUtils.setTenant(tenantId);
					ConstellioFactories.getInstance(tenantId);
					initializedTenantIds.add(tenantId);
				});
				initThread.start();
				initThreadByTenantId.put(tenantId, initThread);
				log.info("Starting tenant " + tenantId);
			});
		} else {
			Thread initThread = new Thread(() -> {
				ConstellioFactories.getInstance();
				initializedTenantIds.add(EMPTY_TENANT_ID);
			});
			initThread.start();
			initThreadByTenantId.put(EMPTY_TENANT_ID, initThread);
			log.info("Starting without any tenant");
		}
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			threadRequest.set(request);

			if (handleContextRootWithoutSlash(request, response)) {
				return;
			}

			boolean supportingTenants = tenantService.isSupportingTenants();

			boolean staticResourceRequest = isStaticResourceRequest(request);
			if (!staticResourceRequest) {
				String host = request.getHeader(HttpHeaders.HOST);
				TenantProperties tenant = tenantService.getTenantByHostname(host);
				String tenantId = tenant != null ? "" + tenant.getId() : null;

				if (supportingTenants && tenantId == null) {
					throw new RuntimeException("No Tenant found for host " + host);
				}

				if (tenantId == null) {
					tenantId = EMPTY_TENANT_ID;
				}

				if (!initializedTenantIds.contains(tenantId)) {
					try {
						initThreadByTenantId.get(tenantId).join();
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
		} finally {
			threadRequest.set(null);
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
	
	public static HttpServletRequest getCurrentHttpServletRequest() {
		return threadRequest.get();
	}

}
