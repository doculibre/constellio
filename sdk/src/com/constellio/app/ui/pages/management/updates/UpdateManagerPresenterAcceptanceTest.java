package com.constellio.app.ui.pages.management.updates;

import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.data.utils.TenantUtils;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.sdk.FakeEncryptionServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedFactories;
import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;

import static com.constellio.app.ui.i18n.i18n.$;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UpdateManagerPresenterAcceptanceTest extends ConstellioTest {

	UpdateManagerPresenter presenter;
	MockedFactories mockedFactories;

	@Mock UpdateManagerView userView;
	@Mock UserCredentialVO userCredentialVO;
	@Mock MockedNavigation navigator;

	@Before
	public void setUp() throws Exception {
		 mockedFactories = new MockedFactories();
		when(userView.getSessionContext()).thenReturn(FakeSessionContext.dakotaInCollection(zeCollection));
		when(userView.getConstellioFactories()).thenReturn(mockedFactories.getConstellioFactories());
		when(mockedFactories.getModelLayerFactory().newEncryptionServices()).thenReturn(new FakeEncryptionServices());

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

	@Test
	public void whenUploadingInvalidLicenseThenErrorMessageShown() throws IOException {
		ArgumentCaptor<String> errorMessageCaptor = ArgumentCaptor.forClass(String.class);
		when(presenter.getFoldersLocator().getUploadLicenseFile()).thenReturn(getTestResourceFile("bad-license.xml"));
		when(presenter.getFoldersLocator().getVerificationKey()).thenReturn(getTestResourceFile("verification-key.pub"));

		presenter.licenseUploadSucceeded();
		verify(userView).showErrorMessage(errorMessageCaptor.capture());
		assertThat(errorMessageCaptor.getValue()).isEqualTo($("UpdateManagerViewImpl.invalidLicense"));

		when(presenter.getFoldersLocator().getUploadLicenseFile()).thenReturn(getTestResourceFile("modified-good-license.xml"));

		presenter.licenseUploadSucceeded();
		verify(userView, times(2)).showErrorMessage(errorMessageCaptor.capture());
		assertThat(errorMessageCaptor.getValue()).isEqualTo($("UpdateManagerViewImpl.invalidLicense"));
	}

	@Test
	public void whenUploadingInvalidLicenseThenSuccessMessageShown() throws IOException {
		ArgumentCaptor<String> successMessageCaptor = ArgumentCaptor.forClass(String.class);

		File goodLicense = getTestResourceFile("good-license.xml");
		when(presenter.getFoldersLocator().getUploadLicenseFile()).thenReturn(goodLicense);
		when(presenter.getFoldersLocator().getVerificationKey()).thenReturn(getTestResourceFile("verification-key.pub"));
		doNothing().when(presenter).storeLicense(any(File.class));
		doNothing().when(presenter).refreshView();

		presenter.licenseUploadSucceeded();
		verify(userView).showMessage(successMessageCaptor.capture());
		assertThat(successMessageCaptor.getValue()).isEqualTo($("UpdateManagerViewImpl.licenseUpdated"));
	}
}
