package com.constellio.app.ui.pages.globalGroup;

import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.entities.GlobalGroupVO;
import com.constellio.app.ui.framework.data.GlobalGroupVODataProvider;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.users.SolrUserCredentialsManager;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedFactories;
import org.junit.Before;
import org.mockito.Mock;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AddEditGlobalGroupPresenterTest extends ConstellioTest {

	public static final String HEROES_GLOBAL_GROUP = "Heroes Global Group";
	public static final String HEROES = "heroes";
	public static final String LEGENDS = "Legends";
	public static final String LEGENDS_GLOBAL_GROUP = "Legends global group";
	public static final String DAKOTA_INDIEN = "dakota.indien";

	@Mock AddEditGlobalGroupViewImpl globalGroupView;
	@Mock UserServices userServices;
	@Mock GlobalGroupVODataProvider dataProvider;
	@Mock GlobalGroupVO heroesGlobalGroupVO, legendsGlobalGroupVO;
	@Mock CoreViews navigator;
	@Mock SolrUserCredentialsManager userCredentialsManager;
	@Mock UserCredential dakotaCredential;
	@Mock GlobalGroup heroesGlobalGroup, legendsGlobalGroup;
	@Mock GlobalGroupVODataProvider globalGroupVODataProvider;

	AddEditGlobalGroupPresenter presenter;
	MockedFactories mockedFactories = new MockedFactories();

	@Before
	public void setUp()
			throws Exception {

		when(globalGroupView.getConstellioFactories()).thenReturn(mockedFactories.getConstellioFactories());
		when(globalGroupView.getSessionContext()).thenReturn(FakeSessionContext.dakotaInCollection(zeCollection));
		when(globalGroupView.navigateTo()).thenReturn(navigator);

		when(heroesGlobalGroup.getCode()).thenReturn(HEROES);
		when(heroesGlobalGroup.getName()).thenReturn(HEROES_GLOBAL_GROUP);
		when(legendsGlobalGroup.getCode()).thenReturn(LEGENDS);
		when(legendsGlobalGroup.getName()).thenReturn(LEGENDS_GLOBAL_GROUP);

		when(heroesGlobalGroupVO.getCode()).thenReturn(HEROES);
		when(heroesGlobalGroupVO.getName()).thenReturn(HEROES_GLOBAL_GROUP);
		when(legendsGlobalGroupVO.getCode()).thenReturn(LEGENDS);
		when(legendsGlobalGroupVO.getName()).thenReturn(LEGENDS_GLOBAL_GROUP);

		when(mockedFactories.getModelLayerFactory().newUserServices()).thenReturn(userServices);
		when(userServices.getGroup(HEROES)).thenReturn(heroesGlobalGroup);
		when(userServices.getGroup(LEGENDS)).thenReturn(legendsGlobalGroup);

		presenter = spy(new AddEditGlobalGroupPresenter(globalGroupView));

		givenBreadCrumbAndParameters();

		when(presenter.getCode()).thenReturn(HEROES);

	}

	private void givenBreadCrumbAndParameters() {
		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put("globalGroupCode", HEROES);
		presenter.setParamsMap(paramsMap);
		presenter.setBreadCrumb("url1/url2/url3");
	}

	//@Test
	public void givenCodeWhenGetGlobalGroupVOThenReturnVO()
			throws Exception {

		GlobalGroupVO globalGroupVO = presenter.getGlobalGroupVO(HEROES);

		assertThat(globalGroupVO.getCode()).isEqualTo(heroesGlobalGroup.getCode());
		assertThat(globalGroupVO.getName()).isEqualTo(heroesGlobalGroup.getName());
	}

	//@Test
	public void givenNullCodeWhenGetGlobalGroupVOThenNewGlobalGroupVO()
			throws Exception {

		GlobalGroupVO globalGroupVO = presenter.getGlobalGroupVO(null);

		assertThat(globalGroupVO).isNotNull();
		assertThat(globalGroupVO.getCode()).isNull();
		assertThat(globalGroupVO.getName()).isNull();
	}

	//@Test
	public void givenNoCodeWhenGetGlobalGroupVOThenNewGlobalGroupVO()
			throws Exception {

		GlobalGroupVO globalGroupVO = presenter.getGlobalGroupVO("");

		assertThat(globalGroupVO).isNotNull();
		assertThat(globalGroupVO.getCode()).isNull();
		assertThat(globalGroupVO.getName()).isNull();
	}

	//@Test
	public void givenActionEditWhenSaveButtonClickedThenSaveChanges()
			throws Exception {

		when(presenter.getCode()).thenReturn(HEROES);
		when(presenter.isEditMode()).thenReturn(true);
		doReturn(heroesGlobalGroup).when(presenter).toGlobalGroup(heroesGlobalGroupVO);

		presenter.saveButtonClicked(heroesGlobalGroupVO);

		verify(presenter).toGlobalGroup(heroesGlobalGroupVO);
		verify(userServices).addUpdateGlobalGroup(heroesGlobalGroup);
		verify(globalGroupView.navigateTo()).url("url3/url1/url2/" + URLEncoder.encode("globalGroupCode=heroes",
				"UTF-8"));
	}

	//@Test
	public void givenActionEditAndChangedCodeWhenSaveButtonClickedThenDoNothing()
			throws Exception {

		when(userServices.getUserCredential(DAKOTA_INDIEN)).thenReturn(dakotaCredential);
		when(presenter.isEditMode()).thenReturn(true);
		when(presenter.getCode()).thenReturn(LEGENDS);
		doReturn(heroesGlobalGroup).when(presenter).toGlobalGroup(heroesGlobalGroupVO);

		presenter.saveButtonClicked(heroesGlobalGroupVO);

		verify(presenter, never()).toGlobalGroup(heroesGlobalGroupVO);
		verify(userServices, never()).addUpdateGlobalGroup(heroesGlobalGroup);
		verify(globalGroupView, never()).navigateTo();
	}

	//@Test
	public void givenActionAddWhenSaveButtonClickedThenSaveChanges()
			throws Exception {

		doThrow(Exception.class).when(userServices).getGroup(HEROES);
		when(presenter.isEditMode()).thenReturn(false);
		doReturn(heroesGlobalGroup).when(presenter).toGlobalGroup(heroesGlobalGroupVO);

		presenter.saveButtonClicked(heroesGlobalGroupVO);

		verify(userServices).getGroup(HEROES);
		verify(presenter).toGlobalGroup(heroesGlobalGroupVO);
		verify(userServices).addUpdateGlobalGroup(heroesGlobalGroup);
		verify(globalGroupView.navigateTo()).url("url3/url1/url2/" + URLEncoder.encode("globalGroupCode=heroes",
				"UTF-8"));
	}

	//@Test
	public void whenCancelButtonClickedThenNavigateToBackPage()
			throws Exception {

		presenter.cancelButtonClicked();

		verify(globalGroupView.navigateTo()).url("url3/url1/url2/" + URLEncoder.encode("globalGroupCode=heroes",
				"UTF-8"));
	}
}
