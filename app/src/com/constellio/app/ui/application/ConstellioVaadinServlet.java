package com.constellio.app.ui.application;

import javax.servlet.ServletConfig;
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

}
