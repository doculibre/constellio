package com.constellio.app.ui.framework.data;

import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.app.ui.framework.builders.UserCredentialToVOBuilder;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.MockedFactories;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class UserCredentialVODataProviderTest extends ConstellioTest {

	public static final String EDOUARD = "edouard";
	public static final String DAKOTA = "dakota";
	public static final String GANDALF = "gandalf";
	public static final String CHUCK = "chuck";
	public static final String BOB = "bob";
	public static final String HEROES = "Heroes";
	public static final String EMAIL = "@email.com";
	MockedFactories mockedFactories = new MockedFactories();
	UserCredentialVODataProvider dataProvider;
	@Mock UserServices userServices;
	@Mock UserCredentialToVOBuilder voBuilder;
	@Mock SystemWideUserInfos edouardUserCredential;
	@Mock SystemWideUserInfos dakotaUserCredential;
	@Mock SystemWideUserInfos gandalfUserCredential;
	@Mock SystemWideUserInfos chuckUserCredential;
	@Mock SystemWideUserInfos bobUserCredential;
	@Mock UserCredentialVO edouardUserCredentialVO;
	@Mock UserCredentialVO dakotaUserCredentialVO;
	@Mock UserCredentialVO gandalfUserCredentialVO;
	@Mock UserCredentialVO chuckUserCredentialVO;
	@Mock UserCredentialVO bobUserCredentialVO;
	@Mock ModelLayerFactory modelLayerFactory;

	List<SystemWideUserInfos> userCredentials;

	@Before
	public void setUp()
			throws Exception {

		when(edouardUserCredential.getUsername()).thenReturn(EDOUARD);
		when(dakotaUserCredential.getUsername()).thenReturn(DAKOTA);
		when(gandalfUserCredential.getUsername()).thenReturn(GANDALF);
		when(chuckUserCredential.getUsername()).thenReturn(CHUCK);
		when(bobUserCredential.getUsername()).thenReturn(BOB);

		when(edouardUserCredentialVO.getUsername()).thenReturn(EDOUARD);
		when(dakotaUserCredentialVO.getUsername()).thenReturn(DAKOTA);
		when(gandalfUserCredentialVO.getUsername()).thenReturn(GANDALF);
		when(chuckUserCredentialVO.getUsername()).thenReturn(CHUCK);
		when(bobUserCredentialVO.getUsername()).thenReturn(BOB);

		when(edouardUserCredentialVO.getEmail()).thenReturn(EDOUARD + EMAIL);
		when(dakotaUserCredentialVO.getEmail()).thenReturn(DAKOTA + EMAIL);
		when(gandalfUserCredentialVO.getEmail()).thenReturn(GANDALF + EMAIL);
		when(chuckUserCredentialVO.getEmail()).thenReturn(CHUCK + EMAIL);
		when(bobUserCredentialVO.getEmail()).thenReturn(BOB + EMAIL);

		when(edouardUserCredentialVO.getFirstName()).thenReturn(EDOUARD);
		when(dakotaUserCredentialVO.getFirstName()).thenReturn(DAKOTA);
		when(gandalfUserCredentialVO.getFirstName()).thenReturn(GANDALF);
		when(chuckUserCredentialVO.getFirstName()).thenReturn(CHUCK);
		when(bobUserCredentialVO.getFirstName()).thenReturn(BOB);

		when(edouardUserCredentialVO.getLastName()).thenReturn(EDOUARD);
		when(dakotaUserCredentialVO.getLastName()).thenReturn(DAKOTA);
		when(gandalfUserCredentialVO.getLastName()).thenReturn(GANDALF);
		when(chuckUserCredentialVO.getLastName()).thenReturn(CHUCK);
		when(bobUserCredentialVO.getLastName()).thenReturn(BOB);

		userCredentials = new ArrayList<>();
		userCredentials.add(edouardUserCredential);
		userCredentials.add(dakotaUserCredential);
		userCredentials.add(gandalfUserCredential);
		userCredentials.add(chuckUserCredential);
		userCredentials.add(bobUserCredential);

		when(mockedFactories.getModelLayerFactory().newUserServices()).thenReturn(userServices);
		when(userServices.getUserInfos(EDOUARD)).thenReturn(edouardUserCredential);
		when(userServices.getGlobalGroupActifUsers(HEROES)).thenReturn(
				Arrays.asList(edouardUserCredential, dakotaUserCredential, gandalfUserCredential, chuckUserCredential,
						bobUserCredential));

		when(voBuilder.build(edouardUserCredential)).thenReturn(edouardUserCredentialVO);
		when(voBuilder.build(dakotaUserCredential)).thenReturn(dakotaUserCredentialVO);
		when(voBuilder.build(gandalfUserCredential)).thenReturn(gandalfUserCredentialVO);
		when(voBuilder.build(chuckUserCredential)).thenReturn(chuckUserCredentialVO);
		when(voBuilder.build(bobUserCredential)).thenReturn(bobUserCredentialVO);

		dataProvider = spy(new UserCredentialVODataProvider(voBuilder, mockedFactories.getModelLayerFactory(), HEROES));
	}

	@Test
	public void whenGetUserCredentialByUsernameThenOk()
			throws Exception {

		UserCredentialVO userCredentialVO = dataProvider.getUserCredentialVO(EDOUARD);

		assertThat(userCredentialVO).isEqualTo(edouardUserCredentialVO);
	}

	@Test
	public void whenGetUserCredentialByIndexThenOk()
			throws Exception {

		UserCredentialVO userCredentialVO = dataProvider.getUserCredentialVO(3);

		assertThat(userCredentialVO).isEqualTo(edouardUserCredentialVO);
	}

	@Test
	public void whenFilterThenOk()
			throws Exception {

		dataProvider.setFilter(DAKOTA);

		List<UserCredentialVO> userCredentialVOs = dataProvider.listUserCredentialVOs();

		assertThat(userCredentialVOs).hasSize(1);
	}

	@Test
	public void whenSizeThenOk()
			throws Exception {
		assertThat(dataProvider.size()).isEqualTo(5);
	}

	@Test
	public void whenListIndexesThenOk()
			throws Exception {
		assertThat(dataProvider.list()).containsOnly(0, 1, 2, 3, 4);
	}

	@Test
	public void whenSetFilterThenOk()
			throws Exception {

		dataProvider.setFilter("dakota");

		assertThat(dataProvider.listUserCredentialVOs()).hasSize(1);
		assertThat(dataProvider.size()).isEqualTo(1);
	}

	@Test
	public void givenDeletedUserWhenListUserCredentialsWithStatusDeletedThenOk()
			throws Exception {

		when(edouardUserCredentialVO.getStatus()).thenReturn(UserCredentialStatus.DISABLED);

		assertThat(dataProvider.listUserCredentialVOsWithStatus(UserCredentialStatus.DISABLED)).hasSize(1);
		assertThat(dataProvider.listUserCredentialVOsWithStatus(UserCredentialStatus.DISABLED).get(0).getUsername())
				.isEqualTo(EDOUARD);
	}

	@Test
	public void whenSubListThenOk()
			throws Exception {
		assertThat(dataProvider.listUserCredentialVOs(2, 3)).hasSize(3);
		assertThat(dataProvider.listUserCredentialVOs(2, 3).get(0).getUsername()).isEqualTo(DAKOTA);
		assertThat(dataProvider.listUserCredentialVOs(2, 3).get(1).getUsername()).isEqualTo(EDOUARD);
		assertThat(dataProvider.listUserCredentialVOs(2, 3).get(2).getUsername()).isEqualTo(GANDALF);
	}

	@Test
	public void givenGreaterStartIndexWhenSubListThenReturnEmptyList()
			throws Exception {
		assertThat(dataProvider.listUserCredentialVOs(10, 19)).hasSize(0);
	}

	@Test
	public void givenGreaterCounterWhenSubListThenOk()
			throws Exception {

		assertThat(dataProvider.listUserCredentialVOs(0, 20)).hasSize(5);
	}
}
