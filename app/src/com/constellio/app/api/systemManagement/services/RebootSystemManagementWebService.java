package com.constellio.app.api.systemManagement.services;

import javax.servlet.http.HttpServletRequest;

import org.jdom2.Element;

import com.constellio.app.services.appManagement.AppManagementServiceException;
import com.constellio.app.services.factories.ConstellioFactories;

public class RebootSystemManagementWebService extends AdminSystemManagementWebService {
	@Override
	protected void doService(HttpServletRequest req, Element responseDocumentRootElement) {
		responseDocumentRootElement.setText("OK");
		new java.util.Timer().schedule(
				new java.util.TimerTask() {
					@Override
					public void run() {
						try {
							ConstellioFactories.getInstance().getAppLayerFactory().newApplicationService().restart();
						} catch (AppManagementServiceException e) {
							e.printStackTrace();
						}
					}
				},
				5000
		);
	}
}
