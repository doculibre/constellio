package com.constellio.app.modules.rm.ui.pages.retentionRule.retentionRuleDocumentType;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.vaadin.ui.Field;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.ValoTheme;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class RecordFormWithHiddableMetadatasWindow extends BaseWindow {
	private final ConstellioFactories constellioFactories;
	private final List<MetadataVO> metadatasToHide;

	private Panel panel;

	public RecordFormWithHiddableMetadatasWindow(ConstellioFactories constellioFactories,
												 List<MetadataVO> metadatasToHide) {
		this.constellioFactories = constellioFactories;

		if (metadatasToHide != null) {
			this.metadatasToHide = new ArrayList<>(metadatasToHide);
		} else {
			this.metadatasToHide = new ArrayList<>();
		}

		panel = new Panel();
		panel.addStyleName(ValoTheme.PANEL_BORDERLESS);
		panel.addStyleName(ValoTheme.PANEL_SCROLL_INDICATOR);

		setContent(panel);

		setHeight("95%");
		setWidth("95%");
		setResizable(true);
		setModal(false);
		center();
	}

	public void show(RecordVO recordVO) {
		show(recordVO, savedRecordVO -> {
		}, cancelledRecordVO -> {
		});
	}

	public void show(RecordVO recordVO, Consumer<RecordVO> formSavedCallback) {
		show(recordVO, formSavedCallback, cancelledRecordVO -> {
		});
	}

	public void show(RecordVO recordVO, Consumer<RecordVO> formSavedCallback,
					 Consumer<RecordVO> formCancelledCallback) {
		panel.setContent(new RecordFormWithHiddableMetadatas(recordVO, constellioFactories, metadatasToHide, this::buildFormField) {
			@Override
			protected void saveButtonClicked(RecordVO viewObject) {
				close();

				if (formSavedCallback != null) {
					formSavedCallback.accept(viewObject);
				}
			}

			@Override
			protected void cancelButtonClick(RecordVO viewObject) {
				close();

				if (formCancelledCallback != null) {
					formCancelledCallback.accept(viewObject);
				}
			}
		});

		ConstellioUI.getCurrent().addWindow(this);
	}

	protected Field<?> buildFormField(RecordVO recordVO, MetadataVO metadataVO, Locale locale) {
		return null;
	}
}
