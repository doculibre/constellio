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
package com.constellio.data.threads;

import org.joda.time.Duration;
import org.joda.time.LocalTime;

public class BackgroundThreadConfiguration {

	private String id;

	private Runnable repeatedAction;

	private Duration executeEvery;

	private LocalTime from;

	private LocalTime to;

	private BackgroundThreadExceptionHandling exceptionHandling = BackgroundThreadExceptionHandling.STOP;

	private BackgroundThreadConfiguration(String id, Runnable repeatedAction) {
		this.id = id;
		this.repeatedAction = repeatedAction;
	}

	public static BackgroundThreadConfiguration repeatingAction(String id, Runnable repeatedAction) {
		return new BackgroundThreadConfiguration(id, repeatedAction);
	}

	public BackgroundThreadConfiguration executedEvery(Duration duration) {
		this.executeEvery = duration;
		return this;
	}

	public BackgroundThreadConfiguration between(LocalTime from, LocalTime to) {
		this.from = from;
		this.to = to;
		return this;
	}

	public BackgroundThreadConfiguration handlingExceptionWith(BackgroundThreadExceptionHandling exceptionHandling) {
		this.exceptionHandling = exceptionHandling;
		return this;
	}

	public Runnable getRepeatedAction() {
		return repeatedAction;
	}

	public Duration getExecuteEvery() {
		return executeEvery;
	}

	public LocalTime getFrom() {
		return from;
	}

	public LocalTime getTo() {
		return to;
	}

	public String getId() {
		return id;
	}

	public BackgroundThreadExceptionHandling getExceptionHandling() {
		return exceptionHandling;
	}
}
