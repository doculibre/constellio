package com.constellio.app.servlet;

import com.constellio.data.conf.FoldersLocator;
import com.constellio.data.conf.FoldersLocatorMode;
import org.apache.chemistry.opencmis.server.impl.webservices.CallContextHandlerInterceptor;
import org.apache.chemistry.opencmis.server.impl.webservices.CmisWebServicesServlet;
import org.apache.chemistry.opencmis.server.impl.webservices.SoapActionRemoveInterceptor;
import org.apache.chemistry.opencmis.server.impl.webservices.UsernameTokenInterceptor;
import org.apache.cxf.Bus;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptor;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Set;

public class ConstellioCmisWebServicesServlet extends CmisWebServicesServlet {

	@Override
	public void init(ServletConfig config)
			throws ServletException {
		if (isEnabled()) {
			super.init(config);
		}
	}

	@Override
	public void handleRequest(final HttpServletRequest request, HttpServletResponse response)
			throws ServletException {
		HttpServletRequestWrapper requestProxyWrapper = new HttpServletRequestWrapper(request) {
			public String getScheme() {
				String scheme = request.getHeader("X-Forwarded-Proto");
				if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
					scheme = request.getScheme();
				}
				return scheme;
			}

			@Override
			public int getServerPort() {
				int port = request.getServerPort();
				String scheme = request.getHeader("X-Forwarded-Proto");
				if ("https".equalsIgnoreCase(scheme)) {
					port = 443;
				}
				return port;
			}
		};
		super.handleRequest(requestProxyWrapper, response);
	}

	/**
	 * Adds and configures interceptors for OpenCMIS.
	 * <p>
	 * Override this method to add more interceptors.
	 */
	protected void configureInterceptors(Bus bus) {
		bus.getInInterceptors().add(new SoapInterceptorDecorator(new SoapActionRemoveInterceptor()));
		bus.getInInterceptors().add(new SoapInterceptorDecorator(new CallContextHandlerInterceptor(getCallContextHandler())));
		bus.getInInterceptors().add(new SoapInterceptorDecorator(new UsernameTokenInterceptor()));
	}

	/**
	 * Only handles SoapMessage
	 */
	private static class SoapInterceptorDecorator implements Interceptor<Message>, PhaseInterceptor<Message> {

		AbstractSoapInterceptor interceptor;

		public SoapInterceptorDecorator(AbstractSoapInterceptor interceptor) {
			this.interceptor = interceptor;
		}

		@Override
		public void handleMessage(Message message) throws Fault {
			if (message instanceof SoapMessage) {
				interceptor.handleMessage((SoapMessage) message);
			}
		}

		@Override
		public void handleFault(Message message) {
			if (message instanceof SoapMessage) {
				interceptor.handleMessage((SoapMessage) message);
			}
		}

		@Override
		public Set<String> getAfter() {
			return interceptor.getAfter();
		}

		@Override
		public Set<String> getBefore() {
			return interceptor.getBefore();
		}

		@Override
		public String getId() {
			return interceptor.getId();
		}

		@Override
		public String getPhase() {
			return interceptor.getPhase();
		}

		@Override
		public Collection<PhaseInterceptor<? extends Message>> getAdditionalInterceptors() {
			return interceptor.getAdditionalInterceptors();
		}
	}

	private boolean isEnabled() {
		FoldersLocator foldersLocator = new FoldersLocator();
		if (foldersLocator.getFoldersLocatorMode() == FoldersLocatorMode.WRAPPER) {
			return true;
		} else {
			return "true".equals(System.getProperty("cmisEnabled"));
		}
	}

	@Override
	public void destroy() {
		if (bus != null) {
			super.destroy();
		}
	}
}
