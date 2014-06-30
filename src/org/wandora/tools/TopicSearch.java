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
 * TopicSearch.java
 *
 * Created on February 27, 2002, 8:06 PM
 */

package org.wandora.tools;


import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import java.io.*;


/**
 *
 * @author  olli
 */
public class TopicSearch extends Object {

    /** Creates new TopicSearch */
    public TopicSearch() {
    }

    private static final float NAMEBOOST=3.0f;
    private static final float KEYWORDBOOST=2.0f;
    private static final float TEXTBOOST=1.0f;
    public static Query parseQuery(String in) {
        // the provided praser can't handle skandinavian letters and weightings per field (keyword/text) can't be done
//        Query q=QueryParser.parse("keywords:("+input+") OR text:("+input+")","keywords",new SimpleAnalyzer());
        
        // this parser supports phrases in double quotes, and prefix-searches when term ends in an asterisk
        // the query string may contain multiple phrases and/or terms separated by white-spaces
        BooleanQuery q=new BooleanQuery();        
        String term="";
        boolean phrase=false;
        PhraseQuery phraseqk=null,phraseqt=null,phraseqn=null;
        int ptr=0;
        while(ptr<in.length()){
            char c=in.charAt(ptr++);
            switch(c){
                case '"':
                    if(phrase){
                        if(term.length()>0){
                            phraseqn.add(new Term("name",term));
                            phraseqk.add(new Term("keyword",term));
                            phraseqt.add(new Term("text",term));
                            term="";
                        }
                        phraseqn.setBoost(NAMEBOOST);
                        phraseqk.setBoost(KEYWORDBOOST);
                        phraseqt.setBoost(TEXTBOOST);
                        q.add(phraseqn,BooleanClause.Occur.SHOULD);
                        q.add(phraseqk,BooleanClause.Occur.SHOULD);
                        q.add(phraseqt,BooleanClause.Occur.SHOULD);
                        phrase=false;
                    }
                    else{
                        if(term.length()>0){
                            TermQuery tq=new TermQuery(new Term("keyword",term));
                            tq.setBoost(KEYWORDBOOST);
                            q.add(tq,BooleanClause.Occur.SHOULD);
                            tq=new TermQuery(new Term("text",term));
                            tq.setBoost(TEXTBOOST);
                            q.add(tq,BooleanClause.Occur.SHOULD);
                            tq=new TermQuery(new Term("name",term));
                            tq.setBoost(NAMEBOOST);
                            q.add(tq,BooleanClause.Occur.SHOULD);
                            term="";
                        }
                        phrase=true;
                        phraseqn=new PhraseQuery();
                        phraseqk=new PhraseQuery();
                        phraseqt=new PhraseQuery();
                    }
                    break;
                case ' ':
                    if(!phrase){
                        if(term.length()>0){
                            TermQuery tq=new TermQuery(new Term("keyword",term));
                            tq.setBoost(KEYWORDBOOST);
                            q.add(tq,BooleanClause.Occur.SHOULD);
                            tq=new TermQuery(new Term("text",term));
                            tq.setBoost(TEXTBOOST);
                            q.add(tq,BooleanClause.Occur.SHOULD);
                            tq=new TermQuery(new Term("name",term));
                            tq.setBoost(NAMEBOOST);
                            q.add(tq,BooleanClause.Occur.SHOULD);
                            term="";                        
                        }
                    }
                    else{
                        if(term.length()>0){
                            phraseqn.add(new Term("name",term));
                            phraseqk.add(new Term("keyword",term));
                            phraseqt.add(new Term("text",term));
                            term="";
                        }
                    }
                    break;
                case '*':
                    if(!phrase){
                        if(term.trim().length()>1) { // do not add very general searches which will most likely cause an OutOfMemoryException later
                            PrefixQuery pq=new PrefixQuery(new Term("keyword",term.trim()));
                            pq.setBoost(KEYWORDBOOST);
                            q.add(pq,BooleanClause.Occur.SHOULD);
                            pq=new PrefixQuery(new Term("text",term.trim()));
                            pq.setBoost(TEXTBOOST);
                            q.add(pq,BooleanClause.Occur.SHOULD);
                            pq=new PrefixQuery(new Term("name",term.trim()));
                            pq.setBoost(NAMEBOOST);
                            q.add(pq,BooleanClause.Occur.SHOULD);
                        }
                        term="";
                    }
                    else{
                        term+=""+Character.toLowerCase(c);
                    }
                    break;
                default:
                    term+=""+Character.toLowerCase(c);
                    break;                        
            }            
        }
        if(term.length()>0 || phrase){
            if(!phrase){
                TermQuery tq=new TermQuery(new Term("keyword",term));
                tq.setBoost(KEYWORDBOOST);
                q.add(tq,BooleanClause.Occur.SHOULD);
                tq=new TermQuery(new Term("text",term));
                tq.setBoost(TEXTBOOST);
                q.add(tq,BooleanClause.Occur.SHOULD);
                tq=new TermQuery(new Term("name",term));
                tq.setBoost(NAMEBOOST);
                q.add(tq,BooleanClause.Occur.SHOULD);
                term="";                        
            }
            else{
                if(term.length()>0){
                    phraseqn.add(new Term("name",term));
                    phraseqk.add(new Term("keyword",term));
                    phraseqt.add(new Term("text",term));
                    term="";
                }
                phraseqn.setBoost(NAMEBOOST);
                phraseqk.setBoost(KEYWORDBOOST);
                phraseqt.setBoost(TEXTBOOST);
                q.add(phraseqn,BooleanClause.Occur.SHOULD);
                q.add(phraseqk,BooleanClause.Occur.SHOULD);
                q.add(phraseqt,BooleanClause.Occur.SHOULD);
                phrase=false;
            }
        }
        return q;
    }
    
    public static void main(String[] args) throws Exception {
        String index="/home/tm/topicmap/tmindex";
        if(args.length>0) index=args[0];
        IndexReader reader=IndexReader.open(new File(index));       
        IndexSearcher searcher=new IndexSearcher(reader);
        BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter query string or quit to stop\n");
        String input=in.readLine();
        while(!input.equalsIgnoreCase("quit")){            
            Query q=parseQuery(input);
            
            Hits hits=searcher.search(q);
            for(int i=0;i<Math.min(10,hits.length());i++){
                double score=hits.score(i);
                String topic=hits.doc(i).get("topic");
                String type=hits.doc(i).get("type");
                String keywords=hits.doc(i).get("keywords");
                // text is not stored
                String url=hits.doc(i).get("url");
                System.out.println(""+score+"   "+type+" "+topic+" "+url);
            }
            if(hits.length()>10) System.out.println("Total number of hits: "+hits.length());
            if(hits.length()==0) System.out.println("No hits");
            System.out.println(); System.out.println();
            input=in.readLine();            
        }
    }

}
