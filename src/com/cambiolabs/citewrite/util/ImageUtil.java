package com.cambiolabs.citewrite.util;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class ImageUtil 
{
	
	public static BufferedImage resize(BufferedImage originalImage, int scaledWidth, int scaledHeight) 
	{
		int height = originalImage.getHeight();
		if(height <= scaledHeight)
		{
			return originalImage;
		}
		
        BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaledBI.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
        g.dispose();
        
        return scaledBI;
    }
}
