package com.constellio.model.entities.batchprocess;

public class AsyncTaskCreationRequest {

	AsyncTask task;

	String collection;

	String title;

	String username;

	boolean inStandby = false;

	public AsyncTaskCreationRequest(AsyncTask task, String collection, String title) {
		this.task = task;
		this.collection = collection;
		this.title = title;
	}

	public AsyncTask getTask() {
		return task;
	}

	public String getCollection() {
		return collection;
	}

	public String getTitle() {
		return title;
	}

	public String getUsername() {
		return username;
	}

	public AsyncTaskCreationRequest setUsername(String username) {
		this.username = username;
		return this;
	}

	public boolean isInStandby() {
		return inStandby;
	}

	public AsyncTaskCreationRequest setInStandby(boolean inStandby) {
		this.inStandby = inStandby;
		return this;
	}
}
