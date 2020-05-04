package com.constellio.app.ui.pages.management.authorizations;

import com.constellio.app.ui.entities.AuthorizationVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.ListOptionGroup;
import com.constellio.app.ui.framework.components.fields.date.JodaDateField;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordLookupField;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;

import static com.constellio.app.ui.i18n.i18n.$;

public class ShareContentViewImpl extends BaseViewImpl implements ShareContentView {
	private final ShareContentPresenter presenter;
	private RecordVO record;

	@PropertyId("users") private ListAddRemoveRecordLookupField users;
	@PropertyId("groups") private ListAddRemoveRecordLookupField groups;
	@PropertyId("accessRoles") private ListOptionGroup accessRoles;
	@PropertyId("userRoles") private ListOptionGroup userRoles;
	@PropertyId("startDate") private JodaDateField startDate;
	@PropertyId("endDate") private JodaDateField endDate;

	public ShareContentViewImpl() {
		presenter = new ShareContentPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		record = presenter.forRequestParams(event.getParameters()).getRecordVO();
	}

	@Override
	protected String getTitle() {
		return $("ShareContentView.viewTitle", record.getTitle());
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				returnFromPage();
			}
		};
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		buildUsersAndGroupsField();
		buildAccessField();
		buildRolesField();
		buildDateFields();
		AuthorizationVO shareVO = presenter.getShareAuthorization(record.getRecord());
		if (shareVO != null) {
			return new BaseForm<AuthorizationVO>(
					shareVO, this, users, groups, accessRoles, userRoles, startDate, endDate) {
				@Override
				protected void saveButtonClick(AuthorizationVO authorization)
						throws ValidationException {
					presenter.authorizationModifyRequested(authorization);
				}

				@Override
				protected void cancelButtonClick(AuthorizationVO authorization) {
					returnFromPage();
				}
			};
		} else {
			return new BaseForm<AuthorizationVO>(
					AuthorizationVO.forContent(record.getId()), this, users, groups, accessRoles, userRoles, startDate, endDate) {
				@Override
				protected void saveButtonClick(AuthorizationVO authorization)
						throws ValidationException {
					presenter.authorizationCreationRequested(authorization);
				}

				@Override
				protected void cancelButtonClick(AuthorizationVO authorization) {
					returnFromPage();
				}
			};
		}
	}

	@Override
	public void returnFromPage() {
		presenter.backButtonClicked();
	}

	private void buildUsersAndGroupsField() {
		users = new ListAddRemoveRecordLookupField(User.SCHEMA_TYPE);
		users.setCaption($("AuthorizationsView.users"));
		users.setId("users");

		groups = new ListAddRemoveRecordLookupField(Group.SCHEMA_TYPE);
		groups.setCaption($("AuthorizationsView.groups"));
		groups.setId("groups");
	}

	private void buildAccessField() {
		accessRoles = new ListOptionGroup($("AuthorizationsView.access"));
		for (String accessCode : presenter.getAllowedAccesses()) {
			accessRoles.addItem(accessCode);
			accessRoles.setItemCaption(accessCode, $("AuthorizationsView." + accessCode));
		}
		accessRoles.setRequired(true);
		accessRoles.setMultiSelect(true);
		accessRoles.setId("accessRoles");
	}

	private void buildRolesField() {
		userRoles = new ListOptionGroup($("AuthorizationsView.userRoles"));
		for (String roleCode : presenter.getAllowedRoles()) {
			userRoles.addItem(roleCode);
			userRoles.setItemCaption(roleCode, roleCode);
		}
		userRoles.setEnabled(presenter.seeRolesField());
		userRoles.setVisible(presenter.seeRolesField());
		userRoles.setMultiSelect(true);
		userRoles.setId("userRoles");
	}

	private void buildDateFields() {
		boolean fieldsRequired = presenter.isDateFieldValuesRequired();

		startDate = new JodaDateField();
		startDate.setCaption($("AuthorizationsView.startDate"));
		startDate.setId("startDate");
		startDate.setRequired(fieldsRequired);

		endDate = new JodaDateField();
		endDate.setCaption($("AuthorizationsView.endDate"));
		endDate.setId("endDate");
		endDate.setRequired(fieldsRequired);
	}

}
