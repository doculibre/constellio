package com.constellio.app.api.pdf.pdfjs.servlets;

import com.constellio.app.api.pdf.pdfjs.services.PdfJSServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.pdf.pdfjs.signature.PdfJSAnnotations;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class GetPdfJSAnnotationsServlet extends BasePdfJSServlet {

	public static final String PATH = "/pdfjs/getAnnotations";

	@Override
	protected void doService(Record record, Metadata metadata, User user, String localeCode, HttpServletRequest request,
							 HttpServletResponse response) throws ServletException, IOException {
		PdfJSServices pdfJSServices = newPdfJSServices();

		PdfJSAnnotations annotations = pdfJSServices.getAnnotations(record, metadata, user);
		if (annotations != null) {
			writeJSONResponse(annotations.getJSONObject(), request, response);
		}
	}

}
