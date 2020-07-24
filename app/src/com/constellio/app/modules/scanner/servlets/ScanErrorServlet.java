package com.constellio.app.modules.scanner.servlets;

import com.constellio.app.modules.scanner.manager.ScannedDocumentsManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ScanErrorServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ScannedDocumentsManager imageManager = ScannedDocumentsManager.get(request);
		String id = request.getParameter("id");
		String message = request.getParameter("message");
		imageManager.setErrorMessage(id, message);
		System.err.println("Error while scanning for id " + id + " : " + message);
	}

}
