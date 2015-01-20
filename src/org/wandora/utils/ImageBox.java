/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2015 Wandora Team
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * 
 *
 * ImageBox.java
 *
 * Created on November 8, 2004, 6:01 PM
 */

package org.wandora.utils;



import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.lang.*;
//import com.sun.image.codec.jpeg.*;
import javax.imageio.*;
import javax.imageio.stream.*;


/**
 *
 * @author  akivela
 */
public class ImageBox {
    
    /** Creates a new instance of ImageBox */
    public ImageBox() {
    }
    
    
    
    public static void makeThumbnail(String ins, String outs, int width, int height, int quality) throws Exception {
        // load image from INFILE
        BufferedImage image = null;
        try {
            URL url = new URL(ins);
            image = ImageIO.read(url);
        }
        catch (Exception e1) {
            if(ins.startsWith("file:")) {
                ins = IObox.getFileFromURL(ins);
            }
//            if(ins.startsWith("file:/")) {
//                ins = ins.substring(6);
//            }
            File imageFile = new File(ins); // remove prefix "file://"
            image = ImageIO.read(imageFile);
        }

        if(image != null) {
            // determine thumbnail size from WIDTH and HEIGHT
            int thumbWidth = width;
            int thumbHeight = height;
            double thumbRatio = (double)thumbWidth / (double)thumbHeight;
            int imageWidth = image.getWidth(null);
            int imageHeight = image.getHeight(null);
            double imageRatio = (double)imageWidth / (double)imageHeight;
            if (thumbRatio < imageRatio) {
                thumbHeight = (int)(thumbWidth / imageRatio);
            } else {
                thumbWidth = (int)(thumbHeight * imageRatio);
            }

            // draw original image to thumbnail image object and
            // scale it to the new size on-the-fly
            BufferedImage thumbImage = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics2D = thumbImage.createGraphics();
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);

            // save thumbnail image to OUTFILE
            ImageWriter writer=ImageIO.getImageWritersByFormatName("jpeg").next();
            IIOImage iioi=new IIOImage(thumbImage,null,null);
            ImageWriteParam param=writer.getDefaultWriteParam();
            param.setCompressionMode(param.MODE_EXPLICIT);
            param.setCompressionQuality((float)quality / 100.0f);
            FileImageOutputStream output=new FileImageOutputStream(new File(outs));
            writer.setOutput(output);
            writer.write(null,iioi,param);
            output.close();
            
/*            
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outs));
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(thumbImage);
            quality = Math.max(0, Math.min(quality, 100));
            param.setQuality((float)quality / 100.0f, false);
            encoder.setJPEGEncodeParam(param);
            encoder.encode(thumbImage);
            out.close(); */
        }
    }

    
    
    
}
