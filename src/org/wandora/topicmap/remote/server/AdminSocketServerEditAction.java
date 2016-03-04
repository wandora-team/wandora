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
 * 
 *
 * AdminSocketServerEditAction.java
 *
 * Created on June 8, 2004, 1:06 PM
 */

package org.wandora.topicmap.remote.server;



import org.wandora.exceptions.WandoraException;
import org.wandora.*;
import java.util.*;
import org.wandora.piccolo.*;
import org.wandora.topicmap.*;
/**
 *
 * @author  olli
 */
public class AdminSocketServerEditAction {
    public static final int TYPE_CHECK=1;
    public static final int TYPE_REMOVEASSOC=2;
    public static final int TYPE_REMOVETOPIC=3;
    public static final int TYPE_REMOVEVARIANTNAME=4;
    public static final int TYPE_REMOVEBASENAME=5;
    public static final int TYPE_REMOVEDATA=6;
    public static final int TYPE_MERGEIN=7;
    public static final int TYPE_REMOVESUBJECTLOCATOR=8;
    public static final int TYPE_REMOVETOPICTYPE=9;
    public static final int TYPE_REMOVESI=10;
    
    private int type;
    private Object param;
    
    private boolean resolved;
    
    /**
     * Creates a new instance of AdminSocketServerEditAction
     */
    public AdminSocketServerEditAction(int type,Object param) {
        this.type=type;
        this.param=param;
        resolved=false;
    }
    
    public int getType(){return type;}
    public Object getParam(){return param;}
    
    public boolean checkRunnability(TopicMap tm) throws WandoraException {
        if(type==AdminSocketServerEditAction.TYPE_REMOVETOPIC){
            Topic t=(Topic)param;
            if(t!=null && !t.isDeleteAllowed()){
                throw new TopicInUseException(t);
            }
            return true;
        }
        else return true;
    }
    public void runAction(TopicMap topicMap) throws WandoraException {
        Topic t;
        Association a;
        TopicMap tm;
        Iterator iter;
        Topic[] ts;
        Object[] os;
        switch(type){
            case TYPE_CHECK:
/*                HashSet failedTopics=new HashSet();
                HashSet removedTopics=new HashSet();
                tm=(TopicMap)param;
                iter=tm.getTopics();
                while(iter.hasNext()){
                    t=(Topic)iter.next();
                    Iterator iter2=t.getSubjectIdentifiers().iterator();
                    boolean found=false;
                    while(iter2.hasNext()){
                        Locator l=(Locator)iter2.next();
                        Topic t2=topicMap.getTopic(l);
                        if(t2!=null){
                            if(t2.getEditTime()>t.getEditTime()) failedTopics.add(t.getSubjectIdentifiers().iterator().next());
                            found=true;
                            break;
                        }
                    }
                    if(!found){
                        removedTopics.add(t.getSubjectIdentifiers().iterator().next());
                    }
                }
                if(failedTopics.size()>0 || removedTopics.size()>0){
                    throw new ConcurrentEditingException(failedTopics,removedTopics);
                }*/
                break;
            case TYPE_REMOVEASSOC:
                a=(Association)param;
                if(a!=null) a.remove();
                break;
            case TYPE_REMOVETOPIC:
                t=(Topic)param;
                if(t!=null) t.remove();
                break;
            case TYPE_REMOVEVARIANTNAME:
                os=(Object[])param;
                if(os!=null && os[0]!=null && os[1]!=null) ((Topic)os[0]).removeVariant((Set)os[1]);
                break;
            case TYPE_REMOVEBASENAME:
                t=(Topic)param;
                if(t!=null) t.setBaseName(null);
                break;
            case TYPE_REMOVESUBJECTLOCATOR:
                t=(Topic)param;
                if(t!=null) t.setSubjectLocator(null);
                break;
            case TYPE_REMOVEDATA:
                ts=(Topic[])param;
                if(ts!=null && ts[0]!=null && ts[1]!=null && ts[2]!=null)
                    ts[0].removeData(ts[1],ts[2]);
                break;
            case TYPE_REMOVETOPICTYPE:
                ts=(Topic[])param;
                if(ts!=null && ts[0]!=null && ts[1]!=null)
                    ts[0].removeType(ts[1]);
                break;
            case TYPE_MERGEIN:
                tm=(TopicMap)param;
                topicMap.mergeIn(tm);
//                manager.mergeInTopicMapNoLock(tm);
                break;
            case TYPE_REMOVESI:
                os=(Object[])param;
                t=(Topic)os[0];
                String l=(String)os[1];
                if(t!=null){
                    t.removeSubjectIdentifier(t.getTopicMap().createLocator(l));
                }
                break;
        }
    }
/*
    public void dispatchAction(ServerInterface server) {
        Topic t;
        Association a;
	Topic[] ts;
	Object[] os;
        switch(type){
            case TYPE_CHECK:
                server.checkTopics((TopicMap)param);
                break;
            case TYPE_REMOVEASSOC:
                server.removeAssociation((Association)param);
                break;
            case TYPE_REMOVETOPIC:
                server.removeTopic((Topic)param);
                break;
            case TYPE_REMOVEVARIANTNAME:
		os=(Object[])param;
                server.removeVariantName((Topic)os[0], (Collection)os[1]);
                break;
            case TYPE_REMOVEBASENAME:
                server.removeBaseName((Topic)param);
                break;
            case TYPE_REMOVESUBJECTLOCATOR:
                server.removeSubjectLocator((Topic)param);
                break;
            case TYPE_REMOVEDATA:
                ts=(Topic[])param;
                server.removeData(ts[0],ts[1],ts[2]);
                break;
            case TYPE_REMOVETOPICTYPE:
                ts=(Topic[])param;
                server.removeTopicType(ts[0],ts[1]);
                break;
            case TYPE_MERGEIN:
                server.mergeIn((TopicMap)param);
                break;
        }
    }
    */
    private Set resolveScope(Object[] param,TopicMap tm) throws TopicMapException {
        HashSet set=new HashSet();
        for(int i=0;i<param.length;i++){
            Topic t=tm.getTopic((String)param[i]);
            if(t!=null) set.add(t);
            else return null;
        }
        return set;
    }
    
    public void resolveParams(TopicMap tm) throws WandoraException {
        if(type==TYPE_MERGEIN){
            return;
        }
        Object[] params=(Object[])param;
        Object[] members;
        param=null;
        Topic t,t2,t3;
        Set scope,scope2;
        String name;
        Iterator iter;
        switch(type){
            case TYPE_CHECK:
                break;
            case TYPE_REMOVEASSOC:
                Topic atype=tm.getTopic((String)params[0]);
                if(atype!=null){
                    Object[] first=(Object[])((Object[])params[1])[0];
                    Object[] second=null;
                    if(((Object[])params[1]).length>1) second=(Object[])((Object[])params[1])[1];
                    t=tm.getTopic((String)first[0]);
                    Topic trole=tm.getTopic((String)first[1]);
                    if(t!=null && trole!=null){
                        Collection as=t.getAssociations(atype,trole);
                        
                        if(second!=null){
                            t2=tm.getTopic((String)second[0]);
                            Topic t2role=tm.getTopic((String)second[1]);
                            if(t2==null || t2role==null) break;
                            Collection as2=t2.getAssociations(atype,t2role);                        
                            if(as2.size()<as.size()){
                                as=as2;
                            }
                        }
                        members=(Object[])params[1];
                        if(as.size()>0){
                            iter=as.iterator();
                            while(iter.hasNext()){
                                Association a=(Association)iter.next();
/*                                if(as2.contains(a)){
                                    param=a;
                                    break;
                                }*/
                                boolean match=true;
                                for(int i=0;i<members.length;i++){
                                    Topic mt=tm.getTopic((String)((Object[])members[i])[0]);
                                    Topic mr=tm.getTopic((String)((Object[])members[i])[1]);
                                    if(mt==null || mr==null) {match=false; break;}
                                    if(mt!=a.getPlayer(mr)) {match=false; break;}
                                }
                                if(match){
                                    param=a;
                                    break;
                                }
                            }
                        }
                    }
                }
                break;
            case TYPE_REMOVETOPIC:
            case TYPE_REMOVESUBJECTLOCATOR:
            case TYPE_REMOVEBASENAME:
                t=tm.getTopic((String)params[0]);
                param=t;
                break;
            case TYPE_REMOVEVARIANTNAME:
                t=tm.getTopic((String)params[0]);
                if(t==null) break;
                scope=resolveScope((Object[])params[1],tm);
                param=new Object[]{t,scope};
                break;
            case TYPE_REMOVEDATA:
                t=tm.getTopic((String)params[0]);
                t2=tm.getTopic((String)params[1]);
                t3=tm.getTopic((String)params[2]);
                param=new Topic[]{t,t2,t3};
                break;
            case TYPE_REMOVETOPICTYPE:
                t=tm.getTopic((String)params[0]);
                t2=tm.getTopic((String)params[1]);
                param=new Topic[]{t,t2};
                break;
            case TYPE_REMOVESI:
                t=tm.getTopic((String)params[0]);
                param=new Object[]{t,params[1]};
                break;
        }        
        resolved=true;
    }
    public boolean isResolved(){
        return resolved;
    }
    
    public String toString(){
        try{
            switch(type){
                case TYPE_CHECK:
                    return "Check modified";
                case TYPE_REMOVEASSOC:
                    return "Remove association";
                case TYPE_REMOVETOPIC:
                    if(param==null) return "Remove topic null";
                    else return "Remove topic "+((Topic)param).getBaseName();
                case TYPE_REMOVEVARIANTNAME:
                    if(param==null || ((Object[])param)[0]==null) return "Remove variant name from null";
                    else return "Remove variant name from "+((Topic)((Object[])param)[0]).getBaseName();
                case TYPE_REMOVEBASENAME:
                    if(param==null) return "Remove base name from null";
                    else return "Remove base name from "+((Topic)param).getBaseName();
                case TYPE_REMOVEDATA:
                    if(param==null || ((Topic[])param)[0]==null) return "Remove data from null";
                    else return "Remove data from "+((Topic[])param)[0].getBaseName();
                case TYPE_MERGEIN:
                    return "Merge in";
                case TYPE_REMOVESUBJECTLOCATOR:
                    if(param==null) return "Remove subject locator from null";
                    else return "Remove subject locator from "+((Topic)param).getBaseName();
                case TYPE_REMOVETOPICTYPE:
                    if(param==null || ((Topic[])param)[0]==null || ((Topic[])param)[1]==null) return "Remove type null from null";
                    else return "Remove type "+((Topic[])param)[1].getBaseName()+" from "+((Topic[])param)[0].getBaseName();
                case TYPE_REMOVESI:
                    if(param==null || ((Object[])param)[0]==null || ((Object[])param)[1]==null) return "Remove subject identifier null from null";
                    else return "Remove subject identifier "+((Object[])param)[1]+" from "+((Topic)((Object[])param)[0]).getBaseName();
            }        
        }catch(TopicMapException tme){
            tme.printStackTrace();
            return "Exception getting action name";
        }
        return "Other action";
    }
}
