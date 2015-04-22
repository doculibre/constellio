/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.data.dao.dto.records;

public class RecordsFlushing {

	public static RecordsFlushing NOW = new RecordsFlushing(true, false, -1);
	public static RecordsFlushing LATER = new RecordsFlushing(false, true, -1);
	boolean now;
	boolean later;
	int withinMilliseconds;

	private RecordsFlushing(boolean now, boolean later, int withinMilliseconds) {
		this.now = now;
		this.later = later;
		this.withinMilliseconds = withinMilliseconds;
	}

	public static RecordsFlushing NOW() {
		return NOW;
	}

	public static RecordsFlushing LATER() {
		return LATER;
	}

	public static RecordsFlushing WITHIN_MILLISECONDS(int withinMilliseconds) {
		return new RecordsFlushing(false, false, withinMilliseconds);
	}

	public static RecordsFlushing WITHIN_SECONDS(int withinSeconds) {
		return new RecordsFlushing(false, false, withinSeconds * 1000);
	}

	public static RecordsFlushing WITHIN_MINUTES(int withinSeconds) {
		return new RecordsFlushing(false, false, withinSeconds * 1000 * 60);
	}

	public int getWithinMilliseconds() {
		return withinMilliseconds;
	}

	@Override
	public String toString() {
		if (now) {
			return "NOW()";

		} else if (later) {
			return "LATER()";

		} else {
			return "WITHIN_MILLISECONDS(" + withinMilliseconds + ")";
		}
	}

}
