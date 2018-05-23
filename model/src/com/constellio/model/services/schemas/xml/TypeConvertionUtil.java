package com.constellio.model.services.schemas.xml;

import com.constellio.model.services.exception.TypeRuntimeException;
import com.rometools.utils.Strings;
import com.sun.javafx.collections.MappingChange;
import org.jdom2.Element;

import java.util.*;

public class TypeConvertionUtil {
    public static final String BOOLEAN_TYPE = "boolean";
    public static final String STRING_TYPE = "string";
    public static final String INTEGER_TYPE = "interger";
    public static final String DOUBLE_TYPE = "double";
    public static final String LIST_TYPE = "list";
    public static final String MAP_WITH_SRING_AS_KEY_TYPE = "map";
    public static final String LONG_TYPE = "long";

    public static final String VALUE_ATTRIBUTE = "value";
    public static final String KEY_ATTRIBUTE = "key";


    public static String getFormatedType(Object object) {
        if(object == null) {
            throw new TypeRuntimeException.CannotGetTypeFromNullValueRunTimeException("object");
        }

        if(object.getClass() == List.class) {
            return LIST_TYPE;
        } else if(object.getClass() == Map.class) {
            return MAP_WITH_SRING_AS_KEY_TYPE;
        } else if (object.getClass() == Boolean.class) {
            return BOOLEAN_TYPE;
        } else if(object.getClass() == Integer.class) {
            return INTEGER_TYPE;
        } else if (object.getClass() == Double.class) {
            return DOUBLE_TYPE;
        } else if(object.getClass() == String.class) {
            return STRING_TYPE;
        } else {
            throw new TypeRuntimeException.UnsupportedTypeRunTimeException(object.getClass().toString());
        }
    }

    public static boolean canObjectValueBeRepresentedInAString(Object object) {
        Class objectClass = object.getClass();

        return (objectClass == Boolean.class
                || objectClass == Long.class
                || objectClass == Double.class
                || objectClass == Integer.class
                || objectClass == String.class);
    }

    public static Map<String, Object> getCustomParameterMap(Element element) {
        Element customParameterElement = element.getChild("customParameter");
        Map<String, Object> customParameterMap = new HashMap<>();

        if(customParameterElement == null) {
            return customParameterMap;
        }

        List<Element> childrenList = customParameterElement.getChildren();

        if(childrenList == null) {
            return customParameterMap;
        }


        for(Element  childElement : childrenList){
             Object objectFromElement = getObjectFromElement(childElement);
             customParameterMap.put(childElement.getAttributeValue(KEY_ATTRIBUTE), objectFromElement);
        }

        return customParameterMap;
    }

    public static Object getObjectFromElement(Element element) {

        String type = element.getText();

        if(type == null) {
            throw new TypeRuntimeException.CannotGetTypeFromNullValueRunTimeException(type);
        }

        if(LIST_TYPE.equals(type)) {
            return parseList(element);
        } else if(MAP_WITH_SRING_AS_KEY_TYPE.equals(type)) {
            return parseMap(element);
        } else if (BOOLEAN_TYPE.equals(type)) {
            return parseBoolean(element);
        } else if(LONG_TYPE.equals(type)) {
            return parseLong(element);
        } else if (DOUBLE_TYPE.equals(type)) {
            return parseDouble(element);
        } else if(INTEGER_TYPE.equals(type)) {
            return parseInt(element);
        } else if(STRING_TYPE.equals(STRING_TYPE)) {
            return element.getAttributeValue(VALUE_ATTRIBUTE);
        } else {
            throw new TypeRuntimeException.UnsupportedTypeRunTimeException(type);
        }
    }

    public static long parseLong(Element element) {
        String value = element.getAttributeValue(VALUE_ATTRIBUTE);

        return Long.parseLong(value);
    }

    public static double parseDouble(Element element) {
        String value = element.getAttributeValue(VALUE_ATTRIBUTE);

        return Double.parseDouble(value);
    }

    public static int parseInt(Element element) {
        String value = element.getAttributeValue(VALUE_ATTRIBUTE);

        return Integer.parseInt(value);
    }

    public static boolean parseBoolean(Element element) {
        String value = element.getAttributeValue(VALUE_ATTRIBUTE);

        if(Boolean.TRUE.equals(value)) {
            return true;
        } else if(Boolean.FALSE.equals(value)) {
            return false;
        } else {
            throw new TypeRuntimeException.InvalidValueForTypeRunTimeException(value, BOOLEAN_TYPE);
        }
    }

    public static Map<String, Object> parseMap(Element elementList){
        Map<String, Object> newMap = new HashMap();

        for(Element element : elementList.getChildren()) {
            newMap.put(element.getAttributeValue(KEY_ATTRIBUTE), getObjectFromElement(element));
        }

        return Collections.unmodifiableMap(newMap);
    }

    public static List parseList(Element listElement) {
        ArrayList newList = new ArrayList();

        for(Element element : listElement.getChildren()) {
            newList.add(getObjectFromElement(element));
        }

        return Collections.unmodifiableList(newList);
    }

    public static Element listToElement(List list) {
        Element listRootElement = new Element(LIST_TYPE);

        for(Object listObject : list) {
            Element element = getElement(null, listObject);
            if(element != null) {
                listRootElement.addContent(element);
            }
        }

        return listRootElement;
    }

    public static Element mapToElement(Map<String, Object> map) {
        Element mapRootElement = new Element(MAP_WITH_SRING_AS_KEY_TYPE);

        for(String key : map.keySet()) {
            Object value = map.get(key);
            Element element = getElement(key, value);
            if(element != null) {
                mapRootElement.addContent(getElement(key, value));
            }
        }

        return mapRootElement;
    }

    public static String getStringFromObject(Object object) {
        if(canObjectValueBeRepresentedInAString(object)) {
            return object.toString();
        } else  {
            throw new TypeRuntimeException.TypeCannotBeRepresentedAsStringRunTimeException(object.getClass().toString());
        }
    }

    public static Element getElement(String mapKey, Object value) {
        String type = TypeConvertionUtil.getFormatedType(value);
        Element element = null;
        if(TypeConvertionUtil.canObjectValueBeRepresentedInAString(value)) {
            if(value != null) {
                element = new Element(type);
                element.setAttribute(TypeConvertionUtil.VALUE_ATTRIBUTE, TypeConvertionUtil.getStringFromObject(value));
            }
        } else {
            if(TypeConvertionUtil.LIST_TYPE.equals(type)) {
                List list = (List) value;
                if(!list.isEmpty()) {
                    element = TypeConvertionUtil.listToElement((List) value);
                }

            } else if(TypeConvertionUtil.MAP_WITH_SRING_AS_KEY_TYPE.equals(type)){
                Map map = (Map) value;
                if(!map.isEmpty()) {
                    element = TypeConvertionUtil.mapToElement((Map<String, Object>) value);
                }
            }
        }
        if(!Strings.isBlank(mapKey)) {
            element.setAttribute(TypeConvertionUtil.KEY_ATTRIBUTE, mapKey);
        }

        return element;
    }
}
