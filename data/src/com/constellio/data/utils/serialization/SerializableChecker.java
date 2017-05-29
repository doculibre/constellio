/**
 * IntelliGID, Open Source Enterprise Search
 * Copyright (C) 2010 DocuLibre inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.constellio.data.utils.serialization;

import java.io.Externalizable;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamField;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapted from wicket.util.io.SerializableChecker
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class SerializableChecker extends ObjectOutputStream
{
	/**
	 * Exception that is thrown when a non-serializable object was found.
	 */
	public static final class ConstellioNotSerializableException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;

		ConstellioNotSerializableException(String message, Throwable cause)
		{
			super(message, cause);
		}
	}

	/**
	 * Does absolutely nothing.
	 */
	private static class NoopOutputStream extends OutputStream
	{
		public void close()
		{
		}

		public void flush()
		{
		}

		public void write(byte[] b)
		{
		}

		public void write(byte[] b, int i, int l)
		{
		}

		public void write(int b)
		{
		}
	}

	private static abstract class ObjectOutputAdaptor implements ObjectOutput
	{

		public void close() throws IOException
		{
		}

		public void flush() throws IOException
		{
		}

		public void write(byte[] b) throws IOException
		{
		}

		public void write(byte[] b, int off, int len) throws IOException
		{
		}

		public void write(int b) throws IOException
		{
		}

		public void writeBoolean(boolean v) throws IOException
		{
		}

		public void writeByte(int v) throws IOException
		{
		}

		public void writeBytes(String s) throws IOException
		{
		}

		public void writeChar(int v) throws IOException
		{
		}

		public void writeChars(String s) throws IOException
		{
		}

		public void writeDouble(double v) throws IOException
		{
		}

		public void writeFloat(float v) throws IOException
		{
		}

		public void writeInt(int v) throws IOException
		{
		}

		public void writeLong(long v) throws IOException
		{
		}

		public void writeShort(int v) throws IOException
		{
		}

		public void writeUTF(String str) throws IOException
		{
		}
	}

	/** Holds information about the field and the resulting object being traced. */
	private static final class TraceSlot
	{
		private final String fieldDescription;

		private final Object object;

		TraceSlot(Object object, String fieldDescription)
		{
			super();
			this.object = object;
			this.fieldDescription = fieldDescription;
		}

		public String toString()
		{
			return object.getClass() + " - " + fieldDescription;
		}
	}

	private static final NoopOutputStream DUMMY_OUTPUT_STREAM = new NoopOutputStream();

	/** log. */
	private static Logger LOGGER = LoggerFactory.getLogger(SerializableChecker.class);

	/** Whether we can execute the tests. If false, check will just return. */
	private static boolean available = true;

	// this hack - accessing the serialization API through introspection - is
	// the only way to use Java serialization for our purposes without writing
	// the whole thing from scratch (and even then, it would be limited). This
	// way of working is of course fragile for internal API changes, but as we
	// do an extra check on availability and we report when we can't use this
	// introspection fu, we'll find out soon enough and clients on this class
	// can fall back on Java's default exception for serialization errors (which
	// sucks and is the main reason for this attempt).
	private static final Method LOOKUP_METHOD;

	private static final Method GET_CLASS_DATA_LAYOUT_METHOD;

	private static final Method GET_NUM_OBJ_FIELDS_METHOD;

	private static final Method GET_OBJ_FIELD_VALUES_METHOD;

	private static final Method GET_FIELD_METHOD;

	private static final Method HAS_WRITE_REPLACE_METHOD_METHOD;

	private static final Method INVOKE_WRITE_REPLACE_METHOD;

	static
	{
		try
		{
			LOOKUP_METHOD = ObjectStreamClass.class.getDeclaredMethod("lookup", Class.class, Boolean.TYPE);
			LOOKUP_METHOD.setAccessible(true);

			GET_CLASS_DATA_LAYOUT_METHOD = ObjectStreamClass.class.getDeclaredMethod(
					"getClassDataLayout", (Class<?>[])null);
			GET_CLASS_DATA_LAYOUT_METHOD.setAccessible(true);

			GET_NUM_OBJ_FIELDS_METHOD = ObjectStreamClass.class.getDeclaredMethod(
					"getNumObjFields", (Class<?>[])null);
			GET_NUM_OBJ_FIELDS_METHOD.setAccessible(true);

			GET_OBJ_FIELD_VALUES_METHOD = ObjectStreamClass.class.getDeclaredMethod(
					"getObjFieldValues", Object.class, Object[].class);
			GET_OBJ_FIELD_VALUES_METHOD.setAccessible(true);

			GET_FIELD_METHOD = ObjectStreamField.class.getDeclaredMethod("getField", (Class<?>[])null);
			GET_FIELD_METHOD.setAccessible(true);

			HAS_WRITE_REPLACE_METHOD_METHOD = ObjectStreamClass.class.getDeclaredMethod(
					"hasWriteReplaceMethod", (Class<?>[])null);
			HAS_WRITE_REPLACE_METHOD_METHOD.setAccessible(true);

			INVOKE_WRITE_REPLACE_METHOD = ObjectStreamClass.class.getDeclaredMethod(
					"invokeWriteReplace", Object.class);
			INVOKE_WRITE_REPLACE_METHOD.setAccessible(true);
		}
		catch (SecurityException e)
		{
			available = false;
			throw new RuntimeException(e);
		}
		catch (NoSuchMethodException e)
		{
			available = false;
			throw new RuntimeException(e);
		}
	}

	/**
	 * Gets whether we can execute the tests. If false, calling {@link #check(Object)} will just
	 * return and you are advised to rely on the {@link NotSerializableException}. Clients are
	 * advised to call this method prior to calling the check method.
	 *
	 * @return whether security settings and underlying API etc allow for accessing the
	 *         serialization API using introspection
	 */
	public static boolean isAvailable()
	{
		return available;
	}

	/** object stack that with the trace path. */
	private final LinkedList traceStack = new LinkedList();

	/** set for checking circular references. */
	private final Map checked = new IdentityHashMap();

	/** string stack with current names pushed. */
	private final LinkedList nameStack = new LinkedList();

	/** root object being analyzed. */
	private Object root;

	/** cache for classes - writeObject methods. */
	private final Map writeObjectMethodCache = new HashMap();

	/** current simple field name. */
	private String simpleName = "";

	/** current full field description. */
	private String fieldDescription;

	/** Exception that should be set as the cause when throwing a new exception. */
	private final NotSerializableException exception;

	/**
	 * Construct.
	 *
	 * @param exception
	 *            exception that should be set as the cause when throwing a new exception
	 *
	 * @throws IOException
	 */
	public SerializableChecker(NotSerializableException exception) throws IOException
	{
		this.exception = exception;
	}

	/**
	 * @see java.io.ObjectOutputStream#reset()
	 */
	public void reset() throws IOException
	{
		root = null;
		checked.clear();
		fieldDescription = null;
		simpleName = null;
		traceStack.clear();
		nameStack.clear();
		writeObjectMethodCache.clear();
	}

	private void check(Object obj)
	{
		if (obj == null)
		{
			return;
		}

		Class cls = obj.getClass();
		nameStack.add(simpleName);
		traceStack.add(new TraceSlot(obj, fieldDescription));

		if (!(obj instanceof Serializable) && (!Proxy.isProxyClass(cls)))
		{
			throw new ConstellioNotSerializableException(
				  toPrettyPrintedStack(obj.getClass().getName()), exception);
		}

		ObjectStreamClass desc;
		for (;;)
		{
			try
			{
				desc = (ObjectStreamClass) LOOKUP_METHOD.invoke(null, cls,
						Boolean.TRUE);
				Class repCl;
				if (!((Boolean)HAS_WRITE_REPLACE_METHOD_METHOD.invoke(desc, (Object [])null)).booleanValue() ||
						(obj = INVOKE_WRITE_REPLACE_METHOD.invoke(desc, obj)) == null ||
						(repCl = obj.getClass()) == cls)
				{
					break;
				}
				cls = repCl;
			}
			catch (IllegalAccessException e)
			{
				throw new RuntimeException(e);
			}
			catch (InvocationTargetException e)
			{
				throw new RuntimeException(e);
			}
		}

		if (cls.isPrimitive())
		{
			// skip
		}
		else if (cls.isArray())
		{
			checked.put(obj, null);
			Class ccl = cls.getComponentType();
			if (!(ccl.isPrimitive()))
			{
				Object[] objs = (Object[])obj;
				for (int i = 0; i < objs.length; i++)
				{
					String arrayPos = "[" + i + "]";
					simpleName = arrayPos;
					fieldDescription += arrayPos;
					check(objs[i]);
				}
			}
		}
		else if (obj instanceof Externalizable && (!Proxy.isProxyClass(cls)))
		{
			Externalizable extObj = (Externalizable)obj;
			try
			{
				extObj.writeExternal(new ObjectOutputAdaptor()
				{
					private int count = 0;

					public void writeObject(Object streamObj) throws IOException
					{
						// Check for circular reference.
						if (checked.containsKey(streamObj))
						{
							return;
						}

						checked.put(streamObj, null);
						String arrayPos = "[write:" + count++ + "]";
						simpleName = arrayPos;
						fieldDescription += arrayPos;

						check(streamObj);
					}
				});
			}
			catch (Exception e)
			{
				if (e instanceof ConstellioNotSerializableException)
				{
					throw (ConstellioNotSerializableException)e;
				}
				LOGGER.warn("error delegating to Externalizable : " + e.getMessage() + ", path: " +
						currentPath());
			}
		}
		else
		{
			Method writeObjectMethod = null;
			Object o = writeObjectMethodCache.get(cls);
			if (o != null)
			{
				if (o instanceof Method)
				{
					writeObjectMethod = (Method)o;
				}
			}
			else
			{
				try
				{
					writeObjectMethod = cls.getDeclaredMethod("writeObject",
							ObjectOutputStream.class);
				}
				catch (SecurityException e)
				{
					// we can't access/ set accessible to true
					writeObjectMethodCache.put(cls, Boolean.FALSE);
				}
				catch (NoSuchMethodException e)
				{
					// cls doesn't have that method
					writeObjectMethodCache.put(cls, Boolean.FALSE);
				}
			}

			final Object original = obj;
			if (writeObjectMethod != null)
			{
				class InterceptingObjectOutputStream extends ObjectOutputStream
				{
					private int counter;

					InterceptingObjectOutputStream() throws IOException
					{
						super(DUMMY_OUTPUT_STREAM);
						enableReplaceObject(true);
					}

					protected Object replaceObject(Object streamObj) throws IOException
					{
						if (streamObj == original)
						{
							return streamObj;
						}

						counter++;
						// Check for circular reference.
						if (checked.containsKey(streamObj))
						{
							return null;
						}

						checked.put(original, null);
						String arrayPos = "[write:" + counter + "]";
						simpleName = arrayPos;
						fieldDescription += arrayPos;
						check(streamObj);
						return streamObj;
					}
				}
				try
				{
					InterceptingObjectOutputStream ioos = new InterceptingObjectOutputStream();
					ioos.writeObject(obj);
				}
				catch (Exception e)
				{
					if (e instanceof ConstellioNotSerializableException)
					{
						throw (ConstellioNotSerializableException)e;
					}
					LOGGER.warn("error delegating to writeObject : " + e.getMessage() + ", path: " +
							currentPath());
				}
			}
			else
			{
				Object[] slots;
				try
				{
					slots = (Object[])GET_CLASS_DATA_LAYOUT_METHOD.invoke(desc, (Object[])null);
				}
				catch (Exception e)
				{
					throw new RuntimeException(e);
				}
				for (int i = 0; i < slots.length; i++)
				{
					ObjectStreamClass slotDesc;
					try
					{
						Field descField = slots[i].getClass().getDeclaredField("desc");
						descField.setAccessible(true);
						slotDesc = (ObjectStreamClass)descField.get(slots[i]);
					}
					catch (Exception e)
					{
						throw new RuntimeException(e);
					}
					checked.put(obj, null);
					checkFields(obj, slotDesc);
				}
			}
		}

		traceStack.removeLast();
		nameStack.removeLast();
	}

	private void checkFields(Object obj, ObjectStreamClass desc)
	{
		int numFields;
		try
		{
			numFields = ((Integer)GET_NUM_OBJ_FIELDS_METHOD.invoke(desc, (Object[])null)).intValue();
		}
		catch (IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException(e);
		}

		if (numFields > 0)
		{
			int numPrimFields;
			ObjectStreamField[] fields = desc.getFields();
			Object[] objVals = new Object[numFields];
			numPrimFields = fields.length - objVals.length;
			try
			{
				GET_OBJ_FIELD_VALUES_METHOD.invoke(desc, obj, objVals);
			}
			catch (IllegalAccessException e)
			{
				throw new RuntimeException(e);
			}
			catch (InvocationTargetException e)
			{
				throw new RuntimeException(e);
			}
			for (int i = 0; i < objVals.length; i++)
			{
				if (objVals[i] instanceof String || objVals[i] instanceof Number ||
						objVals[i] instanceof Date || objVals[i] instanceof Boolean ||
						objVals[i] instanceof Class)
				{
					// filter out common cases
					continue;
				}

				// Check for circular reference.
				if (checked.containsKey(objVals[i]))
				{
					continue;
				}

				ObjectStreamField fieldDesc = fields[numPrimFields + i];
				Field field;
				try
				{
					field = (Field)GET_FIELD_METHOD.invoke(fieldDesc, (Object[])null);
				}
				catch (IllegalAccessException e)
				{
					throw new RuntimeException(e);
				}
				catch (InvocationTargetException e)
				{
					throw new RuntimeException(e);
				}

				String fieldName = field.getName();
				simpleName = field.getName();
				fieldDescription = field.toString();
				check(objVals[i]);
			}
		}
	}

	/**
	 * @return name from root to current node concatenated with slashes
	 */
	private StringBuffer currentPath()
	{
		StringBuffer b = new StringBuffer();
		for (Iterator it = nameStack.iterator(); it.hasNext();)
		{
			b.append(it.next());
			if (it.hasNext())
			{
				b.append('/');
			}
		}
		return b;
	}

	/**
	 * Dump with indentation.
	 *
	 * @param type
	 *            the type that couldn't be serialized
	 * @return A very pretty dump
	 */
	private final String toPrettyPrintedStack(String type)
	{
		StringBuffer result = new StringBuffer();
		StringBuffer spaces = new StringBuffer();
		result.append("Unable to serialize class: ");
		result.append(type);
		result.append("\nField hierarchy is:");
		for (Iterator i = traceStack.listIterator(); i.hasNext();)
		{
			spaces.append("  ");
			TraceSlot slot = (TraceSlot)i.next();
			result.append("\n").append(spaces).append(slot.fieldDescription);
			result.append(" [class=").append(slot.object.getClass().getName());
			result.append("]");
		}
		result.append(" <----- field that is not serializable");
		return result.toString();
	}

	/**
	 * @see java.io.ObjectOutputStream#writeObjectOverride(java.lang.Object)
	 */
	public final void writeObjectOverride(Object obj) throws IOException
	{
		if (!available)
		{
			return;
		}
		root = obj;
		if (fieldDescription == null)
		{
			fieldDescription = "";
		}

		check(root);
	}
}
