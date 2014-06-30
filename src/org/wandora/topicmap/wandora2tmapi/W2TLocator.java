/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2013 Wandora Team
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
package org.wandora.topicmap.wandora2tmapi;

import org.tmapi.core.Locator;
import org.tmapi.core.MalformedIRIException;

/**
 *
 * @author olli
 */


public class W2TLocator implements Locator {
    
    protected org.wandora.topicmap.Locator l;
    
    public W2TLocator(org.wandora.topicmap.Locator l){
        this.l=l;
    }
    
    public W2TLocator(String reference){
        this(new org.wandora.topicmap.Locator(reference));
    }

    @Override
    public String getReference() {
        return l.getReference();
    }

    @Override
    public String toExternalForm() {
        return l.toExternalForm();
    }

    @Override
    public Locator resolve(String string) throws MalformedIRIException {
        if(string.startsWith("#")) return new W2TLocator(new org.wandora.topicmap.Locator(l.getNotation(),l.getReference()+string));
        else return new W2TLocator(string);
    }
    
    // hashCode and equals implementations specified by tmapi

    @Override
    public int hashCode() {
        return this.getReference().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof Locator && this.getReference().equals(((Locator)other).getReference()));
    }
    
    
}
