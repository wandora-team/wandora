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
 * Join.java
 *
 */
package org.wandora.query2;
import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
 *
 * @author olli
 */
public class Join extends Directive implements DirectiveUIHints.Provider {
    private ArrayList<Directive> directives;

    public Join(){}
    
    public Join(Directive[] directives){
        this.directives=new ArrayList<Directive>();
        for(int i=0;i<directives.length;i++){
            this.directives.add(directives[i]);
        }
    }

    public Join(Directive d1,Directive d2){
        this(new Directive[]{d1,d2});
    }
    public Join(Directive d1,Directive d2,Directive d3){
        this(new Directive[]{d1,d2,d3});
    }
    public Join(Directive d1,Directive d2,Directive d3,Directive d4){
        this(new Directive[]{d1,d2,d3,d4});
    }

    @Override
    public DirectiveUIHints getUIHints() {
        DirectiveUIHints ret=new DirectiveUIHints(Join.class,new DirectiveUIHints.Constructor[]{
                new DirectiveUIHints.Constructor(new DirectiveUIHints.Parameter[]{
                        new DirectiveUIHints.Parameter(Directive.class, true, "directives"),
                }, ""),
            },
            Directive.getStandardAddonHints(),
            "Join",
            "Framework");
        return ret;
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
    public void endQuery(QueryContext context) throws QueryException {
        for(Directive d : directives){
            d.endQuery(context);
        }
    }


    @Override
    public Directive join(Directive directive){
        this.directives.add(directive);
        return this;
    }
/*
    @Override
    public ArrayList<ResultRow> query(QueryContext context,ResultRow input,String as) throws QueryException {
        ArrayList<ArrayList<ResultRow>> res=new ArrayList<ArrayList<ResultRow>>();

        for(Directive d: directives){
            ArrayList<ResultRow> r=d.query(context,input,null);
            res.add(r);
        }

        ArrayList<ResultRow> ret=new ArrayList<ResultRow>();

        int[] pointers=new int[res.size()];
        for(int i=0;i<pointers.length;i++) pointers[i]=0;
        Outer: while(true){
            ResultRow row=null;
            for(int i=0;i<pointers.length;i++){
                if(i==0) row=res.get(0).get(pointers[0]);
                else{
                    row=row.join(res.get(i).get(pointers[i]));
                }
            }
            ret.add(row);

            for(int i=pointers.length-1;i>=-1;i--){
                if(i==-1){
                    break Outer;
                }

                pointers[i]++;
                if(pointers[i]>=res.get(i).size()){
                    pointers[i]=0;
                    continue;
                }
                else break;
            }
        }

        return ret;
    }
*/

    @Override
    public ResultIterator queryIterator(QueryContext context,ResultRow input) throws QueryException {
        return new JoinIterator(context,input);
    }

    @Override
    public boolean isStatic(){
        for(Directive d : directives){
            if(!d.isStatic()) return false;
        }
        return true;
    }

    public String debugStringParams(String indent){
        return debugStringInner(directives,indent);
    }


    private class JoinIterator extends ResultIterator {
        public QueryContext context;
        public ResultRow input;

        public ArrayList<ResultIterator> iterators;
        public boolean allDone=false;

        public ResultRow nextRow;
        public ResultRow[] rows;

        public int counter=0;

        public long startTime=0;

        public JoinIterator(QueryContext context,ResultRow input) throws QueryException {
            this.context=context;
            this.input=input;

            iterators=new ArrayList<ResultIterator>();
            
            for(int i=0;i<directives.size();i++){
                ResultIterator iter=directives.get(i).queryIterator(context,input);
                if(i>0 && !(iter instanceof ResultIterator.EmptyIterator)
                       && !(iter instanceof ResultIterator.SingleIterator)
                       && !(iter instanceof ResultIterator.ListIterator)
                       && !(iter instanceof ResultIterator.CachedIterator) )
                    iter=new ResultIterator.CachedIterator(iter);
                iterators.add(iter);
            }
            rows=new ResultRow[iterators.size()];
            for(int i=0;i<rows.length;i++){
                if(!iterators.get(i).hasNext()){
                    allDone=true;
                    break;
                }
                else rows[i]=iterators.get(i).next();
            }

            nextRow=null;
        }

        public boolean hasNext() throws QueryException {
            if(nextRow!=null) return true;
            if(allDone) return false;
            if(context.checkInterrupt()) throw new QueryException("Execution interrupted");

//            if(counter==0) startTime=System.currentTimeMillis();

            ResultRow row=null;
            for(int i=0;i<rows.length;i++){
                if(row==null){
                    row=rows[i];
                }
                else if(rows[i]!=null) {
                    row=row.join(rows[i]);
                }
            }
            if(row==null) return false;
            nextRow=row;
//            counter++;
//            if((counter%50000)==0){
//                double speed=50000.0/(double)(System.currentTimeMillis()-startTime)*1000.0;
//                System.out.println("Join counter "+counter+" "+speed);
//                startTime=System.currentTimeMillis();
//            }

            for(int i=iterators.size()-1;i>=0;i--){
                ResultIterator iter=iterators.get(i);
                if(iter.hasNext()){
                    rows[i]=iter.next();
                    break;
                }
                else{
                    if(i==0){
                        allDone=true;
                        break;
                    }
                    else{
                        iter.reset();
                        rows[i]=null;
                        if(iter.hasNext()) rows[i]=iter.next();
                        continue;
                    }
                }
            }
            return true;
        }

        public ResultRow next() throws QueryException,NoSuchElementException {
            if(hasNext()){
                ResultRow temp=nextRow;
                nextRow=null;
                return temp;
            }
            else throw new NoSuchElementException();
        }

        public void dispose() throws QueryException {
            for(ResultIterator iter : iterators){
                iter.dispose();
            }
        }

        public void reset() throws QueryException {
            for(ResultIterator iter : iterators){
                iter.reset();
            }
            rows=new ResultRow[iterators.size()];
            for(int i=0;i<rows.length;i++){
                if(!iterators.get(i).hasNext())
                    rows[i]=null;
                else rows[i]=iterators.get(i).next();
            }
            nextRow=null;
            allDone=false;
        }
    }
}
