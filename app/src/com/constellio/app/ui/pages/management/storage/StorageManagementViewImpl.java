package com.constellio.app.ui.pages.management.storage;


import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.google.common.base.Strings;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.collections.CollectionUtils;

import java.util.Arrays;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class StorageManagementViewImpl extends BaseViewImpl implements StorageManagementView {

	StorageManagementPresenter presenter;

    TextField primaryMountPointField;

    TextField secondaryMountPointField;

	public StorageManagementViewImpl() {
		super();

		presenter = new StorageManagementPresenter(this);
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout verticalLayout = new VerticalLayout();

        verticalLayout.setSizeFull();
        verticalLayout.setSpacing(true);

        buildReplicatedVaultMountPointFields(verticalLayout);

        buildSaveButton(verticalLayout);

		return verticalLayout;
	}

    private void buildReplicatedVaultMountPointFields(VerticalLayout layout) {
        HorizontalLayout hlayout = new HorizontalLayout();
        hlayout.setSizeFull();
        layout.addComponent(hlayout);

        hlayout.addComponent(new Label($("StorageManagementView.mountPoints")));

        VerticalLayout vlayout = new VerticalLayout();

        primaryMountPointField = new TextField();
        primaryMountPointField.setSizeFull();
        vlayout.addComponent(primaryMountPointField);

        secondaryMountPointField = new TextField();
        secondaryMountPointField.setSizeFull();
        vlayout.addComponent(secondaryMountPointField);

        hlayout.addComponent(vlayout);

        List<String> replicatedVaultMountPointList = presenter.getReplicatedVaultMountPoints();

        if (!CollectionUtils.isEmpty(replicatedVaultMountPointList)) {
            primaryMountPointField.setValue(replicatedVaultMountPointList.get(0));
            secondaryMountPointField.setValue(replicatedVaultMountPointList.get(1));
        }
    }

    private void buildSaveButton(VerticalLayout verticalLayout) {
        Button saveButton = new BaseButton($("save")) {
            @Override
            protected void buttonClick(ClickEvent event) {
                if (Strings.isNullOrEmpty(primaryMountPointField.getValue()) ^ Strings.isNullOrEmpty(secondaryMountPointField.getValue())) {
                    showErrorMessage($("missing entry for replicated vault mount points"));
                } else if (!(Strings.isNullOrEmpty(primaryMountPointField.getValue()) || Strings.isNullOrEmpty(secondaryMountPointField.getValue()))) {
                    presenter.saveConfigurations(Arrays.asList(primaryMountPointField.getValue(), secondaryMountPointField.getValue()));
                }
            }
        };
        saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

        verticalLayout.addComponent(saveButton);
        verticalLayout.setComponentAlignment(saveButton, Alignment.BOTTOM_RIGHT);
    }

	@Override
	protected String getTitle() {
		return $("StorageManagementView.viewTitle");
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonClick();
			}
		};
	}
}
