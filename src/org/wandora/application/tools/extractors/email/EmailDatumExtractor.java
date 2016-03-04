/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2016 Wandora Team
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
 * IdentityDatumExtractor.java
 *
 * Created on 12. heinäkuuta 2005, 13:44
 */

package org.wandora.application.tools.extractors.email;

import org.wandora.application.tools.extractors.*;
import org.wandora.application.*;
import org.wandora.application.tools.extractors.datum.DataStructure;
import org.wandora.application.tools.extractors.datum.DatumExtractor;
import org.wandora.application.tools.extractors.datum.ExtractionException;
import org.wandora.application.tools.oldies.UploadFileOld;
import java.awt.image.*;
import java.util.*;
import java.text.SimpleDateFormat;
import org.wandora.utils.*;
import java.awt.Graphics;
import java.io.*;

/**
 *
 * @author olli
 */
public class EmailDatumExtractor implements DatumExtractor {
    
    private WandoraHolder wandoraAdminHolder;
    private DataStructure lastData=null;
    private String pictureStoreDir;
    
    /** Creates a new instance of IdentityDatumExtractor */
    public EmailDatumExtractor(WandoraHolder wandoraAdminHolder) {
        this(wandoraAdminHolder,"/");
    }
    public EmailDatumExtractor(WandoraHolder wandoraAdminHolder,String pictureStoreDir) {
        this.pictureStoreDir=pictureStoreDir;
        this.wandoraAdminHolder=wandoraAdminHolder;
    }
    
    public static BufferedImage scaleImage(BufferedImage img,int width,int height,boolean crop){
        double sx=(double)width/(double)img.getWidth();
        double sy=(double)height/(double)img.getHeight();
        if(!crop){
            if(sx<sy) sy=sx;
            else sx=sy;
        }
        else{
            if(sx>sy) sy=sx;
            else sx=sy;            
        }

        BufferedImage scaled=new java.awt.image.BufferedImage(width,height,java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics g=scaled.getGraphics();
        int scaledw=(int)(sx*img.getWidth()+0.5);
        int scaledh=(int)(sy*img.getHeight()+0.5);
        final Object notifyThis=new Object();
        synchronized(notifyThis){
            if(!g.drawImage(img,-(scaledw-width)/2,-(scaledh-height)/2,scaledw,scaledh,new java.awt.image.ImageObserver(){
                public boolean imageUpdate(java.awt.Image image,int infoflags,int x,int y,int width,int height){
                    if((infoflags&java.awt.image.ImageObserver.ALLBITS)!=0){
                        synchronized(notifyThis){
                            notifyThis.notify();
                        }
                        return false;
                    }
                    return true;
                }
            })){
                try{
                    notifyThis.wait();
                }catch(InterruptedException e){e.printStackTrace();}
            }
        }
        return scaled;
    }

    private static final SimpleDateFormat sdfDate=new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat sdfTime=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public java.util.Map next(DataStructure data, org.wandora.piccolo.Logger logger) throws ExtractionException {
        if(data==lastData) return null;
        lastData=data;
        Map<String,Object>map=(Map<String,Object>)data.handle;
        // WandoraAdminManager manager=wandoraAdminHolder.getWandoraAdmin().getManager();
        try{
            BufferedImage img=(BufferedImage)map.get("image");
            String msgId=(String)map.get("id");
            logger.writelog("DBG","Extracting data from message "+msgId);
            
            msgId=msgId.replaceAll("[<>]","");
            msgId=msgId.replaceAll("@","_at_");
            map.put("id",msgId);
            
            String fileNameBase=msgId;

            BufferedImage tn=scaleImage(img, 88,66, true);
            //String tnURL=uploadImage(tn,manager,pictureStoreDir+fileNameBase+"_tn.jpg","jpg");

            BufferedImage tv=scaleImage(img, 768,576, false);
            //String tvURL=uploadImage(tv,manager,pictureStoreDir+fileNameBase+"_tv.jpg","jpg");

            //String originalURL=uploadImage(img,manager,pictureStoreDir+fileNameBase+".jpg","jpg");

            Date sentDate=(Date)map.get("sent");
            map.put("name","submitted image "+msgId);
            //map.put("tnimgurl",tnURL);
            //map.put("tvimgurl",tvURL);
            //map.put("originalimgurl",originalURL);
            map.put("date",sdfDate.format(sentDate));
            map.put("time",sdfTime.format(sentDate));
            
            logger.writelog("INF","Extracting email message: "+msgId+", sender:"+map.get("sender"));
            logger.writelog("DBG","Making topics for message "+msgId);
        }catch(Exception e){
            throw new ExtractionException(e);
        }
        
        return map;
    }

    public double getProgress() {
        return -1;
    }
    
}
