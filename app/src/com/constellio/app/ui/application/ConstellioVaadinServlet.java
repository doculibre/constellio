package com.constellio.app.ui.application;

import javax.servlet.ServletConfig;

import com.vaadin.server.ClientConnector;
import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.ServiceException;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletService;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Component;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import com.constellio.app.services.factories.ConstellioFactories;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinServlet;

@SuppressWarnings("serial")
@WebServlet(value = "/*", asyncSupported = true)
@VaadinServletConfiguration(productionMode = false, ui = ConstellioUI.class)
public class ConstellioVaadinServlet extends VaadinServlet {

    @Override
	public void init(ServletConfig servletConfig)
			throws ServletException {
    	super.init(servletConfig);
    	ConstellioFactories.getInstance();
	}

	/**
     * Adapted to support responsive design.
     * 
     * See https://vaadin.com/forum#!/thread/1676923
     * @see com.vaadin.server.VaadinServlet#servletInitialized()
     */

    @Override
    protected final void servletInitialized() throws ServletException {
        super.servletInitialized();
        getService().addSessionInitListener(new ConstellioSessionInitListener());
    }
    
    public static ConstellioVaadinServlet getCurrent() {
    	return (ConstellioVaadinServlet) VaadinServlet.getCurrent();
    }

    @Override
    protected VaadinServletService createServletService(
            DeploymentConfiguration deploymentConfiguration)
            throws ServiceException {
        LocalService service = new LocalService(this, deploymentConfiguration);
        service.init();

        return service;
    }

    private String getPackageName() {
        String pkgName;
        final Package pkg = this.getClass().getPackage();
        if (pkg != null) {
            pkgName = pkg.getName();
        } else {
            final String className = this.getClass().getName();
            pkgName = new String(className.toCharArray(), 0,
                    className.lastIndexOf('.'));
        }
        return pkgName;
    }

    public static class LocalService extends VaadinServletService {
        private static final long serialVersionUID = -5874716650679865909L;

        public LocalService(VaadinServlet servlet,
                DeploymentConfiguration deploymentConfiguration)
                throws ServiceException {
            super(servlet, deploymentConfiguration);
        }

        @Override
        protected VaadinSession createVaadinSession(VaadinRequest request)
                throws ServiceException {
            return new LocalSession(this);
        }
    }

    public static class LocalSession extends VaadinSession {
        private static final long serialVersionUID = 4596901275146146127L;

        public LocalSession(VaadinService service) {
            super(service);
        }

        @Override
        public String createConnectorId(ClientConnector connector) {
            if (connector instanceof Component) {
                Component component = (Component) connector;
                return component.getId() == null ? super
                        .createConnectorId(connector) : component.getId();
            }

            return super.createConnectorId(connector);
        }
    }
}
