package com.constellio.app.api.systemManagement.services;

import com.constellio.app.api.HttpServletRequestAuthenticator;
import com.constellio.app.api.search.isNotAuthenticatedException;
import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class SystemInfoWebService extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		try {
			respondFullSystemInformation(authenticate(req), req, resp);
		} catch (isNotAuthenticatedException e) {
			respondLimitedSystemInformation(req, resp);
		}
	}

	private void respondFullSystemInformation(UserCredential user,
											  HttpServletRequest req, HttpServletResponse resp) throws IOException {
		respondLimitedSystemInformation(req, resp);
	}

	private void respondLimitedSystemInformation(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		JsonObject responseJson = new JsonObject();
		responseJson.addProperty("version", getVersion());

		JsonArray installedPlugins = new JsonArray();
		getInstalledPlugins().stream().forEach(plugin -> installedPlugins.add(plugin.getId()));
		responseJson.add("plugins", installedPlugins);

		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setCharacterEncoding("UTF-8");
		resp.setHeader("Access-Control-Allow-Origin", "*");
		resp.setContentType("application/json; charset=UTF-8");
		resp.getWriter().write(responseJson.toString());
	}

	protected UserCredential authenticate(HttpServletRequest request) {
		HttpServletRequestAuthenticator authenticator = new HttpServletRequestAuthenticator(modelLayerFactory());
		UserCredential user = authenticator.authenticate(request);
		if (user == null) {
			throw new isNotAuthenticatedException();
		}

		return user;
	}

	protected ModelLayerFactory modelLayerFactory() {
		return getConstellioFactories().getModelLayerFactory();
	}

	protected AppLayerFactory appLayerFactory() {
		return getConstellioFactories().getAppLayerFactory();
	}

	protected synchronized ConstellioFactories getConstellioFactories() {
		return ConstellioFactories.getInstance();
	}

	private List<InstallableModule> getInstalledPlugins() {
		return appLayerFactory().getPluginManager().getActivePluginModules();
	}

	private String getVersion() {
		return appLayerFactory().newApplicationService().getWarVersion();
	}

}
