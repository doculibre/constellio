package com.constellio.model.utils;

import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.batchprocess.JsonSerializable;
import com.constellio.model.utils.ParametrizedInstanceUtilsRuntimeException.CannotInstanciate;
import com.constellio.model.utils.ParametrizedInstanceUtilsRuntimeException.NoSuchConstructor;
import com.constellio.model.utils.ParametrizedInstanceUtilsRuntimeException.UnsupportedArgument;
import org.jdom2.Element;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParametrizedInstanceUtils {

	public <T extends Parametrized> T toObject(Element element, Class<T> type) {
		T parent;
		Class parentClass;
		try {
			parentClass = Class.forName(element.getAttribute("name").getValue());
		} catch (ClassNotFoundException e) {
			throw new CannotInstanciate(type.getName(), e);
		}

		try {
			List<Object> parameters = new ArrayList<>();
			List<Class<?>> parameterClasses = new ArrayList<>();
			this.getConstructorParameter(element, parameters, parameterClasses);

			Constructor parentConstructor = this.getConstructor(parentClass, parameterClasses);
			if (parentConstructor == null) {
				throw new NoSuchConstructor(type, parameterClasses, null);
			}
			parent = (T) parentConstructor.newInstance(parameters.toArray());
		} catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
			throw new CannotInstanciate(type.getName(), e);
		}

		return parent;
	}

	protected void getConstructorParameter(Element root, List<Object> parameters, List<Class<?>> parameterClasses) {
		for (Element child : root.getChildren()) {
			parameters.add(toObject(child));
		}

		for (Object parameter : parameters) {
			if (parameter == null) {
				parameterClasses.add(null);
			} else {
				parameterClasses.add(classConvert(parameter.getClass()));
			}
		}
	}

	private Constructor getConstructor(Class parentClass, List<Class<?>> parameter) {

		for (Constructor constructor : parentClass.getConstructors()) {

			Class[] parameterClass = constructor.getParameterTypes();
			boolean sameParameter = parameterClass.length == parameter.size();
			if (sameParameter) {
				for (int i = 0; i < parameterClass.length; i++) {
					if (parameter.get(i) != null && !parameter.get(i).equals(parameterClass[i])) {
						sameParameter = false;
						break;
					}
				}
			}

			if (sameParameter) {
				return constructor;
			}
		}

		return null;
	}

	public Object toObject(Element element) {

		try {

			if (element == null || "null".equals(element.getAttribute("type").getValue())) {
				return null;
			}

			Class childClass = Class.forName(element.getAttribute("type").getValue());

			if (List.class.isAssignableFrom(childClass)) {
				return getListObject(element, childClass);
			} else if (Map.class.isAssignableFrom(childClass)) {
				return getMapObject(element, childClass);
			} else {
				return getObject(element, childClass);
			}
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
			throw new CannotInstanciate(element.getAttribute("type").getValue(), ex);
		}
	}

	protected Object getObject(Element element, Class childClass)
			throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Object object;
		String value = element.getText();

		if (childClass.isAssignableFrom(LocalDateTime.class)) {
			object = LocalDateTime.parse(value);
		} else if (childClass.isAssignableFrom(LocalDate.class)) {
			object = LocalDate.parse(value);
		} else if (EnumWithSmallCode.class.isAssignableFrom(childClass)) { // ENUM CHECK
			object = EnumWithSmallCodeUtils.toEnum(childClass, value);
		} else if (JsonSerializable.class.isAssignableFrom(childClass)) {
			object = JsonSerializable.deserialize(value, childClass);
		} else {
			object = childClass.getConstructor(String.class).newInstance(value);
		}

		return object;
	}

	private Object getMapObject(Element element, Class childClass)
			throws InstantiationException, IllegalAccessException {
		Map<Object, Object> object = (HashMap) childClass.newInstance();

		for (Element entry : element.getChildren()) {
			Object key = this.toObject(entry.getChild("key"));
			Element valueElement = entry.getChild("value");
			Object value = valueElement == null ? null : this.toObject(valueElement);
			object.put(key, value);
		}

		return object;
	}

	private Object getListObject(Element element, Class childClass)
			throws InstantiationException, IllegalAccessException {
		List<Object> object = (List) childClass.newInstance();
		for (Element item : element.getChildren()) {
			object.add(this.toObject(item));
		}

		return object;
	}

	public Element toElement(Parametrized parametrized, String elementTag) {

		Element root = new Element(elementTag);
		root.setAttribute("name", parametrized.getClass().getName());

		Object[] parameters = parametrized.getInstanceParameters();
		for (Object parameter : parameters) {
			if (parameter == null) {
				Element child = new Element("argument");
				child.setAttribute("type", "null");
				root.addContent(child);
				continue;
			}

			if (!isValid(parameter.getClass())) {
				throw new UnsupportedArgument(parameter.getClass());
			}

			this.toElement(parameter, root, "argument");
		}

		return root;
	}

	public void toElement(Object parameter, Element parent, String elementTag) {
		Element child = new Element(elementTag);
		child.setAttribute("type", parameter.getClass().getName());

		if (List.class.isAssignableFrom(parameter.getClass())) {
			child.setAttribute("type", ArrayList.class.getName());
			List<Object> listParameter = (List<Object>) parameter;
			for (Object item : listParameter) {
				this.toElement(item, child, "item");
			}
		} else if (Map.class.isAssignableFrom(parameter.getClass())) {
			Map<Object, Object> mapParameter = (Map<Object, Object>) parameter;
			for (Object entryKey : mapParameter.keySet()) {
				Element entryElement = new Element("entry");
				toElement(entryKey, entryElement, "key");
				Object value = mapParameter.get(entryKey);
				if (value != null) {
					toElement(value, entryElement, "value");
				}
				child.addContent(entryElement);
			}
		} else {
			if (!isValid(parameter.getClass())) {
				throw new UnsupportedArgument(parameter.getClass());
			}

			if (EnumWithSmallCode.class.isAssignableFrom(parameter.getClass())) {
				child.setText("" + EnumWithSmallCodeUtils.toSmallCode((EnumWithSmallCode) parameter));
			} else if (JsonSerializable.class.isAssignableFrom(parameter.getClass())) {
				child.setText(((JsonSerializable) parameter).serialize());
			} else {
				child.setText("" + parameter.toString());
			}
		}

		parent.addContent(child);
	}

	private Class classConvert(Class current) {
		Class newClass;
		if (List.class.isAssignableFrom(current)) {
			newClass = List.class;
		} else if (Map.class.isAssignableFrom(current)) {
			newClass = Map.class;
		} else {
			newClass = current;
		}

		return newClass;
	}

	private boolean isValid(Class clazz) {
		return Map.class.isAssignableFrom(clazz) ||
			   List.class.isAssignableFrom(clazz) ||
			   Integer.class.equals(clazz) ||
			   String.class.equals(clazz) ||
			   Double.class.equals(clazz) ||
			   Float.class.equals(clazz) ||
			   LocalDateTime.class.equals(clazz) ||
			   LocalDate.class.equals(clazz) ||
			   Boolean.class.equals(clazz) ||
			   Long.class.equals(clazz) ||
			   Enum.class.isAssignableFrom(clazz) ||
			   JsonSerializable.class.isAssignableFrom(clazz);
	}
}

