package com.constellio.app.ui.pages.management.publish;

import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.converters.JodaDateToStringConverter;
import com.constellio.app.ui.framework.components.converters.TaxonomyRecordIdToContextCaptionConverter;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.constellio.app.ui.i18n.i18n.$;

public class PublishDocumentTable implements ColumnGenerator {
	public static final String PUBLISHED_START_DATE = "publishingStartDate";
	public static final String PUBLISHED_EXPIRATION_DATE = "publishingExpirationDate";
	public static final String BUTTONS = "buttons";
	public static final String TITLE = "title";

	public enum DocumentSource {
		PUBLICATION
	}

	private DocumentSource documentSource = DocumentSource.PUBLICATION;
	private Locale currentLocale;
	private final JodaDateToStringConverter converter;

	public PublishDocumentTable(Locale currentLocale) {


		this.currentLocale = currentLocale;
		converter = new JodaDateToStringConverter();
	}

	public void attachTo(Table table, boolean negativeAuthorizationConfigEnabled) {
		String primary;
		List<String> columnIds = new ArrayList<>();

		table.addGeneratedColumn(TITLE, this);
		table.setColumnHeader(TITLE, $("AuthorizationsView.content"));
		primary = TITLE;

		table.setColumnExpandRatio(primary, 1);
		columnIds.add(primary);

		table.addGeneratedColumn(PUBLISHED_START_DATE, this);
		table.setColumnHeader(PUBLISHED_START_DATE, $("AuthorizationsView.startDate"));
		columnIds.add(PUBLISHED_START_DATE);

		table.addGeneratedColumn(PUBLISHED_EXPIRATION_DATE, this);
		table.setColumnHeader(PUBLISHED_EXPIRATION_DATE, $("AuthorizationsView.endDate"));
		columnIds.add(PUBLISHED_EXPIRATION_DATE);

		table.setColumnHeader(BUTTONS, "");
		table.setColumnWidth(BUTTONS, 80);
		columnIds.add(BUTTONS);

		table.setVisibleColumns(columnIds.toArray());
		table.setWidth("100%");
	}

	@Override
	public Object generateCell(Table source, Object itemId, Object columnId) {
		DocumentVO document = (DocumentVO) itemId;
		TaxonomyRecordIdToContextCaptionConverter taxonomyCaptionConverter = new TaxonomyRecordIdToContextCaptionConverter();
		switch ((String) columnId) {
			case PUBLISHED_START_DATE:
				LocalDate date = (LocalDate) document.get(Document.PUBLISHED_START_DATE);
				return converter.convertToPresentation(
						date, String.class, ConstellioUI.getCurrentSessionContext().getCurrentLocale());
			case PUBLISHED_EXPIRATION_DATE:
				LocalDate dateEx = (LocalDate) document.get(Document.PUBLISHED_EXPIRATION_DATE);
				return converter.convertToPresentation(
						dateEx, String.class, ConstellioUI.getCurrentSessionContext().getCurrentLocale());
			default:
				ReferenceDisplay referenceDisplay = new ReferenceDisplay(document);
				referenceDisplay.setCaption(document.getTitle());
				return referenceDisplay;
		}
	}

}