package com.constellio.app.modules.scanner.servlets;

import com.constellio.app.modules.scanner.manager.ScannedDocumentsManager;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class UploadScannedImageServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ScannedDocumentsManager scannedDocumentsManager = ScannedDocumentsManager.get(request);

		String id = request.getParameter("id");
		//		int page = Integer.parseInt(request.getParameter("page"));
		boolean last = Boolean.parseBoolean(request.getParameter("last"));

		InputStream in = request.getInputStream();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IOUtils.copy(in, out);

		byte[] imageBytes = out.toByteArray();

		if (scannedDocumentsManager.isFinished(id)) {
			scannedDocumentsManager.clear(id);
		}
		scannedDocumentsManager.addScannedImage(id, imageBytes, last);
	}

}
