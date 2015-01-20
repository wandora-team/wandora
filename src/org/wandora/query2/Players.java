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
 * Players.java
 *
 *
 */
package org.wandora.query2;
import java.util.*;
import org.wandora.query2.DirectiveUIHints.Addon;
import org.wandora.query2.DirectiveUIHints.Constructor;
import org.wandora.query2.DirectiveUIHints.Parameter;
import org.wandora.topicmap.*;
import static org.wandora.utils.Tuples.*;
/**
 *
 * @author olli
 */
public class Players extends Directive implements DirectiveUIHints.Provider {

    private TopicOperand associationType;
    private TopicOperand[] roles;

    private TopicOperand inputRole;

    private String[] columns=null;
    
    public Players(){
        this(null,null);
    }

    public Players(Object associationType){
        this(associationType,null);
    }

    public Players(Object associationType,Object[] roles){
        this.associationType=(associationType==null?null:TopicOperand.makeTopicOperand(associationType));
        this.roles=(roles==null?null:TopicOperand.makeTopicOperands(roles));
        this.inputRole=null;
    }
    public Players(Object associationType,Object r1){
        this(associationType,new Object[]{r1});}
    public Players(Object associationType,Object r1,Object r2){
        this(associationType,new Object[]{r1,r2});}
    public Players(Object associationType,Object r1,Object r2,Object r3){
        this(associationType,new Object[]{r1,r2,r3});}
    public Players(Object associationType,Object r1,Object r2,Object r3,Object r4){
        this(associationType,new Object[]{r1,r2,r3,r4});}

    @Override
    public DirectiveUIHints getUIHints() {
        return new DirectiveUIHints(Players.class,new Constructor[]{
            new Constructor(new Parameter[]{new Parameter(TopicOperand.class, false, "association type")}, "")
        },
        new Addon[]{
        });
    }

    
    
    public Players usingColumns(Object[] cols){
        this.columns=new String[cols.length];
        for(int i=0;i<cols.length;i++) this.columns[i]=cols[i].toString();
        return this;
    }
    public Players usingColumns(Object c1){ return usingColumns(new Object[]{c1}); }
    public Players usingColumns(Object c1,Object c2){ return usingColumns(new Object[]{c1,c2}); }
    public Players usingColumns(Object c1,Object c2,Object c3){ return usingColumns(new Object[]{c1,c2,c3}); }
    public Players usingColumns(Object c1,Object c2,Object c3,Object c4){ return usingColumns(new Object[]{c1,c2,c3,c4}); }
    
    public Players whereInputIs(Object role){
        inputRole=TopicOperand.makeTopicOperand(role);
        return this;
    }

    @Override
    public void endQuery(QueryContext context) throws QueryException {
        if(associationType!=null) associationType.endQuery(context);
        if(roles!=null) Operand.endOperands(context, roles);
    }

    @Override
    public boolean startQuery(QueryContext context) throws QueryException {
        boolean ret=true;
        if(associationType!=null) ret&=associationType.startQuery(context);
        if(roles!=null) ret&=Operand.startOperands(context, roles);
        return ret;
    }


    @Override
    public ResultIterator queryIterator(QueryContext context, ResultRow input) throws QueryException {
        TopicMap tm=context.getTopicMap();
        try{

            Object o=input.getActiveValue();
            if(o==null) return new ResultIterator.EmptyIterator();

            if(!(o instanceof Topic)){
                o=tm.getTopic(o.toString());
                if(o==null) return new ResultIterator.EmptyIterator();
            }
            Topic t=(Topic)o;

            ArrayList<ResultRow> ret=new ArrayList<ResultRow>();

            Topic atype=null;
            if(associationType!=null) atype=associationType.getOperandTopic(context, input);
            Topic inputRoleTopic=null;
            if(inputRole!=null) inputRoleTopic=inputRole.getOperandTopic(context, input);
            Topic[] roleTopics=null;
            String[] roleStrings=null;
            if(roles!=null){
                if(columns!=null) roleStrings=columns;
                boolean setRoleStrings=(roleStrings==null);
                roleTopics=new Topic[roles.length];
                if(setRoleStrings) roleStrings=new String[roles.length];
                for(int i=0;i<roles.length;i++){
                    T2<Topic,String> oper=roles[i].getOperandTopicAndSI(context, input);
                    if(oper.e2==null) continue;
                    roleTopics[i]=oper.e1;
                    if(setRoleStrings) roleStrings[i]=oper.e2;
                }
            }

            Collection<Association> associations;
            if(associationType!=null) associations=t.getAssociations(atype);
            else associations=t.getAssociations();


            for(Association a : associations){
                if(context.checkInterrupt()) throw new QueryException("Execution interrupted");
                if(inputRoleTopic!=null){
                    Topic p=a.getPlayer(inputRoleTopic);
                    if(p==null) continue;
                    if(!p.mergesWithTopic(t)) continue;
                }

                if(roles!=null){
                    Object[] newValues=new Object[roles.length];
                    for(int i=0;i<roleTopics.length;i++){
                        if(roleTopics[i]==null){
                            newValues[i]=null;
                        }
                        else{
                            Topic p=a.getPlayer(roleTopics[i]);
                            newValues[i]=p;
                        }
                    }
                    ret.add(input.addValues(roleStrings, newValues));
                }
                else {
                    Object[] newValues=null;
                    int counter=0;
                    if(associationType==null){
                        roleStrings=new String[a.getRoles().size()+1];
                        newValues=new Object[a.getRoles().size()+1];
                        roleStrings[0]=Directive.DEFAULT_NS+"association_type";
                        newValues[0]=a.getType();
                        counter=1;
                    }
                    else{
                        roleStrings=new String[a.getRoles().size()];
                        newValues=new Object[a.getRoles().size()];
                    }

                    for(Topic role : a.getRoles()){
                        roleStrings[counter]=role.getOneSubjectIdentifier().toString();
                        newValues[counter]=a.getPlayer(role);
                        counter++;
                    }
                    ret.add(input.addValues(roleStrings, newValues));
                }
            }

            if(ret.size()==0) return new ResultIterator.EmptyIterator();
            else if(ret.size()==1) return new ResultIterator.SingleIterator(ret.get(0));
            else return new ResultIterator.ListIterator(ret);
        }catch(TopicMapException tme){
            throw new QueryException(tme);
        }
    }

}
