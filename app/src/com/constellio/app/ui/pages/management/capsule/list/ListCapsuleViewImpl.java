package com.constellio.app.ui.pages.management.capsule.list;

import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.management.searchConfig.SearchConfigurationViewImpl;
import com.vaadin.data.Container;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import org.vaadin.dialogs.ConfirmDialog;

import static com.constellio.app.ui.i18n.i18n.$;

public class ListCapsuleViewImpl extends BaseViewImpl implements ListCapsuleView {
	private ListCapsulePresenter presenter;

	@Override
	protected String getTitle() {
		return $("ListCapsuleView.title");
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
		presenter = new ListCapsulePresenter(this);
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return SearchConfigurationViewImpl.getSearchConfigurationBreadCrumbTrail(this, getTitle());
	}

	@Override
	protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		Button addButton = new AddButton($("ListCapsuleView.addCapsule")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.addButtonClicked();
			}
		};

		Label infoLabel = new Label($("ListCapsuleView.info"));
		infoLabel.setWidth("100%");

		Container tableContainer = new RecordVOLazyContainer(presenter.getCapsuleDataProvider());
		ButtonsContainer buttonTableContainer = new ButtonsContainer(tableContainer, "buttons");

		buttonTableContainer.addButton(new ButtonsContainer.ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new EditButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.editButtonClicked(presenter.getRecordsWithIndex(itemId));
					}
				};
			}
		});
		buttonTableContainer.addButton(new ButtonsContainer.ContainerButton() {

			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DeleteButton() {

					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						presenter.deleteButtonClicked(presenter.getRecordsWithIndex(itemId));
					}
				};
			}
		});

		tableContainer = buttonTableContainer;
		Table table = new RecordVOTable($("ListCapsuleView.table.title", tableContainer.size()), tableContainer);
		setTableProperty(table, tableContainer.size());

		mainLayout.addComponents(infoLabel, addButton, table);
		mainLayout.setComponentAlignment(addButton, Alignment.BOTTOM_RIGHT);
		mainLayout.setExpandRatio(table, 1);

		return mainLayout;
	}

	@Override
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonClicked();
			}
		};
	}

}
