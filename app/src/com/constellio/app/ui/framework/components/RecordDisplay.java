package com.constellio.app.ui.framework.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.fields.comment.RecordCommentsDisplayImpl;
import com.constellio.app.ui.framework.components.fields.comment.RecordCommentsEditorImpl;
import com.constellio.app.ui.util.SchemaVOUtils;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class RecordDisplay extends BaseDisplay {
	public static final String STYLE_NAME = "record-display";
	private RecordVO recordVO;
	private MetadataDisplayFactory metadataDisplayFactory;


	public RecordDisplay(RecordVO recordVO) {
		this(recordVO, new MetadataDisplayFactory(), STYLE_NAME);
	}


	public RecordDisplay(RecordVO recordVO, MetadataDisplayFactory metadataDisplayFactory) {
		this(recordVO, metadataDisplayFactory, STYLE_NAME);
	}

	public RecordDisplay(RecordVO recordVO, MetadataDisplayFactory metadataDisplayFactory, String styleName) {
		super(toCaptionsAndComponents(recordVO, metadataDisplayFactory));
		this.recordVO = recordVO;
		this.metadataDisplayFactory = metadataDisplayFactory;
		addStyleName(styleName);
	}

	private static List<CaptionAndComponent> toCaptionsAndComponents(RecordVO recordVO,
																	 MetadataDisplayFactory metadataDisplayFactory) {
		List<CaptionAndComponent> captionsAndComponents = new ArrayList<CaptionAndComponent>();

		Locale locale = ConstellioUI.getCurrentSessionContext().getCurrentLocale();
		for (MetadataValueVO metadataValue : recordVO.getDisplayMetadataValues()) {
			if (SchemaVOUtils.isMetadataNotPresentInList(metadataValue.getMetadata(), recordVO.getExcludedMetadataCodeList())) {
				Component displayComponent = metadataDisplayFactory.build(recordVO, metadataValue);
				if (displayComponent != null) {
					MetadataVO metadata = metadataValue.getMetadata();
					String caption = metadata.getLabel(locale);
					Label captionLabel = new Label(caption);

					String captionId = STYLE_CAPTION + "-" + metadata.getCode();
					captionLabel.setId(captionId);
					captionLabel.addStyleName(captionId);
					captionLabel.setVisible(displayComponent.isVisible());

					String valueId = STYLE_VALUE + "-" + metadata.getCode();
					displayComponent.setId(valueId);
					displayComponent.addStyleName(valueId);

					captionsAndComponents.add(new CaptionAndComponent(captionLabel, displayComponent));
				}
			}
		}
		return captionsAndComponents;
	}

	public final RecordVO getRecordVO() {
		return recordVO;
	}

	public final void setRecordVO(RecordVO recordVO) {
		this.recordVO = recordVO;
	}
	
	private Component commentsComponent;

	@Override
	protected void addCaptionAndDisplayComponent(Label captionLabel, Component displayComponent) {
		if ((displayComponent instanceof RecordCommentsEditorImpl) || (displayComponent instanceof RecordCommentsDisplayImpl)) {
			VerticalLayout verticalLayout = new VerticalLayout(displayComponent);
			verticalLayout.addStyleName("record-comments-layout");
			verticalLayout.setWidth("100%");
			verticalLayout.setSpacing(true);
			verticalLayout.addStyleName("record-comments-editor");
			mainLayout.addComponent(commentsComponent = verticalLayout);
		} else {
			if (commentsComponent != null) {
				mainLayout.removeComponent(commentsComponent);
			}
			super.addCaptionAndDisplayComponent(captionLabel, displayComponent);
			if (commentsComponent != null) {
				mainLayout.addComponent(commentsComponent);
			}
		}
	}

	public void refresh() {
		setCaptionsAndComponents(toCaptionsAndComponents(this.recordVO, metadataDisplayFactory));
	}

}
