/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2014 Wandora Team
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
 * PreviewPanel.java
 */

package org.wandora.application.gui.previews;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import org.gstreamer.GstException;
import org.wandora.application.gui.previews.formats.*;
import org.wandora.application.Wandora;
import org.wandora.topicmap.Locator;
import org.wandora.utils.Option;
import org.wandora.utils.Functional.*;
import static org.wandora.utils.Option.none;
import static org.wandora.utils.Option.some;
import static org.wandora.application.gui.previews.Util.endsWithAny;
/**
 *
 * @author anttirt
 */
public class PreviewFactory {
    
    private final Wandora admin;
    
    public PreviewFactory(Wandora admin) {
        this.admin = admin;
    }
    
    private Fn1<PreviewPanel, PreviewPanel> wrapHeavy = new Fn1<PreviewPanel, PreviewPanel>() {
    @Override
    public PreviewPanel invoke(PreviewPanel panel) {
        if(panel.isHeavy())
            return new AWTWrapper(admin, panel);
        else
            return panel;
    }};
    
    
    
    public Option<PreviewPanel> create(final Locator subjectLocator) {
        return createAux(subjectLocator).map(wrapHeavy);
    }
    
    
    
    private Option<? extends PreviewPanel> createAux(final Locator subjectLocator) {
        final String urlString = subjectLocator.toExternalForm();
        final String urlLower = subjectLocator.toExternalForm().toLowerCase();
        final Map<String, String> options = admin.getOptions().asMap();
        final java.awt.Frame dlgParent = admin;
        
        if     (endsWithAny(urlLower, ".mid", ".rmf")) {
            try { return some(new AudioMidi(urlString, options)); } catch(Exception e) { }
        }
        else if(endsWithAny(urlLower, ".aif", /*".mp3", */".wav", ".au")) {
            try { return some(new AudioSample(urlString, options)); } catch(Exception e) { }
        }
        else if(endsWithAny(urlLower, ".gif", ".jpg", ".jpeg", ".tif", ".tiff", ".bmp", ".png")) {
            try { return some(new Picture(urlString, admin)); } catch(Exception e) { admin.handleError(e); }
        }
        else if(endsWithAny(urlLower, ".pdf")) {
            //try { return some(new PDF(urlString, admin)); } catch(Exception e) { }
            try {
                return some(new PDFnew(new URI(urlLower), admin, options));
            }
            catch(URISyntaxException e) { }
            catch(MalformedURLException e) { }
            catch(IOException e) { } 
        }
        else if(endsWithAny(urlLower, ".xml")) {
            try { return some(new XML(urlLower, admin)); } catch(Exception e) { }
        }
        else {
            final String mediafw = System.getProperty("org.wandora.mediafw");
            System.err.println(mediafw);
            if("FMJ".equals(mediafw)) {
                try {
                    return some(new FMJ(urlLower, dlgParent, options));
                }
                catch(Exception e) {
                    System.err.println(e.getClass().getName() + ": " + e.getMessage());
                }
            }
            else if("GST".equals(mediafw)) {
                try {
                    return some(new GST(new URI(subjectLocator.toString()), dlgParent, options));
                }

                catch(URISyntaxException e) {
                    System.err.println("Invalid URI syntax: " + subjectLocator.toString());
                }
                catch(GstException e) {
                    System.err.println("GST exception: " + e.getMessage());
                }
            }
        }

        //try { return some(new JDICBrowser(urlString, options)); } catch(Exception e) { }
        try { return some(new HTML(urlString, admin)); } catch(Exception e) { }
        return none();
    }
}