package com.constellio.app.servlet;

import com.constellio.app.ui.pages.externals.ExternalSignInSuccessViewImpl;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.records.reindexing.SystemReindexingInfos;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class ConstellioSignInSuccessServlet extends HttpServlet {

	public static final String PATH_WITHOUT_SLASH = "signInSuccess";
	public static final String PATH = "/" + PATH_WITHOUT_SLASH;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		SystemReindexingInfos reindexingInfos = ReindexingServices.getReindexingInfos();
		PrintWriter pw = resp.getWriter();

		pw.append(
				"<html><head><META HTTP-EQUIV=\"Refresh\" CONTENT=\"30\"/><link rel=\"stylesheet\" type=\"text/css\" href=\"./VAADIN/themes/constellio/styles.css?v=7.7.4\"></link></head><body>");

		pw.append(ExternalSignInSuccessViewImpl.getHtmlContent());

		pw.append("</body></html>");

		pw.close();
	}
}
