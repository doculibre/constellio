package com.constellio.app.modules.rm.services.menu;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType;
import com.constellio.app.modules.rm.ui.pages.folder.DisplayFolderViewImpl;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus;
import com.constellio.app.services.menu.MenuItemServices;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.constellio.sdk.tests.FakeSessionContext.forRealUserIncollection;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

public class RMRecordsMenuItemServicesAcceptanceTest extends ConstellioTest {

	private Users users = new Users();
	private RMTestRecords records = new RMTestRecords(zeCollection);

	private MenuItemServices menuItemServices;
	private MenuItemActionBehaviorParams params;

	@Mock DisplayFolderViewImpl view;
	@Mock SessionContext sessionContext;

	@Before
	public void setup() {
		prepareSystem(withZeCollection().withConstellioRMModule().withConstellioESModule().withAllTest(users)
				.withRMTest(records).withDocumentsHavingContent().withFoldersAndContainersOfEveryStatus());

		menuItemServices = new MenuItemServices(zeCollection, getAppLayerFactory());

		params = new MenuItemActionBehaviorParams() {
			@Override
			public BaseView getView() {
				return view;
			}

			@Override
			public Map<String, String> getFormParams() {
				return Collections.emptyMap();
			}

			@Override
			public User getUser() {
				return users.adminIn(zeCollection);
			}
		};

		sessionContext = forRealUserIncollection(users.adminIn(zeCollection));
		when(view.getSessionContext()).thenReturn(sessionContext);
	}

	@Test
	public void givenRecordsContainsDocumentAndFolderThenReturnCorrectActionStates() {
		List<Record> currentRecords = asList(records.getFolder_A01().getWrappedRecord(),
				records.getDocumentWithContent_A19().getWrappedRecord());

		List<MenuItemAction> actions = menuItemServices.getActionsForRecords(currentRecords, params);

		assertThat(actions.stream().map(MenuItemAction::getType).collect(Collectors.toList()))
				.containsAll(Arrays.stream(RMRecordsMenuItemActionType.values())
						.map(RMRecordsMenuItemActionType::name).collect(Collectors.toList()));

		assertThat(actions).extracting("type", "state.status").isEqualTo(asList(
				tuple("CONSULTATION_LINK", MenuItemActionStateStatus.HIDDEN),
				tuple(RMRecordsMenuItemActionType.RMRECORDS_ADD_CART.name(), MenuItemActionStateStatus.VISIBLE),
				tuple(RMRecordsMenuItemActionType.RMRECORDS_MOVE.name(), MenuItemActionStateStatus.VISIBLE),
				tuple(RMRecordsMenuItemActionType.RMRECORDS_COPY.name(), MenuItemActionStateStatus.VISIBLE),
				tuple(RMRecordsMenuItemActionType.RMRECORDS_CREATE_SIP.name(), MenuItemActionStateStatus.VISIBLE),
				tuple(RMRecordsMenuItemActionType.RMRECORDS_SEND_EMAIL.name(), MenuItemActionStateStatus.DISABLED),
				tuple(RMRecordsMenuItemActionType.RMRECORDS_CREATE_PDF.name(), MenuItemActionStateStatus.DISABLED),
				tuple(RMRecordsMenuItemActionType.RMRECORDS_PRINT_LABEL.name(), MenuItemActionStateStatus.VISIBLE),
				tuple(RMRecordsMenuItemActionType.RMRECORDS_ADD_SELECTION.name(), MenuItemActionStateStatus.VISIBLE),
				tuple(RMRecordsMenuItemActionType.RMRECORDS_DOWNLOAD_ZIP.name(), MenuItemActionStateStatus.DISABLED),
				tuple(RMRecordsMenuItemActionType.RMRECORDS_BATCH_DELETE.name(), MenuItemActionStateStatus.VISIBLE),
				tuple(RMRecordsMenuItemActionType.RMRECORDS_CONSULT_LINK.name(), MenuItemActionStateStatus.VISIBLE),
				tuple(RMRecordsMenuItemActionType.RMRECORDS_CHECKIN.name(), MenuItemActionStateStatus.DISABLED),
				tuple(RMRecordsMenuItemActionType.RMRECORDS_CREATE_TASK.name(), MenuItemActionStateStatus.VISIBLE)
		));

	}

}
