package com.constellio.app.modules.rm.extensions;

import com.constellio.app.api.extensions.PagesComponentsExtension;
import com.constellio.app.api.extensions.params.DecorateMainComponentAfterInitExtensionParams;
import com.constellio.app.modules.rm.services.RMRecordDeletionServices;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.CleanAdministrativeUnitButton;
import com.constellio.app.ui.framework.decorators.base.ActionMenuButtonsDecorator;
import com.constellio.app.ui.pages.base.BasePresenterUtils;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.management.taxonomy.TaxonomyManagementViewImpl;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.containingText;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static java.util.Arrays.asList;

/**
 * Created by Constellio on 2017-03-16.
 */
public class RMCleanAdministrativeUnitButtonExtension extends PagesComponentsExtension {

	String collection;
	AppLayerFactory appLayerFactory;

	public RMCleanAdministrativeUnitButtonExtension(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	public void decorateMainComponentBeforeViewAssembledOnViewEntered(
			DecorateMainComponentAfterInitExtensionParams params) {
		super.decorateMainComponentAfterViewAssembledOnViewEntered(params);
		Component mainComponent = params.getMainComponent();
		if (mainComponent instanceof TaxonomyManagementViewImpl) {
			TaxonomyManagementViewImpl view = (TaxonomyManagementViewImpl) mainComponent;
			if (view.hasCurrentUserAccessToCurrentConcept()) {
				view.addActionMenuButtonsDecorator(new ActionMenuButtonsDecorator() {
					@Override
					public void decorate(final BaseViewImpl view, List<Button> actionMenuButtons) {
						TaxonomyManagementViewImpl taxonomyView = (TaxonomyManagementViewImpl) view;
						RecordVO currentConcept = taxonomyView.getCurrentConcept();
						User currentUser = getCurrentUser(view);
						if (currentConcept != null && AdministrativeUnit.SCHEMA_TYPE.equals(currentConcept.getSchema().getTypeCode())) {
							actionMenuButtons.add(buildNewMenuButton(currentConcept, currentUser, taxonomyView));
						}
					}
				});
			}
		}
	}

	private Button buildNewMenuButton(final RecordVO currentConcept, final User currentUser,
									  final TaxonomyManagementViewImpl view) {
		return new CleanAdministrativeUnitButton($("TaxonomyManagementView.cleanAdministrativeUnit")) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				cleanAdministrativeUnitButtonClicked(currentConcept, currentUser);
				view.showMessage($("TaxonomyManagementView.administrativeUnitCleaned"));
				view.refresh();
			}
		};
	}

	public void cleanAdministrativeUnitButtonClicked(RecordVO currentConcept, User currentUser) {
		if (hasCurrentUserRequiredRightsToCleanAdminUnitChilds(currentUser, currentConcept)) {
			RMRecordDeletionServices.cleanAdministrativeUnit(collection, currentConcept.getId(), appLayerFactory);
		}
	}

	private boolean hasCurrentUserRequiredRightsToCleanAdminUnitChilds(User currentUser, RecordVO currentConcept) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();

		AdministrativeUnit administrativeUnit = rm.wrapAdministrativeUnit(searchServices.
				searchSingleResult(from(rm.administrativeUnit.schema()).where(Schemas.IDENTIFIER)
						.isEqualTo(currentConcept.getId())));

		boolean hasAllRights = hasCurrentUserDeletionRightsToCleanTasks(administrativeUnit, currentUser);
		if (hasAllRights) {
			hasAllRights = hasCurrentUserDeletionRightsToCleanFolders(administrativeUnit, currentUser);
		}
		if (hasAllRights) {
			hasAllRights = hasCurrentUserDeletionRightsToCleanContainers(administrativeUnit, currentUser);
		}
		return hasAllRights;
	}

	private boolean hasCurrentUserDeletionRightsToCleanFolders(AdministrativeUnit administrativeUnit,
															   User currentUser) {

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		TasksSchemasRecordsServices taskSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);

		SearchResponseIterator<Record> documentIterator = searchServices
				.recordsIterator(new LogicalSearchQuery().setCondition(from(rm.document.schema())
						.where(Schemas.PRINCIPAL_PATH).isContainingText(administrativeUnit.getId()))
						.sortDesc(Schemas.PRINCIPAL_PATH));
		SearchResponseIterator<Record> folderIterator = searchServices
				.recordsIterator(new LogicalSearchQuery().setCondition(from(rm.folder.schema())
						.where(Schemas.PRINCIPAL_PATH).isContainingText(administrativeUnit.getId()))
						.sortDesc(Schemas.PRINCIPAL_PATH));
		List<Record> taskList = searchServices.search(new LogicalSearchQuery().setCondition(from(taskSchemas.userTask.schema())
				.where(Schemas.PRINCIPAL_PATH).isNot(containingText(administrativeUnit.getId())))
				.sortDesc(Schemas.PRINCIPAL_PATH));

		while (documentIterator.hasNext()) {
			Record document = documentIterator.next();
			if (!hasCurrentUserWriteRightsToUnlinkDocumentFromDecommissioningLists(document, currentUser)) {
				return false;
			}
			if (!hasCurrentUserWriteRightsToUnlinkDocumentFromTask(document, taskList, currentUser)) {
				return false;
			}
			if (!currentUser.hasDeleteAccess().on(document)) {
				return false;
			}
		}
		while (folderIterator.hasNext()) {
			Record folder = folderIterator.next();
			if (!hasCurrentUserWriteRightsToUnlinkFolderFromDecommissioningLists(folder, currentUser)) {
				return false;
			}
			if (!hasCurrentUserWriteRightsToUnlinkFolderFromTask(folder, taskList, currentUser)) {
				return false;
			}
			if (!currentUser.hasDeleteAccess().on(folder)) {
				return false;
			}
		}
		return true;
	}

	private boolean hasCurrentUserDeletionRightsToCleanContainers(AdministrativeUnit administrativeUnit,
																  User currentUser) {

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();

		SearchResponseIterator<Record> containerIterator = searchServices
				.recordsIterator(new LogicalSearchQuery().setCondition(from(rm.containerRecord.schema())
						.where(Schemas.PRINCIPAL_PATH).isContainingText(administrativeUnit.getId()))
						.sortDesc(Schemas.PRINCIPAL_PATH));

		while (containerIterator.hasNext()) {
			Record container = containerIterator.next();
			if (!hasCurrentUserWriteRightsToUnlinkContainerFromDecommissioningLists(container, currentUser)) {
				return false;
			}
			if (!currentUser.hasDeleteAccess().on(container)) {
				return false;
			}
		}
		return true;
	}

	private boolean hasCurrentUserDeletionRightsToCleanTasks(AdministrativeUnit administrativeUnit, User currentUser) {

		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		TasksSchemasRecordsServices schemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
		LogicalSearchQuery query = new LogicalSearchQuery().setCondition(from(schemas.userTask.schema())
				.where(Schemas.PRINCIPAL_PATH).isContainingText(administrativeUnit.getId())).sortDesc(Schemas.PRINCIPAL_PATH);

		SearchResponseIterator<Record> userTaskIterator = searchServices.recordsIterator(query);
		while (userTaskIterator.hasNext()) {
			Record userTask = userTaskIterator.next();
			if (!currentUser.hasDeleteAccess().on(userTask)) {
				return false;
			}
		}
		return true;
	}

	private boolean hasCurrentUserWriteRightsToUnlinkDocumentFromDecommissioningLists(Record document,
																					  User currentUser) {

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		List<DecommissioningList> decommissioningLists = rm.searchDecommissioningLists(
				where(rm.decommissioningList.documents()).isContaining(asList(document.getId())));
		for (DecommissioningList decommissioningList : decommissioningLists) {
			if (!currentUser.hasWriteAccess().on(decommissioningList)) {
				return false;
			}
		}
		return true;
	}

	private boolean hasCurrentUserWriteRightsToUnlinkFolderFromDecommissioningLists(Record folder, User currentUser) {

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		List<DecommissioningList> decommissioningLists = rm.searchDecommissioningLists(
				where(rm.decommissioningList.folders()).isContaining(asList(folder.getId())));
		for (DecommissioningList decommissioningList : decommissioningLists) {
			if (!currentUser.hasWriteAccess().on(decommissioningList)) {
				return false;
			}
		}
		return true;
	}

	private boolean hasCurrentUserWriteRightsToUnlinkContainerFromDecommissioningLists(Record container,
																					   User currentUser) {

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		List<DecommissioningList> decommissioningLists = rm.searchDecommissioningLists(
				where(rm.decommissioningList.containers()).isContaining(asList(container.getId())));
		for (DecommissioningList decommissioningList : decommissioningLists) {
			if (!currentUser.hasWriteAccess().on(decommissioningList)) {
				return false;
			}
		}
		return true;
	}

	private boolean hasCurrentUserWriteRightsToUnlinkDocumentFromTask(Record document, List<Record> taskList,
																	  User currentUser) {

		for (Record task : taskList) {
			MetadataSchema curTaskSchema = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager()
					.getSchemaTypes(collection).getSchema(task.getSchemaCode());
			List<String> linkedDocumentsIDs = task.get(curTaskSchema.getMetadata(RMTask.LINKED_DOCUMENTS));
			linkedDocumentsIDs = new ArrayList<>(linkedDocumentsIDs);
			if (linkedDocumentsIDs.contains(document.getId())) {
				if (!currentUser.hasWriteAccess().on(task)) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean hasCurrentUserWriteRightsToUnlinkFolderFromTask(Record folder, List<Record> taskList,
																	User currentUser) {

		for (Record task : taskList) {
			MetadataSchema curTaskSchema = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager()
					.getSchemaTypes(collection).getSchema(task.getSchemaCode());
			List<String> linkedDocumentsIDs = task.get(curTaskSchema.getMetadata(RMTask.LINKED_DOCUMENTS));
			linkedDocumentsIDs = new ArrayList<>(linkedDocumentsIDs);
			if (linkedDocumentsIDs.contains(folder.getId())) {
				if (!currentUser.hasWriteAccess().on(task)) {
					return false;
				}
			}
		}
		return true;
	}

	private User getCurrentUser(BaseView view) {
		BasePresenterUtils basePresenterUtils = new BasePresenterUtils(view.getConstellioFactories(), view.getSessionContext());
		return basePresenterUtils.getCurrentUser();
	}
}
