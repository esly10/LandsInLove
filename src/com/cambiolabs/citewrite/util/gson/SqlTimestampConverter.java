package com.cambiolabs.citewrite.util.gson;



 public class SqlTimestampConverter extends SqlTimestampConverterBase {

	 
	@Override
	public SqlTimestampConverterBase getTypeAdapter(String pattern) {
		setDateFormat(pattern);
		return this;
	}
        
}