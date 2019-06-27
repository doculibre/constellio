package com.constellio.app.modules.rm.services.menu;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType;
import com.constellio.app.modules.rm.ui.pages.folder.DisplayFolderViewImpl;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus;
import com.constellio.app.services.menu.MenuItemServices;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
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

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.FakeSessionContext.forRealUserIncollection;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class RMRecordsMenuItemServicesAcceptanceTest extends ConstellioTest {

	private Users users = new Users();
	private RMTestRecords records = new RMTestRecords(zeCollection);
	private RMSchemasRecordsServices rm;

	private MenuItemServices menuItemServices;
	private MenuItemActionBehaviorParams params;

	@Mock DisplayFolderViewImpl view;
	@Mock SessionContext sessionContext;

	@Before
	public void setup() {
		prepareSystem(withZeCollection().withConstellioRMModule().withConstellioESModule().withAllTest(users)
				.withRMTest(records).withDocumentsHavingContent().withFoldersAndContainersOfEveryStatus());

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		menuItemServices = new MenuItemServices(zeCollection, getAppLayerFactory());

		params = new MenuItemActionBehaviorParams() {
			@Override
			public BaseView getView() {
				return view;
			}

			@Override
			public RecordVO getRecordVO() {
				return null;
			}

			@Override
			public ContentVersionVO getContentVersionVO() {
				return null;
			}

			@Override
			public Map<String, String> getFormParams() {
				return Collections.emptyMap();
			}

			@Override
			public User getUser() {
				return users.adminIn(zeCollection);
			}

			@Override
			public boolean isContextualMenu() {
				return false;
			}

			@Override
			public boolean isNestedView() {
				return false;
			}
		};

		sessionContext = forRealUserIncollection(users.adminIn(zeCollection));
		when(view.getSessionContext()).thenReturn(sessionContext);
	}

	@Test
	public void givenRecordContainsDocumentAndFolderThenReturnCorrectActionStates() {
		List<Record> currentRecords = asList(records.getFolder_A01().getWrappedRecord(),
				records.getDocumentWithContent_A19().getWrappedRecord());

		List<MenuItemAction> actions = menuItemServices.getActionsForRecords(currentRecords, params);

		assertThat(actions.stream().map(MenuItemAction::getType).collect(Collectors.toList()))
				.containsAll(Arrays.stream(RMRecordsMenuItemActionType.values())
						.map(RMRecordsMenuItemActionType::name).collect(Collectors.toList()));
		assertThat(actions.get(0).getType()).isEqualTo(RMRecordsMenuItemActionType.RMRECORDS_ADD_CART.name());
		assertThat(actions.get(0).getState().getStatus()).isEqualTo(MenuItemActionStateStatus.VISIBLE);
		assertThat(actions.get(1).getType()).isEqualTo(RMRecordsMenuItemActionType.RMRECORDS_MOVE.name());
		assertThat(actions.get(1).getState().getStatus()).isEqualTo(MenuItemActionStateStatus.VISIBLE);
		assertThat(actions.get(2).getType()).isEqualTo(RMRecordsMenuItemActionType.RMRECORDS_COPY.name());
		assertThat(actions.get(2).getState().getStatus()).isEqualTo(MenuItemActionStateStatus.VISIBLE);
		assertThat(actions.get(3).getType()).isEqualTo(RMRecordsMenuItemActionType.RMRECORDS_CREATE_SIP.name());
		assertThat(actions.get(3).getState().getStatus()).isEqualTo(MenuItemActionStateStatus.VISIBLE);
		assertThat(actions.get(4).getType()).isEqualTo(RMRecordsMenuItemActionType.RMRECORDS_SEND_EMAIL.name());
		assertThat(actions.get(4).getState().getStatus()).isEqualTo(MenuItemActionStateStatus.DISABLED);
		assertThat(actions.get(5).getType()).isEqualTo(RMRecordsMenuItemActionType.RMRECORDS_CREATE_PDF.name());
		assertThat(actions.get(5).getState().getStatus()).isEqualTo(MenuItemActionStateStatus.DISABLED);
		assertThat(actions.get(6).getType()).isEqualTo(RMRecordsMenuItemActionType.RMRECORDS_PRINT_LABEL.name());
		assertThat(actions.get(6).getState().getStatus()).isEqualTo(MenuItemActionStateStatus.VISIBLE);
		assertThat(actions.get(7).getType()).isEqualTo(RMRecordsMenuItemActionType.RMRECORDS_ADD_SELECTION.name());
		assertThat(actions.get(7).getState().getStatus()).isEqualTo(MenuItemActionStateStatus.VISIBLE);
		assertThat(actions.get(8).getType()).isEqualTo(RMRecordsMenuItemActionType.RMRECORDS_DOWNLOAD_ZIP.name());
		assertThat(actions.get(8).getState().getStatus()).isEqualTo(MenuItemActionStateStatus.DISABLED);
	}

	@Test
	public void givenQueryWithSelectedRecordsThenReturnCorrectStates() {
		sessionContext.addSelectedRecordId(records.document_A19, rm.document.schema().getCode());
		sessionContext.addSelectedRecordId(records.document_B30, rm.document.schema().getCode());

		LogicalSearchQuery query = new LogicalSearchQuery(from(rm.document.schema()).returnAll());

		List<MenuItemAction> actions = menuItemServices.getActionsForRecords(query, params);

		actions.forEach(a -> assertThat(a.getState().getStatus()).isEqualTo(MenuItemActionStateStatus.VISIBLE));
	}

	@Test
	public void givenQueryWithNoSelectedRecordsThenReturnCorrectStates() {
		LogicalSearchQuery query = new LogicalSearchQuery(
				from(rm.document.schema()).where(rm.document.folder()).is(records.folder_A01));

		List<MenuItemAction> actions = menuItemServices.getActionsForRecords(query, params);

		actions.forEach(a -> assertThat(a.getState().getStatus()).isEqualTo(MenuItemActionStateStatus.HIDDEN));
	}

}
