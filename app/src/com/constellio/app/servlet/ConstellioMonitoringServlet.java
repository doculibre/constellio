package com.constellio.app.servlet;

import com.constellio.data.services.tenant.TenantLocal;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.records.reindexing.SystemReindexingInfos;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static com.constellio.app.ui.i18n.i18n.$;

public class ConstellioMonitoringServlet extends HttpServlet {

	public static boolean systemRestarting;
	public static final TenantLocal<Boolean> tenantRestarting = new TenantLocal<>();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		SystemReindexingInfos reindexingInfos = ReindexingServices.getReindexingInfos();
		PrintWriter pw = resp.getWriter();

		if (isRestarting()) {
			pw.append(
					"<html><head><META HTTP-EQUIV=\"Refresh\" CONTENT=\"120\"/><link rel=\"stylesheet\" type=\"text/css\" href=\"./VAADIN/themes/constellio/styles.css?v=7.7.4\"></link></head><body>");
		} else {
			pw.append(
					"<html><head><META HTTP-EQUIV=\"Refresh\" CONTENT=\"30\"/><link rel=\"stylesheet\" type=\"text/css\" href=\"./VAADIN/themes/constellio/styles.css?v=7.7.4\"></link></head><body>");
		}

		pw.append("<div id=\"constellio-202051832\" class=\" v-app constellio constellioui\">\n"
				  + "    <div width-range=\"1101px-\" style=\"width: 100%; height: 100%;\" class=\"v-ui valo-menu-responsive loginview v-scrollable\"\n"
				  + "         tabindex=\"1\">\n"
				  + "        <div class=\"v-loading-indicator first\" style=\"position: absolute; display: none;\"></div>\n"
				  + "        <div style=\"position: absolute; width: 100%; height: 100%;\"\n"
				  + "             class=\"v-verticallayout v-layout v-vertical v-widget v-has-width v-has-height\">\n"
				  + "            <div style=\"padding-top: 0px;\" class=\"v-expand\">\n"
				  + "                <div style=\"height: 100%; margin-top: 0px;\" class=\"v-slot v-slot-login-panel v-align-center v-align-middle\">\n"
				  + "                    <div class=\"v-verticallayout v-layout v-vertical v-widget login-panel v-verticallayout-login-panel\">\n"
				  + "                        <div class=\"v-slot v-slot-labels\">\n"
				  + "                            <div class=\"v-csslayout v-layout v-widget labels v-csslayout-labels\">\n"
				  + "                                <div style=\"width: 100%; height: 200px;\"\n"
				  + "                                     class=\"v-horizontallayout v-layout v-horizontal v-widget v-has-width v-has-height\">\n"
				  + "                                    <div style=\"padding-left: 0px;\" class=\"v-expand\">\n"
				  + "                                        <div style=\"width: 100%; margin-left: 0px;\" class=\"v-slot v-slot-login-logo\">\n"
				  + "                                            <br/>");

		pw.append("<h1>");
		pw.append($("ConstellioMonitoringServlet.status"));
		pw.append(" : ");
		if (isRestarting()) {
			pw.append($("ConstellioMonitoringServlet.restarting"));
		} else {
			if (reindexingInfos != null) {
				pw.append($("ConstellioMonitoringServlet.reindexing"));
			} else {
				pw.append($("ConstellioMonitoringServlet.online"));
			}
		}
		pw.append("</h1>");

		if (reindexingInfos != null) {

			pw.append("<br/><br/><h4>");
			pw.append($("ConstellioMonitoringServlet.reindexingOf", reindexingInfos.getSchemaTypeLabel(),
					reindexingInfos.getCollection()));
			pw.append(" : ");
			pw.append("" + reindexingInfos.getProgression());
			pw.append(" / ");
			pw.append("" + reindexingInfos.getTotal());
			pw.append("</h4>");

		} else if (!isRestarting()) {

			pw.append(
					"<br/><br/><div  onclick=\"location.href='/constellio';\" class=\"v-button v-widget primary v-button-primary\" role=\"button\" tabindex=\"0\"><span class=\"v-button-wrap\"><span class=\"v-button-caption\">"
					+ $("ConstellioMonitoringServlet.returnToConstellio")
					+ "</span></span></div>");

		}

		pw.append("                                        </div>\n"
				  + "                                    </div>\n"
				  + "                                </div>\n"
				  + "                            </div>\n"
				  + "                        </div>\n"
				  + "                        <div class=\"v-spacing\"></div>\n"
				  + "                    </div>\n"
				  + "                </div>\n"
				  + "            </div>\n"
				  + "        </div>\n"
				  + "    </div>\n"
				  + "</div>\n");

		pw.append("</body></html>");

		pw.close();

		if (tenantRestarting != null) {
			tenantRestarting.set(false);
		}
	}

	private static boolean isRestarting() {
		if (systemRestarting) {
			return true;
		}

		if (tenantRestarting == null) {
			return false;
		}

		try {
			return tenantRestarting.get();
		} catch (Exception e) {
			return false;
		}
	}
}
