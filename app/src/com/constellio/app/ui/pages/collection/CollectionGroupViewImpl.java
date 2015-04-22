/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.pages.collection;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.AuthorizationsButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.RolesButton;
import com.constellio.app.ui.framework.components.MetadataDisplayFactory;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.records.wrappers.Group;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

public class CollectionGroupViewImpl extends BaseViewImpl implements CollectionGroupView {
	public static final String GROUP_ROLES = Group.DEFAULT_SCHEMA + "_" + Group.ROLES;
	public static final String GROUP_CODE = Group.DEFAULT_SCHEMA + "_" + Group.CODE;

	private final CollectionGroupPresenter presenter;
	private RecordVO group;

	public CollectionGroupViewImpl() {
		presenter = new CollectionGroupPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forRequestParams(event.getParameters());
	}

	@Override
	protected String getTitle() {
		return $("CollectionGroupView.viewTitle");
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> buttons = super.buildActionMenuButtons(event);

		Button authorizations = new AuthorizationsButton(false) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.authorizationsButtonClicked();
			}
		};
		buttons.add(authorizations);

		Button roles = new RolesButton(false) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.rolesButtonClicked();
			}
		};
		buttons.add(roles);

		Button delete = new DeleteButton(false) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				String code = group.get(GROUP_CODE);
				presenter.deleteButtonClicked(code);
			}
		};
		buttons.add(delete);

		return buttons;
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				navigateTo().collectionSecurity();
			}
		};
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		group = presenter.getGroup();
		return new RecordDisplay(group, new GroupMetadataDisplayFactory());
	}

	public class GroupMetadataDisplayFactory extends MetadataDisplayFactory {
		@Override
		public Component buildSingleValue(MetadataVO metadata, Object displayValue) {
			switch (metadata.getCode()) {
			case GROUP_ROLES:
				return new Label(presenter.getRoleTitle((String) displayValue));
			default:
				return super.buildSingleValue(metadata, displayValue);
			}
		}
	}
}
