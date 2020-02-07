package com.constellio.app.modules.rm.ui.pages.borrowing;

import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.VerticalLayout;

import static com.constellio.app.ui.i18n.i18n.$;

// TODO::JOLA --> Fix document "forced return" issue
// TODO::JOLA --> Add batch return option
// TODO::JOLA --> Add batch reminder option
public class ListBorrowingsViewImpl extends BaseViewImpl implements ListBorrowingsView {
	private final ListBorrowingsPresenter presenter;

	private LookupRecordField administrativeUnitFilter;
	private CheckBox overdueFilter;
	private TabSheet tabSheet;

	public ListBorrowingsViewImpl() {
		presenter = new ListBorrowingsPresenter(this);
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

	@Override
	protected String getTitle() {
		return $("ListBorrowingsView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);

		administrativeUnitFilter = new LookupRecordField(AdministrativeUnit.SCHEMA_TYPE);
		administrativeUnitFilter.setCaption($("ListBorrowingsView.administrativeUnit"));
		administrativeUnitFilter.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				refreshTabs();
			}
		});
		mainLayout.addComponent(administrativeUnitFilter);

		overdueFilter = new CheckBox($("ListBorrowingsView.overdueFilter"));
		overdueFilter.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				refreshTabs();
			}
		});
		mainLayout.addComponent(overdueFilter);

		ListBorrowingsTab documentTab = presenter.getDocumentTab();
		ListBorrowingsTab folderTab = presenter.getFolderTab();
		ListBorrowingsTab containerTab = presenter.getContainerTab();

		tabSheet = new TabSheet();
		tabSheet.addTab(documentTab.getLayout(), documentTab.getCaption());
		tabSheet.addTab(folderTab.getLayout(), folderTab.getCaption());
		tabSheet.addTab(containerTab.getLayout(), containerTab.getCaption());
		tabSheet.addSelectedTabChangeListener(new SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				refreshTabs();
			}
		});
		mainLayout.addComponent(tabSheet);
		refreshTabs();

		return mainLayout;
	}

	private void refreshTabs() {
		Component selectedTab = tabSheet.getSelectedTab();
		String selectedAdministrativeUnit = (String) administrativeUnitFilter.getValue();
		boolean showOnlyOverdue = overdueFilter.getValue();
		presenter.refreshTabs(selectedTab, selectedAdministrativeUnit, showOnlyOverdue);
	}
}
