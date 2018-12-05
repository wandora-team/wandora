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
package org.wandora.modules;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.wandora.modules.usercontrol.User;

/**
 * A basic implementation of the replacements system. You can add replacements
 * in the module parameters using parameter names replacement.key. The every occurrence
 * of key is replaced in the text with the value of the parameter. In addition to these
 * some default dynamic replacements are available. $DATETIME is replaced with a date
 * and time stamp. $USER is replaced with the name of the logged in user, if one is
 * available in the context.
 * 
 * @author olli
 */


public class DefaultReplacementsModule extends AbstractModule implements ReplacementsModule {

    protected HashMap<String,Replacement> replacements;
    
    @Override
    public void init(ModuleManager manager, Map<String, Object> settings) throws ModuleException {
        replacements=new HashMap<String,Replacement>();
        
        final SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        replacements.put("$DATETIME",new Replacement(){
            @Override
            public String replace(String value, String key,HashMap<String,Object> context) {
                if(value.indexOf(key)>=0){
                    String date=dateFormat.format(new Date());
                    return value.replace(key, date);
                }
                else return value;
            }
        });
        replacements.put("$USER",new Replacement(){
            @Override
            public String replace(String value, String key, HashMap<String, Object> context) {
                if(value.indexOf(key)>=0){
                    Object o=context.get("user");
                    if(o!=null && o instanceof User){
                        User user=(User)o;
                        value.replace(key, user.getUserName());
                    }
                }
                return value;
            }
        });
        
        
        for(Map.Entry<String,Object> e : settings.entrySet()){
            String key=e.getKey();
            if(key.startsWith("replacement.")){
                key=key.substring("replacement.".length());
                Object o=e.getValue();
                if(o==null) {
                    replacements.remove(key);
                }
                else {
                    if(o instanceof Replacement) replacements.put(key,(Replacement)o);
                    else if(o instanceof String) replacements.put(key,new StringReplacement(o.toString()));
                    else throw new ModuleException("Invalid replacement value");
                }
            }
        }
        
        super.init(manager, settings);
    }

    
    
    @Override
    public String replaceValue(String value,HashMap<String,Object> context) {
        if(context==null) context=new HashMap<String,Object>();
        for(Map.Entry<String,Replacement> e : replacements.entrySet()){
            String key=e.getKey();
            Replacement r=e.getValue();
            value=r.replace(value, key, context);
        }
        return value;
    }

    public static interface Replacement {
        public String replace(String value,String key,HashMap<String,Object> context);
    }
    
    public static class StringReplacement implements Replacement {
        private String repl;
        public StringReplacement(String repl){ this.repl=repl; }
        public String replace(String value,String key,HashMap<String,Object> context){
            return value.replace(key, repl);
        }
    }
    
    public static class RegexReplacement implements Replacement {
        private Pattern pattern;
        private String repl;
        public RegexReplacement(String pattern,String repl) {
            this(Pattern.compile(pattern),repl);
        }
        public RegexReplacement(Pattern pattern,String repl){
            this.pattern=pattern;
            this.repl=repl;
        }

        @Override
        public String replace(String value, String key,HashMap<String,Object> context) {
            Matcher m=pattern.matcher(value);
            return m.replaceAll(repl);
        }
        
    }
}
