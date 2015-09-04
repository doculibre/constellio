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
package com.constellio.app.modules.tasks.model.wrappers.structures;

import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.entities.schemas.StructureFactory;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDate;

import static com.constellio.model.entities.records.wrappers.structure.StructureFactoryUtils.newLocalDateJsonSerializerDeserializer;

import java.io.IOException;

public class TaskReminderFactory implements StructureFactory {
	transient private GsonBuilder gsonBuilder;
	transient private Gson gson;

	public TaskReminderFactory() {
		initTransientObjects();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		ExclusionStrategy strategy = new ExclusionStrategy() {
			@Override
			public boolean shouldSkipField(FieldAttributes f) {
				if (f.getName().equals("dirty")) {
					return true;
				}
				return false;
			}

			@Override
			public boolean shouldSkipClass(Class<?> clazz) {
				return false;
			}
		};
		gsonBuilder = new GsonBuilder().setExclusionStrategies(strategy)
				.registerTypeAdapter(LocalDate.class, newLocalDateJsonSerializerDeserializer());
		gson = gsonBuilder.create();
	}

	@Override
	public String toString(ModifiableStructure structure) {
		return gson.toJson(structure);
	}

	@Override
	public ModifiableStructure build(String serializedCriterion) {
		TaskReminder returnReminder = new TaskReminder();
		if (StringUtils.isNotBlank(serializedCriterion)) {
			returnReminder = gson.fromJson(serializedCriterion, TaskReminder.class);
			returnReminder = rebuildTaskReminder(returnReminder);
		}
		return returnReminder;
	}

	public TaskReminder rebuildTaskReminder(TaskReminder taskReminder) {

		LocalDate fixedDate = taskReminder.getFixedDate();
		Integer numberOfDaysInFlexibleDate = taskReminder.getNumberOfDaysToRelativeDate();
		Boolean beforeFlexibleDate = taskReminder.isBeforeRelativeDate();
		String flexibleDateMetadataCode = taskReminder.getRelativeDateMetadataCode();
		boolean processed = taskReminder.isProcessed();

		return new TaskReminder().setBeforeRelativeDate(beforeFlexibleDate).setFixedDate(fixedDate)
				.setRelativeDateMetadataCode(flexibleDateMetadataCode)
				.setNumberOfDaysToRelativeDate(numberOfDaysInFlexibleDate).setProcessed(processed)
				.setDirty(false);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

}
