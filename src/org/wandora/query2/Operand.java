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
 * Operand.java
 *
 */

package org.wandora.query2;

/**
 *
 * @author olli
 */
public class Operand extends Directive implements DirectiveUIHints.Provider {
    protected Object operand;
    protected boolean stat;

    protected Object cache;
    protected boolean isCached;

    public Operand(){}
    
    public Operand(Object operand){
        this.operand=operand;
        if(operand!=null && operand instanceof Directive){
            stat=((Directive)operand).isStatic();
        }
        else stat=true;
    }
    
    @Override
    public DirectiveUIHints getUIHints() {
        DirectiveUIHints ret=new DirectiveUIHints(Players.class,new DirectiveUIHints.Constructor[]{
                new DirectiveUIHints.Constructor(new DirectiveUIHints.Parameter[]{
                        new DirectiveUIHints.Parameter(Object.class, false, "operand"),
                }, "")
            },
            Directive.getStandardAddonHints(),
            "Operand",
            "Framework");
        return ret;
    }         

    public static Operand makeOperand(Object o){
        if(o!=null && o instanceof Operand) return (Operand)o;
        else return new Operand(o);
    }
    public static Operand[] makeOperands(Object[] os){
        Operand[] ret=new Operand[os.length];
        for(int i=0;i<ret.length;i++){
            ret[i]=makeOperand(os[i]);
        }
        return ret;
    }

    public static boolean startOperands(QueryContext context,Operand ... os) throws QueryException{
        boolean ret=true;
        for(int i=0;i<os.length;i++){
            ret&=os[i].startQuery(context);
        }
        return ret;
    }
    public static void endOperands(QueryContext context,Operand ... os) throws QueryException {
        for(int i=0;i<os.length;i++){
            os[i].endQuery(context);
        }
    }

    @Override
    public boolean startQuery(QueryContext context) throws QueryException {
        isCached=false;
        cache=null;
        if(operand!=null && operand instanceof Directive){
            return ((Directive)operand).startQuery(context);
        }
        else return true;
    }

    @Override
    public void endQuery(QueryContext context) throws QueryException {
        if(operand!=null && operand instanceof Directive){
            ((Directive)operand).endQuery(context);
        }
    }

    @Override
    public boolean isStatic() {
        return stat;
    }

    @Override
    public ResultIterator queryIterator(QueryContext context, ResultRow input) throws QueryException {
        // This isn't usually used for operand directives. getOperandObejct is used instead.
        return new ResultRow(getOperandObject(context,input)).toIterator();
    }
    public String getOperandString(QueryContext context,ResultRow input) throws QueryException {
        Object ret=getOperandObject(context,input);
        if(ret==null) return null;
        else return ret.toString();
    }
    public Object getOperandObject(QueryContext context,ResultRow input) throws QueryException {
        if(stat && isCached) return cache;

        Object ret=null;

        if(operand==null) ret=null;
        else if(operand.getClass()==Of.class){
            // Special handling for one common case. Skips the creation of ResultIterators and such.
            ret=input.getValue(((Of)operand).getRole());
        }
        else if(operand instanceof Directive) {
            ResultIterator iter=((Directive)operand).queryIterator(context, input);
            if(iter.hasNext()){
                ret=iter.next().getActiveValue();
            }
            else {
                ret=null;
            }
            iter.dispose();
        }
        else ret=operand;

        if(stat){
            isCached=true;
            cache=ret;
        }
        return ret;
    }

}
