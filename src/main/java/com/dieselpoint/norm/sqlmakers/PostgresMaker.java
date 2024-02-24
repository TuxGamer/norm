package com.dieselpoint.norm.sqlmakers;

import javax.persistence.Column;

public class PostgresMaker extends StandardSqlMaker {

	private final NamingConvention namingConvention;

	public PostgresMaker() {
		this(NamingConvention.LOWER_CASE);
	}

	public PostgresMaker(NamingConvention namingConvention) {
		this.namingConvention = namingConvention;
	}

	@Override
	protected StandardPojoInfo constructPojoInfo(Class<?> rowClass) {
		StandardPojoInfo pi = super.constructPojoInfo(rowClass);

		pi.propertyMap.values().forEach(prop -> {
			if (prop.columnAnnotation != null && !prop.columnAnnotation.name().isEmpty()) {
				// Name was manually defined, don't override it
				return;
			}

			switch (namingConvention) {
				case PRESERVE_CASE: {
					prop.name = "\"" + prop.name + "\"";
					break;
				}
				case UNDERSCORE: {
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < prop.name.length(); i++) {
						char c = prop.name.charAt(i);
						if (Character.isUpperCase(c)) {
							sb.append('_').append(Character.toLowerCase(c));
						} else {
							sb.append(c);
						}
					}
					prop.name = sb.toString();
					break;
				}
				case LOWER_CASE: {
					// no action needed
				}
			}
		});

		return pi;
	}

	@Override
	public String getCreateTableSql(Class<?> clazz) {

		StringBuilder buf = new StringBuilder();

		StandardPojoInfo pojoInfo = getPojoInfo(clazz);
		buf.append("create table ");
		buf.append(pojoInfo.table);
		buf.append(" (");

		boolean needsComma = false;
		for (Property prop : pojoInfo.propertyMap.values()) {

			if (needsComma) {
				buf.append(',');
			}
			needsComma = true;

			Column columnAnnot = prop.columnAnnotation;
			if (columnAnnot == null) {

				buf.append(prop.name);
				buf.append(" ");
				if (prop.isGenerated) {
					buf.append(" serial");
				} else {
					buf.append(getColType(prop.dataType, 255, 10, 2));
				}

			} else {
				if (columnAnnot.columnDefinition() == null) {

					// let the column def override everything
					buf.append(columnAnnot.columnDefinition());

				} else {

					buf.append(prop.name);
					buf.append(" ");
					if (prop.isGenerated) {
						buf.append(" serial");
					} else {
						buf.append(getColType(prop.dataType, columnAnnot.length(), columnAnnot.precision(), columnAnnot.scale()));
					}

					if (columnAnnot.unique()) {
						buf.append(" unique");
					}

					if (!columnAnnot.nullable()) {
						buf.append(" not null");
					}
				}
			}
		}

		if (pojoInfo.primaryKeyNames.size() > 0) {
			buf.append(", primary key (");
			for (int i = 0; i < pojoInfo.primaryKeyNames.size(); i++) {
				if (i > 0) {
					buf.append(",");
				}
				buf.append(pojoInfo.primaryKeyNames.get(i));
			}
			buf.append(")");
		}

		buf.append(")");

		return buf.toString();
	}

	public enum NamingConvention {
		/**
		 * Will put double quotes around the table and column names, stopping PostgeSQL from converting this to lowercase.
		 * Example: "userID", "relatedEventId"
		 */
		PRESERVE_CASE,
		/**
		 * Will convert all column names to lowercase (default for backwards compatibility).
		 * Example: userid, relatedeventid
		 */
		LOWER_CASE,
		/**
		 * Will convert all uppercase letters to lowercase and separate words with underscores.
		 * Example: user_id, related_event_id
		 */
		UNDERSCORE;
	}

}
