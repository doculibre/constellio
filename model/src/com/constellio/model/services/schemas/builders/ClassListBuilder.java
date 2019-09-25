package com.constellio.model.services.schemas.builders;

import com.constellio.model.utils.ClassProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClassListBuilder<T> {

	private static Map<String, Object> cache = new HashMap<>();

	Class<?> implementedClass;

	Set<String> implementationsClassname = new HashSet<>();

	private ClassProvider classProvider;

	public ClassListBuilder(ClassProvider classProvider, Class<?> implementedClass) {
		this.classProvider = classProvider;
		this.implementedClass = implementedClass;
	}

	public ClassListBuilder(ClassProvider classProvider, Class<?> implementedClass, Set<T> implementations) {
		this.implementedClass = implementedClass;
		this.classProvider = classProvider;
		for (T implementation : implementations) {
			this.implementationsClassname.add(implementation.getClass().getName());
		}
	}

	public ClassListBuilder<T> add(String name) {
		this.implementationsClassname.add(name);
		return this;
	}

	public <I> ClassListBuilder<T> add(Class<I> implementationClass) {
		this.implementationsClassname.add(implementationClass.getName());
		return this;
	}

	public Set<T> build() {
		return build(new HashSet<T>());
	}

	@SuppressWarnings("unchecked")
	public Set<T> build(Set<T> otherIncludedImplementations) {
		Set<T> instanciatedImplementations = new HashSet<>();
		Set<String> allClassNames = new HashSet<>();
		for (T otherIncludedImplementation : otherIncludedImplementations) {
			allClassNames.add(otherIncludedImplementation.getClass().getName());
		}
		allClassNames.addAll(implementationsClassname);

		for (String implementationClassname : allClassNames) {
			instanciatedImplementations.add(getObjectWithClassname(implementationClassname));
		}
		return instanciatedImplementations;
	}

	private T getObjectWithClassname(String implementationClassname) {

		T object = (T) cache.get(implementationClassname);

		if (object == null) {
			object = createObjectWithClassname(implementationClassname);
			cache.put(implementationClassname, object);
		}

		return object;
	}

	private T createObjectWithClassname(String implementationClassname) {
		try {

			Class<T> implementationClass = classProvider.loadClass(implementationClassname);
			if (!implementedClass.isAssignableFrom(implementationClass)) {
				throw new ClassListBuilderRuntimeException.ClassDoesntImplementInterface(implementationClass.getName(),
						implementedClass);
			}
			return implementationClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new ClassListBuilderRuntimeException.CannotInstanciate(implementationClassname, e);
		} catch (ClassNotFoundException | NoClassDefFoundError e) {
			throw new ClassListBuilderRuntimeException.ClassNotFound(implementationClassname, e);

		}
	}

	public <I> ClassListBuilder<T> remove(Class<I> implementationClass) {
		this.implementationsClassname.remove(implementationClass.getName());
		return this;
	}

	public ClassListBuilder<T> remove(String name) {
		this.implementationsClassname.remove(name);
		return this;
	}

	public void set(List<String> validators) {
		this.implementationsClassname.clear();
		this.implementationsClassname.addAll(validators);
	}

	public static <T> Set<T> combine(Set<T> set1, Set<T> set2) {

		Set<T> validators = new HashSet<>();
		Set<String> validatorCodes = new HashSet<>();
		for (T item1 : set1) {
			validatorCodes.add(item1.getClass().getName());
			validators.add(item1);
		}

		for (T item2 : set2) {

			if (!validatorCodes.contains(item2.getClass().getName())) {
				validatorCodes.add(item2.getClass().getName());
				validators.add(item2);
			}
		}

		return Collections.unmodifiableSet(validators);
	}

	public Set<String> getClassnames() {
		return implementationsClassname;
	}

	public static <T> boolean isSameValues(Set<T> values1, Set<T> values2) {

		Set<String> values1Classes = new HashSet<>();
		for (T value : values1) {
			values1Classes.add(value.getClass().getName());
		}

		Set<String> values2Classes = new HashSet<>();
		for (T value : values2) {
			values2Classes.add(value.getClass().getName());
		}

		return values1Classes.equals(values2Classes);
	}

	public boolean contains(Class<?> aClass) {
		return implementationsClassname.contains(aClass.getName());
	}

	public boolean contains(String aClassName) {
		return implementationsClassname.contains(aClassName);
	}
}
