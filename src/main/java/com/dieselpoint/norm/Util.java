package com.dieselpoint.norm;

import java.util.Collection;
import java.util.Collections;

public class Util {

	public static String join(String[] strs) {
		return String.join(",", strs);
	}

	public static String join(Collection<String> strs) {
		return String.join(",", strs);
	}

	public static String getQuestionMarks(int count) {
		return String.join(",", Collections.nCopies(count, "?"));
	}

	public static boolean isPrimitiveOrString(Class<?> c) {
		if (c.isPrimitive()) {
			return true;
		}

		return c == Byte.class || c == Short.class || c == Integer.class || c == Long.class || c == Float.class
                || c == Double.class || c == Boolean.class || c == Character.class || c == String.class;
	}

	public static Class<?> wrap(Class<?> type) {
		if (!type.isPrimitive()) {
			return type;
		}
		if (type == int.class) {
			return Integer.class;
		}
		if (type == long.class) {
			return Long.class;
		}
		if (type == boolean.class) {
			return Boolean.class;
		}
		if (type == byte.class) {
			return Byte.class;
		}
		if (type == char.class) {
			return Character.class;
		}
		if (type == double.class) {
			return Double.class;
		}
		if (type == float.class) {
			return Float.class;
		}
		if (type == short.class) {
			return Short.class;
		}
		if (type == void.class) {
			return Void.class;
		}
		throw new RuntimeException("Will never get here");
	}
}
