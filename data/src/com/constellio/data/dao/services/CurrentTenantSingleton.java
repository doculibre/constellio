package com.constellio.data.dao.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CurrentTenantSingleton {

	public static class Tenant {
		byte id;

		int index;

		String name;

		public Tenant(int index, byte id, String name) {
			this.index = index;
			this.id = id;
			this.name = name;
		}

		public int getIndex() {
			return index;
		}

		public byte getId() {
			return id;
		}

		public String getName() {
			return name;
		}
	}


	static ThreadLocal<Tenant> currentTenantThreadLocal = new ThreadLocal<>();

	static Map<String, Tenant> allTenantsByName = null;
	static Map<Byte, Tenant> allTenantsById = null;

	public static void initializeTenants(List<String> tenants) {

		allTenantsByName = new HashMap<>();
		allTenantsById = new HashMap<>();
		for (int i = 0; i < tenants.size(); i++) {
			byte id = (byte) (i + Byte.MIN_VALUE);
			String name = tenants.get(i);
			Tenant tenant = new Tenant(i, id, name);
			allTenantsByName.put(name, tenant);
			allTenantsById.put(id, tenant);
		}

	}

	public static void setCurrentTenant(String tenantName) {

		if (allTenantsByName == null) {
			throw new IllegalStateException("Tenant map has not been initialized");
		}

		if (tenantName == null) {
			throw new IllegalArgumentException("Tenant argument is null");
		}

		Tenant tenant = allTenantsByName.get(tenantName);
		if (tenant == null) {
			throw new IllegalArgumentException("No such tenant with name " + tenantName);
		}
		currentTenantThreadLocal.set(tenant);
	}

	public static void removeCurrentTenant() {
		currentTenantThreadLocal.set(null);
	}

	public static boolean hasCurrentTenant() {
		return true;//currentTenantThreadLocal.get() != null;
	}

	public static Tenant getCurrentTenant() {
		Tenant tenant = currentTenantThreadLocal.get();
		if (tenant == null) {
			return new Tenant(0, Byte.MIN_VALUE, "test");
			//throw new IllegalStateException("No current tenant");
		}
		return tenant;
	}

	public static boolean isInitialized() {
		return allTenantsByName != null;
	}

	public static void clearTenantNames() {
		allTenantsByName = null;
		allTenantsById = null;
		currentTenantThreadLocal = new ThreadLocal<>();
	}
}
