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
 * TokenizingDatumExtractor.java
 *
 * Created on 24. marraskuuta 2004, 18:55
 */

package org.wandora.application.tools.extractors.datum;


import java.util.*;


/**
 *
 * @author  olli
 */
public class FilteringDatumExtractor implements DatumExtractor {
    
    protected DatumExtractor de;
    protected Map filters;
    protected Map duplicates;
    
    /** Creates a new instance of TokenizingDatumExtractor */
    public FilteringDatumExtractor(DatumExtractor de, Object[] tokenizers) {
        this(de,null,tokenizers);
    }
    public FilteringDatumExtractor(DatumExtractor de, String[] duplicates, Object[] tokenizers) {
        this.de=de;
        this.duplicates=new HashMap();
        if(duplicates!=null){
            for(int i=0;i+1<duplicates.length;i+=2){
                this.duplicates.put(duplicates[i],duplicates[i+1]);
            }
        }
        this.filters=new HashMap();
        String field=null;
        Vector v=null;
        for(int i=0;i<tokenizers.length;i++){
            if(tokenizers[i] instanceof String){
                if(field!=null){
                    filters.put(field,v);
                }
                field=(String)tokenizers[i];
                v=new Vector();
            }
            else{
                v.add(tokenizers[i]);
            }
        }
        if(field!=null){
            filters.put(field,v);
        }
    }
    public FilteringDatumExtractor(DatumExtractor de, Map/*<String,List<DatumFilter>>*/ filters) {
        this.de=de;
        this.filters=filters;
    }
    
    public double getProgress() {
        return de.getProgress();
    }
    
    public java.util.Map next(DataStructure data, org.wandora.piccolo.Logger logger) throws ExtractionException {
        Map datum=de.next(data,logger);
        if(datum==null) return null;
        Iterator iter=duplicates.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry e=(Map.Entry)iter.next();
            datum.put(e.getValue(),datum.get(e.getKey()));
        }
        
        iter=datum.entrySet().iterator();
        HashMap newMap=new LinkedHashMap();
        while(iter.hasNext()){
            Map.Entry e=(Map.Entry)iter.next();
            Object key=e.getKey();
            Object value=e.getValue();
            List l=(List)filters.get(key);
            if(l!=null){
                Iterator iter2=l.iterator();
                while(iter2.hasNext()){
                    DatumFilter f=(DatumFilter)iter2.next();
                    value=f.filter(value);
                }
            }
            newMap.put(key,value);
        }
        return newMap;
    }
    
}
