package com.constellio.app.api.pdf.pdfjs.servlets;

import com.constellio.app.api.pdf.pdfjs.services.PdfJSServices;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.pdf.pdfjs.signature.PdfJSAnnotations;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CertifyPdfJSSignaturesServlet extends BasePdfJSServlet {

	public static final String PATH = "/pdfjs/certify";

	@Override
	protected void doService(Record record, Metadata metadata, User user, String localeCode, HttpServletRequest request,
							 HttpServletResponse response) throws ServletException, IOException {
		PdfJSServices pdfJSServices = newPdfJSServices();

		PdfJSAnnotations annotations = getAnnotationsFromRequest(request);
		try {
			pdfJSServices.signAndCertifyPdf(record, metadata, user, annotations);
			response.setStatus(HttpServletResponse.SC_OK);
			JSONObject result = new JSONObject();
			writeResponse(result.toString(), request, response);
		} catch (PdfSignatureException e) {
			throw new ServletException(e);
		}
	}

}
