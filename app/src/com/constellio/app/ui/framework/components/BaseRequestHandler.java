package com.constellio.app.ui.framework.components;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.pages.base.VaadinSessionContext;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.UserServices;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinServletRequest;
import com.vaadin.server.VaadinSession;

public abstract class BaseRequestHandler implements RequestHandler {
	
	private String path;
	
	public BaseRequestHandler(String path) {
		if (!StringUtils.endsWith(path, "/")) {
			path += "/";
		}
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	@Override
	public final boolean handleRequest(VaadinSession session, VaadinRequest request, VaadinResponse response)
			throws IOException {
		boolean requestHandled;
    	String sep = "/" + path;
    	String requestURI = ((VaadinServletRequest) request).getRequestURI();
        if (requestURI.indexOf(sep) != -1) {
    		ConstellioFactories constellioFactories = getConstellioFactories();
    		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
    		UserServices userServices = modelLayerFactory.newUserServices();
    		
    		VaadinSession vaadinSession = VaadinSession.getCurrent();
    		UserVO userVO = (UserVO) vaadinSession.getSession().getAttribute(VaadinSessionContext.CURRENT_USER_ATTRIBUTE);
    		String collection = (String) vaadinSession.getSession().getAttribute(VaadinSessionContext.CURRENT_COLLECTION_ATTRIBUTE);
    		
    		User user = userServices.getUserInCollection(userVO.getUsername(), collection);
    		
            String params = StringUtils.substringAfter(requestURI, sep);
    		Map<String, String> paramsMap = ParamUtils.getParamsMap(params);
    		requestHandled = handleRequest(requestURI, paramsMap, user, vaadinSession, request, response);
        } else {
        	requestHandled = false;
        }
        return requestHandled;
	}
	
	protected ConstellioFactories getConstellioFactories() {
		return ConstellioFactories.getInstance();
	}
	
	protected abstract boolean handleRequest(String requestURI, Map<String, String> paramsMap, User user, VaadinSession session, VaadinRequest request, VaadinResponse response) throws IOException; 

}
