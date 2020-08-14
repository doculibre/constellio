package com.constellio.app.ui.pages.globalGroup;

import com.constellio.app.ui.entities.GlobalGroupVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;

import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class AddEditGlobalGroupViewImpl extends BaseViewImpl implements AddEditGlobalGroupView {

	public static final String PARENT_GLOBAL_GROUP_CODE = "parentGlobalGroupCode";
	public static final String GLOBAL_GROUP_CODE = "globalGroupCode";
	private AddEditGlobalGroupPresenter presenter;

	private GlobalGroupVO globalGroupVO;

	private Map<String, String> paramsMap;

	private boolean addActionMode = true;

	@PropertyId("code")
	private TextField codeField;

	@PropertyId("name")
	private TextField nameField;

	@PropertyId("collections")
	private OptionGroup collectionsField;

	public AddEditGlobalGroupViewImpl() {
		this.presenter = new AddEditGlobalGroupPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		setupParamsAndVO(event);
	}

	private void setupParamsAndVO(ViewChangeEvent event) {
		String parameters = event.getParameters();
		int indexOfSlash = parameters.lastIndexOf("/");
		String breadCrumb = "";
		if (indexOfSlash != -1) {
			breadCrumb = parameters.substring(0, indexOfSlash);
		}
		paramsMap = ParamUtils.getParamsMap(parameters);
		if (paramsMap.containsKey(GLOBAL_GROUP_CODE)) {
			globalGroupVO = presenter.getGlobalGroupVO(paramsMap.get(GLOBAL_GROUP_CODE));
			addActionMode = false;
		} else {
			String parent = null;
			if (paramsMap.containsKey(PARENT_GLOBAL_GROUP_CODE)) {
				parent = paramsMap.get(PARENT_GLOBAL_GROUP_CODE);
			}
			globalGroupVO = new GlobalGroupVO(parent);
		}
		presenter.setParamsMap(paramsMap);
		presenter.setBreadCrumb(breadCrumb);
	}

	@Override
	protected String getTitle() {
		return $("AddEditGlobalGroupView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		codeField = new TextField();
		codeField.setCaption($("GlobalGroup.Code"));
		codeField.setRequired(true);
		codeField.setNullRepresentation("");
		codeField.setId("code");
		codeField.addStyleName("code");
		codeField.addStyleName("code-" + globalGroupVO.getCode());
		codeField.setEnabled(addActionMode && globalGroupVO.isLocallyCreated());

		nameField = new TextField();
		nameField.setCaption($("GlobalGroup.Name"));
		nameField.setRequired(true);
		nameField.setNullRepresentation("");
		nameField.setId("name");
		nameField.addStyleName("name");
		nameField.addStyleName("name-" + globalGroupVO.getCode());
		nameField.setEnabled(globalGroupVO.isLocallyCreated());

		collectionsField = new OptionGroup("Collections");
		collectionsField.addStyleName("collections");
		collectionsField.addStyleName("collections-username");
		collectionsField.setImmediate(true);
		collectionsField.setId("collections");
		collectionsField.setMultiSelect(true);
		for (String collection : presenter.getAllCollections()) {
			collectionsField.addItem(collection);
			if (globalGroupVO.getCollections() != null && globalGroupVO.getCollections().contains(collection)) {
				collectionsField.select(collection);
			}
		}

		collectionsField.setEnabled(addActionMode);


		return new BaseForm<GlobalGroupVO>(globalGroupVO, this, codeField, nameField, collectionsField) {
			@Override
			protected void saveButtonClick(GlobalGroupVO globalGroupVO)
					throws ValidationException {
				presenter.saveButtonClicked(globalGroupVO);
			}

			@Override
			protected void cancelButtonClick(GlobalGroupVO globalGroupVO) {
				presenter.cancelButtonClicked();
			}
		};
	}

}