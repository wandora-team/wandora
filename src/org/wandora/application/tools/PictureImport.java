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
 * PictureImport.java
 *
 * Created on July 15, 2004, 8:58 AM
 */

package org.wandora.application.tools;


import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;



/**
 *
 * @author  olli
 */
public class PictureImport extends AbstractWandoraTool implements WandoraTool {
    
    private boolean tvPictureCrop=true;
    
    /** Creates a new instance of PictureImport */
    public PictureImport() {
    }
    public PictureImport(boolean tvPictureCrop) {
        this.tvPictureCrop=tvPictureCrop;
    }
    
    public void execute(Wandora admin, Context context)  throws TopicMapException {
        PictureImportDialog d=new PictureImportDialog(admin,true,tvPictureCrop);
        d.setVisible(true);
    }

    @Override
    public String getName() {
        return "Import picture";
    }
    
}
