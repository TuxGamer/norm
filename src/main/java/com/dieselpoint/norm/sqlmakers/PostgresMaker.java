package com.dieselpoint.norm.sqlmakers;

import com.dieselpoint.norm.Query;

import javax.persistence.Column;
import java.util.ArrayList;
import java.util.Objects;

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
		StandardPojoInfo pi = new PostgresPojoInfo(rowClass);

		new ArrayList<>(pi.propertyMap.values()).forEach(prop -> {
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

					pi.propertyMap.remove(prop.name);
					prop.name = sb.toString();
					pi.propertyMap.put(prop.name, prop);
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

	@Override
	public void makeUpsertSql(StandardPojoInfo pojoInfo) {
		StringBuilder sql = new StringBuilder(pojoInfo.insertSql + " on conflict (");

		boolean hasAnyPrimaryKey = false;
		for (Property prop : pojoInfo.propertyMap.values()) {
			if (prop.isPrimaryKey) {
				if (hasAnyPrimaryKey) {
					sql.append(", ");
				}
				hasAnyPrimaryKey = true;
				sql.append(prop.name);
			}
		}
		if (!hasAnyPrimaryKey) {
			throw new IllegalArgumentException("No primary key defined for " + pojoInfo.table);
		}

		sql.append(") do update set ");
		boolean first = true;
		for (Property prop : pojoInfo.propertyMap.values()) {
			if (prop.isGenerated) {
				continue;
			}
			if (!first) {
				sql.append(", ");
			}
			first = false;
			sql.append(prop.name).append(" = excluded.").append(prop.name);
		}
		pojoInfo.upsertSql = sql.toString();
	}

	@Override
	public String getUpsertSql(Query query, Object row) {
		StandardPojoInfo pojoInfo = getPojoInfo(row.getClass());
		return String.format(pojoInfo.upsertSql, Objects.requireNonNullElse(query.getTable(), pojoInfo.table));
	}

	@Override
	public Object[] getUpsertArgs(Query query, Object row) {
		return super.getInsertArgs(query, row);
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
