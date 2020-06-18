package com.constellio.app.api.admin.services;

import com.constellio.data.services.tenant.TenantProperties;
import com.constellio.data.services.tenant.TenantService;
import com.constellio.data.utils.TenantUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public class TenantFilter implements Filter {

	private TenantService tenantService;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		tenantService = TenantService.getInstance();
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		if (tenantService.isSupportingTenants()) {
			String host = request.getServerName();
			int port = request.getServerPort();
			if (port != 80 && port != 443) {
				host = host + ":" + port;
			}

			TenantProperties tenant = tenantService.getTenantByHostname(host);
			if (tenant == null) {
				throw new RuntimeException("No Tenant found for host " + host);
			}
			TenantUtils.setTenant(tenant.getId());
		}

		chain.doFilter(request, response);

		if (tenantService.isSupportingTenants()) {
			TenantUtils.setTenant(null);
		}
	}

	@Override
	public void destroy() {
	}
}
