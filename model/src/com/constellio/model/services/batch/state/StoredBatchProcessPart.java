package com.constellio.model.services.batch.state;

public class StoredBatchProcessPart {

	private int index;

	private String batchProcessId;

	private String firstId;

	private String lastId;

	private boolean finished;

	private boolean started;

	public StoredBatchProcessPart(String batchProcessId, int index, String firstId, String lastId, boolean finished,
			boolean started) {
		this.batchProcessId = batchProcessId;
		this.index = index;
		this.firstId = firstId;
		this.lastId = lastId;
		this.finished = finished;
		this.started = started;
	}

	public String getBatchProcessId() {
		return batchProcessId;
	}

	public boolean isStarted() {
		return started;
	}

	public int getIndex() {
		return index;
	}

	public String getFirstId() {
		return firstId;
	}

	public String getLastId() {
		return lastId;
	}

	public boolean isFinished() {
		return finished;
	}

	public StoredBatchProcessPart whichIsInStandby() {
		return new StoredBatchProcessPart(batchProcessId, index, firstId, lastId, false, false);
	}

	public StoredBatchProcessPart whichIsStarted() {
		return new StoredBatchProcessPart(batchProcessId, index, firstId, lastId, false, true);
	}

	public StoredBatchProcessPart whichIsFinished() {
		return new StoredBatchProcessPart(batchProcessId, index, firstId, lastId, true, started);
	}

}
