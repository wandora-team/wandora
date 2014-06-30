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
 *
 *
 * VariantDirective.java
 *
 *
 */
package org.wandora.query;
import org.wandora.topicmap.*;
import java.util.*;


/**
 *
 * @author olli
 */
public class VariantDirective implements Directive {
    private Locator nameRole;
    private Locator variantType;
    private Locator variantVersion;

    public static final String NAME_SI="http://wandora.org/si/query/variantname";

    public VariantDirective(Locator variantType,Locator variantVersion,Locator nameRole) {
        this.variantType=variantType;
        this.variantVersion=variantVersion;
        this.nameRole=nameRole;
    }
    public VariantDirective(Locator variantType,Locator variantVersion) {
        this(variantType,variantVersion,new Locator(NAME_SI));
    }
    public VariantDirective(String variantType,String variantVersion,String namerole) {
        this(new Locator(variantType),new Locator(variantVersion),new Locator(namerole));
    }
    public VariantDirective(String variantType,String variantVersion) {
        this(new Locator(variantType),new Locator(variantVersion),new Locator(NAME_SI));
    }
    public ArrayList<ResultRow> query(QueryContext context) throws TopicMapException {
        Topic contextTopic=context.getContextTopic();
        TopicMap tm=contextTopic.getTopicMap();
        ArrayList<ResultRow> res=new ArrayList<ResultRow>();

        Topic t=tm.getTopic(variantType);
        Topic v=tm.getTopic(variantVersion);
        String variant=null;
        if(t!=null && v!=null){
            HashSet<Topic> scope=new HashSet<Topic>();
            scope.add(t);
            scope.add(v);
            variant=contextTopic.getVariant(scope);
        }
        res.add(new ResultRow(nameRole,nameRole,variant));

        return res;
    }
    public boolean isContextSensitive(){
        return true;
    }

}
