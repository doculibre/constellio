package com.constellio.app.ui.framework.components.fields;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaTypeToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.pages.MetadataSorterUtil;
import com.constellio.app.ui.pages.base.BasePresenterUtils;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.search.AdvancedSearchCriteriaComponent.SearchCriteriaPresenter;
import com.constellio.app.ui.pages.search.SearchCriteriaPresenterUtils;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.vaadin.navigator.View;
import com.vaadin.server.Page;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdvancedSearchCriteriaFieldPresenter implements SearchCriteriaPresenter {
	public AdvancedSearchCriteriaField field;
	private AppLayerFactory appLayerFactory;
	private SessionContext sessionContext;
	private String schemaType;
	private BasePresenterUtils presenterUtils;


	public AdvancedSearchCriteriaFieldPresenter(AdvancedSearchCriteriaField field,
												ConstellioFactories constellioFactories) {
		this.field = field;
		this.appLayerFactory = constellioFactories.getAppLayerFactory();
		this.sessionContext = ConstellioUI.getCurrentSessionContext();
		this.presenterUtils = new BasePresenterUtils(constellioFactories, sessionContext);
	}

	public List<MetadataSchemaTypeVO> getSchemaTypes() {
		//		String collection = view.getCollection();
		MetadataSchemaTypeToVOBuilder builder = new MetadataSchemaTypeToVOBuilder();

		List<MetadataSchemaTypeVO> result = new ArrayList<>();
		MetadataSchemaTypes types = types();
		if (types != null) {
			String[] supportedSchemaTypeCodes = {Folder.SCHEMA_TYPE};
			for (String supportedSchemaTypeCode : supportedSchemaTypeCodes) {
				MetadataSchemaType schemaType = types.getSchemaType(supportedSchemaTypeCode);
				result.add(builder.build(schemaType));
			}
		}
		return result;
	}

	private boolean isMetadataVisibleForUser(Metadata metadata, User currentUser) {
		if (MetadataValueType.REFERENCE.equals(metadata.getType())) {
			String referencedSchemaType = metadata.getAllowedReferences().getTypeWithAllowedSchemas();
			Taxonomy taxonomy = appLayerFactory.getModelLayerFactory().getTaxonomiesManager()
					.getTaxonomyFor(sessionContext.getCurrentCollection(), referencedSchemaType);
			if (taxonomy != null) {
				List<String> taxonomyGroupIds = taxonomy.getGroupIds();
				List<String> taxonomyUserIds = taxonomy.getUserIds();
				List<String> userGroups = currentUser.getUserGroups();
				for (String group : taxonomyGroupIds) {
					for (String userGroup : userGroups) {
						if (userGroup.equals(group)) {
							return true;
						}
					}
				}
				return (taxonomyGroupIds.isEmpty() && taxonomyUserIds.isEmpty()) || taxonomyUserIds.contains(currentUser.getId());
			} else {
				return true;
			}
		}
		return true;
	}

	public void selectSchemaType(String schemaType) {
		this.schemaType = schemaType;
	}

	public final MetadataSchemaTypes types() {
		MetadataSchemasManager metadataSchemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		return metadataSchemasManager.getSchemaTypes(sessionContext.getCurrentCollection());
	}

	@Override
	public void addCriterionRequested() {
		field.getAdvancedCriterionComponent().addEmptyCriterion();
	}

	@Override
	public List<MetadataVO> getMetadataAllowedInCriteria() {
		//		String collection = view.getCollection();
		MetadataSchemaTypes types = types();
		MetadataSchemaType metadataSchemaType = types.getSchemaType(schemaType);
		List<String> metadataCodes = new ArrayList<>();

		List<MetadataVO> result = new ArrayList<>();
		MetadataList allMetadatas = metadataSchemaType.getAllMetadatas();

		for (Metadata metadata : metadataSchemaType.getAllMetadatas()) {
			metadataCodes.add(metadata.getCode());
		}

		MetadataToVOBuilder builder = new MetadataToVOBuilder();
		for (Metadata metadata : allMetadatas) {
			if (!metadataSchemaType.hasSecurity() || (metadataCodes.contains(metadata.getCode()))) {
				boolean isTextOrString =
						metadata.getType() == MetadataValueType.STRING || metadata.getType() == MetadataValueType.TEXT;

				if (this.presenterUtils.getCurrentUser().hasGlobalAccessToMetadata(metadata)
					&& isMetadataVisibleForUser(metadata, this.presenterUtils.getCurrentUser()) &&
					(!isTextOrString || isTextOrString ||
					 Schemas.PATH.getLocalCode().equals(metadata.getLocalCode()) ||
					 ConnectorSmbFolder.PARENT_CONNECTOR_URL.equals(metadata.getLocalCode()) ||
					 ConnectorSmbDocument.PARENT_CONNECTOR_URL.equals(metadata.getLocalCode()))) {
					result.add(builder.build(metadata, sessionContext));
				}
			}
		}
		MetadataSorterUtil.sort(result, sessionContext);
		return result;
	}

	@Override
	public Map<String, String> getMetadataSchemasList(String schemaTypeCode) {
		SearchCriteriaPresenterUtils searchCriteriaPresenterUtils = new SearchCriteriaPresenterUtils(
				ConstellioUI.getCurrentSessionContext());
		return searchCriteriaPresenterUtils.getMetadataSchemasList(schemaTypeCode);
	}

	@Override
	public MetadataVO getMetadataVO(String metadataCode) {
		return presenterUtils.presenterService().getMetadataVO(metadataCode, sessionContext);
	}

	@Override
	public Component getExtensionComponentForCriterion(Criterion criterion) {
		AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollection(sessionContext.getCurrentCollection());
		return extensions.getComponentForCriterion(criterion);
	}

	@Override
	public void showErrorMessage(String message) {
		View view = ConstellioUI.getCurrent().getCurrentView();

		if (view instanceof BaseViewImpl) {
			((BaseViewImpl) view).showErrorMessage(message);
		} else {
			Notification notification = new Notification(message);
			Page.getCurrent().showNotification(notification);
		}
	}
}
