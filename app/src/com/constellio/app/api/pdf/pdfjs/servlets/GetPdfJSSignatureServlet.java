package com.constellio.app.api.pdf.pdfjs.servlets;

import com.constellio.app.api.pdf.pdfjs.services.PdfJSServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class GetPdfJSSignatureServlet extends BasePdfJSServlet {

	public static final String PATH = "/pdfjs/getSignature";

	@Override
	protected void doService(Record record, Metadata metadata, User user, String localeCode, HttpServletRequest request,
							 HttpServletResponse response) throws ServletException, IOException {
		boolean initials = "true".equals(request.getParameter("initials"));

		PdfJSServices pdfJSServices = newPdfJSServices();

		String signatureBase64Url = pdfJSServices.getSignatureBase64Url(user, initials);
		if (StringUtils.isBlank(signatureBase64Url)) {
			signatureBase64Url = "";
		}
		writeResponse(signatureBase64Url, request, response);
	}

}
