package com.dieselpoint.norm.sqlmakers;

public class PostgresPojoInfo extends StandardPojoInfo {

	public PostgresPojoInfo(Class<?> clazz) {
		super(clazz);
	}

	@Override
	protected Property resolveProperty(String name) {
		Property prop = super.resolveProperty(name);
		if (prop == null) {
			// TODO This needs cleanup
			prop = super.propertyMap.values()
					.stream()
					.filter(p -> p.field.getName().equals(name))
					.findFirst()
					.orElse(null);
		}
		return prop;
	}

}
