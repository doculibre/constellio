package com.constellio.model.services.appManagement;

import com.constellio.model.entities.structures.TenantProperties;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TenantService {

	private static final String TENANTS_FILE_PATH = "/opt/constellio/conf/";
	private static final String TENANTS_FILENAME = "tenants.json";

	public static final String CANNOT_LOAD_TENANT_PROPERTIES = "Integrity exception. Cannot load tenant properties.";
	public static final String DUPLICATE_CODE_EXCEPTION = "Integrity exception. Two tenant cannot have the same code: %s.";
	public static final String DUPLICATE_ID_EXCEPTION = "Integrity exception. Two tenant cannot have the same id: %s.";
	public static final String DUPLICATE_HOSTNAME_EXCEPTION = "Integrity exception. Two tenant cannot have the same hostname: %s.";

	private String tenantFilePath;
	private List<TenantProperties> tenants;

	private Map<Byte, TenantProperties> tenantsById;
	private Map<String, TenantProperties> tenantsByCode;
	private Map<String, TenantProperties> tenantsByHostname;

	public TenantService() {
		this.tenantFilePath = TENANTS_FILE_PATH + TENANTS_FILENAME;
		tenants = readProperties();
		buildCache();
	}

	private List<TenantProperties> readProperties() {
		try (Reader reader = Files.newBufferedReader(Paths.get(tenantFilePath))) {
			Gson gson = new Gson();
			TenantProperties[] tenants = gson.fromJson(reader, TenantProperties[].class);
			return tenants == null ? new ArrayList<>() : Arrays.asList(tenants);
		} catch (NoSuchFileException ignored) {
			return new ArrayList<>();
		} catch (IOException e) {
			throw new RuntimeException(CANNOT_LOAD_TENANT_PROPERTIES);
		}
	}

	private void buildCache() {
		if (tenants == null || tenants.isEmpty()) {
			throw new RuntimeException(CANNOT_LOAD_TENANT_PROPERTIES);
		}

		tenantsById = new HashMap<>();
		tenantsByCode = new HashMap<>();
		tenantsByHostname = new HashMap<>();

		for (TenantProperties tenant : tenants) {
			if (tenantsById.containsKey(tenant.getId())) {
				throw new RuntimeException(String.format(DUPLICATE_ID_EXCEPTION, tenant.getId()));
			}
			tenantsById.put(tenant.getId(), tenant);

			if (tenantsByCode.containsKey(tenant.getCode())) {
				throw new RuntimeException(String.format(DUPLICATE_CODE_EXCEPTION, tenant.getCode()));
			}
			tenantsByCode.put(tenant.getCode(), tenant);

			for (String hostname : tenant.getHostnames()) {
				if (tenantsByHostname.containsKey(hostname)) {
					throw new RuntimeException(String.format(DUPLICATE_HOSTNAME_EXCEPTION, hostname));
				}
				tenantsByHostname.put(hostname, tenant);
			}
		}
	}

	public List<TenantProperties> getTenants() {
		return tenants;
	}

	public TenantProperties getTenantById(byte id) {
		return tenantsById.get(id);
	}

	public TenantProperties getTenantById(int id) {
		return tenantsById.get((byte) id);
	}

	public TenantProperties getTenantByCode(String code) {
		return tenantsByCode.get(code);
	}

	public TenantProperties getTenantByHostname(String hostname) {
		return tenantsByHostname.get(hostname);
	}


	//
	// TEST UTILS
	//

	public TenantService(File tempFolder) throws IOException {
		File propertiesFile = new File(tempFolder, TENANTS_FILENAME);
		this.tenantFilePath = propertiesFile.getPath();

		if (!propertiesFile.exists()) {
			propertiesFile.getParentFile().mkdirs();
			propertiesFile.createNewFile();
		}
	}

	public void addTenant(TenantProperties tenant) throws IOException {
		List<TenantProperties> tenants = new ArrayList<>(readProperties());
		tenants.add(tenant);
		writeProperties(tenants);
	}

	private void writeProperties(List<TenantProperties> tenants) throws IOException {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Writer writer = Files.newBufferedWriter(Paths.get(tenantFilePath));
		gson.toJson(tenants, writer);
		writer.close();
	}

	public void refreshTenants() {
		tenants = readProperties();
		buildCache();
	}
}