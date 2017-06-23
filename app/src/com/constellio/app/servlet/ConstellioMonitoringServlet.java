package com.constellio.app.servlet;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.records.reindexing.SystemReindexingInfos;

public class ConstellioMonitoringServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		SystemReindexingInfos reindexingInfos = ReindexingServices.getReindexingInfos();
		PrintWriter pw = resp.getWriter();

		pw.append("<html><head><META HTTP-EQUIV=\"Refresh\" CONTENT=\"3\"/></head><body>");

		pw.append("<h1>");
		pw.append($("ConstellioMonitoringServlet.status"));
		pw.append(" : ");
		pw.append($("ConstellioMonitoringServlet.online"));
		pw.append("</h1>");

		if (reindexingInfos != null) {

			pw.append("<br/><br/><h2>");
			pw.append($("ConstellioMonitoringServlet.reindexingOf", reindexingInfos.getSchemaTypeLabel(),
					reindexingInfos.getCollection()));
			pw.append(" : ");
			pw.append("" + reindexingInfos.getProgression());
			pw.append(" / ");
			pw.append("" + reindexingInfos.getTotal());
			pw.append("</h2>");

		}

		pw.append("</body></html>");

		pw.close();
	}
}
