package com.constellio.app.ui.pages.management.authorizations;

import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.AuthorizationVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.AuthorizationToVOBuilder;
import com.constellio.app.ui.pages.base.PresenterService;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.global.AuthorizationDeleteRequest;
import com.constellio.model.entities.security.global.AuthorizationDetails;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedFactories;
import com.constellio.sdk.tests.SDKViewNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ListContentAccessAuthorizationsPresenterTest extends ConstellioTest {
	public static final String ZE_SECURED_OBJECT = "zeObject";
	public static final String ZENOTHER_SECURED_OBJECT = "zenotherObject";
	public static final String ZE_PRINCIPAL = "zePrincipal";

	@Mock AuthorizationsServices authorizationsServices;
	@Mock PresenterService presenterService;
	@Mock ListContentAccessAuthorizationsView view;
	SDKViewNavigation sdkViewNavigation;
	@Mock User user;
	@Mock RecordVO object;
	@Mock AuthorizationVO authorizationVO;
	@Mock AuthorizationVO inherited1;
	@Mock AuthorizationVO inherited2;
	@Mock AuthorizationVO own1;
	@Mock AuthorizationVO own2;
	@Mock Authorization authorization;
	@Mock
	AuthorizationDetails details;
	MockedFactories factories = new MockedFactories();

	ListAuthorizationsPresenter presenter;

	@Before
	public void setUp()
			throws Exception {
		when(authorizationVO.getAuthId()).thenReturn("zeAuth");
		when(details.getId()).thenReturn("zeAuth");
		when(view.getConstellioFactories()).thenReturn(factories.getConstellioFactories());
		SessionContext context = FakeSessionContext.gandalfInCollection(zeCollection);
		when(view.getSessionContext()).thenReturn(context);
		when(view.getCollection()).thenReturn(zeCollection);
		sdkViewNavigation = new SDKViewNavigation(view);

		when(factories.getAppLayerFactory().newPresenterService()).thenReturn(presenterService);
		when(presenterService.getRecordVO(ZE_SECURED_OBJECT, VIEW_MODE.DISPLAY, context)).thenReturn(object);
		when(presenterService.getCurrentUser(isA(SessionContext.class))).thenReturn(user);

		when(factories.getModelLayerFactory().newAuthorizationsServices()).thenReturn(authorizationsServices);

		presenter = spy(new ListContentAccessAuthorizationsPresenter(view).forRequestParams(ZE_SECURED_OBJECT));
	}

	@Test
	public void givenIdThenReturnTheCorrectObject() {
		assertThat(presenter.getRecordVO()).isEqualTo(object);
	}

	@Test
	public void givenBackButtonPressedWhenObjectIsFolderWithDefaultSchemaThenNavigateToFolder() {
		presenter.backButtonClicked(Folder.DEFAULT_SCHEMA);
	}

	@Test
	public void givenBackButtonPressedWhenObjectIsFolderWithCustomSchemaThenNavigateToFolder() {
		presenter.backButtonClicked(Folder.SCHEMA_TYPE + "_custom");
	}

	@Test
	public void givenBackButtonPressedWhenObjectIsTaxonomyConceptThenNavigateToTaxonomyManagement() {
		TaxonomiesManager manager = mock(TaxonomiesManager.class, "TaxonomiesManager");
		when(factories.getModelLayerFactory().getTaxonomiesManager()).thenReturn(manager);
		Taxonomy taxonomy = mock(Taxonomy.class, "Taxonomy");
		when(manager.getPrincipalTaxonomy(zeCollection)).thenReturn(taxonomy);
		when(taxonomy.getCode()).thenReturn("taxo");

		presenter.backButtonClicked(AdministrativeUnit.DEFAULT_SCHEMA);
		verify(sdkViewNavigation.coreViews, times(1)).taxonomyManagement("taxo", ZE_SECURED_OBJECT);
	}

	@Test
	public void givenAuthorizationDeletedThenRemoveTheAuthorizationAndRefreshTheView() {
		ArgumentCaptor<AuthorizationDeleteRequest> requestArgumentCaptor = forClass(AuthorizationDeleteRequest.class);
		givenAuthorizationWithId(aString());
		presenter.deleteButtonClicked(authorizationVO);
		verify(authorizationsServices, times(1)).execute(requestArgumentCaptor.capture());
		verify(view, times(1)).removeAuthorization(authorizationVO);

		assertThat(requestArgumentCaptor.getValue().getAuthId()).isEqualTo("zeAuth");
		assertThat(requestArgumentCaptor.getValue().getExecutedBy()).isEqualTo(user);
	}

	private void givenObjectWithTwoInheritedAndTwoOwnAuthorizations() {
		Authorization authorization1 = mock(Authorization.class, "Authorization1");
		Authorization authorization2 = mock(Authorization.class, "Authorization2");
		Authorization authorization3 = mock(Authorization.class, "Authorization3");
		Authorization authorization4 = mock(Authorization.class, "Authorization4");
		Authorization authorization5 = mock(Authorization.class, "Authorization5");
		Authorization authorization6 = mock(Authorization.class, "Authorization6");

		when(authorization1.getGrantedOnRecord()).thenReturn(ZE_SECURED_OBJECT);
		when(authorization1.getGrantedToPrincipals()).thenReturn(Arrays.asList(ZE_PRINCIPAL));

		when(authorization2.getGrantedOnRecord()).thenReturn(ZENOTHER_SECURED_OBJECT);
		when(authorization2.getGrantedToPrincipals()).thenReturn(Arrays.asList(ZE_PRINCIPAL));

		when(authorization3.getGrantedOnRecord()).thenReturn(ZE_SECURED_OBJECT);
		when(authorization3.getGrantedToPrincipals()).thenReturn(Arrays.asList(ZE_PRINCIPAL));

		when(authorization4.getGrantedOnRecord()).thenReturn(ZENOTHER_SECURED_OBJECT);
		when(authorization4.getGrantedToPrincipals()).thenReturn(Arrays.asList(ZE_PRINCIPAL));

		when(authorization5.getGrantedOnRecord()).thenReturn(ZE_SECURED_OBJECT);
		when(authorization5.getGrantedToPrincipals()).thenReturn(new ArrayList<String>());

		when(authorization6.getGrantedOnRecord()).thenReturn(ZENOTHER_SECURED_OBJECT);
		when(authorization6.getGrantedToPrincipals()).thenReturn(new ArrayList<String>());

		Record record = mock(Record.class, "Record");
		when(presenterService.getRecord(ZE_SECURED_OBJECT)).thenReturn(record);

		when(authorizationsServices.getRecordAuthorizations(record)).thenReturn(
				Arrays.asList(authorization1, authorization2, authorization3, authorization4, authorization5, authorization6));

		AuthorizationToVOBuilder builder = mock(AuthorizationToVOBuilder.class, "AuthorizationToVOBuilder");
		when(builder.build(authorization1)).thenReturn(own1);
		when(builder.build(authorization2)).thenReturn(inherited1);
		when(builder.build(authorization3)).thenReturn(own2);
		when(builder.build(authorization4)).thenReturn(inherited2);

		doReturn(builder).when(presenter).newAuthorizationToVOBuilder();
	}

	private void givenAuthorizationWithId(String authId) {
		when(authorizationVO.getAuthId()).thenReturn(authId);
		when(authorizationsServices.getAuthorization(zeCollection, authId)).thenReturn(authorization);
		when(authorization.getDetail()).thenReturn(details);
	}
}
