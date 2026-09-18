package com.dabi.habitv.provider.tf1plus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

final class Tf1PlusJson {

	private Tf1PlusJson() {
	}

	@SuppressWarnings("unchecked")
	static Map<String, Object> asMap(final Object raw) {
		if (raw instanceof Map) {
			return (Map<String, Object>) raw;
		}
		return Collections.emptyMap();
	}

	static List<Map<String, Object>> asMapList(final Object raw) {
		if (!(raw instanceof List)) {
			return Collections.emptyList();
		}
		final List<?> list = (List<?>) raw;
		final List<Map<String, Object>> items = new ArrayList<Map<String, Object>>(list.size());
		for (final Object entry : list) {
			if (entry instanceof Map) {
				items.add(asMap(entry));
			}
		}
		return items;
	}

	static String asString(final Object raw) {
		if (raw == null) {
			return null;
		}
		final String value = String.valueOf(raw).trim();
		return value.isEmpty() || "null".equals(value) ? null : value;
	}

	static String firstString(final Map<String, Object> map, final String... keys) {
		if (map == null) {
			return null;
		}
		for (final String key : keys) {
			final String value = asString(map.get(key));
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	static Integer asPositiveInt(final Object raw) {
		if (raw instanceof Number) {
			final int value = ((Number) raw).intValue();
			return value > 0 ? Integer.valueOf(value) : null;
		}
		if (raw instanceof String) {
			try {
				final int value = Integer.parseInt(((String) raw).trim());
				return value > 0 ? Integer.valueOf(value) : null;
			} catch (final NumberFormatException e) {
				return null;
			}
		}
		return null;
	}

	static Long asPositiveLong(final Object raw) {
		if (raw instanceof Number) {
			final long value = ((Number) raw).longValue();
			return value > 0L ? Long.valueOf(value) : null;
		}
		return null;
	}

	static Object nested(final Map<String, Object> root, final String... keys) {
		Object current = root;
		for (final String key : keys) {
			if (!(current instanceof Map)) {
				return null;
			}
			current = asMap(current).get(key);
		}
		return current;
	}

}
