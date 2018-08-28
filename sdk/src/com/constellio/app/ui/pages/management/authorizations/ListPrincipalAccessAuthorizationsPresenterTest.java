package com.constellio.app.ui.pages.management.authorizations;

import com.constellio.app.ui.entities.AuthorizationVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.AuthorizationToVOBuilder;
import com.constellio.app.ui.pages.base.PresenterService;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.global.AuthorizationDeleteRequest;
import com.constellio.model.entities.security.global.AuthorizationDetails;
import com.constellio.model.entities.security.global.AuthorizationModificationRequest;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedFactories;
import com.constellio.sdk.tests.MockedNavigation;
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

public class ListPrincipalAccessAuthorizationsPresenterTest extends ConstellioTest {
	public static final String ZE_PRINCIPAL = "zePrincipal";
	public static final String ZENOTHER_PRINCIPAL = "zenotherPrincipal";

	@Mock AuthorizationsServices authorizationsServices;
	@Mock PresenterService presenterService;
	@Mock ListPrincipalAccessAuthorizationsView view;
	MockedNavigation navigator;
	@Mock User user;
	@Mock RecordVO principal;
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
		navigator = new MockedNavigation();

		when(view.getConstellioFactories()).thenReturn(factories.getConstellioFactories());
		SessionContext context = FakeSessionContext.gandalfInCollection(zeCollection);
		when(view.getSessionContext()).thenReturn(context);
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.navigate()).thenReturn(navigator);

		when(factories.getAppLayerFactory().newPresenterService()).thenReturn(presenterService);
		when(presenterService.getRecordVO(ZE_PRINCIPAL, VIEW_MODE.DISPLAY, context)).thenReturn(principal);
		when(presenterService.getCurrentUser(isA(SessionContext.class))).thenReturn(user);

		when(factories.getModelLayerFactory().newAuthorizationsServices()).thenReturn(authorizationsServices);

		presenter = spy(new ListPrincipalAccessAuthorizationsPresenter(view).forRequestParams(ZE_PRINCIPAL));
	}

	@Test
	public void givenIdThenReturnTheCorrectPrincipal() {
		assertThat(presenter.getRecordVO()).isEqualTo(principal);
	}

	@Test
	public void givenBackButtonPressedWhenPrincipalIsGroupThenNavigateToGroup() {
		presenter.backButtonClicked(Group.DEFAULT_SCHEMA);
		verify(navigator.to(), times(1)).displayCollectionGroup(ZE_PRINCIPAL);
	}

	@Test
	public void givenBackButtonPressedWhenPrincipalIsUserThenNavigateToUser() {
		presenter.backButtonClicked(User.DEFAULT_SCHEMA);
		verify(navigator.to(), times(1)).displayCollectionUser(ZE_PRINCIPAL);
	}

	@Test
	public void givenAuthorizationDeletedWhenSinglePrincipalThenRemoveTheAuthorizationAndRefreshTheView() {
		ArgumentCaptor<AuthorizationDeleteRequest> requestArgumentCaptor = forClass(AuthorizationDeleteRequest.class);
		givenAuthorizationWithId(aString(), false);
		presenter.deleteButtonClicked(authorizationVO);
		verify(authorizationsServices, times(1)).execute(requestArgumentCaptor.capture());
		verify(view, times(1)).removeAuthorization(authorizationVO);

		assertThat(requestArgumentCaptor.getValue().getAuthId()).isEqualTo("zeAuth");
		assertThat(requestArgumentCaptor.getValue().getExecutedBy()).isEqualTo(user);
	}

	@Test
	public void givenAuthorizationDeletedWhenMultiPrincipalThenRemoveThePrincipalAndRefreshTheView() {
		ArgumentCaptor<AuthorizationModificationRequest> captor = forClass(AuthorizationModificationRequest.class);
		givenAuthorizationWithId(aString(), true);
		presenter.deleteButtonClicked(authorizationVO);
		verify(authorizationsServices, times(1)).execute(captor.capture());
		verify(view, times(1)).removeAuthorization(authorizationVO);

		assertThat(captor.getValue().getAuthorizationId()).isEqualTo("zeAuth");
		assertThat(captor.getValue().getNewPrincipalIds()).containsOnly(ZENOTHER_PRINCIPAL);
	}

	private void givenPrincipalWithTwoInheritedAndTwoOwnAuthorizations() {
		Authorization authorization1 = mock(Authorization.class, "Authorization1");
		Authorization authorization2 = mock(Authorization.class, "Authorization2");
		Authorization authorization3 = mock(Authorization.class, "Authorization3");
		Authorization authorization4 = mock(Authorization.class, "Authorization4");

		when(authorization1.getGrantedToPrincipals()).thenReturn(Arrays.asList(ZE_PRINCIPAL));
		when(authorization2.getGrantedToPrincipals()).thenReturn(Arrays.asList(ZENOTHER_PRINCIPAL));
		when(authorization3.getGrantedToPrincipals()).thenReturn(Arrays.asList(ZE_PRINCIPAL));
		when(authorization4.getGrantedToPrincipals()).thenReturn(Arrays.asList(ZENOTHER_PRINCIPAL));

		Record record = mock(Record.class, "Record");
		when(presenterService.getRecord(ZE_PRINCIPAL)).thenReturn(record);

		when(authorizationsServices.getRecordAuthorizations(record)).thenReturn(
				Arrays.asList(authorization1, authorization2, authorization3, authorization4));

		AuthorizationToVOBuilder builder = mock(AuthorizationToVOBuilder.class, "AuthorizationToVOBuilder");
		when(builder.build(authorization1)).thenReturn(own1);
		when(builder.build(authorization2)).thenReturn(inherited1);
		when(builder.build(authorization3)).thenReturn(own2);
		when(builder.build(authorization4)).thenReturn(inherited2);

		doReturn(builder).when(presenter).newAuthorizationToVOBuilder();
	}

	private void givenAuthorizationWithId(String authId, boolean multiPrincipal) {
		when(authorizationVO.getAuthId()).thenReturn(authId);
		when(authorizationsServices.getAuthorization(zeCollection, authId)).thenReturn(authorization);
		when(authorization.getGrantedToPrincipals()).thenReturn(multiPrincipal ?
																new ArrayList<>(Arrays.asList(ZE_PRINCIPAL, ZENOTHER_PRINCIPAL)) :
																Arrays.asList(ZE_PRINCIPAL));
		when(authorization.getDetail()).thenReturn(details);
	}
}
