package com.constellio.data.utils;

import com.constellio.data.services.tenant.TenantProperties;
import com.constellio.data.services.tenant.TenantService;
import org.apache.logging.log4j.ThreadContext;

public class TenantUtils {

	private static InheritableThreadLocal<TenantProperties> tenantThreadLocal = new InheritableThreadLocal<>();

	private static TenantService tenantService = TenantService.getInstance();

	public static final String EMPTY_TENANT_ID = "-1";

	public static String getTenantId() {
		TenantProperties tenant = getTenant();
		return tenant != null ? "" + getTenant().getId() : null;
	}

	public static String getTenantCode() {
		TenantProperties tenant = getTenant();
		return tenant != null ? "" + getTenant().getCode() : null;
	}

	public static byte getByteTenantId() {
		return getTenant().getId();
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
		ThreadContext.put("tenant.id", tenant.getCode());
		tenantThreadLocal.set(tenant);
	}

	public static void setTenant(String tenantId) {
		if (tenantId == null) {
			ThreadContext.remove("tenant.id");
			tenantThreadLocal.set(null);
		} else {
			TenantProperties tenant = tenantService.getTenantById(Integer.valueOf(tenantId));
			ThreadContext.put("tenant.id", tenant.getCode());

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
