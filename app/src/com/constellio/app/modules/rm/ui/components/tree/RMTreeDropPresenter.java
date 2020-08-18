package com.constellio.app.modules.rm.ui.components.tree;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.data.TreeNode;
import com.constellio.app.ui.framework.data.trees.DefaultLazyTreeDataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.users.UserServices;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class RMTreeDropPresenter implements Serializable {

	private static final Logger LOGGER = Logger.getLogger(RMTreeDropPresenter.class);

	private RMTreeDropHander dropHandler;

	public RMTreeDropPresenter(RMTreeDropHander dropHandler) {
		this.dropHandler = dropHandler;
	}

	String recordDropped(String sourceRecordId, String targetRecordId) {
		String newParentId;

		SessionContext sessionContext = dropHandler.getSessionContext();
		String collection = sessionContext.getCurrentCollection();

		ConstellioFactories constellioFactories = dropHandler.getConstellioFactories();
		AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		UserServices userServices = modelLayerFactory.newUserServices();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		DecommissioningService decommissioningService = new DecommissioningService(collection, appLayerFactory);

		TreeNode sourceTreeNode = DefaultLazyTreeDataProvider.toTreeNodeSupportingLegacyProviders(sourceRecordId);
		TreeNode targetTreeNode = DefaultLazyTreeDataProvider.toTreeNodeSupportingLegacyProviders(targetRecordId);

		Record sourceRecord = rm.get(sourceTreeNode.getId());
		Record targetRecord = rm.get(targetTreeNode.getId());

		SchemaUtils schemaUtils = new SchemaUtils();
		String sourceSchemaTypeCode = schemaUtils.getSchemaTypeCode(sourceRecord.getSchemaCode());
		String targetSchemaTypeCode = schemaUtils.getSchemaTypeCode(targetRecord.getSchemaCode());

		UserVO userVO = sessionContext.getCurrentUser();
		User user = userServices.getUserInCollection(userVO.getUsername(), collection);

		if (Folder.SCHEMA_TYPE.equals(sourceSchemaTypeCode)) {
			Folder sourceFolder = rm.wrapFolder(sourceRecord);
			if (Folder.SCHEMA_TYPE.equals(targetSchemaTypeCode)) {
				Folder targetFolder = rm.wrapFolder(targetRecord);
				sourceFolder.setParentFolder(targetFolder);
				try {
					recordServices.update(sourceFolder, user);
					newParentId = targetRecordId;
				} catch (RecordServicesException e) {
					dropHandler.showErrorMessage(e.getMessage());
					LOGGER.error("Error while dropping folder on folder", e);
					newParentId = null;
				}
			} else if (Category.SCHEMA_TYPE.equals(targetSchemaTypeCode)) {
				Category category = rm.wrapCategory(targetRecord);
				String categoryId = category.getId();
				List<String> retentionRules = decommissioningService.getRetentionRulesForCategory(
						categoryId, null, StatusFilter.ACTIVES);
				if (retentionRules.isEmpty()) {
					dropHandler.showErrorMessage($("RMTreeDropHandler.noRetentionRulesForCategory"));
					newParentId = null;
				} else if (retentionRules.size() > 1 && !retentionRules.contains(sourceFolder.getRetentionRule())) {
					dropHandler.showErrorMessage($("RMTreeDropHandler.moreThanOneRetentionRuleForCategory"));
					newParentId = null;
				} else {
					String retentionRule = retentionRules.get(0);
					sourceFolder.setCategoryEntered(category);
					sourceFolder.setRetentionRuleEntered(retentionRule);

					if (decommissioningService.isCopyStatusInputPossible(sourceFolder, user) && sourceFolder.getCopyStatusEntered() == null) {
						dropHandler.showErrorMessage($("RMTreeDropHandler.copyStatusInputRequired"));
						newParentId = null;
					} else {
						try {
							recordServices.update(sourceFolder, user);
							newParentId = targetRecordId;
						} catch (RecordServicesException e) {
							dropHandler.showErrorMessage(e.getMessage());
							LOGGER.error("Error while dropping folder on category", e);
							newParentId = null;
						}
					}
				}
			} else if (AdministrativeUnit.SCHEMA_TYPE.equals(targetSchemaTypeCode)) {
				AdministrativeUnit targetAdministrativeUnit = rm.wrapAdministrativeUnit(targetRecord);
				sourceFolder.setAdministrativeUnitEntered(targetAdministrativeUnit);
				if (decommissioningService.isCopyStatusInputPossible(sourceFolder, user) && sourceFolder.getCopyStatusEntered() == null) {
					dropHandler.showErrorMessage($("RMTreeDropHandler.copyStatusInputRequired"));
					newParentId = null;
				} else {
					try {
						recordServices.update(sourceFolder, user);
						newParentId = targetRecordId;
					} catch (RecordServicesException e) {
						dropHandler.showErrorMessage(e.getMessage());
						LOGGER.error("Error while dropping folder on administrative unit", e);
						newParentId = null;
					}
				}
			} else {
				newParentId = null;
			}
		} else if (Document.SCHEMA_TYPE.equals(sourceSchemaTypeCode) && Folder.SCHEMA_TYPE.equals(targetSchemaTypeCode)) {
			Document sourceDocument = rm.wrapDocument(sourceRecord);
			Folder targetFolder = rm.wrapFolder(targetRecord);

			sourceDocument.setFolder(targetFolder);
			try {
				recordServices.update(sourceDocument, user);
				newParentId = targetRecordId;
			} catch (RecordServicesException e) {
				dropHandler.showErrorMessage(e.getMessage());
				LOGGER.error("Error while dropping document on folder", e);
				newParentId = null;
			}
		} else if (Document.SCHEMA_TYPE.equals(sourceSchemaTypeCode) && Document.SCHEMA_TYPE.equals(targetSchemaTypeCode)) {
			Document sourceDocument = rm.wrapDocument(sourceRecord);
			Document targetDocument = rm.wrapDocument(targetRecord);
			Folder targetFolder = rm.getFolder(targetDocument.getFolder());

			sourceDocument.setFolder(targetFolder);
			try {
				recordServices.update(sourceDocument, user);
				newParentId = StringUtils.substringBeforeLast(targetRecordId, "|");
			} catch (RecordServicesException e) {
				dropHandler.showErrorMessage(e.getMessage());
				LOGGER.error("Error while dropping document on document", e);
				newParentId = null;
			}
		} else {
			newParentId = null;
		}
		return newParentId;
	}

}
