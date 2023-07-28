/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
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


public class Redo extends AbstractWandoraTool implements WandoraTool {


	private static final long serialVersionUID = 1L;

	@Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/undo_redo.png");
    }

    @Override
    public String getName() {
        return "Redo";
    }

    @Override
    public String getDescription() {
        return "Redo next topic map operation.";
    }
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        if(wandora != null) {
            try {
                wandora.redo();
            }
            catch(UndoException ue) {
                if(ue.getMessage() != null) {
                    WandoraOptionPane.showMessageDialog(wandora, 
                        ue.getMessage()+" In order to redo you need to undo something first.", 
                        ue.getMessage());
                }
                else {
                    ue.printStackTrace();
                    WandoraOptionPane.showMessageDialog(wandora, 
                        "Redo exception occurred. In order to redo you need to undo something first.", 
                        "Redo exception occurred");
                }
            }
            catch(Exception e) {
                log(e);
            }
        }
    }
    
    

    // -------------------------------------------------------------------------
    
    
    

    @Override
    public void initialize(Wandora wandora,org.wandora.utils.Options options,String prefix) throws TopicMapException {
    }
    
    @Override
    public boolean isConfigurable(){
        return true;
    }
    
    @Override
    public void configure(Wandora wandora,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        //System.out.println(prefix);
        UndoRedoOptions dialog=new UndoRedoOptions();
        dialog.open(wandora);
    }
    
    @Override
    public void writeOptions(Wandora wandora,org.wandora.utils.Options options,String prefix){
    }
    

    
    // -------------------------------------------------------------------------
    
    
}
