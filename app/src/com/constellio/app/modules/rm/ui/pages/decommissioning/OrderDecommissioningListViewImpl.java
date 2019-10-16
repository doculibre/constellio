package com.constellio.app.modules.rm.ui.pages.decommissioning;

import com.constellio.app.modules.rm.ui.entities.FolderDetailVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import static com.constellio.app.ui.i18n.i18n.$;

public class OrderDecommissioningListViewImpl extends BaseViewImpl implements OrderDecommissioningListView {
	private OrderDecommissioningListPresenter presenter;
	private ListSelect listSelect;
	public static final String BUTTONS_LAYOUT = "base-form-buttons-layout";
	public static final String SAVE_BUTTON = "base-form-save";
	public static final String CANCEL_BUTTON = "base-form-cancel";
	private RecordVO decommissioningList;

	public OrderDecommissioningListViewImpl() {
		presenter = new OrderDecommissioningListPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		decommissioningList = presenter.forParams(event.getParameters()).getDecommissioningList();
	}

	@Override
	protected String getTitle() {
		return $("OrderDecommissioningListView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		HorizontalLayout subLayout = new HorizontalLayout();
		subLayout.setSizeFull();
		subLayout.setSpacing(true);
		subLayout.addComponents(folderList());
		subLayout.addComponent(getSideButtonLayout());

		mainLayout.addComponent(subLayout);
		mainLayout.addComponent(getFormButton());
		return mainLayout;
	}

	private Component getFormButton() {
		HorizontalLayout mainLayout = new HorizontalLayout();
		mainLayout.setSpacing(true);

		Button save = new Button($("save"));
		save.addStyleName(SAVE_BUTTON);
		save.addStyleName(ValoTheme.BUTTON_PRIMARY);
		save.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.saveButtonClicked();
			}
		});

		Button cancel = new Button($("cancel"));
		cancel.addStyleName(CANCEL_BUTTON);
		cancel.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.cancelButtonClicked();
			}
		});

		mainLayout.addComponent(save);
		mainLayout.addComponent(cancel);
		mainLayout.addStyleName(BUTTONS_LAYOUT);

		return mainLayout;
	}

	private Component getSideButtonLayout() {
		VerticalLayout verticalLayout = new VerticalLayout();

		Button up = new Button($("OrderFacetConfigurationView.up"));
		up.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				String folderId = (String) listSelect.getValue();
				if (folderId != null) {
					presenter.swap(folderId, -1);
					refreshList();
					listSelect.select(folderId);
				}
			}
		});

		Button down = new Button($("OrderFacetConfigurationView.down"));
		down.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				String folderId = (String) listSelect.getValue();
				if (folderId != null) {
					presenter.swap(folderId, 1);
					refreshList();
					listSelect.select(folderId);
				}
			}
		});

		verticalLayout.addComponent(up);
		verticalLayout.addComponent(down);

		return verticalLayout;
	}

	private void refreshList() {
		listSelect.removeAllItems();

		for (FolderDetailVO folderDetailVO : presenter.getFolderDetails()) {
			String folderId = folderDetailVO.getFolderId();
			listSelect.addItem(folderId);
			String folderTitle = presenter.getLabelForCode(folderId);
			listSelect.setItemCaption(folderId, folderTitle);
		}
	}

	private ListSelect folderList() {
		listSelect = new ListSelect("") {
			@Override
			public void select(Object itemId) {
				super.select(itemId);
				focus();
			}
		};

		listSelect.setWidth("100%");
		listSelect.setNullSelectionAllowed(false);

		refreshList();

		return listSelect;
	}
}
