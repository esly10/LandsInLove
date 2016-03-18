package com.cambiolabs.citewrite.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

//import com.cambiolabs.citewrite.data.CiteDeviceFields;
//import com.cambiolabs.citewrite.data.CiteField;
//import com.cambiolabs.citewrite.data.CiteFields;
import com.cambiolabs.citewrite.data.ConfigItem;
//import com.cambiolabs.citewrite.data.FieldOption;
//import com.cambiolabs.citewrite.data.HotListColumnMetaData;
//import com.cambiolabs.citewrite.data.LateFee;
import com.cambiolabs.citewrite.data.PasswordConfig;
import com.cambiolabs.citewrite.data.PasswordConfig.AuthorizationType;
import com.cambiolabs.citewrite.data.PasswordConfig.Intervals;
import com.cambiolabs.citewrite.data.User;
import com.cambiolabs.citewrite.db.Column;
import com.cambiolabs.citewrite.db.DBConnection;
import com.cambiolabs.citewrite.db.DBFilter;
import com.cambiolabs.citewrite.db.DBFilterList;
import com.cambiolabs.citewrite.db.UnknownObjectException;
//import com.cambiolabs.citewrite.ecommerce.CreditCardType;
//import com.cambiolabs.citewrite.ecommerce.merchant.Merchant;
//import com.cambiolabs.citewrite.ecommerce.merchant.MerchantException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class AdministrationController extends MultiActionController
{
	protected final Log logger = LogFactory.getLog(getClass());
	
	public ModelAndView onLoad(HttpServletRequest request,
			HttpServletResponse response)
			throws Exception {
			return null;
			}
	
}

	