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
 *
 *
 *
 * Variant.java
 *
 *
 */
package org.wandora.query2;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
/**
 *
 * @author olli
 */
public class Variant extends Directive implements DirectiveUIHints.Provider  {
    private TopicOperand lang;
    private TopicOperand type;


    public Variant(){
        this(null,null);
    }
    public Variant(Object type){
        this(type,null);
    }
    public Variant(Object type,Object lang){
        this.type=(type==null?null:new TopicOperand(type));
        this.lang=(lang==null?null:new TopicOperand(lang));
    }

    
    @Override
    public DirectiveUIHints getUIHints() {
        DirectiveUIHints ret=new DirectiveUIHints(Variant.class,new DirectiveUIHints.Constructor[]{
                new DirectiveUIHints.Constructor(new DirectiveUIHints.Parameter[]{}, ""),
                new DirectiveUIHints.Constructor(new DirectiveUIHints.Parameter[]{
                        new DirectiveUIHints.Parameter(TopicOperand.class, false, "type"),
                }, ""),
                new DirectiveUIHints.Constructor(new DirectiveUIHints.Parameter[]{
                        new DirectiveUIHints.Parameter(TopicOperand.class, false, "type"),
                        new DirectiveUIHints.Parameter(TopicOperand.class, false, "lang"),
                }, "")
            },
            Directive.getStandardAddonHints(),
            "Variant",
            "Topic map");
        return ret;
    }  
    
    @Override
    public boolean startQuery(QueryContext context) throws QueryException {
        boolean ret=true;
        if(type!=null) ret&=type.startQuery(context);
        if(lang!=null) ret&=lang.startQuery(context);
        return ret;
    }

    @Override
    public void endQuery(QueryContext context) throws QueryException {
        if(type!=null) type.endQuery(context);
        if(lang!=null) lang.endQuery(context);
    }

    @Override
    public ResultIterator queryIterator(QueryContext context, ResultRow input) throws QueryException {
        Object o=input.getActiveValue();
        if(o==null) return new ResultIterator.EmptyIterator();
        try{
            if(!(o instanceof Topic)){
                TopicMap tm=context.getTopicMap();
                o=tm.getTopic(o.toString());
                if(o==null) return new ResultIterator.EmptyIterator();
            }
            if(type!=null && lang!=null){
                Topic typeT=type.getOperandTopic(context, input);
                Topic langT=lang.getOperandTopic(context, input);
                if(typeT==null || langT==null) return input.addValue(DEFAULT_COL, null).toIterator();

                HashSet<Topic> scope=new HashSet<Topic>();
                scope.add(typeT);
                scope.add(langT);
                String name=((Topic)o).getVariant(scope);
                return input.addValue(DEFAULT_COL, name).toIterator();
            }
            else{
                Topic typeT=(type==null?null:type.getOperandTopic(context,input));
                Topic langT=(lang==null?null:lang.getOperandTopic(context,input));
                if(type!=null && typeT==null) return new ResultIterator.EmptyIterator();
                if(lang!=null && langT==null) return new ResultIterator.EmptyIterator();
                Set<Set<Topic>> scopes=((Topic)o).getVariantScopes();
                ArrayList<ResultRow> ret=new ArrayList<ResultRow>();
                Topic langType=context.getTopicMap().getTopic(TMBox.LANGUAGE_SI);
                Topic typeType=context.getTopicMap().getTopic(TMBox.VARIANT_NAME_VERSION_SI);
                if(langType==null || typeType==null) return new ResultIterator.EmptyIterator();
                for(Set<Topic> scope : scopes){
                    Topic variantLang=null;
                    Topic variantType=null;
                    for(Topic t : scope){
                        if(t.isOfType(langType)) variantLang=t;
                        if(t.isOfType(typeType)) variantType=t;
                    }
                    if( variantType!=null && variantLang!=null && (
                            (lang==null && type==null) ||
                            (lang!=null && langT.mergesWithTopic(variantLang)) ||
                            (type!=null && typeT.mergesWithTopic(variantType)) ) ){
                        String v=((Topic)o).getVariant(scope);
                        if(lang==null && type==null)
                            ret.add(input.addValues(new String[]{DEFAULT_NS+"variant_type",DEFAULT_NS+"variant_lang",DEFAULT_COL}, new Object[]{variantType,variantLang,v}));
                        else if(lang==null)
                            ret.add(input.addValues(new String[]{DEFAULT_NS+"variant_lang",DEFAULT_COL}, new Object[]{variantLang,v}));
                        else
                            ret.add(input.addValues(new String[]{DEFAULT_NS+"variant_type",DEFAULT_COL}, new Object[]{variantType,v}));
                    }
                }
                return new ResultIterator.ListIterator(ret);
            }
        }catch(TopicMapException tme){
            throw new QueryException(tme);
        }

    }

}
