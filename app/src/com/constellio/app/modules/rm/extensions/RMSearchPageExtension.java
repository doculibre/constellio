package com.constellio.app.modules.rm.extensions;

import com.constellio.app.api.extensions.SearchPageExtension;
import com.constellio.app.api.extensions.params.AddComponentToSearchResultParams;
import com.constellio.app.api.extensions.params.GetSearchResultSimpleTableWindowComponentParam;
import com.constellio.app.api.extensions.params.SearchPageConditionParam;
import com.constellio.app.api.extensions.taxonomies.GetCustomResultDisplayParam;
import com.constellio.app.api.extensions.taxonomies.UserSearchEvent;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.components.DocumentSearchResultDisplay;
import com.constellio.app.modules.rm.ui.pages.containers.DisplayContainerPresenter;
import com.constellio.app.modules.rm.ui.pages.containers.DisplayContainerViewImpl;
import com.constellio.app.modules.rm.ui.pages.document.DisplayDocumentViewImpl;
import com.constellio.app.modules.rm.ui.pages.folder.DisplayFolderViewImpl;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMUser;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.SearchResultDisplay;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.pages.search.AdvancedSearchViewImpl;
import com.constellio.app.ui.pages.search.SimpleSearchViewImpl;
import com.constellio.data.utils.LangUtils.StringReplacer;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.allConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.anyConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;

public class RMSearchPageExtension extends SearchPageExtension {

	public static final StringReplacer STRING_REPLACER = new StringReplacer().replacingRegex("\\|", "/");

	AppLayerFactory appLayerFactory;
	String collection;
	RMSchemasRecordsServices rm;

	public RMSearchPageExtension(String collection, AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.collection = collection;
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
	}

	@Override
	public SearchResultDisplay getCustomResultDisplayFor(GetCustomResultDisplayParam param) {
		if (param.getSchemaType().equals(Document.SCHEMA_TYPE)) {
			return new DocumentSearchResultDisplay(param.getSearchResultVO(), param.getComponentFactory(), appLayerFactory, param.getQuery());
		}
		return super.getCustomResultDisplayFor(param);
	}

	@Override
	public void notifyNewUserSearch(UserSearchEvent event) {
	}

	@Override
	public Component getSimpleTableWindowComponent(GetSearchResultSimpleTableWindowComponentParam param) {
		Component result;
		RecordVO recordVO = param.getRecordVO();
		String typeCode = param.getSchemaType();
		//TODO add event
		if (typeCode.equals(Document.SCHEMA_TYPE)) {
			//			result = new RecordDisplay(recordVO, new RMMetadataDisplayFactory());
			DisplayDocumentViewImpl view = new DisplayDocumentViewImpl(recordVO, true);
			view.enter(null);
			result = view;
		} else if (typeCode.equals(Folder.SCHEMA_TYPE)) {
			DisplayFolderViewImpl view = new DisplayFolderViewImpl(recordVO, true);
			view.enter(null);
			result = view;
		} else {
			if (typeCode.equals(ContainerRecord.SCHEMA_TYPE) &&
				DisplayContainerPresenter.hasRestrictedRecordAccess(rm, param.getUser(), recordVO.getRecord())) {
				DisplayContainerViewImpl view = new DisplayContainerViewImpl(recordVO, true);
				view.enter(null);
				result = view;
			} else {
				result = null;
			}
		}
		return result;
	}

	@Override
	public LogicalSearchCondition adjustSearchPageCondition(SearchPageConditionParam param) {
		Component mainComponent = param.getMainComponent();
		LogicalSearchCondition logicalSearchCondition = param.getCondition();
		if (mainComponent instanceof SimpleSearchViewImpl || mainComponent instanceof AdvancedSearchViewImpl) {
			User user = param.getUser();
			if (Boolean.TRUE.equals(user.get(RMUser.HIDE_NOT_ACTIVE))) {
				List<String> notActiveCodes = new ArrayList<>();
				notActiveCodes.add(FolderStatus.SEMI_ACTIVE.getCode());
				notActiveCodes.add(FolderStatus.INACTIVE_DEPOSITED.getCode());
				notActiveCodes.add(FolderStatus.INACTIVE_DESTROYED.getCode());

				LogicalSearchCondition activesOnlyCondition = anyConditions(
						where(rm.folder.archivisticStatus()).isNotIn(notActiveCodes),
						where(rm.folder.archivisticStatus()).isNull()
				);
				logicalSearchCondition = allConditions(logicalSearchCondition, activesOnlyCondition);
			}
		}
		return logicalSearchCondition;
	}

	@Override
	public List<Component> addComponentToSearchResult(AddComponentToSearchResultParams addComponentToSearchResultParams) {
		String schemaTypeCode = addComponentToSearchResultParams.getSearchResultVO().getRecordVO().getSchema().getTypeCode();

		if(!schemaTypeCode.equals(Folder.SCHEMA_TYPE) && !schemaTypeCode.equals(Document.SCHEMA_TYPE)) {
			return null;
		}

		String captionTxt = addComponentToSearchResultParams.getSearchResultVO().getRecordVO().get(Schemas.CAPTION);
		ArrayList<Component> componentListToReturn = new ArrayList();

		captionTxt = STRING_REPLACER.replaceOn(captionTxt);

		Label value = new Label( "/ " + captionTxt, ContentMode.HTML);

		I18NHorizontalLayout item = new I18NHorizontalLayout(value);
		item.setHeight("100%");
		item.setSpacing(true);
		item.addStyleName("metadata-caption-layout");
		item.addStyleName(SearchResultDisplay.METADATA_STYLE);
		componentListToReturn.add(item);

		return componentListToReturn;
	}
}
