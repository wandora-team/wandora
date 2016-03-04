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
 *
 *
 * ImageCacheSimple.java
 *
 * Created on 18. tammikuuta 2006, 13:33
 */

package org.wandora.piccolo.actions;
import org.wandora.piccolo.*;
import org.wandora.application.*;
import org.wandora.utils.*;
import java.util.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.*;
import javax.imageio.stream.*;
import java.util.regex.*;
import org.w3c.dom.*;
//import com.sun.image.codec.jpeg.*;
import java.net.*;
import java.awt.*;



/**
 *
 * @author olli, akivela
 */

/*
    <mapentry>
        <key>imagecache</key>
        <action xp:class="org.wandora.piccolo.actions.ImageCache">
            <cachedir>C:/Program files/Apache Software Foundation/Tomcat 5.5/webapps/wandora/WEB-INF/cache/</cachedir>
            <authorizer xp:idref="authorizer"/>
            <profile id="500crop" width="500" height="200" bgcolor="255,0,0" crop="1"
                watermark="http://192.168.1.48:8080/wandora/copy.png" watermarkmode="lowerright"
                urlfilter="http://192\.168\.1\.48.*"/>
            <profile id="500nocrop" width="500" height="200" bgcolor="255,0,0" crop="0"/>
            <profile id="50nocrop" width="50" height="50" bgcolor="255,0,0" crop="0"/>
            <profile id="low" width="-1" height="-1" bgcolor="255,0,0" crop="0" quality="10"/>
        </action>
    </mapentry>
 */


public class ImageCacheSimple implements Action,XMLParamAware {

    private Logger logger;
    private String cachedir;
    private HashMap<String,Profile> profiles;

    private String defaultProfile=null;
    private HttpAuthorizer authorizer = null;
    private HttpAuthorizer secondaryAuthorizer = null;
    private String secondaryUrl = null;
    private String secondarySavedir = null;




    /** Creates a new instance of ImageCacheSimple */
    public ImageCacheSimple() {
        profiles=new HashMap<String,Profile>();
    }




    public void xmlParamInitialize(Element element, XMLParamProcessor processor) {
        logger=(Logger)processor.getObject("logger");
        if(logger==null) logger=new SimpleLogger();

        NodeList nl=element.getChildNodes();
        for(int i=0;i<nl.getLength();i++){
            Node n=nl.item(i);
            if(n instanceof Element){
                Element e=(Element)n;
                if(e.getNodeName().equals("cachedir")){
                    cachedir=XMLParamProcessor.getElementContents(e);
                }
                else if(e.getNodeName().equals("authorizer")){
                    try{
                        authorizer=(HttpAuthorizer)processor.createObject(e);
                    }catch(Exception ex){
                        logger.writelog("ERR",ex);
                    }
                }
                else if(e.getNodeName().equals("secondarysource")){
                    NodeList ss=e.getChildNodes();
                    for(int j=0;j<ss.getLength();j++){
                        Node ssn=ss.item(j);
                        if(ssn instanceof Element) {
                            Element sse=(Element)ssn;
                            if(sse.getNodeName().equals("authorizer")) {
                                try {
                                    secondaryAuthorizer = (HttpAuthorizer)processor.createObject(sse);
                                }catch(Exception ex) {
                                    logger.writelog("WRN",ex);
                                }
                            }
                            else if(sse.getNodeName().equals("url")) {
                                secondaryUrl = XMLParamProcessor.getElementContents(sse);
                            }
                            else if(sse.getNodeName().equals("savedir")) {
                                secondarySavedir = XMLParamProcessor.getElementContents(sse);
                            }
                        }
                    }
                }
                else if(e.getNodeName().equals("profile")){
                    String id=e.getAttribute("id");
                    String widths=e.getAttribute("width");
                    String heights=e.getAttribute("height");
                    if(widths.length()==0) widths="-1";
                    if(heights.length()==0) heights="-1";
                    int width=-1;
                    int height=-1;
                    try{
                        width=Integer.parseInt(widths);
                        height=Integer.parseInt(heights);
                    }
                    catch(NumberFormatException nfe){
                        logger.writelog("WRN",nfe);
                    }
                    if(width<=0) width=-1;
                    if(height<=0) height=-1;
                    java.awt.Color bgcolor=java.awt.Color.BLACK;
                    String cs=e.getAttribute("bgcolor");
                    if(cs.length()>0){
                        String[] components=cs.split(",",3);
                        if(components.length!=3) logger.writelog("WRN","Invalid bgcolor format");
                        else{
                            try{
                                int r=Integer.parseInt(components[0]);
                                int g=Integer.parseInt(components[1]);
                                int b=Integer.parseInt(components[2]);
                                bgcolor=new java.awt.Color(r,g,b);
                            }
                            catch(NumberFormatException nfe){
                                logger.writelog("WRN",nfe);
                            }
                        }
                    }

                    double scale=1.0;
                    String scales=e.getAttribute("scale");
                    if(scales.length()>0){
                        try{
                            scale=Double.parseDouble(scales);
                        }catch(NumberFormatException nfe){
                            logger.writelog("WRN",nfe);
                        }
                    }

                    String crops=e.getAttribute("crop");
                    boolean crop=false;
                    if(crops.equalsIgnoreCase("true") || crops.equals("1")) crop=true;

                    String noExtraCanvasS=e.getAttribute("noextracanvas");
                    boolean noExtraCanvas=false;
                    if(noExtraCanvasS.equalsIgnoreCase("true") || noExtraCanvasS.equals("1")) noExtraCanvas=true;

                    int quality=80;
                    String qualitys=e.getAttribute("quality");
                    if(qualitys.length()>0) {
                        try{
                            quality=Integer.parseInt(qualitys);
                            if(quality<0) quality=0;
                            if(quality>100) quality=100;
                        }
                        catch(NumberFormatException nfe){
                            logger.writelog("WRN",nfe);
                        }
                    }

                    String watermark=e.getAttribute("watermark");
                    if(watermark != null && watermark.length() == 0) watermark = null;
                    String watermarkMode=null;
                    if(watermark!=null){
                        watermarkMode=e.getAttribute("watermarkmode");
                        if(watermarkMode==null) watermarkMode="lowerright";
                    }

                    String urlfilter=e.getAttribute("urlfilter");
                    if(urlfilter!= null && urlfilter.length() == 0) urlfilter = null;

                    String cacheDir=e.getAttribute("cachedir");
                    if(cacheDir!= null && cacheDir.length() == 0) cacheDir = null;

                    String errorimg=e.getAttribute("errorimg");
                    if(errorimg!= null && errorimg.length() == 0) errorimg= null;

                    ImageMaker im=null;
                    String custom=e.getAttribute("customImageMaker");
                    if(custom!=null && custom.equalsIgnoreCase("true")){
                        try{
                            im=(ImageMaker)processor.processElement(e);
                        }catch(Exception ex){
                            logger.writelog("WRN","Exception making custom ImageMaker",ex);
                            continue;
                        }
                    }
                    else{
                        im=new DefaultImageMaker(width,height,bgcolor,crop,noExtraCanvas,scale,watermark,watermarkMode);
                    }
                    Profile profile=new Profile(id,quality,urlfilter,cacheDir,errorimg,im);
                    profiles.put(id,profile);
                    if(defaultProfile==null) defaultProfile=id;
                }
            }
        }
        if(cachedir==null){
            logger.writelog("WRN","no cachedir provided for ImageCache");
        }
    }




    /**
     * Encodes an url and profile into a name of a real file that will contain
     * the cached data. Same url and profile must always encode to same file name
     * and different urls and profiles must encode to different file names.
     */
    protected String encodeFileName(String url, Profile profile) {
        if(profile.cacheDir!=null) return profile.cacheDir + url.hashCode() + "-" + profile.id;
        else return cachedir + url.hashCode() + "-" + profile.id;
    }




    /**
     * Checks if the specified (encoded) file is cached. Effectively just
     * checks if the given file exists allready.
     */
    protected boolean haveCached(String filename){
        File f=new File(filename);
        return f.exists();
    }



    /**
     * Reads a file and writes it to the output stream.
     *
     * Note: In the future this method could cache often used images so they don't
     *       need to be read from disk all the time.
     */
    protected void sendFile(String filename, OutputStream out) throws IOException {
        FileInputStream fis = null;
        try {
            fis=new FileInputStream(filename);
            byte[] buf=new byte[128];
            int r=0;
            while( (r=fis.read(buf))!=-1 ){
                out.write(buf,0,r);
            }
            out.flush();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            if(fis != null) {
                fis.close();
            }
        }
    }

    /**
     * Writes an image to disk.
     */
    protected void writeFile(String filename, BufferedImage img, Profile profile) throws IOException {
        int quality=( profile != null ? profile.quality : 85 );
        try {
            ImageWriter writer=ImageIO.getImageWritersByFormatName("jpeg").next();
            IIOImage iioi=new IIOImage(img,null,null);
            ImageWriteParam param=writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality((float)quality / 100.0f);
            FileImageOutputStream output=new FileImageOutputStream(new File(filename));
            writer.setOutput(output);
            writer.write(null,iioi,param);
            output.close();
/*
            FileOutputStream out=new FileOutputStream(filename);

            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(img);
            param.setQuality((float)quality / 100.0f, false);
            encoder.setJPEGEncodeParam(param);
            encoder.encode(img);
            out.close();
            */
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally{ }
    }

    public BufferedImage readImage(String url, HttpAuthorizer authorizer) throws IOException {
        return readImage(new URL(url), authorizer);
    }

    public BufferedImage readImage(URL url, HttpAuthorizer authorizer) throws IOException {
        URLConnection con=null;
        try{
            if(authorizer!=null) con=authorizer.getAuthorizedAccess(url);
            else {
                con=url.openConnection();
                Wandora.initUrlConnection(con);
            }
        }
        catch(Exception e) {
            if(e instanceof IOException) throw (IOException)e;
            else throw new IOException("Couldn't open authorized connection");
        }
        return ImageIO.read(con.getInputStream());
    }


    protected BufferedImage makeIMG(String url,Profile profile) throws Exception {
        return makeIMG(url,profile, null);
    }

    /**
     * Uses the image maker of the profile to make an image with the given url.
     * If url does not resolve as image secondary url is generated and tried.
     * If secondary url resolves the image is saved to secondary save directory and
     * the method returns valid image. Otherwise an exception is thrown.
     *
     * Notice: method should not use global variables to save data since
     * there may be multiple threads executing the method simultaneusly!!
     */
    protected BufferedImage makeIMG(String url,Profile profile, String secondaryID) throws Exception {
        BufferedImage original = null;
        try {
            original=readImage(new URL(url), authorizer);
        }
        catch(Exception e) {
            if(original == null && secondaryUrl != null && secondaryID != null) {
                String secondaryUrlString = secondaryUrl.replaceAll("__ID__", secondaryID);
                logger.writelog("Url '"+url+"' failed. Trying secondary url '"+secondaryUrlString+".");
                original=readImage(new URL(secondaryUrlString), secondaryAuthorizer);
                if(secondarySavedir != null) {
                    try {
                        String saveFile = secondarySavedir+"/"+secondaryID+".jpg";
                        logger.writelog("Saving secondary image to "+saveFile);
                        writeFile(saveFile, original, null);
                    }
                    catch(Exception e2) {
                        logger.writelog("WRN","Unable to save image to file "+secondarySavedir+"/"+secondaryID+".jpg");
                        throw e2;
                    }
                }
                else {
                    logger.writelog("WRN","No save directory defined for secondary source image!");
                    throw e;
                }
            }
            else {
                logger.writelog("Unable to make image!");
                throw e;
            }
        }
        return profile.imageMaker.makeImg(this,original);
    }




    public void doAction(Profile profile, String url, String imageID, boolean reload, javax.servlet.ServletResponse response){
        String fn;
        if(imageID!=null) fn=encodeFileName("id:"+imageID, profile);
        else fn=encodeFileName(url, profile);

        try {
            // if reload is forced or file is not cached, make the image and write it to cache.
            if(reload || !haveCached(fn)) {
                // logger.writelog("DBG","Getting image from "+url);
                try {
                    BufferedImage img = makeIMG(url, profile, imageID);
                    writeFile(fn, img, profile);
                }
                catch(Exception e){
                    if(profile.errorimg!=null){
                        fn=profile.errorimg;
                    }
                }
            }
            else {
                // logger.writelog("DBG","Using cached image for "+url);
            }
            // send the image
            response.setContentType("image/jpeg");
            sendFile(fn, response.getOutputStream());
        }
        catch(Exception e){
            logger.writelog("DBG", e);
        }
    }



    public void doAction(User user, javax.servlet.ServletRequest request, javax.servlet.ServletResponse response, Application application) {
        String url = request.getParameter("url");
        String imageID = request.getParameter("imageid");
        String reloads = request.getParameter("reload");

        boolean reload=false;
        if(reloads!=null && (reloads.equalsIgnoreCase("true") || reloads.equals("1"))) reload=true;
        String prof=request.getParameter("profile");
        if(prof==null) prof=defaultProfile;
        Profile profile=profiles.get(prof);
        if(profile==null) {logger.writelog("DBG","Invalid profile "+prof); return;}

        if(profile.urlfilter!=null && !profile.urlfilter.matcher(url).matches()){
            logger.writelog("WRN","URL "+url+" does not match filter in profile "+prof);
            return;
        }

        doAction(profile,url,imageID,reload,response);
    }




    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------



    




    public static class Profile {
        public String id; // id of the profile; used in url
        public Pattern urlfilter; // regex filter to restrict the use of this profile
        public int quality; // quality at which the cached image is saved (for jpg 0-100)
        public String cacheDir; // cache dir for profile
        public String errorimg; // image to use if unable to get the requested image

        public ImageMaker imageMaker; // ImageMaker that transforms original image into cached image


        public Profile(String id,int quality,String urlfilter,String cacheDir,String errorimg,ImageMaker imageMaker){
            this.id=id;
            this.quality=quality;
            this.urlfilter=null;
            this.errorimg=errorimg;
            if(urlfilter!=null) this.urlfilter=Pattern.compile(urlfilter);
            this.cacheDir=cacheDir;
            this.imageMaker=imageMaker;
        }
    }





    public static interface ImageMaker {
        /**
         * Transforms the original image to a cached version. The cached version
         * may be resized, rotated or have any other effects applied to it.
         * Does not need to check cache for existing image, that has been
         * done before call to this method.
         */
        public BufferedImage makeImg(ImageCacheSimple cache,BufferedImage original);
    }

    public static class DefaultImageMaker implements ImageMaker {
        public int width; // width of the image or -1 to determine it automatically
        public int height; // height of the image or -1 to determine it automatically
        public java.awt.Color bgColor; // bg color of image if the original image doesn't fill the cached image completely
        boolean crop; // if true, original image is cropped so that it fills the cached image, otherwise it is scaled to fit in the cached image
        boolean noExtraCanvas;
        public String watermark; // image file for watermark
        public String watermarkMode; // how to place watermark, currently only supports "lowerright"
        public double scale=1.0; // scale image after width, height and cropping mode have been decided
        public BufferedImage watermarkImage=null; // watermark image is cached here when it is first needed



        public DefaultImageMaker(int width,int height,java.awt.Color bgColor,boolean crop, boolean noExtraCanvas, double scale,String watermark,String watermarkMode){
            this.width=width;
            this.height=height;
            this.bgColor=bgColor;
            this.crop=crop;
            this.noExtraCanvas=noExtraCanvas;
            this.scale=scale;
            this.watermark=watermark;
            this.watermarkMode=watermarkMode;
        }

        public BufferedImage makeImg(ImageCacheSimple cache,BufferedImage original){

            int width=this.width;
            int height=this.height;

            int ow=original.getWidth();
            int oh=original.getHeight();
            double or=(double)ow/(double)oh;
            int nw=0;
            int nh=0;
            if(width==-1 && height!=-1) {
                width=(int)(height*or+0.5);
            }
            else if(width!=-1 && height==-1){
                height=(int)(width/or+0.5);
            }
            else if(width==-1 && height==-1){
                height=oh;
                width=ow;
            }

            double nr=(double)width/(double)height;

            if(crop){
                if(or>nr){
                    nh=height;
                    nw=(int)(height*or+0.5);
                }
                else{
                    nw=width;
                    nh=(int)(width/or+0.5);
                }
            }
            else{
                if(or>nr){
                    nw=width;
                    nh=(int)(width/or+0.5);
                }
                else{
                    nh=height;
                    nw=(int)(height*or+0.5);
                }
            }

            if(scale!=1.0){
                nw=(int)((double)ow+scale*((double)nw-(double)ow)+0.5);
                nh=(int)((double)oh+scale*((double)nh-(double)oh)+0.5);
            }

            if(noExtraCanvas) {
                width = nw;
                height = nh;
            }

            BufferedImage img = null;
            img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = img.createGraphics();
            g2.setColor(bgColor);
            g2.fillRect(0,0, width,height);

            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
            g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY );

            Image resized = original.getScaledInstance( nw, nh, BufferedImage.SCALE_SMOOTH );

            g2.drawImage(resized, (int)(width/2.0-nw/2.0+0.5), (int)(height/2.0-nh/2.0+0.5), null);
            //g2.drawImage(original, (int)(width/2.0-nw/2.0+0.5), (int)(height/2.0-nh/2.0+0.5), nw, nh, null);

            if(watermark!=null){
                if(watermarkImage==null){
                    try{
                        watermarkImage=cache.readImage(new URL(watermark), cache.authorizer);
                    }
                    catch(IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
                if(watermarkMode.equals("lowerright")){
                    if(watermarkImage != null) {
                        try {
                            int x=width-watermarkImage.getWidth();
                            int y=height-watermarkImage.getHeight();
                            g2.drawImage(watermarkImage,x,y,null);
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            return img;

        }
    }
}
