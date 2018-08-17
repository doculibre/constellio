package com.constellio.app.ui.framework.containers;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.SummaryConfigElementVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.IconButton;
import com.constellio.app.ui.framework.data.SummaryConfigDataProvider;
import com.constellio.app.ui.pages.summaryconfig.SummaryConfigParams;
import com.constellio.app.ui.pages.summaryconfig.SummaryConfigView;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.server.ThemeResource;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class SummaryConfigContainer extends DataContainer<SummaryConfigDataProvider> {
	public static final String UP = "up";
	public static final String DOWN = "down";
	public static final String METADATA_VO = "metadataVo";
	public static final String PREFIX = "prefix";
	public static final String DISPLAY_CONDITION = "displayCondition";
	public static final String REFERENCE_METADATA_DISPLAY = "referenceMetadataDisplay";
	public static final String MODIFY = "modify";
	public static final String DELETE = "delete";

	SummaryConfigView view;

	public SummaryConfigContainer(SummaryConfigDataProvider dataProvider, SummaryConfigView summaryConfigView) {
		super(dataProvider);
		this.view = summaryConfigView;
	}

	@Override
	protected void populateFromData(SummaryConfigDataProvider dataProvider) {
		for (SummaryConfigElementVO summaryConfigElementVO : dataProvider.getSummaryConfigElementVOS()) {
			addItem(summaryConfigElementVO);
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
			type = SummaryConfigParams.DisplayCondition.class;
		} else if (REFERENCE_METADATA_DISPLAY.equals(propertyId)) {
			type = SummaryConfigParams.ReferenceMetadataDisplay.class;
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
		final SummaryConfigElementVO summaryConfigElementVOItemId = (SummaryConfigElementVO) itemId;
		final SummaryConfigDataProvider summaryConfigDataProvider = getDataProvider();
		Object value;

		if (UP.equals(propertyId)) {

			value = new IconButton(new ThemeResource("images/icons/actions/arrow_up_blue.png"), $("UP"), true) {

				@Override
				protected void buttonClick(ClickEvent event) {
					if (!view.getSummaryConfigPresenter().isReindextionFlag()) {
						view.showReindexationWarningIfRequired(new ConfirmDialog.Listener() {
							@Override
							public void onClose(ConfirmDialog dialog) {
								if (dialog.isConfirmed()) {
									moveRowUp(summaryConfigDataProvider, summaryConfigElementVOItemId);
								}
							}
						});
					} else {
						moveRowUp(summaryConfigDataProvider, summaryConfigElementVOItemId);
					}
				}
			};
		} else if (DOWN.equals(propertyId)) {
			value = new IconButton(new ThemeResource("images/icons/actions/arrow_down_blue.png"), $("DOWN"), true) {
				@Override
				protected void buttonClick(ClickEvent event) {
					if (!view.getSummaryConfigPresenter().isReindextionFlag()) {
						view.showReindexationWarningIfRequired(new ConfirmDialog.Listener() {
							@Override
							public void onClose(ConfirmDialog dialog) {
								if (dialog.isConfirmed()) {
									moveRowDown(summaryConfigDataProvider, summaryConfigElementVOItemId);
								}
							}
						});
					} else {
						moveRowDown(summaryConfigDataProvider, summaryConfigElementVOItemId);
					}
				}
			};
		} else if (METADATA_VO.equals(propertyId)) {
			value = summaryConfigElementVOItemId.getMetadataVO();
		} else if (PREFIX.equals(propertyId)) {
			value = summaryConfigElementVOItemId.getPrefix();
		} else if (DISPLAY_CONDITION.equals(propertyId)) {
			value = summaryConfigElementVOItemId.isAlwaysShown() ? SummaryConfigParams.DisplayCondition.ALWAYS : SummaryConfigParams.DisplayCondition.COMPLETED;
		} else if (REFERENCE_METADATA_DISPLAY.equals(propertyId)) {
			if (summaryConfigElementVOItemId.getReferenceMetadataDisplay() != null) {
				value = SummaryConfigParams.ReferenceMetadataDisplay
						.fromInteger(summaryConfigElementVOItemId.getReferenceMetadataDisplay());
			} else {
				value = null;
			}

		} else if (MODIFY.equals(propertyId)) {
			value = new IconButton(EditButton.ICON_RESOURCE, $("edit"), true) {
				@Override
				protected void buttonClick(ClickEvent event) {
					view.alterSummaryMetadata(summaryConfigElementVOItemId);
				}
			};
		} else if (DELETE.equals(propertyId)) {
			value = new IconButton(new ThemeResource("images/icons/actions/delete.png"), $("delete"), true) {
				@Override
				protected void buttonClick(ClickEvent event) {
					view.deleteRow(summaryConfigElementVOItemId);
				}
			};
		} else {
			throw new IllegalArgumentException("Invalid propertyId : " + propertyId);
		}
		Class<?> type = getType(propertyId);
		return new ObjectProperty(value, type);
	}

	private void moveRowUp(SummaryConfigDataProvider summaryConfigDataProvider,
						   SummaryConfigElementVO summaryConfigElementVOItemId) {
		List<SummaryConfigElementVO> summaryConfigElementVOS = summaryConfigDataProvider.getSummaryConfigElementVOS();
		int index = summaryConfigElementVOS.indexOf(summaryConfigElementVOItemId);

		if (index <= 0) {
			return;
		}

		view.getSummaryConfigPresenter().moveMetadataUp(summaryConfigElementVOItemId.getMetadataVO().getCode());
		SummaryConfigElementVO summaryConfigElementVO = summaryConfigDataProvider.removeSummaryConfigItemVO(index);
		summaryConfigDataProvider.addSummaryConfigItemVO(index - 1, summaryConfigElementVO);
		summaryConfigDataProvider.fireDataRefreshEvent();
	}

	private void moveRowDown(SummaryConfigDataProvider summaryConfigDataProvider,
							 SummaryConfigElementVO summaryConfigElementVOItemId) {
		List<SummaryConfigElementVO> summaryConfigElementVOS = summaryConfigDataProvider.getSummaryConfigElementVOS();
		int index = summaryConfigElementVOS.indexOf(summaryConfigElementVOItemId);

		if (index >= (summaryConfigElementVOS.size() - 1)) {
			return;
		}

		view.getSummaryConfigPresenter().moveMetadataDown(summaryConfigElementVOItemId.getMetadataVO().getCode());
		SummaryConfigElementVO summaryConfigElementVO = summaryConfigDataProvider.removeSummaryConfigItemVO(index);
		summaryConfigDataProvider.addSummaryConfigItemVO(index + 1, summaryConfigElementVO);
		summaryConfigDataProvider.fireDataRefreshEvent();
	}


}
