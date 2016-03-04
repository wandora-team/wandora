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
 */
package org.wandora.modules.usercontrol;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.servlet.ActionAuthenticationException;
import org.wandora.modules.servlet.ActionException;
import org.wandora.modules.servlet.ModulesServlet.HttpMethod;

/**
 *
 * @author olli
 */


public class FrequencyRestrictedContext extends AbstractControlledContext {

    private String keyPrefix="";
    private static String postfixCurrentTime="_time";
    private static String postfixCurrentValue="_value";
    
    private String requiredRole=null;
    
    @Override
    public void init(ModuleManager manager, HashMap<String, Object> settings) throws ModuleException {
        Object o=settings.get("keyPrefix");
        if(o!=null) keyPrefix=o.toString();
        
        o=settings.get("requiredRole");
        if(o!=null) requiredRole=o.toString();
        
        super.init(manager, settings);
    }
    

    @Override
    protected AbstractControlledContext.ForwardResult doForwardRequest(HttpServletRequest req, HttpServletResponse resp, HttpMethod method) throws ServletException, IOException, ActionException {
        try{
            UserAuthenticator.AuthenticationResult res=authenticate(requiredRole, req, resp, method);
            if(res.authenticated) {
                User user=res.user;
                try{
                    boolean allowed=access(user,keyPrefix);
                    if(!allowed && exceptionOnAuthentication) throw new ActionAuthenticationException("Access frequency exceeded for user.",this);
                    return new AbstractControlledContext.ForwardResult(allowed, false, user);
                } catch(UserStoreException use){
                    throw new ActionException(use);
                }
            }
            else return new AbstractControlledContext.ForwardResult(false, res.responded, res.user);
        }catch(AuthenticationException ae){
            throw new ActionException(ae);
        }
    }
    
    
    private static long[] getCurrent(User user,Keys key,long time,String prefix){
        String currentTimeS=user.getOption(prefix+key.toString()+postfixCurrentTime);
        String currentValueS=user.getOption(prefix+key.toString()+postfixCurrentValue);

        if(currentTimeS==null || currentValueS==null || currentTimeS.length()==0 || currentValueS.length()==0) {
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
    
    private static boolean _access(User user,long time,boolean update,String prefix) throws UserStoreException {
        
        for(Keys key : Keys.values()){
            String limitS=user.getOption(prefix+key.toString());
            if(limitS==null || limitS.length()==0) continue;
            int limit=Integer.parseInt(limitS);
            if(limit<0) continue;
            
            long[] current=getCurrent(user,key,time,prefix);
            if(current[1]>=limit) return false;
            else {
                if(update){
                    user.setOption(prefix+key+postfixCurrentTime,""+current[0]);
                    user.setOption(prefix+key+postfixCurrentValue,""+(current[1]+1));
                    if(!user.saveUser()) return false;
                }
            }
        }
        
        return true;
    }    
    
    /*
     * These methods don't update the counters, merely check whether access is allowed. 
     */
    public static boolean isAccessAllowed(User user,String prefix) throws UserStoreException {
        return isAccessAllowed(user,System.currentTimeMillis(),prefix);
    }
    public static boolean isAccessAllowed(User user,long time,String prefix) throws UserStoreException {
        return _access(user,time,false,prefix);
    }
    
    /*
     * These methods check that access is allowed and also update the counters.
     */    
    public static boolean access(User user,String prefix) throws UserStoreException {
        return access(user,System.currentTimeMillis(),prefix);
    }
    public static boolean access(User user,long time,String prefix) throws UserStoreException {
        return _access(user,time,true,prefix);
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
