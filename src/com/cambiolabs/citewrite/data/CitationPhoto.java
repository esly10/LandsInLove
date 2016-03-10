package com.cambiolabs.citewrite.data;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import com.cambiolabs.citewrite.db.DBObject;
import com.cambiolabs.citewrite.db.UnknownObjectException;
import com.google.gson.annotations.Expose;

public class CitationPhoto extends DBObject {
	
	@Expose	public int citation_photo_id = 0;
	@Expose	public int citation_id = 0;
	public String photo = null;
	public CitationPhoto() {
		super("citation_photo", "citation_photo_id", new String[]{"photo"});
	}

	public CitationPhoto(int citation_photo_id) throws UnknownObjectException {
		super("citation_photo", "citation_photo_id", new String[]{"photo"});

		if (citation_photo_id > 0) {
			this.citation_photo_id = citation_photo_id;
			this.populate();
		}
	}

	/**
	 * Gets the bytes representation of the photo
	 * 
	 * @return
	 */
	public byte[] getPhoto() {
		
		try 
		{
			if (citation_photo_id <= 0) 
			{
				return null;
			}
			
			return  IOUtils.toByteArray(getFileInputStreamPhoto());
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return null;
	}
	
	
	/**
	 * Gets the bytes representation of the photo
	 * 
	 * @return
	 */
	public InputStream getFileInputStreamPhoto() {
		
		ConfigItem itemPath;
		String dirName;
		File file;
		FileInputStream in;
		
		try 
		{
			if (citation_photo_id <= 0) 
			{
				return null;
			}
			
			itemPath = ConfigItem.lookup("IMG_PATH");	
		 	dirName= itemPath.text_value+"/";
		 	file = new File(dirName+citation_photo_id);
		    in = new FileInputStream(file);
		    
		    return  in;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return null;
	}
	
	
	public boolean exists()
	{
		try 
		{
			if (this.citation_photo_id <= 0) 
			{
				return false;
			}
			
			ConfigItem itemPath = ConfigItem.lookup("IMG_PATH");
		 	File file = new File(itemPath.text_value+"/"+citation_photo_id);
		    return file.exists();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return false;
	}
	
	/**
	 * Gets the bytes representation of the photo
	 * 
	 * @return
	 */
	public String getBase64Photo() {
		
		Base64 ba = new Base64();
		
		try 
		{
			return ba.encodeToString(getPhoto());
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return "";
	}
	
	
	private void setPhoto(String photo) {
		
		ConfigItem itemPath = null;
		
		if (photo != null && photo.length() == 0) {
            return;
        }
		
        try {
    		itemPath = ConfigItem.lookup("IMG_PATH");	
 			BufferedImage imag = ImageIO.read(new ByteArrayInputStream( Base64.decodeBase64(photo)));
 			ImageIO.write(imag, "jpg", new File(itemPath.getTextValue(), String.valueOf(this.citation_photo_id)));
		} catch (Exception e) {
			e.printStackTrace();
		}
        
	}

	public int getPhotoId() {
		return citation_photo_id;
	}
	
	public boolean commit()
	{
		boolean save = false;
		save = super.commit();
		if(save){
			 setPhoto(this.photo);
		}
		return save;
		
	}



}
