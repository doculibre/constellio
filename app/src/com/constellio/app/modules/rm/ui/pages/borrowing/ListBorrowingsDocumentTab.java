package com.constellio.app.modules.rm.ui.pages.borrowing;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;

import static com.constellio.model.services.contents.ContentFactory.checkedOut;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;

public class ListBorrowingsDocumentTab extends ListBorrowingsTab {
	public ListBorrowingsDocumentTab(AppLayerFactory appLayerFactory, SessionContext sessionContext) {
		super(appLayerFactory, sessionContext);
	}

	@Override
	protected MetadataSchemaType getSchemaType() {
		return recordsServices.documentSchemaType();
	}

	@Override
	protected LogicalSearchCondition getCheckedOutCondition() {
		return where(recordsServices.document.content()).is(checkedOut());
	}

	@Override
	protected LogicalSearchCondition getAdministrativeUnitCondition(String administrativeUnit) {
		return where(recordsServices.document.administrativeUnit()).isEqualTo(administrativeUnit);
	}

	@Override
	protected LogicalSearchCondition getOverdueCondition() {
		return where(getContentCheckedOutDateMetadata()).isLessThan(TimeProvider.getLocalDateTime().minus(Period.days(getBorrowingDuration())));
	}

	private Metadata getContentCheckedOutDateMetadata() {
		return recordsServices.defaultDocumentSchema().getMetadata(Document.CONTENT_CHECKED_OUT_DATE);
	}

	private int getBorrowingDuration() {
		return new RMConfigs(appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager()).getDocumentBorrowingDurationDays();
	}

	@Override
	protected SelectionTable buildViewableRecordItemTable(RecordVODataProvider dataProvider) {
		return new DocumentViewableRecordItemTable(dataProvider);
	}

	private class DocumentViewableRecordItemTable extends ViewableRecordItemTable {
		private DocumentViewableRecordItemTable(RecordVODataProvider dataProvider) {
			super(dataProvider);
		}

		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			RecordVO recordVO = ((RecordVOItem) source.getItem(itemId)).getRecord();

			switch ((String) columnId) {
				case BORROWING_DATE:
					String convertedJodaDate = jodaDateTimeConverter
							.convertToPresentation(getBorrowingDate(recordVO), String.class, sessionContext.getCurrentLocale());
					return new Label(convertedJodaDate);
				case BORROWING_DUE_DATE:
					convertedJodaDate = jodaDateTimeConverter
							.convertToPresentation(getBorrowingDueDate(recordVO), String.class, sessionContext.getCurrentLocale());
					return new Label(convertedJodaDate);
			}

			return super.generateCell(source, itemId, columnId);
		}

		@Override
		protected boolean isOverdue(RecordVO recordVO) {
			return TimeProvider.getLocalDateTime().isAfter(getBorrowingDueDate(recordVO));
		}

		@Override
		protected String getBorrowingUserId(RecordVO recordVO) {
			return recordVO.get(recordsServices.document.contentCheckedOutBy());
		}

		private LocalDateTime getBorrowingDate(RecordVO recordVO) {
			return recordVO.get(getContentCheckedOutDateMetadata());
		}

		private LocalDateTime getBorrowingDueDate(RecordVO recordVO) {
			return getBorrowingDate(recordVO).plus(Period.days(getBorrowingDuration()));
		}
	}
}
