package com.constellio.app.ui.pages.management.updates;

import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.utils.TenantUtils;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedFactories;
import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class UpdateManagerPresenterAcceptanceTest extends ConstellioTest {

	UpdateManagerPresenter presenter;
	MockedFactories mockedFactories = new MockedFactories();

	@Mock UpdateManagerView userView;
	@Mock UserCredentialVO userCredentialVO;
	@Mock MockedNavigation navigator;

	@Before
	public void setUp() throws Exception {
		when(userView.getSessionContext()).thenReturn(FakeSessionContext.dakotaInCollection(zeCollection));
		when(userView.getConstellioFactories()).thenReturn(mockedFactories.getConstellioFactories());

		when(userView.navigate()).thenReturn(navigator);

		when(userCredentialVO.getUsername()).thenReturn("dakota");

		presenter = spy(new UpdateManagerPresenter(userView));
	}

	@Test
	public void whenSupportingMultiTenantWithAdminFeature() {
		givenTwoTenants();
		TenantUtils.setTenant("1");

		Toggle.ENABLE_CLOUD_SYSADMIN_FEATURES.enable();

		assertThat(presenter.hasUpdatePermission()).isTrue();
	}

	@Test
	public void whenSupportingMultiTenantWithoutAdminFeature() {
		givenTwoTenants();
		TenantUtils.setTenant("1");

		assertThat(presenter.hasUpdatePermission()).isFalse();
	}

	@Test
	public void whenNotSupportingMultiTenantWithAdminFeature() {
		Toggle.ENABLE_CLOUD_SYSADMIN_FEATURES.enable();

		assertThat(presenter.hasUpdatePermission()).isTrue();
	}

	@Test
	public void whenNotSupportingMultiTenantWithoutAdminFeature() {
		assertThat(presenter.hasUpdatePermission()).isTrue();
	}
}
