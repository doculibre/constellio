package com.constellio.app.servlet;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.ping.PingServices;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.records.reindexing.SystemReindexingInfos;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class ConstellioPingServlet extends HttpServlet {

	public static boolean systemRestarting;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		AppLayerFactory appLayerFactory = ConstellioFactories.getInstanceIfAlreadyStarted().getAppLayerFactory();
		PingServices pingServices = new PingServices(appLayerFactory);

		boolean testSolr = !"false".equals(req.getParameter("pingSolr"));

		PrintWriter pw = resp.getWriter();

		boolean online = true;
		if (systemRestarting) {
			pw.append("Constellio status : online (restarting)");
			pw.append("\n");
			online = changeOnlineStatus(online, true);

		} else {
			if (appLayerFactory != null) {
				getConstellioStatus(pw);

				if (testSolr) {
					online = pingServices.testZookeeperAndSolr(pw, online);
				}
			} else {
				pw.append("Constellio status : online (starting)");
				pw.append("\n");
				online = changeOnlineStatus(online, true);
			}
		}

		if (online) {
			pw.append("success");
		} else {
			pw.append("This ping contain error");
		}

		pw.close();
	}


	private boolean changeOnlineStatus(boolean currentVal, boolean isOnline) {
		if (!currentVal) {
			return false;
		} else {
			return isOnline;
		}
	}

	private void getConstellioStatus(PrintWriter pw) {
		SystemReindexingInfos reindexingInfos = ReindexingServices.getReindexingInfos();

		if (reindexingInfos != null) {
			pw.append("Constellio status : online (reindexing)");
			pw.append("\n");

		} else {
			pw.append("Constellio status : online (running)");
			pw.append("\n");
		}
	}
}
