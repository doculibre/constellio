package com.constellio.app.modules.scanner.ui;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.scanner.manager.ScannedDocumentsManager;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.utils.HttpRequestUtils;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentManager.ContentVersionDataSummaryResponse;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.users.UserServices;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletService;
import com.vaadin.ui.Label;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.UUID;

public class ScanDocumentWindow extends BaseWindow {

	private String scanId;

	public ScanDocumentWindow() {
		scanId = UUID.randomUUID().toString();

		sendScanRequest();
		buildUI();
		waitForScanToComplete();
	}

	private void buildUI() {
		setModal(true);
		setClosable(true);
		center();

		Label progressLabel = new Label("Scan in progress...");
		setContent(progressLabel);
	}

	private void waitForScanToComplete() {
		final ScannedDocumentsManager scannedDocumentsManager = ScannedDocumentsManager.get();
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		String collection = sessionContext.getCurrentCollection();
		UserVO userVO = sessionContext.getCurrentUser();

		AppLayerFactory appLayerFactory = ConstellioUI.getCurrent().getConstellioFactories().getAppLayerFactory();
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();

		final UserServices userServices = modelLayerFactory.newUserServices();
		final RecordServices recordServices = modelLayerFactory.newRecordServices();
		final ContentManager contentManager = modelLayerFactory.getContentManager();
		final IOServices ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		ConstellioUI.getCurrent().runAsync(new Runnable() {
			@Override
			public void run() {
				while (!scannedDocumentsManager.isFinished(scanId)) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				if (scannedDocumentsManager.isScanSuccess(scanId)) {
					String filename = "Scan_" + new LocalDateTime().toString() + ".pdf";
					byte[] pdfContent;
					try {
						pdfContent = scannedDocumentsManager.getPDFContent(scanId);

						InputStream in = ioServices.newBufferedByteArrayInputStream(pdfContent, "ScannedDocumentWindow.content");
						ContentVersionDataSummaryResponse contentVersionDataSummaryResponse = contentManager.upload(in, filename);
						ioServices.closeQuietly(in);
						ContentVersionDataSummary contentVersionDataSummary = contentVersionDataSummaryResponse.getContentVersionDataSummary();

						User user = userServices.getUserInCollection(userVO.getUsername(), collection);
						Content content = contentManager.createMajor(user, filename, contentVersionDataSummary);

						UserDocument userDocument = rm.newUserDocumentWithId(scanId);
						userDocument.setTitle(filename);
						userDocument.setContent(content);
						userDocument.setUser(user);

						recordServices.add(userDocument, user);
						ConstellioUI.getCurrent().access(new Runnable() {
							@Override
							public void run() {
								ConstellioUI.getCurrent().navigate().to(RMViews.class).addScannedDocument(scanId);
							}
						});
					} catch (IOException e1) {
						e1.printStackTrace();
					} catch (RecordServicesException e) {
						e.printStackTrace();
					}
				}
				ConstellioUI.getCurrent().access(new Runnable() {
					@Override
					public void run() {
						close();
					}
				});
			}
		}, 1000, this);
	}

	private void sendScanRequest() {
		String url = getScannerCallbackURL(scanId);
		Page.getCurrent().open(url, null);
	}

	public static String getScannerCallbackURL(String scanId) {
		String baseURL = getScannerBaseURL(VaadinServletService.getCurrentServletRequest());
		try {
			return "cwscan://" + URLEncoder.encode(baseURL + "/" + scanId, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getScannerBaseURL(HttpServletRequest request) {
		String agentBaseURL;
		ModelLayerFactory modelLayerFactory = ConstellioFactories.getInstance().getModelLayerFactory();
		SystemConfigurationsManager systemConfigurationsManager = modelLayerFactory.getSystemConfigurationsManager();
		RMConfigs rmConfigs = new RMConfigs(systemConfigurationsManager);
		if (rmConfigs.isAgentEnabled()) {
			String baseURL;
			if (request != null) {
				baseURL = HttpRequestUtils.getBaseURL(request, true);
			} else {
				Page page = Page.getCurrent();
				URI location = page.getLocation();
				String contextPath = VaadinServlet.getCurrent().getServletContext().getContextPath();

				String schemeSpecificPart = location.getSchemeSpecificPart().substring(2);
				String schemeSpecificPartBeforeContextPath;
				if (StringUtils.isNotBlank(contextPath)) {
					if (schemeSpecificPart.indexOf(contextPath) != -1) {
						schemeSpecificPartBeforeContextPath = StringUtils.substringBeforeLast(schemeSpecificPart, contextPath);
					} else {
						contextPath = null;
						schemeSpecificPartBeforeContextPath = StringUtils.removeEnd(schemeSpecificPart, "/");
					}
				} else {
					schemeSpecificPartBeforeContextPath = StringUtils.removeEnd(schemeSpecificPart, "/");
				}

				StringBuffer baseURLSB = new StringBuffer();
				baseURLSB.append(location.getScheme());
				baseURLSB.append("://");
				baseURLSB.append(schemeSpecificPartBeforeContextPath);
				if (StringUtils.isNotBlank(contextPath)) {
					baseURLSB.append(contextPath);
				}
				baseURL = baseURLSB.toString();
			}

			StringBuffer sb = new StringBuffer();
			sb.append(baseURL);
			sb.append("/scanner");
			agentBaseURL = sb.toString();
		} else {
			agentBaseURL = null;
		}
		return agentBaseURL;
	}

}
