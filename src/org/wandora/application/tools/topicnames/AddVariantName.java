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
 */


package org.wandora.application.tools.topicnames;


import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;

import java.util.*;

/**
 *
 * @author akivela
 */
public class AddVariantName extends AbstractWandoraTool implements WandoraTool {


	private static final long serialVersionUID = 1L;


	public AddVariantName() {
    }
    public AddVariantName(Context preferredContext) {
        setContext(preferredContext);
    }



    @Override
    public String getName() {
        return "Add variant name";
    }

    @Override
    public String getDescription() {
        return "Add variant name to selected topics.";
    }


    @Override
    public void execute(Wandora wandora, Context context) {
        try {
            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;

            VariantNameEditor editor = new VariantNameEditor(wandora);
            editor.openEditor("Add variant name");

            if(editor.wasAccepted()) {
                String variantString = editor.getVariantString();
                Set<Topic> variantScope = editor.getVariantScope();

                Topic topic = null;
                while(topics.hasNext() && !forceStop()) {
                    try {
                        topic = (Topic) topics.next();
                        if(topic != null && !topic.isRemoved()) {
                            topic.setVariant(variantScope, variantString);
                        }
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        catch (Exception e) {
            log(e);
        }
    }


}
