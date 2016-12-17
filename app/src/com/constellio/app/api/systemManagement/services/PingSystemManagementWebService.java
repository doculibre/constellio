package com.constellio.app.api.systemManagement.services;

import javax.servlet.http.HttpServletRequest;

import org.jdom2.Element;

public class PingSystemManagementWebService extends AdminSystemManagementWebService {
	@Override
	protected void doService(HttpServletRequest req, Element responseDocumentRootElement) {
		responseDocumentRootElement.setText("success");
	}
}
