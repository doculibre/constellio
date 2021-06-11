package com.constellio.app.modules.tasks.model.wrappers.structures;

import com.constellio.model.entities.schemas.CombinedStructureFactory;
import com.constellio.model.entities.schemas.ModifiableStructure;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDate;

import java.io.IOException;

import static com.constellio.model.entities.records.wrappers.structure.StructureFactoryUtils.newLocalDateJsonSerializerDeserializer;

public class TaskReminderFactory implements CombinedStructureFactory {
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
