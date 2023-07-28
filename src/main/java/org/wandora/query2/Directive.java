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
 * Directive.java
 *
 */
package org.wandora.query2;
import java.util.*;
import org.wandora.query2.DirectiveUIHints.Addon;
import org.wandora.query2.DirectiveUIHints.Parameter;

/**
 *
 * Subclasses must override at least one of query or queryIterator. Default
 * implementation has them calling eachother. Whenever the result can
 * meaningfully be delivered in smaller chunks, iterator should be used to
 * avoid having huge array lists in memory.
 *
 * @author olli
 */
public abstract class Directive {

    public static final String DEFAULT_NS="http://wandora.org/si/query/";
    public static final String DEFAULT_COL="#DEFAULT";

    public static Addon[] getStandardAddonHints(){
        return getStandardAddonHints(new Addon[0]);
    }
    public static Addon[] getStandardAddonHints(Addon[] extras){
        ArrayList<Addon> addons=new ArrayList<Addon>();
        
        addons.add(new Addon("as", new Parameter[]{new Parameter(String.class,false,"column name")}, "as"));
        addons.add(new Addon("as", new Parameter[]{
            new Parameter(String.class,false,"old column name"),
            new Parameter(String.class,false,"new column name")
        }, "as"));
        addons.add(new Addon("from", new Parameter[]{new Parameter(String.class,true,"literal")}, "from literals"));
        addons.add(new Addon("of", new Parameter[]{new Parameter(String.class,false,"column")}, "of"));
        addons.add(new Addon("where", new Parameter[]{new Parameter(Directive.class,false,"where directive")}, "where"));
        addons.add(new Addon("where", new Parameter[]{
            new Parameter(Operand.class,Object.class,false,"operand 1"),
            new Parameter(String.class,false,"operator"),
            new Parameter(Operand.class,Object.class,false,"operand 2")
        }, "where"));
        addons.add(new Addon("where", new Parameter[]{
            new Parameter(TopicOperand.class,Object.class,false,"topic operand 1"),
            new Parameter(String.class,false,"operator"),
            new Parameter(TopicOperand.class,Object.class,false,"topic operand 2")
        }, "where"));
        
        if(extras!=null && extras.length>0) {
            for(int i=0;i<extras.length;i++) { addons.add(extras[i]); }
        }
        return addons.toArray(new Addon[addons.size()]);
    }
    
    /**
     * Prepares the query for execution. Extending classes must propagate
     * the call to any contained queries.
     */
    public boolean startQuery(QueryContext context) throws QueryException {
        return true;
    }
    /**
     * Signals end of query and performs any cleanup that may be needed.
     * Extending classes must propagate the call to any contained queries.
     */
    public void endQuery(QueryContext context) throws QueryException {
    }

    /**
     * Executes the query buffering all results in a list and returning that.
     * This may be useful at the top level where the results are going to be
     * retrieved entirely in a list anyway. Inside the query you should avoid
     * using this as the intermediate results can become very large due to
     * join operations.
     *
     * startQuery must have been called before calling this and endQuery after
     * the query is done. If this is the top level directive you may have to do
     * it manually. These call should propaget automaticall to all inner queries.
     */
    public ArrayList<ResultRow> query(QueryContext context,ResultRow input) throws QueryException {
        ResultIterator iter=queryIterator(context,input);
        ArrayList<ResultRow> res=new ArrayList<ResultRow>();
        while(iter.hasNext()) {
            if(context.checkInterrupt()) throw new QueryException("Execution interrupted");
            res.add(iter.next());
        }
        iter.dispose();
        return res;
    }

    /**
     * This method does all necessary preparations, executes the query and
     * returns with a list containing the results. This is the easiest way
     * to execute a query. You can interrupt the query through the context
     * object by calling interrupt in it.
     */
    public ArrayList<ResultRow> doQuery(QueryContext context,ResultRow input) throws QueryException {
        if(!startQuery(context)) return new ArrayList<ResultRow>();
        ArrayList<ResultRow> ret=query(context,input);
        endQuery(context);
        return ret;
    }

    /**
     * You must call startQuery before calling this and endQuery after you are
     * done with the result iterator. You should also call dispose of the
     * result iterator when you're done.
     */
    public ResultIterator queryIterator(QueryContext context,ResultRow input) throws QueryException {
        return new ResultIterator.ListIterator(query(context,input));
    }

    public Directive join(Directive directive){
        return new Join(this,directive);
    }

    public Directive to(Directive[] directives){
        Directive to=null;
        for(int i=0;i<directives.length;i++){
            if(to==null) to=directives[i];
            else to=to.join(directives[i]);
        }
        return new From(to,this);
    }
    public Directive to(Directive d1){return to(new Directive[]{d1});}
    public Directive to(Directive d1,Directive d2){return to(new Directive[]{d1,d2});}
    public Directive to(Directive d1,Directive d2,Directive d3){return to(new Directive[]{d1,d2,d3});}
    public Directive to(Directive d1,Directive d2,Directive d3,Directive d4){return to(new Directive[]{d1,d2,d3,d4});}

    public Directive from(Directive[] directives){
        Directive from=null;
        for(int i=0;i<directives.length;i++){
            if(from==null) from=directives[i];
            else from=from.join(directives[i]);
        }
        return new From(this,from);
    }
    public Directive from(Directive d1){return from(new Directive[]{d1});}
    public Directive from(Directive d1,Directive d2){return from(new Directive[]{d1,d2});}
    public Directive from(Directive d1,Directive d2,Directive d3){return from(new Directive[]{d1,d2,d3});}
    public Directive from(Directive d1,Directive d2,Directive d3,Directive d4){return from(new Directive[]{d1,d2,d3,d4});}

    public Directive from(String[] literals){
        return from(new Literals(literals));
    }
    public Directive from(String s1){return from(new String[]{s1});}
    public Directive from(String s1,String s2){return from(new String[]{s1,s2});}
    public Directive from(String s1,String s2,String s3){return from(new String[]{s1,s2,s3});}
    public Directive from(String s1,String s2,String s3,String s4){return from(new String[]{s1,s2,s3,s4});}

    public Directive of(String col){
        return this.from(new Of(col));
    }

    public Directive where(Object c1,String comp,Object c2){
        return where(new Compare(c1,comp,c2));
    }

    public Directive where(Directive d){
        return d.from(this);
    }

    public Directive as(String newRole){
        return new As(newRole).from(this);
    }

    public Directive as(String original,String newRole){
        return new As(original,newRole).from(this);
    }

    public boolean isStatic(){
        return false;
    }

    public static String debugStringInner(Directive[] directives,String indent){
        StringBuilder ret=new StringBuilder("");
        if(directives==null){
            ret.append("null");
        }
        else {
            ret.append("\n");
            for(int i=0;i<directives.length;i++){
                if(i>0) ret.append(",\n");
                ret.append(indent+"\t");
                ret.append(directives[i].debugString(indent+"\t"));
            }
            ret.append("\n");
        }
        ret.append(indent);
        return ret.toString();
    }

    public static String debugStringInner(List<Directive> directives,String indent){
        StringBuilder ret=new StringBuilder("\n");
        for(int i=0;i<directives.size();i++){
            if(i>0) ret.append(",\n");
            ret.append(indent+"\t");
            ret.append(directives.get(i).debugString(indent+"\t"));
        }
        ret.append("\n");
        ret.append(indent);
        return ret.toString();
    }

    public static String debugStringInner(Directive directive,String indent){
        StringBuilder sb=new StringBuilder("");
        sb.append("\n\t"+indent);
        if(directive==null) sb.append("null");
        else sb.append(directive.debugString(indent+"\t"));
        sb.append("\n"+indent);
        return sb.toString();
    }

    public String debugStringParams(){return "";}
    public String debugStringParams(String indent){return debugStringParams();}

    public String debugString(){
        return debugString("");
    }
    public String debugString(String indent){
        return this.getClass().getSimpleName()+"("+debugStringParams(indent)+")";
    }
/*
    public static String getOperandString(Object o,QueryContext context,ResultRow input) throws QueryException {
        Object ob=getOperand(o,context,input);
        if(ob==null) return null;
        else return ob.toString();
    }
    public static Object getOperand(Object o,QueryContext context,ResultRow input) throws QueryException {
        if(o==null) return null;
        else if(o instanceof Directive) {
            ResultIterator iter=((Directive)o).queryIterator(context, input);
            if(iter.hasNext()){
                Object ret=iter.next().getActiveValue();
                iter.dispose();
                return ret;
            }
            else {
                return null;
            }
        }
        else return o;
    }
*/
    public static void main(String[] args) throws Exception {
/*        String debug=
            new BaseName().of("#in").as("#bn").from(
              new Instances().from("http://wandora.org/si/core/schema-type").as("#in"),
              new Literals("Content type").as("#literals")
            ).where("#bn","=","#literals").debugString();*/
/*        String debug=
            new Players("http://www.wandora.net/freedb/track",
                    "http://www.wandora.net/freedb/artist"
            ).as("#artist").of("#track").from(
                new Regex("^([^\\(]*).*$","$1",0).from(
                    new BaseName().from(
                        new Instances().from("http://www.wandora.net/freedb/track")
                            .as("#track")
                    )
                ).as("#trackname"),
                new Regex("^([^\\(]*).*$","$1",0).from(
                    new BaseName().from(
                        new Players(
                            "http://www.wandora.net/freedb/track",
                            "http://www.wandora.net/freedb/track"
                        ).as("#queentrack").from(
                            new Literals("http://www.wandora.net/freedb/artist/QUEEN").as("#queen")
                        )
                    )
                ).as("#queenname")
            ).where("#trackname","=","#queenname")
            .where("#artist","t!=","#queen").debugString();*/
        String debug=new Identity().as("#b").of("#c").from(new BaseName()).debugString();
        System.out.println(debug);
        debug=new Identity().of("#c").as("#b").from(new BaseName()).debugString();
        System.out.println(debug);
        debug=new Identity().of("#c").from(new BaseName()).as("#b").debugString();
        System.out.println(debug);
    }
}
