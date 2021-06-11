package com.constellio.app.services.menu;

import com.constellio.model.entities.records.Record;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.DISABLED;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.HIDDEN;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.VISIBLE;
import static com.constellio.app.ui.i18n.i18n.$;

public class MenuItemUtil {
	public static MenuItemActionState calculateCorrectActionState(int possibleCount, int notPossibleCount,
																  String reason) {
		return calculateCorrectActionState(possibleCount, notPossibleCount, reason, ActionDisplayOption.REQUIRE_VISIBLE_FOR_ALL_RECORDS);
	}

	public static MenuItemActionState calculateCorrectActionState(int possibleCount, int notPossibleCount,
																  String reason,
																  ActionDisplayOption displayOption) {
		if (possibleCount > 0 && (displayOption == ActionDisplayOption.REQUIRE_VISIBLE_FOR_ONE_RECORD || notPossibleCount == 0)) {
			return new MenuItemActionState(VISIBLE);
		} else if (possibleCount == 0 && notPossibleCount > 0) {
			return new MenuItemActionState(HIDDEN, reason);
		}
		return new MenuItemActionState(DISABLED, reason);
	}


	public static MenuItemActionState testLimitAndSchemaType(List<String> schemaTypes, int limit,
															 List<Record> records) {
		if (records.isEmpty()) {
			return new MenuItemActionState(DISABLED, $("RMRecordsMenuItemServices.noRecordSelected"));
		}

		if (records.size() > limit) {
			return new MenuItemActionState(DISABLED, $("RMRecordsMenuItemServices.recordsLimitReached", String.valueOf(limit)));
		}
		long recordWithSupportedSchemaTypeCount = getRecordWithSupportedSchemaTypeCount(records, schemaTypes);
		if (recordWithSupportedSchemaTypeCount == 0) {
			return new MenuItemActionState(HIDDEN);
		} else if (recordWithSupportedSchemaTypeCount != records.size()) {
			return new MenuItemActionState(HIDDEN, $("RMRecordsMenuItemServices.unsupportedSchema",
					StringUtils.join(schemaTypes, ",")));
		}

		return null;
	}

	public static long getRecordWithSupportedSchemaTypeCount(List<Record> records, List<String> schemaTypes) {
		return records.stream()
				.filter(r -> schemaTypes.contains(getSchemaType(r)))
				.count();
	}

	public static String getSchemaType(Record record) {
		return record.getSchemaCode().substring(0, record.getSchemaCode().indexOf("_"));
	}
}
