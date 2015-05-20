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

package org.wandora.application.tools.exporters.iiifexport;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.MimeTypes;
import org.wandora.utils.Tuples;

/**
 *
 * Simple IIIF builder that uses subject locators of selected topics for
 * images. The class is broken into many overrideable methods so that it can
 * also be used as a base class for other IIIF builders. 
 * 
 * @author olli
 */


public class SimpleSelectionIIIFBuilder implements IIIFBuilder {

    @Override
    public String getBuilderName(){
        return "Simple Selection Builder";
    }
    
    // This reads the image dimensions without reading the whole file, which would take
    // a lot of time for a big collection of big images. The parameter can be
    // a variety of things like a File or an InputStream, 
    // see ImageIO.createImageInputStream documentation.
    public static int[] getImageDimensions(Object f) throws IOException {
        if(f instanceof URL) f=((URL)f).openStream();
        ImageInputStream in = ImageIO.createImageInputStream(f);
        try{
            final Iterator readers = ImageIO.getImageReaders(in);
            if(readers.hasNext()){
                    ImageReader reader=(ImageReader)readers.next();
                    try{
                            reader.setInput(in);
                            return new int[]{reader.getWidth(0), reader.getHeight(0)};
                    }finally{
                            reader.dispose();
                    }
            }
        }finally {
            if(in!=null) in.close();
        }
        return null;
    }
    
    
    protected String guessFormat(String url){
        int ind=url.lastIndexOf("?");
        if(ind>0) url=url.substring(0,ind);
        ind=url.lastIndexOf("#");
        if(ind>0) url=url.substring(0,ind);
        return MimeTypes.getMimeType(url);
    }

    @Override
    public Manifest buildIIIF(Wandora wandora, Context context,IIIFExport tool) throws TopicMapException {
        startBuild(wandora,context);
        
        Manifest manifest=prepareManifest(wandora, context, tool);
        Sequence sequence=prepareSequence(wandora, context, manifest, tool);
        
        processTopics(wandora, context, sequence, tool);
        
        endBuild();
        
        return manifest;
    }    
    
    protected void startBuild(Wandora wandora, Context context) throws TopicMapException {
    }
        
    protected Manifest prepareManifest(Wandora wandora,Context context,IIIFExport tool) throws TopicMapException {
        Manifest manifest=new Manifest();
        manifest.addLabel(new LanguageString("Wandora IIIF Export"));
        // ID is required, create something
        manifest.setId(wandora.getTopicMap().makeSubjectIndicator());
        return manifest;
    }
    
    protected Sequence prepareSequence(Wandora wandora,Context context,Manifest manifest,IIIFExport tool) throws TopicMapException {
        Sequence sequence=new Sequence();
        sequence.addLabel(new LanguageString("Default sequence"));
        manifest.addSequence(sequence);
        return sequence;
    }
    
    protected void processTopics(Wandora wandora,Context context, Sequence sequence,IIIFExport tool) throws TopicMapException {
        Iterator iter=context.getContextObjects();
        while(iter.hasNext()){
            Object o=iter.next();
            if(!(o instanceof Topic)) continue;
            Topic t=(Topic)o;
            
            processTopic(t, sequence, tool);
        }        
    }
   
    protected void processTopic(Topic t, Sequence sequence,IIIFExport tool) throws TopicMapException {
        Content content=getTopicContent(t, tool);
        if(content==null) return;
        
        Canvas canvas=new Canvas();
        canvas.addLabel(new LanguageString(t.getDisplayName()));
        canvas.setId(t.getOneSubjectIdentifier().toExternalForm());
        canvas.setWidth(content.getWidth());
        canvas.setHeight(content.getHeight());
        sequence.addCanvas(canvas);
        
        canvas.addImage(content);
    }    
    
    protected Content getTopicContent(Topic t,IIIFExport tool) throws TopicMapException {
        Tuples.T2<String,String> urlAndFormat=getImageUrlAndFormat(t, tool);
        if(urlAndFormat==null) return null;
        
        int[] dimensions=null;
        try{
            dimensions=getImageDimensions(new URL(urlAndFormat.e1));
        }catch(IOException ioe){
            tool.log("Unable to get image dimensions for "+urlAndFormat.e1,ioe);
            return null;
        }
        
        Content content=new Content();
        content.setResourceId(urlAndFormat.e1);
        content.setFormat(urlAndFormat.e2);
        content.setResourceType(Content.RESOURCE_TYPE_IMAGE);
        content.setWidth(dimensions[0]);
        content.setHeight(dimensions[1]);
        
        return content;
    }
    
    protected Tuples.T2<String,String> getImageUrlAndFormat(Topic t,IIIFExport tool) throws TopicMapException {
        Locator sl=t.getSubjectLocator();
        if(sl==null) return null;
        String url=sl.toExternalForm();
        String format=guessFormat(url);
        if(format==null) return null;
        return Tuples.t2(url,format);        
    }
    
    protected void endBuild(){
    }
    
    
    
}
