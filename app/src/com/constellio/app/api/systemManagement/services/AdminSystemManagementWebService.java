package com.constellio.app.api.systemManagement.services;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.constellio.app.api.systemManagement.services.AdminHttpServletRuntimeException.AdminHttpServletRuntimeException_Unauthorized;
import com.constellio.app.services.appManagement.AppManagementService.LicenseInfo;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.services.factories.ModelLayerFactory;

public abstract class AdminSystemManagementWebService extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		Element rootElement = new Element("response");

		PrintWriter writer = resp.getWriter();

		try {
			authenticate(req);
			doService(req, rootElement);
			writer.append(xmlDocumentToString(rootElement));

		} catch (AdminHttpServletRuntimeException_Unauthorized e) {
			rootElement = new Element("response");
			rootElement.addContent(new Element("unauthorizedAccess").setText(e.getMessage()));

		} catch (AdminHttpServletRuntimeException e) {
			rootElement = new Element("response");
			rootElement.addContent(new Element("error").setText(e.getMessage()));

		} catch (Throwable t) {
			rootElement.addContent(new Element("exception").setText(ExceptionUtils.getStackTrace(t)));
		}

		writer.append(xmlDocumentToString(rootElement));
		writer.close();
	}

	private void authenticate(HttpServletRequest req) {
		String certificatHash = req.getParameter("certificateHash");

		LicenseInfo info = appLayerFactory().newApplicationService().getLicenseInfo();

		if (info == null) {
			throw new AdminHttpServletRuntimeException_Unauthorized("No certificate");
		}

		if (certificatHash == null) {
			throw new AdminHttpServletRuntimeException_Unauthorized("No certificate in parameter");
		}

		if (!certificatHash.equals(info.getSignature())) {
			throw new AdminHttpServletRuntimeException_Unauthorized(
					"Certificate in parameter does not match uploaded certificate");
		}

	}

	AppLayerFactory appLayerFactory() {
		return ConstellioFactories.getInstance().getAppLayerFactory();
	}

	ModelLayerFactory modelLayerFactory() {
		return ConstellioFactories.getInstance().getModelLayerFactory();
	}

	IOServices ioServices() {
		return ConstellioFactories.getInstance().getIoServicesFactory().newIOServices();
	}

	protected abstract void doService(HttpServletRequest req, Element responseDocumentRootElement);

	String xmlDocumentToString(Element rootElement) {
		Document document = new Document();
		document.addContent(rootElement);
		XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
		return xmlOutput.outputString(document);
	}

	protected String getRequiredParameter(HttpServletRequest req, String parameterName) {
		String value = req.getParameter(parameterName);
		if (StringUtils.isBlank(value)) {
			throw new AdminHttpServletRuntimeException("Parameter '" + parameterName + "' is required");
		}

		return value;
	}

	protected void downloadTo(String url, File file) {
		try {
			URL fileUrl = new URL(url);

			FileUtils.copyURLToFile(fileUrl, file);
		} catch (Throwable t) {
			throw new RuntimeException("Failed to download '" + url + "'", t);
		}
	}

}
