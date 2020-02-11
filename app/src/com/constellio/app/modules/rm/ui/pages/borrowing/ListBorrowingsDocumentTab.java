package com.constellio.app.modules.rm.ui.pages.borrowing;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.SessionContext;
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
		// TODO::JOLA --> Replace metadata for content_borrowing_date after the merge!
		return where(recordsServices.document.createdOn()).isLessThan(LocalDateTime.now().minus(Period.days(getBorrowingDuration())));
	}

	private int getBorrowingDuration() {
		// TODO::JOLA --> Replace config for getDocumentBorrowingDurationDays after the merge!
		return new RMConfigs(appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager()).getBorrowingDurationDays();
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
				case BORROWING_USER:
					return new ReferenceDisplay(getBorrowingUserId(recordVO));
				case BORROWING_DATE:
					String convertedJodaDate = jodaDateTimeConverter
							.convertToPresentation(getBorrowingDate(recordVO), String.class, sessionContext.getCurrentLocale());
					return new Label(convertedJodaDate);
				case BORROWING_DUE_DATE:
					convertedJodaDate = jodaDateTimeConverter
							.convertToPresentation(getBorrowingDueDate(recordVO), String.class, sessionContext.getCurrentLocale());
					return new Label(convertedJodaDate);
				case ACTIONS:
					// TODO::JOLA --> Implements
			}

			return null;
		}

		@Override
		protected boolean isOverdue(RecordVO recordVO) {
			return LocalDateTime.now().isAfter(getBorrowingDueDate(recordVO));
		}

		private String getBorrowingUserId(RecordVO recordVO) {
			return recordVO.get(recordsServices.document.contentCheckedOutBy());
		}

		private LocalDateTime getBorrowingDate(RecordVO recordVO) {
			// TODO::JOLA --> Replace metadata for content_borrowing_date after the merge!
			return recordVO.get(recordsServices.document.createdOn());
		}

		private LocalDateTime getBorrowingDueDate(RecordVO recordVO) {
			return getBorrowingDate(recordVO).plus(Period.days(getBorrowingDuration()));
		}
	}
}
