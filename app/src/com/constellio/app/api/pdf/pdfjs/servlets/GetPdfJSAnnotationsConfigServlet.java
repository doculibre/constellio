package com.constellio.app.api.pdf.pdfjs.servlets;

import com.constellio.app.api.pdf.pdfjs.services.PdfJSServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class GetPdfJSAnnotationsConfigServlet extends BasePdfJSServlet {

	public static final String PATH = "/pdfjs/getAnnotationsConfig";

	@Override
	protected void doService(Record record, Metadata metadata, User user, String localeCode, HttpServletRequest request,
							 HttpServletResponse response) throws ServletException, IOException {
		String serviceKey = authenticator.getUserServiceKey(request);
		String token = authenticator.getUserToken(request);
		String urlPrefix = request.getParameter("urlPrefix");

		PdfJSServices pdfJSServices = newPdfJSServices();

		String pdfJSAnnotationsConfig = pdfJSServices.getAnnotationsConfig(record, metadata, user, localeCode, serviceKey, token, urlPrefix);
		writeResponse(pdfJSAnnotationsConfig, request, response);
	}

}

