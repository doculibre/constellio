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

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		RecordsFlushing that = (RecordsFlushing) o;

		if (now != that.now)
			return false;
		if (later != that.later)
			return false;
		return withinMilliseconds == that.withinMilliseconds;

	}

	@Override
	public int hashCode() {
		int result = (now ? 1 : 0);
		result = 31 * result + (later ? 1 : 0);
		result = 31 * result + withinMilliseconds;
		return result;
	}
}
