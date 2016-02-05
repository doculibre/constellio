package com.constellio.data.dao.services.solr;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;

public class DateUtils {

	public static LocalDateTime correctDate(LocalDateTime localDateTime) {
		int offsetMillis = -1 * (DateTimeZone.getDefault().getOffset(localDateTime.toDate().getTime()));
		return localDateTime.plusMillis(offsetMillis);
	}

}
