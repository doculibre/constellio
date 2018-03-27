package com.constellio.data.dao.dto.records;

public class RecordsFlushing {

	public static RecordsFlushing NOW = new RecordsFlushing(true, false, false, -1);
	public static RecordsFlushing LATER = new RecordsFlushing(false, false, true, -1);
	public static RecordsFlushing ADD_LATER = new RecordsFlushing(false, true, true, -1);
	boolean now;
	boolean addLater;
	boolean flushLater;

	int withinMilliseconds;

	private RecordsFlushing(boolean now, boolean addLater, boolean flushLater, int withinMilliseconds) {
		this.now = now;
		this.addLater = addLater;
		this.flushLater = flushLater;
		this.withinMilliseconds = withinMilliseconds;
	}

	public static RecordsFlushing NOW() {
		return NOW;
	}

	public static RecordsFlushing LATER() {
		return LATER;
	}

	public static RecordsFlushing ADD_LATER() {
		return ADD_LATER;
	}

	public static RecordsFlushing WITHIN_MILLISECONDS(int withinMilliseconds) {
		return new RecordsFlushing(false, false, false, withinMilliseconds);
	}

	public static RecordsFlushing WITHIN_SECONDS(int withinSeconds) {
		return new RecordsFlushing(false, false, false, withinSeconds * 1000);
	}

	public static RecordsFlushing WITHIN_MINUTES(int withinSeconds) {
		return new RecordsFlushing(false, false, false, withinSeconds * 1000 * 60);
	}

	public int getWithinMilliseconds() {
		return withinMilliseconds;
	}

	@Override
	public String toString() {
		if (now) {
			return "NOW()";

		} else if (addLater) {
			return "ADD_LATER()";

		} else if (flushLater) {
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
		if (addLater != that.addLater)
			return false;
		if (flushLater != that.flushLater)
			return false;
		return withinMilliseconds == that.withinMilliseconds;

	}

	@Override
	public int hashCode() {
		int result = (now ? 1 : 0);
		result = 31 * result + (addLater ? 1 : 0);
		result = 31 * result + (flushLater ? 1 : 0);
		result = 31 * result + withinMilliseconds;
		return result;
	}
}
