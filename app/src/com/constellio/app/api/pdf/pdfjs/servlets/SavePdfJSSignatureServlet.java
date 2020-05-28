package com.constellio.app.api.pdf.pdfjs.servlets;

import com.constellio.app.api.pdf.pdfjs.services.PdfJSServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.records.RecordServicesException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SavePdfJSSignatureServlet extends BasePdfJSServlet {

	public static final String PATH = "/pdfjs/saveSignature";

	@Override
	protected void doService(Record record, Metadata metadata, User user, String localeCode, HttpServletRequest request,
							 HttpServletResponse response) throws ServletException, IOException {
		boolean initials = "true".equals(request.getParameter("initials"));

		PdfJSServices pdfJSServices = newPdfJSServices();

		String signatureBase64 = getJSONFromRequest(request);
		try {
			pdfJSServices.saveSignatureBase64Url(user, signatureBase64, initials);
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (RecordServicesException e) {
			throw new ServletException(e);
		}
	}

}
