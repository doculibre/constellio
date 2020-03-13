package com.constellio.app.ui.framework.components.fields;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataSchemaTypeToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.pages.base.BasePresenterUtils;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.search.AdvancedSearchCriteriaComponent.SearchCriteriaPresenter;
import com.constellio.app.ui.pages.search.SearchCriteriaPresenterUtils;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
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
	private ConstellioFactories constellioFactories;
	private BasePresenterUtils presenterUtils;


	public AdvancedSearchCriteriaFieldPresenter(AdvancedSearchCriteriaField field,
												ConstellioFactories constellioFactories) {
		this.field = field;
		this.appLayerFactory = constellioFactories.getAppLayerFactory();
		this.constellioFactories = constellioFactories;
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

	public void selectAdvancedSearchSchemaType(String schemaType) {
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
		MetadataSchemaType schemaType = types().getSchemaType(Folder.SCHEMA_TYPE);
		MetadataSchema schema = schemaType.getDefaultSchema();

		List<MetadataVO> metadataVOList = new ArrayList<>();

		MetadataToVOBuilder metadataToVOBuilder = new MetadataToVOBuilder();
		MetadataSchemaToVOBuilder metadataSchemaToVOBuilder = new MetadataSchemaToVOBuilder();

		for (Metadata metadata : schemaType.getAllMetadatas()) {
			metadataVOList.add(metadataToVOBuilder.build(metadata, metadataSchemaToVOBuilder.build(schema, VIEW_MODE.DISPLAY, sessionContext), sessionContext));
		}

		return metadataVOList;
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
