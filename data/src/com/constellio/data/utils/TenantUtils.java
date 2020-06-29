package com.constellio.data.utils;

import com.constellio.data.services.tenant.TenantProperties;
import com.constellio.data.services.tenant.TenantService;

public class TenantUtils {

	private static InheritableThreadLocal<TenantProperties> tenantThreadLocal = new InheritableThreadLocal<>();

	private static TenantService tenantService = TenantService.getInstance();

	public static final String EMPTY_TENANT_ID = "-1";

	public static String getTenantId() {
		TenantProperties tenant = getTenant();
		return tenant != null ? "" + getTenant().getId() : null;
	}

	public static Byte getByteTenantId() {
		TenantProperties tenant = getTenant();
		return tenant != null ? getTenant().getId() : null;
	}

	private static TenantProperties getTenant() {
		TenantProperties tenant = tenantThreadLocal.get();
		if (tenantService.isSupportingTenants() && tenant == null) {
			throw new RuntimeException("No tenant found in InheritableThreadLocal variable");
		}
		return tenant;
	}

	public static void setTenant(int tenantId) {
		setTenant((byte) tenantId);
	}

	public static void setTenant(byte tenantId) {
		TenantProperties tenant = tenantService.getTenantById(tenantId);
		if (tenant == null) {
			throw new RuntimeException("Invalid tenant id");
		}
		tenantThreadLocal.set(tenant);
	}

	public static void setTenant(String tenantId) {
		if (tenantId == null) {
			tenantThreadLocal.set(null);
		} else {
			TenantProperties tenant = tenantService.getTenantById(Integer.valueOf(tenantId));
			if (tenant == null) {
				throw new RuntimeException("Invalid tenant id");
			}
			tenantThreadLocal.set(tenant);
		}
	}

	public static boolean isSupportingTenants() {
		return tenantService.isSupportingTenants();
	}

}
