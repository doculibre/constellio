package com.constellio.app.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.records.reindexing.SystemReindexingInfos;

public class ConstellioPingServlet extends HttpServlet {

	public static boolean systemRestarting;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		ConstellioFactories constellioFactories = ConstellioFactories.getInstanceIfAlreadyStarted();

		boolean testSolr = !"false".equals(req.getParameter("pingSolr"));

		SystemReindexingInfos reindexingInfos = ReindexingServices.getReindexingInfos();
		PrintWriter pw = resp.getWriter();

		boolean online;
		if (systemRestarting) {
			pw.append("Status : online (restarting)");
			online = true;

		} else {
			if (constellioFactories != null) {
				try {
					if (testSolr) {
						constellioFactories.getDataLayerFactory().newRecordDao().documentsCount();
					}
					if (reindexingInfos != null) {
						pw.append("Status : online (reindexing)");
						online = true;

					} else {
						pw.append("Status : online (running)");
						online = true;
					}
				} catch (Exception e) {
					pw.append("Status : offline (Solr error)");
					online = false;
					e.printStackTrace();
				}
			} else {
				pw.append("Status : online (starting)");
				online = true;
			}

		}

		if (online) {
			pw.println();
			pw.append("success");
		}

		pw.println();
		pw.append(
				"Layers : " + DataLayerFactory.countConstructor + " constructed, " + DataLayerFactory.countInit + " initialized");

		pw.close();
	}
}
