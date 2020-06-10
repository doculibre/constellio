package com.constellio.model.services.tenant;

import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.constellio.model.services.tenant.TenantService.CANNOT_LOAD_TENANT_PROPERTIES;
import static com.constellio.model.services.tenant.TenantService.DUPLICATE_CODE_EXCEPTION;
import static com.constellio.model.services.tenant.TenantService.DUPLICATE_HOSTNAME_EXCEPTION;
import static com.constellio.model.services.tenant.TenantService.DUPLICATE_ID_EXCEPTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class TenantServiceAcceptanceTest extends ConstellioTest {

	private TenantService tenantService;
	private TenantProperties tenant200, tenant1, tenant2;

	@Before
	public void setUp() throws Exception {
		tenantService = new TenantService(newTempFolder());
		tenant200 = new TenantProperties("Équipe, Constellio test!", "T200", 200, Arrays.asList("tenant200.cloud.constellio.com"));
		tenant1 = new TenantProperties("tenant 1", "T01", 1, Arrays.asList("tenant1.cloud.constellio.com:7070"));
		tenant2 = new TenantProperties("tenant 2", "T02", 2, Arrays.asList("tenant2.cloud.constellio.com"));
	}

	@Test
	public void givenTenantsThenOk()
			throws Exception {
		tenantService.addTenant(tenant200);
		tenantService.addTenant(tenant1);
		tenantService.addTenant(tenant2);
		tenantService.refreshTenants();

		List<TenantProperties> tenants = tenantService.getTenants();
		assertThat(tenants).containsExactly(tenant200, tenant1, tenant2);

		TenantProperties tenantById = tenantService.getTenantById(200);
		assertThat(tenantById.getName()).isEqualTo(tenant200.getName());
		assertThat(tenantById.getCode()).isEqualTo(tenant200.getCode());
		assertThat(tenantById.getId()).isEqualTo(tenant200.getId());
		assertThat(tenantById.getHostnames()).containsExactlyElementsOf(tenant200.getHostnames());

		TenantProperties tenantByCode = tenantService.getTenantByCode("T01");
		assertThat(tenantByCode.getName()).isEqualTo(tenant1.getName());
		assertThat(tenantByCode.getCode()).isEqualTo(tenant1.getCode());
		assertThat(tenantByCode.getId()).isEqualTo(tenant1.getId());
		assertThat(tenantByCode.getHostnames()).containsExactlyElementsOf(tenant1.getHostnames());

		TenantProperties tenantByHostname = tenantService.getTenantByHostname("tenant2.cloud.constellio.com");
		assertThat(tenantByHostname.getName()).isEqualTo(tenant2.getName());
		assertThat(tenantByHostname.getCode()).isEqualTo(tenant2.getCode());
		assertThat(tenantByHostname.getId()).isEqualTo(tenant2.getId());
		assertThat(tenantByHostname.getHostnames()).containsExactlyElementsOf(tenant2.getHostnames());
	}

	@Test
	public void givenEmptyTenantsThenException()
			throws Exception {
		try {
			tenantService.refreshTenants();
			fail("Starting without any tenant should throws an exception.");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo(CANNOT_LOAD_TENANT_PROPERTIES);
		}
	}

	@Test
	public void givenDuplicatedIdThenException()
			throws Exception {
		tenantService.addTenant(tenant1);
		tenant2.setId(tenant1.getId());
		tenantService.addTenant(tenant2);

		try {
			tenantService.refreshTenants();
			fail("Adding a duplicated id should throws an exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo(String.format(DUPLICATE_ID_EXCEPTION, tenant2.getId()));
		}
	}

	@Test
	public void givenDuplicatedCodeThenException()
			throws Exception {
		tenantService.addTenant(tenant1);
		tenant2.setCode(tenant1.getCode());
		tenantService.addTenant(tenant2);

		try {
			tenantService.refreshTenants();
			fail("Adding a duplicated code should throws an exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo(String.format(DUPLICATE_CODE_EXCEPTION, tenant2.getCode()));
		}
	}

	@Test
	public void givenDuplicatedHostnameThenException()
			throws Exception {
		tenantService.addTenant(tenant1);
		tenant2.setHostnames(tenant1.getHostnames());
		tenantService.addTenant(tenant2);

		try {
			tenantService.refreshTenants();
			fail("Adding a duplicated hostname should throws an exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo(
					String.format(DUPLICATE_HOSTNAME_EXCEPTION, tenant2.getHostnames().get(0)));
		}
	}

	@Test
	public void givenTooMuchTenantThenException()
			throws Exception {

		for (int i = 0; i <= 256; i++) {
			tenantService.addTenant(new TenantProperties("Tenant " + i, "T" + i, i,
					Arrays.asList("tenant" + i + ".cloud.constellio.com")));
		}

		try {
			tenantService.refreshTenants();
			fail("Adding more than 256 tenants should throws an exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo(String.format(DUPLICATE_ID_EXCEPTION, (byte) 256));
		}
	}
}
