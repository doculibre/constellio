package com.constellio.app.ui.pages.collection;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.mockito.Mock;

import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.GlobalGroupVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.app.ui.framework.builders.GlobalGroupToVOBuilder;
import com.constellio.app.ui.framework.data.GlobalGroupVODataProvider;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedFactories;

public class ListCollectionUserPresenterTest extends ConstellioTest {

	ListCollectionUserPresenter presenter;
	@Mock ListCollectionUserView view;
	@Mock UserCredentialVO dakotaCredentialVO;
	@Mock RecordVO dakotaRecordVO;
	@Mock UserCredential dakotaCredential;
	@Mock CoreViews navigator;
	@Mock UserServices userServices;
	@Mock GlobalGroupToVOBuilder globalGroupToVOBuilder;
	@Mock GlobalGroupVODataProvider globalGroupVODataProvider;
	@Mock GlobalGroupVO heroesVO;
	@Mock GlobalGroup heroes;
	@Mock GlobalGroup heroesInZeCollection;

	MockedFactories mockedFactories = new MockedFactories();

	@Before
	public void setUp()
			throws Exception {

		List<String> collections = new ArrayList<>();
		collections.add(zeCollection);

		List<String> emptyCollections = new ArrayList<>();

		when(view.getConstellioFactories()).thenReturn(mockedFactories.getConstellioFactories());
		when(view.getSessionContext()).thenReturn(FakeSessionContext.dakotaInCollection(zeCollection));
		when(view.navigateTo()).thenReturn(navigator);

		when(mockedFactories.getModelLayerFactory().newUserServices()).thenReturn(userServices);
		when(userServices.getUserCredential("dakota")).thenReturn(dakotaCredential);
		when(dakotaCredentialVO.getUsername()).thenReturn("dakota");
		when(dakotaRecordVO.getId()).thenReturn("dakotaId");
		when(heroesVO.getCode()).thenReturn("heroes");
		when(userServices.getGroup("heroes")).thenReturn(heroes);
		when(heroes.withUsersAutomaticallyAddedToCollections(collections)).thenReturn(heroesInZeCollection);
		when(heroesInZeCollection.withUsersAutomaticallyAddedToCollections(emptyCollections)).thenReturn(heroes);
		when(view.getCollection()).thenReturn(zeCollection);

		presenter = spy(new ListCollectionUserPresenter(view));

		doReturn("bobId").when(presenter).getCurrentUserId();

	}

	//TODO Broken @Test
	public void whenAddButtonClikedThenAddUserToCollection()
			throws Exception {

		//		presenter.addButtonClicked(dakotaCredentialVO);

		verify(userServices).getUserCredential("dakota");
		verify(userServices).addUserToCollection(dakotaCredential, view.getCollection());
	}

	//TODO Broken @Test
	public void whenDisplayButtonClickedThenNavigatoToDisplaySchema()
			throws Exception {

		presenter.displayButtonClicked(dakotaRecordVO);

		verify(view.navigateTo()).displaySchemaRecord("dakotaId");
	}

	//TODO Broken @Test
	public void whenGetGlobalGroupDataProviderThenReturnIt()
			throws Exception {

		doReturn(globalGroupToVOBuilder).when(presenter).newGlobalGroupVOBuilder();
		doReturn(globalGroupVODataProvider).when(presenter).newGlobalGroupVODataProvider(globalGroupToVOBuilder);

		presenter.getGlobalGroupVODataProvider();

		verify(presenter.newGlobalGroupVODataProvider(globalGroupToVOBuilder));
	}

	//TODO Broken @Test
	public void whenAddGlobalGroupButtonClickedThenAddGroupToCollection()
			throws Exception {

		//		presenter.addGlobalGroupButtonClicked(heroesVO);

		verify(userServices).getGroup("heroes");
		verify(userServices).addUpdateGlobalGroup(heroesInZeCollection);
	}

	//TODO Broken @Test
	public void whenDeleteGlobalGroupButtonClickedThenAddGroupToCollection()
			throws Exception {

		when(userServices.getGroup("heroes")).thenReturn(heroesInZeCollection);

		presenter.deleteGlobalGroupButtonClicked(heroesVO);

		verify(userServices).getGroup("heroes");
		verify(userServices).addUpdateGlobalGroup(heroes);
	}

	//TODO Broken @Test
	public void whenDisplayGlobalGroupButtonClickedThenAddGroupToCollection()
			throws Exception {

		presenter.displayGlobalGroupButtonClicked(heroesVO);

		Map<String, Object> globalGroupCodeMap = new HashMap();
		globalGroupCodeMap.put("globalGroupCode", "heroes");
		String params = ParamUtils.addParams(NavigatorConfigurationService.COLLECTION_USER_LIST, globalGroupCodeMap);
		verify(view.navigateTo()).displayGlobalGroup(params);
	}
}
