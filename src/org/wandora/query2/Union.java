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
 *
 * Union.java
 *
 *
 */
package org.wandora.query2;
import java.util.*;
/**
 *
 * @author olli
 */
public class Union extends Directive implements DirectiveUIHints.Provider {
    private Directive[] directives;
    
    private ArrayList<String> staticRoles;
    private boolean useActive=false;
            
    public Union(){this(new Directive[0]);}
    
    public Union(Directive[] directives){
        this.directives=directives;
    }
    public Union(Directive d1){this(new Directive[]{d1});}
    public Union(Directive d1,Directive d2){this(new Directive[]{d1,d2});}
    public Union(Directive d1,Directive d2,Directive d3){this(new Directive[]{d1,d2,d3});}
    public Union(Directive d1,Directive d2,Directive d3,Directive d4){this(new Directive[]{d1,d2,d3,d4});}

    @Override
    public DirectiveUIHints getUIHints() {
        DirectiveUIHints ret=new DirectiveUIHints(Union.class,new DirectiveUIHints.Constructor[]{
                new DirectiveUIHints.Constructor(new DirectiveUIHints.Parameter[]{
                    new DirectiveUIHints.Parameter(Directive.class, true, "directives")
                }, "")
            },
            Directive.getStandardAddonHints(),
            "Union",
            "Structure");
        return ret;
    }            
    
    @Override
    public void endQuery(QueryContext context) throws QueryException {
        for(Directive d : directives){
            d.endQuery(context);
        }
    }
    @Override
    public boolean startQuery(QueryContext context) throws QueryException {
        boolean r=true;
        for(Directive d : directives){
            r&=d.startQuery(context);
        }
        return r;
    }

    @Override
    public ResultIterator queryIterator(QueryContext context, ResultRow input) throws QueryException {
        if(staticRoles==null && !useActive)
            return new UnionIterator(context,input);
        else return new StaticUnionIterator(context,input);
    }

    @Override
    public boolean isStatic(){
        for(Directive d : directives){
            if(!d.isStatic()) return false;
        }
        return true;
    }
    
    public Union ofActiveRole(){
        useActive=true;
        return this;
    }
    
    public void ofRoles(String ... role){
        staticRoles=new ArrayList<String>();
        for(String r : role){
            staticRoles.add(r);
        }
    }
    public void ofRoles(String r1){
        ofRoles(new String[]{r1});        
    }
    public void ofRoles(String r1,String r2){
        ofRoles(new String[]{r1,r2});        
    }
    public void ofRoles(String r1,String r2,String r3){
        ofRoles(new String[]{r1,r2,r3});        
    }
    public void ofRoles(String r1,String r2,String r3,String r4){
        ofRoles(new String[]{r1,r2,r3,r4});
    }

    @Override
    public String debugStringParams(String indent) {
        return debugStringInner(directives,indent);
    }
    
    private class StaticUnionIterator extends ResultIterator {

        public QueryContext context;
        public ResultRow input;
        
        public ResultIterator[] iterators;
        public int currentDirective=-1;

        public ResultRow nextRow;
        
        
        public StaticUnionIterator(QueryContext context,ResultRow input) throws QueryException {
            this.context=context;
            this.input=input;
            iterators=new ResultIterator[directives.length];
        }
        
        private ResultRow makeRow(ResultRow row) throws QueryException {
            if(useActive) return new ResultRow(row.getActiveValue());
            else {
                ArrayList<Object> values=new ArrayList<Object>(staticRoles.size());
                ArrayList<String> rowRoles=row.getRoles();
                for(int i=0;i<staticRoles.size();i++){
                    int ind=rowRoles.indexOf(staticRoles.get(i));
                    Object v=null;
                    if(ind>=0) v=row.getValue(ind);
                    values.add(v);
                }
                return new ResultRow(staticRoles,values,0,true);
            }
        }        
        
        @Override
        public void dispose() throws QueryException {
            for(int i=0;i<iterators.length;i++){
                if(iterators[i]==null) break;
                iterators[i].dispose();
            }
        }

        @Override
        public boolean hasNext() throws QueryException {
            if(nextRow!=null) return true;
            
            while( currentDirective<directives.length && (currentDirective==-1 || !iterators[currentDirective].hasNext()) ) {
                currentDirective++;
                if(currentDirective>=directives.length) break;
                if(iterators[currentDirective]==null)
                    iterators[currentDirective]=directives[currentDirective].queryIterator(context, input);
            }
            
            if(currentDirective>=directives.length) return false;
            
            nextRow=makeRow(iterators[currentDirective].next());
            
            return true;
        }

        @Override
        public ResultRow next() throws QueryException, NoSuchElementException {
            if(hasNext()) {
                ResultRow temp=nextRow;
                nextRow=null;
                return temp;
            }
            else throw new NoSuchElementException();
        }

        @Override
        public void reset() throws QueryException {
            for(int i=0;i<currentDirective;i++){
                iterators[i].reset();
            }
            currentDirective=-1;
            nextRow=null;
        }
        
    }

    private class UnionIterator extends ResultIterator {
        public QueryContext context;
        public ResultRow input;

        public ResultRow[] firstRows;
        public ResultIterator[] iterators;
        public int currentDirective;

        public ArrayList<String> roles;

        public ResultRow nextRow;

        public UnionIterator(QueryContext context,ResultRow input) throws QueryException {
            this.context=context;
            this.input=input;

            currentDirective=-1;

            LinkedHashSet<String> rolesHash=new LinkedHashSet<String>();
            firstRows=new ResultRow[directives.length];
            iterators=new ResultIterator[directives.length];
            for(int i=0;i<directives.length;i++){
                iterators[i]=directives[i].queryIterator(context, input);
                if(iterators[i].hasNext()) {
                    firstRows[i]=iterators[i].next();
                    for(String role : firstRows[i].getRoles()){
                        rolesHash.add(role);
                    }
                }
            }
            roles=new ArrayList<String>(rolesHash);
        }

        private ResultRow makeRow(ResultRow row) throws QueryException {
            ArrayList<Object> values=new ArrayList<Object>(roles.size());
            ArrayList<String> rowRoles=row.getRoles();
            for(int i=0;i<roles.size();i++){
                int ind=rowRoles.indexOf(roles.get(i));
                Object v=null;
                if(ind>=0) v=row.getValue(ind);
                values.add(v);
            }
            return new ResultRow(roles,values,0,true);
        }

        @Override
        public void dispose() throws QueryException {
            for(int i=0;i<iterators.length;i++){
                iterators[i].dispose();
            }
        }

        @Override
        public boolean hasNext() throws QueryException {
            if(nextRow!=null) return true;
            while(currentDirective<firstRows.length &&
                    (currentDirective<0 || (firstRows[currentDirective]==null && !iterators[currentDirective].hasNext())) ) {
                currentDirective++;
            }
            if(currentDirective>=firstRows.length) return false;
            if(firstRows[currentDirective]!=null){
                nextRow=makeRow(firstRows[currentDirective]);
                firstRows[currentDirective]=null;
            }
            else nextRow=makeRow(iterators[currentDirective].next());
            return true;
        }

        @Override
        public ResultRow next() throws QueryException, NoSuchElementException {
            if(hasNext()) {
                ResultRow temp=nextRow;
                nextRow=null;
                return temp;
            }
            else throw new NoSuchElementException();
        }

        @Override
        public void reset() throws QueryException {
            LinkedHashSet<String> rolesHash=new LinkedHashSet<String>();
            for(int i=0;i<iterators.length;i++){
                iterators[i].reset();
                if(iterators[i].hasNext()) {
                    firstRows[i]=iterators[i].next();
                    for(String role : firstRows[i].getRoles()){
                        rolesHash.add(role);
                    }
                }
                else firstRows[i]=null;
            }
            roles=new ArrayList<String>(rolesHash);
            currentDirective=-1;
            nextRow=null;
        }

    }

}
