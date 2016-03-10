package com.cambiolabs.citewrite.data;


import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Calendar;

import com.cambiolabs.citewrite.db.DBConnection;
import com.google.gson.annotations.Expose;

public class PasswordConfig {

	private static final int TYPE_LOCATION = 3;
	private static final String AUTHORIZATION_CONFIG_EXPRESSION = "AUTHORIZATION_CONFIG_EXPRESSION";
	private static final String AUTHORIZATION_CONFIG_MESSAGE = "AUTHORIZATION_CONFIG_MESSAGE";
	private static final String AUTHORIZATION_CONFIG_EXPIRATION = "AUTHORIZATION_CONFIG_EXPIRATION";

	public static PasswordConfig passwordConfigOwner = null;
	public static PasswordConfig passwordConfigUser = null;

	public enum Intervals {
		DAY, WEEK
	};

	public enum AuthorizationType {
		USER, OWNER
	};

	@Expose
	private String regExpresion = null;
	@Expose
	private String message = null;
	@Expose
	private boolean isEnable;
	@Expose
	private AuthorizationType type = null;
	@Expose
	private Intervals intervalsTime = null;
	@Expose
	private int time;
	@Expose
	private boolean isExpirationEnable;

	public PasswordConfig(String message, String regExpresion,AuthorizationType type, boolean isEnable, Intervals intervalsTime, int time, boolean isExpirationEnable) 
	{
		this.regExpresion = regExpresion;
		this.message = message;
		this.type = type;
		this.isEnable = isEnable;
		this.intervalsTime = intervalsTime;
		this.time = time;
		this.isExpirationEnable = isExpirationEnable;
	}

	public PasswordConfig(AuthorizationType type)
	{
		ConfigItem item = ConfigItem.lookup(AUTHORIZATION_CONFIG_EXPRESSION + "_"+ type.name());
		this.regExpresion = item.text_value;
		this.isEnable = item.int_value > 0 ? true: false;
		this.type = item.name.split("_").length >= TYPE_LOCATION ? AuthorizationType.valueOf(item.name.split("_")[TYPE_LOCATION]) : null;

		item = ConfigItem.lookup(AUTHORIZATION_CONFIG_MESSAGE + "_" + type.name());
		this.message = item.text_value;
		
		item = ConfigItem.lookup(AUTHORIZATION_CONFIG_EXPIRATION + "_" + type.name());
		this.isExpirationEnable = item.item_order > 0 ? true: false;
		this.intervalsTime = (item.text_value != null && !item.text_value.equals("")) ? Intervals.valueOf(item.text_value) : Intervals.DAY;
		this.time = item.int_value;	
	}

	public String getRegExpresion() 
	{
		return regExpresion;
	}

	public void setRegExpresion(String regExpresion) 
	{
		this.regExpresion = regExpresion;
	}

	public String getMessage() 
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public AuthorizationType getType() 
	{
		return type;
	}

	public void setType(AuthorizationType type) 
	{
		this.type = type;
	}

	public boolean isEnable() {
		return isEnable;
	}

	public void setEnable(boolean isEnable) 
	{
		this.isEnable = isEnable;
	}

	public Intervals getIntervalsTime() 
	{
		return intervalsTime;
	}

	public void setIntervalsTime(Intervals intervalsTime) 
	{
		this.intervalsTime = intervalsTime;
	}

	public int getTime() 
	{
		return time;
	}

	public void setTime(int time) 
	{
		this.time = time;
	}

	public boolean getIsExpirationEnable() 
	{
		return isExpirationEnable;
	}

	public void setIsExpirationEnable(boolean isExpirationEnable)
	{
		this.isExpirationEnable = isExpirationEnable;
	}

	public static PasswordConfig get(AuthorizationType type)
	{
		PasswordConfig passwordConfig = null;
		
		if(type.equals(AuthorizationType.OWNER))
		{
			if(passwordConfigOwner == null){
				passwordConfig = new PasswordConfig(type);
				passwordConfigOwner = passwordConfig;
			}else{
				passwordConfig = passwordConfigOwner;
			}
		}else if(type.equals(AuthorizationType.USER))
		{
			if(passwordConfigUser == null){
				passwordConfig = new PasswordConfig(type);
				passwordConfigUser = passwordConfig;
			}else{
				passwordConfig = passwordConfigUser;
			}
		}
		
		return passwordConfig;
	}

	public boolean save() 
	{
		boolean save = false;
		ConfigItem item;

		try {

			item = ConfigItem.lookup(AUTHORIZATION_CONFIG_EXPRESSION + "_" + type.name());
			item.setTextValue(regExpresion);
			item.setIntValue(isEnable ? 1 : 0);
			item.commit();

			item = ConfigItem.lookup(AUTHORIZATION_CONFIG_MESSAGE + "_" + type.name());
			item.setTextValue(message);
			item.commit();

			item = ConfigItem.lookup(AUTHORIZATION_CONFIG_EXPIRATION + "_" + type.name());
			item.setTextValue(intervalsTime == null ? Intervals.DAY.name() : intervalsTime.name());
			item.setIntValue(time);
			item.setItemOrder(isExpirationEnable ? 1 : 0);
			item.commit();
			
			if(type.equals(AuthorizationType.OWNER)){
				passwordConfigOwner = null;
			}else if (type.equals(AuthorizationType.USER)){
				passwordConfigUser = null;
			}

			save = true;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return save;
	}

	public static void clear(AuthorizationType type) 
	{
		
		String sql = "DELETE from config_item where name = ? or name = ? or name = ?";//$NON-NLS-1$
		DBConnection connection = null;

		try {

			connection = new DBConnection();
			PreparedStatement pst = connection.prepare(sql);

			pst.setString(1,AUTHORIZATION_CONFIG_EXPRESSION + "_" + type.name());
			pst.setString(2, AUTHORIZATION_CONFIG_MESSAGE + "_" + type.name());
			pst.setString(3,AUTHORIZATION_CONFIG_EXPIRATION + "_" + type.name());

			connection.execute(pst);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				connection.close();
				connection = null;
			}
		}
	}

	public boolean isValid(String password) 
	{

		try {

			return !isEnable || password.matches(regExpresion);

		} catch (Exception e) {
		}

		return false;
	}

	public boolean isExpired(Timestamp timestamp) 
	{
		boolean expired = true;
		Calendar calendar = null;

		try {

			if ((!isExpirationEnable)) {
				return false;
			}else if (timestamp == null) {
				return true;
			} 

			calendar = Calendar.getInstance();
			calendar.setTimeInMillis(timestamp.getTime());

			switch (intervalsTime) {
			case DAY:
				calendar.add(Calendar.DAY_OF_MONTH, time);
				expired = calendar.compareTo(Calendar.getInstance()) >= 0 ? false : true;
				break;
			case WEEK:
				calendar.add(Calendar.WEEK_OF_MONTH, time);
				expired = calendar.compareTo(Calendar.getInstance()) >= 0 ? false  : true;
				break;
			default:
				break;
			}

		} catch (Exception e) {}

		return expired;
	}

}
