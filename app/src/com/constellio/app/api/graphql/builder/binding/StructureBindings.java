package com.constellio.app.api.graphql.builder.binding;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleFactory;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.model.entities.schemas.StructureFactory;
import com.google.common.collect.ImmutableMap;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

public class StructureBindings {

	public static Map<Class<?>, Class<?>> structureByStructureFactory =
			ImmutableMap.of(CopyRetentionRuleFactory.class, CopyRetentionRule.class);
	public static Map<Class<?>, Map<String, String>> referenceFieldsByStructure =
			ImmutableMap.of(CopyRetentionRule.class, ImmutableMap.of("mediumTypeIds", MediumType.SCHEMA_TYPE));

	public static Class<?> getStructure(StructureFactory structureFactory) {
		return structureByStructureFactory.get(structureFactory.getClass());
	}

	public static Map<String, String> getReferenceFields(Class<?> structureClass) {
		return referenceFieldsByStructure.get(structureClass);
	}

	public static boolean isReference(Field field) {
		return referenceFieldsByStructure.get(field.getDeclaringClass()).containsKey(field.getName());
	}

	public static String getFieldReferenceSchemaType(Field field) {
		return referenceFieldsByStructure.get(field.getDeclaringClass()).get(field.getName());
	}

	public static Collection<Class<?>> getSupportedStructures() {
		return structureByStructureFactory.values();
	}
}
