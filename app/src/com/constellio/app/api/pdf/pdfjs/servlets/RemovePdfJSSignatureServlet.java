package com.constellio.app.api.pdf.pdfjs.servlets;

import com.constellio.app.api.pdf.pdfjs.services.PdfJSServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RemovePdfJSSignatureServlet extends BasePdfJSServlet {

	public static final String PATH = "/pdfjs/removeSignature";

	@Override
	protected void doService(Record record, Metadata metadata, User user, String localeCode, HttpServletRequest request,
							 HttpServletResponse response) throws ServletException, IOException {
		boolean initials = "true".equals(request.getParameter("initials"));

		PdfJSServices pdfJSServices = newPdfJSServices();

		try {
			pdfJSServices.removeSignature(user, initials);
			writeJSONResponse(new JSONObject(), request, response);
		} catch (Throwable t) {
			if (t instanceof IOException) {
				throw (IOException) t;
			} else {
				throw new ServletException(t);
			}
		}
	}

}
