package com.constellio.app.api.pdf.pdfjs.servlets;

import com.constellio.app.api.pdf.pdfjs.services.PdfJSServices;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException.PdfSignatureException_CannotSignDocumentException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.pdf.pdfjs.PdfJSAnnotations;
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
			pdfJSServices.signAndCertifyPdf(record, metadata, user, annotations, localeCode);
			response.setStatus(HttpServletResponse.SC_OK);
			writeJSONResponse(new JSONObject(), request, response);
		} catch (PdfSignatureException_CannotSignDocumentException e) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
		} catch (Throwable t) {
			if (t instanceof IOException) {
				throw (IOException) t;
			} else {
				throw new ServletException(t);
			}
		}
	}

}
