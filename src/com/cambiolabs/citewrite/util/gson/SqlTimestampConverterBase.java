package com.cambiolabs.citewrite.util.gson;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

abstract class SqlTimestampConverterBase  implements JsonSerializer<Timestamp> {

	private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	public JsonElement serialize(Timestamp src, Type srcType,
			JsonSerializationContext context) {
		return new JsonPrimitive(simpleDateFormat.format(src));
	}

	 abstract SqlTimestampConverterBase getTypeAdapter(String pattern);

	 protected void setDateFormat(String dateFormat) {
		try {
			simpleDateFormat = new SimpleDateFormat(dateFormat);
		} catch (Exception exception) {/**/}
	}

}