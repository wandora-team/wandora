
package org.wandora.piccolo.accesscontrol;

import java.util.GregorianCalendar;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wandora.piccolo.Action;
import org.wandora.piccolo.Application;
import org.wandora.piccolo.Logger;
import org.wandora.piccolo.User;
import org.wandora.utils.XMLParamAware;
import org.wandora.utils.XMLParamProcessor;

/**
 *
 * @author olli
 */


public class FrequencyController implements Action,XMLParamAware {
    
    private Action action;
    
    public FrequencyController(){} // empty constructor only for xml initialisation, action must be provided in xml
    public FrequencyController(Action action){
        this.action=action;
    }
    
    public static Action makeRestrictedAction(final Action action){
        return new FrequencyController(action);
    }
    
    private static long[] getCurrent(User user,Keys key,long time){
        String currentTimeS=user.getStringProperty(key.toString()+postfixCurrentTime);
        String currentValueS=user.getStringProperty(key.toString()+postfixCurrentValue);

        if(currentTimeS==null || currentValueS==null) {
            long next=key.getEndTime(time).getTimeInMillis();
            return new long[]{next,0};            
        }
        
        long currentTime=Long.parseLong(currentTimeS);
        int currentValue=Integer.parseInt(currentValueS);
        
        if(currentTime<time) {
            long next=key.getEndTime(time).getTimeInMillis();
            return new long[]{next,0};
        }
            
        else return new long[]{currentTime,currentValue};
    }
    
    private static boolean _access(User user,long time,boolean update){
        
        for(Keys key : Keys.values()){
            String limitS=user.getStringProperty(key.toString());
            if(limitS==null) continue;
            int limit=Integer.parseInt(limitS);
            if(limit<0) continue;
            
            long[] current=getCurrent(user,key,time);
            if(current[1]>=limit) return false;
            else {
                if(update){
                    user.setProperty(key+postfixCurrentTime,current[0]);
                    user.setProperty(key+postfixCurrentValue,current[1]+1);
                }
            }
        }
        
        return true;
    }
    
    /*
     * These methods don't update the counters, merely check whether access is allowed. 
     */
    public static boolean isAccessAllowed(User user){
        return isAccessAllowed(user,System.currentTimeMillis());
    }
    public static boolean isAccessAllowed(User user,long time){
        return _access(user,time,false);
    }
    
    /*
     * These methods check that access is allowed and also update the counters.
     */
    public static boolean access(User user){
        return access(user,System.currentTimeMillis());
    }
    public static boolean access(User user,long time){
        return _access(user,time,true);
    }
    
    private static String postfixCurrentTime="_time";
    private static String postfixCurrentValue="_value";

    @Override
    public void xmlParamInitialize(Element element, XMLParamProcessor processor) {
        NodeList nl=element.getChildNodes();
        for(int i=0;i<nl.getLength();i++){
            Node n=nl.item(i);
            if(n instanceof Element){
                Element e=(Element)n;
                try{
                    String name=e.getNodeName();
                    if(name.equals("action")){
                        Object o=processor.createObject(e);
                        this.action=(Action)o;
                    }
                }catch(Exception ex){
                    if(processor.getObject("logger")!=null)
                        ((Logger)processor.getObject("logger")).writelog("WRN",ex.getClass().getName()+" when processing config file, element "+e.getNodeName()+". "+ex.getMessage());  
                }
            }
        }
        if(this.action==null){
            if(processor.getObject("logger")!=null)
                ((Logger)processor.getObject("logger")).writelog("WRN","Action not provided to FrequencyController.");  
        }
    }

    @Override
    public void doAction(User user, ServletRequest request, ServletResponse response, Application application) {
        if(access(user)) action.doAction(user, request, response, application);        
    }
    
    public static enum Keys {
        per1sec(1), 
        per10sec(10), 
        per1min(60), 
        per10min(600), 
        per1hour(3600), 
        per1day(3600*24), 
        per1month(3600*24*30), 
        per1year(3600*34*365);
        
        private int seconds;
        Keys(int seconds){
            this.seconds=seconds;
        }
        public GregorianCalendar getStartTime(long time){
            if(this==per1month){
                GregorianCalendar ret=new GregorianCalendar();
                ret.setTimeInMillis(time);
                ret.set(GregorianCalendar.MILLISECOND, 0);
                ret.set(GregorianCalendar.SECOND, 0);
                ret.set(GregorianCalendar.MINUTE, 0);
                ret.set(GregorianCalendar.HOUR, 0);
                ret.set(GregorianCalendar.DAY_OF_MONTH, 1);
                return ret;                
            }
            else if(this==per1year){
                GregorianCalendar ret=new GregorianCalendar();
                ret.setTimeInMillis(time);
                ret.set(GregorianCalendar.MILLISECOND, 0);
                ret.set(GregorianCalendar.SECOND, 0);
                ret.set(GregorianCalendar.MINUTE, 0);
                ret.set(GregorianCalendar.HOUR, 0);
                ret.set(GregorianCalendar.DAY_OF_MONTH, 1);
                ret.set(GregorianCalendar.MONTH, 0);
                return ret;                                
            }
            else{
                GregorianCalendar ret=new GregorianCalendar();
                ret.setTimeInMillis((time/(this.seconds*1000))*this.seconds*1000);
                return ret;
            }
        }
        public GregorianCalendar getEndTime(long time){
            if(this==per1month){
                GregorianCalendar ret=new GregorianCalendar();
                ret.setTimeInMillis(time);
                ret.set(GregorianCalendar.MILLISECOND, 0);
                ret.set(GregorianCalendar.SECOND, 0);
                ret.set(GregorianCalendar.MINUTE, 0);
                ret.set(GregorianCalendar.HOUR, 0);
                ret.set(GregorianCalendar.DAY_OF_MONTH, 1);
                ret.add(GregorianCalendar.MONTH, 1);
                return ret;                
            }
            else if(this==per1year){
                GregorianCalendar ret=new GregorianCalendar();
                ret.setTimeInMillis(time);
                ret.set(GregorianCalendar.MILLISECOND, 0);
                ret.set(GregorianCalendar.SECOND, 0);
                ret.set(GregorianCalendar.MINUTE, 0);
                ret.set(GregorianCalendar.HOUR, 0);
                ret.set(GregorianCalendar.DAY_OF_MONTH, 1);
                ret.set(GregorianCalendar.MONTH, 0);
                ret.add(GregorianCalendar.YEAR, 1);
                return ret;                                
            }
            else{
                GregorianCalendar ret=new GregorianCalendar();
                ret.setTimeInMillis(((time+this.seconds*1000)/(this.seconds*1000))*this.seconds*1000);
                return ret;            
            }
        }
    }
}
