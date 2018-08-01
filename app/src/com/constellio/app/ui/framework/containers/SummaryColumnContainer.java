package com.constellio.app.ui.framework.containers;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.SummaryColumnVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.IconButton;
import com.constellio.app.ui.framework.data.SummaryColumnDataProvider;
import com.constellio.app.ui.pages.summarycolumn.SummaryColumnParams;
import com.constellio.app.ui.pages.summarycolumn.SummaryColumnView;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.server.ThemeResource;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class SummaryColumnContainer extends DataContainer<SummaryColumnDataProvider> {
	public static final String UP = "up";
	public static final String DOWN = "down";
	public static final String METADATA_VO = "metadataVo";
	public static final String PREFIX = "prefix";
	public static final String DISPLAY_CONDITION = "displayCondition";
	public static final String REFERENCE_METADATA_DISPLAY = "referenceMetadataDisplay";
	public static final String MODIFY = "modify";
	public static final String DELETE = "delete";

	SummaryColumnView view;

	public SummaryColumnContainer(SummaryColumnDataProvider dataProvider, SummaryColumnView summaryColumnView) {
		super(dataProvider);
		this.view = summaryColumnView;
	}

	@Override
	protected void populateFromData(SummaryColumnDataProvider dataProvider) {
		for (SummaryColumnVO summaryColumnVO : dataProvider.getSummaryColumnVOs()) {
			addItem(summaryColumnVO);
		}
	}

	@Override
	protected Collection<?> getOwnContainerPropertyIds() {
		List<String> containerPropertyIds = new ArrayList<>();
		containerPropertyIds.add(UP);
		containerPropertyIds.add(DOWN);
		containerPropertyIds.add(METADATA_VO);
		containerPropertyIds.add(PREFIX);
		containerPropertyIds.add(DISPLAY_CONDITION);
		containerPropertyIds.add(REFERENCE_METADATA_DISPLAY);
		containerPropertyIds.add(MODIFY);
		containerPropertyIds.add(DELETE);


		return containerPropertyIds;
	}

	@Override
	protected Class<?> getOwnType(Object propertyId) {
		Class<?> type;
		if (UP.equals(propertyId)) {
			type = BaseButton.class;
		} else if (DOWN.equals(propertyId)) {
			type = BaseButton.class;
		} else if (METADATA_VO.equals(propertyId)) {
			type = MetadataVO.class;
		} else if (PREFIX.equals(propertyId)) {
			type = String.class;
		} else if (DISPLAY_CONDITION.equals(propertyId)) {
			type = SummaryColumnParams.DisplayCondition.class;
		} else if (REFERENCE_METADATA_DISPLAY.equals(propertyId)) {
			type = SummaryColumnParams.ReferenceMetadataDisplay.class;
		} else if (MODIFY.equals(propertyId)) {
			type = BaseButton.class;
		} else if (DELETE.equals(propertyId)) {
			type = BaseButton.class;
		} else {
			throw new IllegalArgumentException("Invalid propertyId : " + propertyId);
		}

		return type;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	protected Property<?> getOwnContainerProperty(Object itemId, Object propertyId) {
		final SummaryColumnVO summaryColumnVOItemId = (SummaryColumnVO) itemId;
		final SummaryColumnDataProvider summaryColumnDataProvider = getDataProvider();
		Object value;

		if (UP.equals(propertyId)) {

			value = new IconButton(new ThemeResource("images/icons/actions/arrow_up_blue.png"), $("UP"), true) {

				@Override
				protected void buttonClick(ClickEvent event) {
					if (!view.getSummaryColumnPresenter().isReindextionFlag()) {
						view.showReindexationWarningIfRequired(new ConfirmDialog.Listener() {
							@Override
							public void onClose(ConfirmDialog dialog) {
								if (dialog.isConfirmed()) {
									moveRowUp(summaryColumnDataProvider, summaryColumnVOItemId);
								}
							}
						});
					} else {
						moveRowUp(summaryColumnDataProvider, summaryColumnVOItemId);
					}
				}
			};
		} else if (DOWN.equals(propertyId)) {
			value = new IconButton(new ThemeResource("images/icons/actions/arrow_down_blue.png"), $("DOWN"), true) {
				@Override
				protected void buttonClick(ClickEvent event) {
					if (!view.getSummaryColumnPresenter().isReindextionFlag()) {
						view.showReindexationWarningIfRequired(new ConfirmDialog.Listener() {
							@Override
							public void onClose(ConfirmDialog dialog) {
								if (dialog.isConfirmed()) {
									moveRowDown(summaryColumnDataProvider, summaryColumnVOItemId);
								}
							}
						});
					} else {
						moveRowDown(summaryColumnDataProvider, summaryColumnVOItemId);
					}
				}
			};
		} else if (METADATA_VO.equals(propertyId)) {
			value = summaryColumnVOItemId.getMetadataVO();
		} else if (PREFIX.equals(propertyId)) {
			value = summaryColumnVOItemId.getPrefix();
		} else if (DISPLAY_CONDITION.equals(propertyId)) {
			value = summaryColumnVOItemId.isAlwaysShown() ? SummaryColumnParams.DisplayCondition.ALWAYS : SummaryColumnParams.DisplayCondition.COMPLETED;
		} else if (REFERENCE_METADATA_DISPLAY.equals(propertyId)) {
			if (summaryColumnVOItemId.getReferenceMetadataDisplay() != null) {
				value = SummaryColumnParams.ReferenceMetadataDisplay
						.fromInteger(summaryColumnVOItemId.getReferenceMetadataDisplay());
			} else {
				value = null;
			}

		} else if (MODIFY.equals(propertyId)) {
			value = new IconButton(EditButton.ICON_RESOURCE, $("edit"), true) {
				@Override
				protected void buttonClick(ClickEvent event) {
					view.alterSummaryMetadata(summaryColumnVOItemId);
				}
			};
		} else if (DELETE.equals(propertyId)) {
			value = new IconButton(new ThemeResource("images/icons/actions/delete.png"), $("delete"), true) {
				@Override
				protected void buttonClick(ClickEvent event) {
					view.deleteRow(summaryColumnVOItemId);
				}
			};
		} else {
			throw new IllegalArgumentException("Invalid propertyId : " + propertyId);
		}
		Class<?> type = getType(propertyId);
		return new ObjectProperty(value, type);
	}

	private void moveRowUp(SummaryColumnDataProvider summaryColumnDataProvider, SummaryColumnVO summaryColumnVOItemId) {
		List<SummaryColumnVO> summaryColumnVOS = summaryColumnDataProvider.getSummaryColumnVOs();
		int index = summaryColumnVOS.indexOf(summaryColumnVOItemId);

		if (index <= 0) {
			return;
		}

		view.getSummaryColumnPresenter().moveMetadataUp(summaryColumnVOItemId.getMetadataVO().getCode());
		SummaryColumnVO summaryColumnVO = summaryColumnDataProvider.removeSummaryColumnVO(index);
		summaryColumnDataProvider.addSummaryColumnVO(index - 1, summaryColumnVO);
		summaryColumnDataProvider.fireDataRefreshEvent();
	}

	private void moveRowDown(SummaryColumnDataProvider summaryColumnDataProvider,
							 SummaryColumnVO summaryColumnVOItemId) {
		List<SummaryColumnVO> summaryColumnVOS = summaryColumnDataProvider.getSummaryColumnVOs();
		int index = summaryColumnVOS.indexOf(summaryColumnVOItemId);

		if (index >= (summaryColumnVOS.size() - 1)) {
			return;
		}

		view.getSummaryColumnPresenter().moveMetadataDown(summaryColumnVOItemId.getMetadataVO().getCode());
		SummaryColumnVO summaryColumnVO = summaryColumnDataProvider.removeSummaryColumnVO(index);
		summaryColumnDataProvider.addSummaryColumnVO(index + 1, summaryColumnVO);
		summaryColumnDataProvider.fireDataRefreshEvent();
	}


}
