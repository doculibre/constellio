package com.constellio.app.modules.restapi.core.config.filter;

import com.constellio.app.modules.restapi.ConstellioRestApiModule;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.services.tenant.TenantProperties;
import com.constellio.data.services.tenant.TenantService;
import com.constellio.data.utils.TenantUtils;
import com.constellio.model.services.extensions.ConstellioModulesManager;

import javax.annotation.PostConstruct;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@PreMatching
public class RestApiTenantFilter implements ContainerRequestFilter, ContainerResponseFilter {

	private TenantService tenantService;

	@PostConstruct
	public void init() {
		tenantService = TenantService.getInstance();
	}

	@Override
	public void filter(ContainerRequestContext requestContext) {
		if (tenantService.isSupportingTenants()) {
			String host = requestContext.getHeaderString(HttpHeaders.HOST);
			if (host == null) {
				throw new RuntimeException("Missing host header");
			}
			TenantProperties tenant = tenantService.getTenantByHostname(host);
			if (tenant == null) {
				throw new RuntimeException("No Tenant found for host " + host);
			}
			TenantUtils.setTenant(tenant.getId());

			ConstellioModulesManager modulesManager =
					ConstellioFactories.getInstance().getAppLayerFactory().getModulesManager();
			if (!modulesManager.isInstalled(ConstellioRestApiModule.ID)) {
				requestContext.abortWith(Response.status(Status.BAD_REQUEST).build());
			}
		}
	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
		if (tenantService.isSupportingTenants()) {
			TenantUtils.setTenant(null);
		}
	}
}
