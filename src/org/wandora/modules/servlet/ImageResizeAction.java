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
 */
package org.wandora.modules.servlet;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.*;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.servlet.ModulesServlet.HttpMethod;
import org.wandora.modules.usercontrol.User;

/**
 * <p>
 * An action that reads images from a source directory and resizes them
 * to fit specified dimensions. Can optionally also watermark the images.
 * One or more image profiles are defined in the initialisation parameters,
 * images may then be resized to fit any of the defined profiles. Typically you
 * will use the caching with this action so that the fairly costly resize
 * operation is only done once for each image and profile.
 * </p>
 * <p>
 * Images are read from one or more image directories. Each directory is specified
 * as a source. Typically the source directories themselves should not be open
 * to the internet, they should only be accessible locally to the servlet.
 * Images are retrieved using an image id, which can really be any string uniquely
 * defining the image file. Typically it is some part or the full file name.
 * One or more sources can be specified which translate the image id into an actual
 * full file path for the image. This is done using regular expression matching and
 * replacement. More complicated image file resolution requires extending this
 * action.
 * </p>
 * <p>
 * In the initialisation parameters, you can specify the default profile with
 * a parameter named defaultProfile. This defaults to null, i.e. no default
 * profile. The profile used must be specified in the request or an error
 * is generated. If a default profile is specified, that will be used if the
 * request doesn't specify otherwise.
 * </p>
 * <p>
 * profileParamKey parameter can be used to specify which HTTP request parameter
 * contains the profile name. This defaults to "profile".
 * </p>
 * <p>
 * imageIdParamKey parameter can be used to specify which HTTP request parameter
 * contains the image id. This defaults to "imageid".
 * </p>
 * <p>
 * Image sources are defined using parameter names "source.sourcekey.pattern" 
 * and "source.sourcekey.replacement". The sourcekey part in the middle is
 * replaced with any alphanumeric string identifying the source. It has no other
 * significance except to link the two different parameters. For example,
 * to define two different sources, you would use a total of four keys:
 * </p>
 * <pre>
 * source.source1.pattern = ...
 * source.source1.replacement = ...
 * source.source2.pattern = ...
 * source.source2.replacement = ...
 * </pre>
 * <p>
 * The pattern is a regular expression that the image id needs to match for this
 * source to be applicable. Replacement is a replacement string, with back
 * references to the matching done with dollar sign and number combination. The
 * replacement is done using default Java String.replaceAll. The result of this
 * should be a URL to the actual image. Typically a URL to a local file but
 * not necessarily.
 * </p>
 * <p>
 * Sources are tried in the order they are specified in initialisation 
 * parameters until one is found where the pattern matches and the replacement
 * results in a URL from which an actual image can be read. It is not considered
 * to be an error if the URL points to nothing and nothing can be read from there,
 * in such a case the next source will simply be tried.
 * </p>
 * <p>
 * Image profiles are defined in a similar manner. The pattern for the parameter
 * names is "profile.profilename.property" where profilename is substituted for
 * the profile name and property for the property you want to define for the
 * profile. Unlike the sources, the profile name is significant here in the
 * sense that it is referred to with this name in the HTTP requests and the
 * defaultProfile parameter.
 * </p>
 * <p>
 * Supported profile properties are width, height, crop, noextracanvas, bgcolor,
 * quality, watermark, watermarkmode and errorimage.
 * </p>
 * <p>
 * Width and height specify the dimensions of the image. If either one is -1,
 * or left undefined, then the image is scaled according to the other dimension
 * retaining aspect ratio. You can also leave both undefined to do no scaling at
 * all. If both are defined, the image is scaled down to fit the dimension so that
 * it's at most their size. If noextracanvas is set to true, this is the image
 * that is returned. Otherwise blank bars are added as needed to make the image
 * exactly the size of the specified dimensions. These bars are black by default
 * but can be changed to other colour with the bgcolor parameter.
 * the returned image will have exactly those dimensions. If crop
 * is set to true, original image is scaled and cropped so that it completely
 * fills the specified dimensions. Otherwise the image is scaled to fit the 
 * dimensions with black bars at edges as needed. The colour of the bars can
 * but can be changed to other colour with the bgcolor parameter, which is
 * specified with the #XXXXXX format. Quality of the returned jpeg
 * image can be specified with the quality parameter.
 * </p>
 * <p>
 * A watermark can be added by specifying the watermark image URL in the
 * watermark parameter. The only supported watermarkmode currently is
 * "lowerright". In the future this parameter could be used to specify other
 * placements for the watermark. 
 * </p>
 * <p>
 * You can also specify an error image for the profile which will be used if no
 * source image was found, or any other error occurred while processing the image.
 * The error image is not scaled according to the profile settings, it should be
 * preprocessed to be exactly as it needs to be.
 * </p>
 * <p>
 * To change the image source resolution to something more complicated, override
 * the init method and add an ImageSource object in the imageSources map.
 * ImageSource is a simple interface which just transforms an image id into
 * a URL pointing to the image. If needed, you can also override readImage to
 * change how the image is read. You could use a custom ad-hoc URL scheme and do
 * your own handling for that here if the image cannot be retrieved using a normal
 * URL.
 * </p>
 * 
 * @author olli
 */


public class ImageResizeAction extends CachedAction {
    
    protected boolean exceptionOnImageRead=false;
    
    protected String defaultProfile=null;
    
    protected String imageIdParamKey="imageid";
    protected String profileParamKey="profile";
    protected HashMap<String,ImageProfile> imageProfiles;
    protected LinkedHashMap<String,ImageSource> imageSources;
    
    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        requireLogging(manager, deps);
        return deps;
    }

    @Override
    protected void returnOutput(InputStream cacheIn, HttpServletResponse resp) throws IOException {
        try{
            Map<String,String> metadata=readMetadata(cacheIn);
            String contenttype=metadata.get("contenttype");
            resp.setContentType(contenttype);
            super.returnOutput(cacheIn, resp);
        }
        finally{
            cacheIn.close();
        }
    }

    
    
    @Override
    public void init(ModuleManager manager, HashMap<String, Object> settings) throws ModuleException {
        imageProfiles=new HashMap<String,ImageProfile>();
        imageSources=new LinkedHashMap<String,ImageSource>();
        
        Object o=settings.get("defaultProfile");
        if(o!=null) defaultProfile=o.toString();
        
        o=settings.get("imageIdParamKey");
        if(o!=null) imageIdParamKey=o.toString();
        
        o=settings.get("profileParamKey");
        if(o!=null) profileParamKey=o.toString();
        
        for(Map.Entry<String,Object> e : settings.entrySet()){
            String key=e.getKey();
            if(key.startsWith("profile.")){
                key=key.substring("profile.".length());
                
                int ind=key.indexOf(".");
                if(ind<0) continue;
                
                String profileName=key.substring(0,ind);
                key=key.substring(ind+1);
                
                ImageProfile profile=imageProfiles.get(profileName);
                if(profile==null) {
                    profile=new ImageProfile();
                    profile.id=profileName;
                    profile.imageMaker=new DefaultImageMaker();
                    imageProfiles.put(profileName,profile);
                }

                String value=e.getValue().toString();
                
                if(key.equalsIgnoreCase("quality")) profile.quality=Integer.parseInt(value);
                else if(key.equalsIgnoreCase("width")) ((DefaultImageMaker)profile.imageMaker).width=Integer.parseInt(value);
                else if(key.equalsIgnoreCase("height")) ((DefaultImageMaker)profile.imageMaker).height=Integer.parseInt(value);
                else if(key.equalsIgnoreCase("bgcolor")) {
                    if(value.startsWith("#")) value=value.substring(1);
                    int r=Integer.parseInt(value.substring(0, 2),16);
                    int g=Integer.parseInt(value.substring(2, 4),16);
                    int b=Integer.parseInt(value.substring(4, 6),16);
                    ((DefaultImageMaker)profile.imageMaker).bgColor=new java.awt.Color(r, g, b);
                }
                else if(key.equalsIgnoreCase("crop")) ((DefaultImageMaker)profile.imageMaker).crop=Boolean.parseBoolean(value);
                else if(key.equalsIgnoreCase("noextracanvas")) ((DefaultImageMaker)profile.imageMaker).noExtraCanvas=Boolean.parseBoolean(value);
                else if(key.equalsIgnoreCase("watermark")) ((DefaultImageMaker)profile.imageMaker).watermark=value;
                else if(key.equalsIgnoreCase("watermarkmode")) ((DefaultImageMaker)profile.imageMaker).watermarkMode=value;
                else if(key.equalsIgnoreCase("errorimage")) profile.error=value;
            }
            else if(key.startsWith("source.")){
                key=key.substring("source.".length());
                
                int ind=key.indexOf(".");
                if(ind<0) continue;
                
                String sourceName=key.substring(0,ind);
                key=key.substring(ind+1);
                
                ImageSource source=imageSources.get(sourceName);
                if(source==null) {
                    source=new PatternImageSource();
                    ((PatternImageSource)source).id=sourceName;
                    imageSources.put(sourceName,source);
                }

                String value=e.getValue().toString();
                
                if(key.equalsIgnoreCase("pattern")) ((PatternImageSource)source).pattern=Pattern.compile(value);
                else if(key.equalsIgnoreCase("replacement")) ((PatternImageSource)source).replacement=value;
                
            }
        }
                
        super.init(manager, settings);
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        if(defaultProfile!=null && !imageProfiles.containsKey(defaultProfile)) {
            logging.warn("Default profile \""+defaultProfile+"\" doesn't exist.");
        }
        
        super.start(manager);
    }

    @Override
    public void stop(ModuleManager manager) {
        
        super.stop(manager);
    }
    
    /**
     * Reads an image from the specified URL and returns it as a 
     * BufferedImage.
     * @param url The URL pointing to the image.
     * @return The image as a BufferedImage.
     * @throws IOException 
     */
    protected BufferedImage readImage(String url) throws IOException {
        return ImageIO.read(new URL(url));
    }
    
    /**
     * Writes the image into the given output stream. Does not perform
     * full image processing with the profile. Only uses the relevant output format
     * parameters from the profile. The default implementation only uses the quality.
     * 
     * @param img The image that must be written.
     * @param profile The profile used for the image.
     * @param out The output stream where the image is written.
     * @throws IOException 
     */
    protected void writeImage(BufferedImage img, ImageProfile profile, OutputStream out) throws IOException {
        int quality=( profile != null ? profile.quality : 85 );
        ImageOutputStream output = null;
        try {
            ImageWriter writer=ImageIO.getImageWritersByFormatName("jpeg").next();
            IIOImage iioi=new IIOImage(img,null,null);
            ImageWriteParam param=writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality((float)quality / 100.0f);
            output=new MemoryCacheImageOutputStream(out);
            writer.setOutput(output);
            writer.write(null,iioi,param);
        }
        catch(Exception e) {
            logging.warn(e);
        }
        finally { 
            if(output != null) {
                output.close();
            }
        }
    }
    
    
    @Override
    protected boolean doOutput(HttpServletRequest req, HttpMethod method, String action, OutputProvider out, User user) throws ServletException, IOException {
        String profileKey=req.getParameter(profileParamKey);
        if(profileKey==null || profileKey.length()==0) {
            if(defaultProfile!=null) profileKey=defaultProfile;
            else return false;
        }
        ImageProfile profile=imageProfiles.get(profileKey);
        if(profile==null) return false;
        
        String imageid=req.getParameter(imageIdParamKey);
        if(imageid==null || imageid.length()==0) return false;
                
        for(ImageSource source : imageSources.values()){
            String s=source.matchSource(imageid);
            if(s!=null){
                try {
                    BufferedImage img=readImage(s);
                    if(img==null) continue;
                    
                    BufferedImage processedImg=profile.imageMaker.makeImg(img);
                    if(processedImg==null) {
                        logging.warn("Image maker returned null");
                        break;
                    }
                    
                    OutputStream outStream=out.getOutputStream();
                    try {
                        HashMap<String,String> metadata=new HashMap<String,String>();
                        metadata.put("contenttype","image/jpeg");
                        writeMetadata(metadata, outStream);
                        writeImage(processedImg, profile, outStream);
                    }
                    finally{ 
                        outStream.close(); 
                    }
                    return true;
                    
                }
                catch(IOException ioe){
                    logging.debug("Couldn't read image "+s,ioe);
                }
            }
        }
        
        BufferedImage errorImage=profile.getErrorImage();
        if(errorImage!=null){
            OutputStream outStream=out.getOutputStream();
            try {
                HashMap<String,String> metadata=new HashMap<String,String>();
                metadata.put("contenttype","image/jpeg");
                writeMetadata(metadata, outStream);
                writeImage(errorImage, profile, outStream);
            }
            finally{ 
                outStream.close(); 
                return true;
            }
        }
        
        return false;
    }

    private LinkedHashMap<String,String> getCacheKeyParams(HttpServletRequest req, HttpMethod method, String action) {
        LinkedHashMap<String,String> params=new LinkedHashMap<String,String>();
        
        String profile=req.getParameter(profileParamKey);
        if(profile==null || profile.length()==0) {
            if(defaultProfile!=null) {
                profile=defaultProfile;
            }
            else return null;
        }
        
        String imageid=req.getParameter(imageIdParamKey);
        if(imageid==null || imageid.length()==0) return null;

        params.put("profile",profile);
        params.put("imageid",imageid);
        
        params.put("action",action);

        return params;
    }
    
    @Override
    protected String getCacheKey(HttpServletRequest req, HttpMethod method, String action) {
        LinkedHashMap<String,String> cacheKeyParams=getCacheKeyParams(req, method, action);
        if(cacheKeyParams==null) return null;
        return CachedAction.buildCacheKey(cacheKeyParams);
    }
    
    /**
     * A simple interface for image sources. An image source is just a
     * function that translates an image id into an image URL.
     */
    public static interface ImageSource {
        /**
         * Translate an image id into an image URL. If this image source
         * is not applicable with the given id, return null instead.
         * @param imageid The image id.
         * @return The URL the image id translates to, or null if this source
         *          is not applicable with the given id.
         */
        public String matchSource(String imageid);
    }
    
    /**
     * An image source that does the image id translation using regular
     * expressions.
     */
    public static class PatternImageSource implements ImageSource {
        public String id;
        public Pattern pattern;
        public String replacement;
        public PatternImageSource(){}

        @Override
        public String matchSource(String imageid) {
            Matcher m=pattern.matcher(imageid);
            if(m.matches()){
                return m.replaceAll(replacement);
            }
            else return null;
        }
        
    }

    /**
     * A data structure that holds information about the image profile.
     */
    public static class ImageProfile {
    
        public String id; // id for the profile used in settings file, used also in the URL to select profile
        public int quality=85; // quality at which the cached image is saved (for jpg 0-100)
        public String error=null; // image to use in all error cases
        public BufferedImage errorImage=null; // error image is cached here when it's first needed        
        public ImageMaker imageMaker; // ImageMaker that transforms original image into cached image

        public ImageProfile(){}
        
        public BufferedImage getErrorImage(){
            if(error!=null){
                if(errorImage==null){
                    try{
                        errorImage=ImageIO.read(new URL(error));
                    }
                    catch(IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
                return errorImage;
            }
            else return null;
        }
    }





    public static interface ImageMaker {
        public BufferedImage makeImg(BufferedImage original);
    }

    public static class DefaultImageMaker implements ImageMaker {
        public int width=-1; // width of the image or -1 to determine it automatically
        public int height=-1; // height of the image or -1 to determine it automatically
        public java.awt.Color bgColor=java.awt.Color.BLACK; // bg color of image if the original image doesn't fill the cached image completely
        boolean crop=false; // if true, original image is cropped so that it fills the cached image, otherwise it is scaled to fit in the cached image
        boolean noExtraCanvas=false;
        public String watermark=null; // image file for watermark
        public String watermarkMode="lowerright"; // how to place watermark, currently only supports "lowerright"
        public double scale=1.0; // scale image after width, height and cropping mode have been decided
        public BufferedImage watermarkImage=null; // watermark image is cached here when it is first needed

        public DefaultImageMaker(){};

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
        
        

        @Override
        public BufferedImage makeImg(BufferedImage original){

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

            BufferedImage img;
            Graphics2D g2;
            
            if(ow==width && oh==height && nw==width && nh==height && watermark==null) {
                img=original;
                g2 = img.createGraphics();   
            }
            else {            
                img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                g2 = img.createGraphics();
                g2.setColor(bgColor);
                g2.fillRect(0,0, width,height);

                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
                g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY );

                Image resized = original.getScaledInstance( nw, nh, BufferedImage.SCALE_SMOOTH );

                g2.drawImage(resized, (int)(width/2.0-nw/2.0+0.5), (int)(height/2.0-nh/2.0+0.5), null);
                //g2.drawImage(original, (int)(width/2.0-nw/2.0+0.5), (int)(height/2.0-nh/2.0+0.5), nw, nh, null);
            }

            if(watermark!=null){
                if(watermarkImage==null){
                    try{
                        watermarkImage=ImageIO.read(new URL(watermark));
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
