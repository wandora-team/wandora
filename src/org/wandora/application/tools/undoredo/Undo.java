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
 */


package org.wandora.application.tools.undoredo;

import javax.swing.Icon;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.undowrapper.UndoException;



/**
 *
 * @author olli
 */


public class Undo extends AbstractWandoraTool implements WandoraTool{

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/undo_undo.png");
    }

    @Override
    public String getName() {
        return "Undo";
    }

    @Override
    public String getDescription() {
        return "Undo previous topic map operation.";
    }
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        if(wandora != null) {
            try {
                wandora.undo();
            }
            catch(UndoException ue) {
                if(ue.getMessage() != null) {
                    WandoraOptionPane.showMessageDialog(wandora, 
                        ue.getMessage()+" Undo covers topic map operations only. In order to undo you need to change topic maps, topics or associations.", 
                        ue.getMessage());
                }
                else {
                    ue.printStackTrace();
                    WandoraOptionPane.showMessageDialog(wandora, 
                        "Undo exception occurred. In order to undo you need to change topic maps, topics or associations.", 
                        "Undo exception occurred");
                }
            }
            catch(Exception e) {
                log(e);
            }
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    

    @Override
    public void initialize(Wandora admin,org.wandora.utils.Options options,String prefix) throws TopicMapException {
    }
    
    @Override
    public boolean isConfigurable(){
        return true;
    }
    
    @Override
    public void configure(Wandora w,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        //System.out.println(prefix);
        UndoRedoOptions dialog=new UndoRedoOptions();
        dialog.open(w);
    }
    
    @Override
    public void writeOptions(Wandora admin,org.wandora.utils.Options options,String prefix){
    }
    

    
    // -------------------------------------------------------------------------
    
    
    
}
