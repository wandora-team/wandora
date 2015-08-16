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


package org.wandora.application.gui.previews;

import org.wandora.utils.Option;
import org.wandora.application.gui.simple.SimpleFileChooser;
import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Map;
import org.wandora.utils.Functional.Fn0;
import org.wandora.utils.Functional.Fn1;
import static org.wandora.utils.Option.*;

public class Util {
    
    private static final Fn1<SimpleFileChooser, String> makeNamedChooser = new Fn1<SimpleFileChooser, String>() {
    @Override
    public SimpleFileChooser invoke(String path) {
        return new SimpleFileChooser(path);
    }};
    
    private static final Fn0<SimpleFileChooser> makeChooser = new Fn0<SimpleFileChooser>() {
    @Override
    public SimpleFileChooser invoke() {
        return new SimpleFileChooser();
    }};
    
    public static InputStream makeInputStream(final ByteBuffer buffer)
    {
        return new InputStream() {
            @Override
            public synchronized int read() throws IOException {
                return buffer.hasRemaining() ? buffer.get() : -1;
            }
            
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                final int rv = Math.min(len, buffer.remaining());
                buffer.get(b, off, rv);
                return rv == 0 ? -1 : rv;
            }
        };
    }

    public static Option<String> getOption(final Map<String, String> options, final String key) {
        final String opt = options.get(key);
        if(opt != null)
            return some(opt);
        
        return none();
    }
    // Transforms a string containing a path like "/home/antti/foo.bar"
    // into a URI containing "file:///home/antti/foo.bar".
    // If a conversion isn't possible, returns none()
    public static Fn1<Option<URI>, String> makeFileURI = new Fn1<Option<URI>, String>() {
        @Override
        public Option<URI> invoke(final String path) {
            try {
                //return some(new URI("file://" + path));
                return some(new URI(path));
            }
            catch(URISyntaxException e) {
                return none();
            }
        }
    };
    
    
   
    public static boolean startsWithAny(String str, String... args) {
        if(str != null) {
            for(String s : args) {
                if(str.startsWith(s)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    
    public static boolean endsWithAny(String str, String... args) {
        if(str != null) {
            for(String s : args) {
                if(str.endsWith(s))
                    return true;
            }
        }
        return false;
    }

    
    public static Option<String> choosePath(
			final Map<String, String> options,
			final JComponent dlgParent,
			final String optionsPrefix)
    {
        //try {
            final SimpleFileChooser chooser = new SimpleFileChooser();
            if (chooser.open(dlgParent, SimpleFileChooser.SAVE_DIALOG) != SimpleFileChooser.APPROVE_OPTION) {
                return Option.none();
            }
            
            //final String path = chooser.getSelectedFile().getCanonicalPath();
            //options.put(optionsPrefix + "currentDirectory", chooser.getCurrentDirectory().getCanonicalPath());

            return Option.some(chooser.getSelectedFile().toURI().toString());
            /*
        }
        catch(IOException e) {
            // TODO: bad uri syntax, should log it;
            // currently the whole operation will just fail silently
        }
        return Option.none();
             */
    }
    /*
	{
        try {
            SimpleFileChooser chooser= new SimpleFileChooser(path);
            if (chooser.open(dlgParent, SimpleFileChooser.SAVE_DIALOG) !=
					JFileChooser.APPROVE_OPTION)
			{
                return Option.none();
            }
            path = chooser.getSelectedFile().getCanonicalPath();
            if(options != null)
                options.put(optionsPrefix + "currentDirectory", chooser.getCurrentDirectory().getCanonicalPath());

            return Option.some(path);
        }
        catch(IOException e) {
            // TODO: bad uri syntax, should log it;
            // currently the whole operation will just fail silently
        }
        return Option.none();
    }
    */
}
