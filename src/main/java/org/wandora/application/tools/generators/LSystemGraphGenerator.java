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
 * LSystemGraphGenerator.java
 *
 * Created on 2008-09-20, 16:42
 *
 */


package org.wandora.application.tools.generators;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.Tuples.T2;

/**
 *
 * @author akivela
 */
public class LSystemGraphGenerator extends AbstractGenerator implements WandoraTool {

	private static final long serialVersionUID = 1L;

	public static String DEFAULT_SI_PREFIX = "http://wandora.org/si/l-system/";
    public static String DEFAULT_ASSOCIATION_TYPE_SI = DEFAULT_SI_PREFIX+"association-type";
    public static String DEFAULT_ROLE1_SI = DEFAULT_SI_PREFIX+"role-1";
    public static String DEFAULT_ROLE2_SI = DEFAULT_SI_PREFIX+"role-2";
    
    public String userSiPrefix = DEFAULT_SI_PREFIX;   

    public String currentColor = null;

    private int topicCounter = 0;
    private int associationCounter = 0;
    

    
    private LSystemGraphGeneratorDialog sourceDialog = null;
    
    /** Creates a new instance of LSystemGraphGenerator */
    public LSystemGraphGenerator() {
    }


    @Override
    public String getName() {
        return "L-system graph generator";
    }
    
    @Override
    public String getDescription() {
        return "Generates topic maps with L-systems.";
    }
    
    @Override
    public void execute(Wandora admin, Context context) throws TopicMapException {
        TopicMap tm = solveContextTopicMap(admin, context);
        topicCounter = 0;
        associationCounter = 0;
        
        if(sourceDialog == null) sourceDialog = new LSystemGraphGeneratorDialog(admin, this, true);
        sourceDialog.setAccepted(false);
        sourceDialog.setVisible(true);
        if(!sourceDialog.wasAccepted()) return;
        
        setDefaultLogger();
        setLogTitle("L-system generator");
        
        int sourceType = sourceDialog.getContentType();
        if(sourceType == LSystemGraphGeneratorDialog.L_SYSTEM) {
            String systemStr = sourceDialog.getContent();
            int depth = sourceDialog.getDepth();

            log("Starting L-system generation.");
            
            userSiPrefix = DEFAULT_SI_PREFIX;
    
            boolean initiatorFound = false;

            String[] systemArray = systemStr.split("\n");
            if(systemArray.length > 1) {
                ArrayList<Rule> rules = new ArrayList<Rule>();
                String str = null;
                Word initiator = null;
                
                for(int i=0; i<systemArray.length; i++) {
                    str = systemArray[i];
                    if(str != null) {
                        str = str.trim();
                        if(str.length() > 0) {
                            if(!str.startsWith("#")) {
                                if(!initiatorFound) {
                                    initiator = new Word( str );
                                    initiatorFound = true;
                                }
                                else {
                                    String[] ruleStr = str.split("-->");
                                    if(ruleStr.length == 2) {
                                        ruleStr[0] = ruleStr[0].trim();
                                        ruleStr[1] = ruleStr[1].trim();

                                        if(ruleStr[0].length() > 0 && ruleStr[1].length() > 0) {
                                            Word rule_pred = new Word( ruleStr[0] );
                                            Word rule_succ = new Word( ruleStr[1] );

                                            Rule rule = new Rule( rule_pred, rule_succ );
                                            rules.add(rule);
                                        }
                                    }
                                }
                            }
                            else {
                                if(str.startsWith("#si-prefix:")) {
                                    str = str.substring(11);
                                    str = str.trim();
                                    if(str.matches("[a-zA-Z0-9]+\\:\\/\\/.+?")) {
                                        userSiPrefix = str;
                                    }
                                }
                            }
                        }
                    }
                }
                if(initiator == null) {
                    log("Invalid L-system initiator given. First text line of L-system should contain initiator.");
                }
                else if(rules.isEmpty()) {
                    log("No L-system rules given. L-system rule format is 'predecessor' --> 'successor' ");
                }
                else {
                    boolean doit = true;
                    if(depth > 10) {
                        int a = WandoraOptionPane.showConfirmDialog(admin, "You have specified L-system iteration depth higher than 10. "+
                                "It is very likely L-system generates very high number of topics and associations. "+
                                "Would you like to continue anyway with given iteration depth?", "Iteration depth accepted?");
                        if(a == WandoraOptionPane.NO_OPTION) doit = false;
                    }
                    if(doit) {
                        log("Generating L-system string:");
                        LSystem lsystem = new LSystem(initiator, rules, depth );
                        log(lsystem.getState().toString());

                        LSystemParser lparser = new LSystemParser(this, lsystem.getState(), tm);
                        log("Parsing L-system results.");
                        lparser.parse();
                    }
                }
            }
            else {
                log("No L-system given.");
            }
        }
        else if(sourceType == LSystemGraphGeneratorDialog.RAW_RESULT) {
            LSystemParser lparser = new LSystemParser(this, new Word( sourceDialog.getContent() ), tm);
            log("Parsing L-system string.");
            lparser.parse();
        }
        if(forceStop()) log("User has stopped the L-system generator.");
        log("Total "+topicCounter+" topics created.");
        log("Total "+associationCounter+" associations created.");
        log("Ready.");
        setState(WAIT);
    }
    
    
    

    
    
    public Topic createNamedTopic(TopicMap tm, String name) throws Exception {
        Topic t = tm.getTopicWithBaseName("topic "+name);
        if(t == null) {
            t = tm.createTopic();
            t.addSubjectIdentifier(new Locator(userSiPrefix + "topic-" + name));
            t.setBaseName("topic "+name);
        }
        if(currentColor != null) {
            String typeSi = userSiPrefix+currentColor+"/topic-type";
            Topic type = tm.getTopic(typeSi);
            if(type == null) {
                type = tm.createTopic();
                type.addSubjectIdentifier(new Locator(typeSi));
                type.setBaseName("type-"+currentColor);
            }
            t.addType(type);
        }
        return t;
    }
    
    
    
    public Topic createTopic(TopicMap tm) throws Exception {
        Topic t = tm.getTopicWithBaseName("L-system topic "+topicCounter);
        if(t == null) {
            t = tm.createTopic();
            t.addSubjectIdentifier(new Locator(userSiPrefix + "topic-" + topicCounter));
            t.setBaseName("topic "+topicCounter);
        }
        if(currentColor != null) {
            String typeSi = userSiPrefix+currentColor+"/topic-type";
           
            Topic type = tm.getTopic(typeSi);
            if(type == null) {
                type = tm.createTopic();
                type.addSubjectIdentifier(new Locator(typeSi));
                type.setBaseName("L-system type-"+currentColor);
            }
            t.addType(type);
        }
        topicCounter++;
        return t;
    }
    
    
    
    public Association createAssociation(TopicMap tm, Topic t1, Topic t2) throws Exception {
        if(t1 == null || t2 == null) return null;
        
        String actualAssociationTypeSi = userSiPrefix+"association-type";
        String actualRole1Si = userSiPrefix+"role-1";
        String actualRole2Si = userSiPrefix+"role-2";
    
        String actualAssociationTypeName = "association-type";
        String actualRole1Name = "role-1";
        String actualRole2Name = "role-2";

        if(currentColor != null) {
            actualAssociationTypeSi = userSiPrefix+currentColor+"/association-type";
            actualRole1Si = userSiPrefix+currentColor+"/role-1";
            actualRole2Si = userSiPrefix+currentColor+"/role-2";
            
            actualAssociationTypeName = "association-type ("+currentColor+")";
            actualRole1Name = "role-1 ("+currentColor+")";
            actualRole2Name = "role-2 ("+currentColor+")";
        }
        
        Topic atype = tm.getTopic(actualAssociationTypeSi);
        if(atype == null) {
            atype = tm.createTopic();
            atype.addSubjectIdentifier(new Locator(actualAssociationTypeSi));
            atype.setBaseName(actualAssociationTypeName);
        }
        Topic role1 = tm.getTopic(actualRole1Si);
        if(role1 == null) {
            role1 = tm.createTopic();
            role1.addSubjectIdentifier(new Locator(actualRole1Si));
            role1.setBaseName(actualRole1Name);
        }
        
        Topic role2 = tm.getTopic(actualRole2Si);
        if(role2 == null) { 
            role2 = tm.createTopic();
            role2.addSubjectIdentifier(new Locator(actualRole2Si));
            role2.setBaseName(actualRole2Name);
        }

        //System.out.println("create association: "+atype);
        //System.out.println("  with player: "+t1+" and role "+role1);
        //System.out.println("  with player: "+t2+" and role "+role2);
        
        Association a = tm.createAssociation(atype);
        a.addPlayer(t1, role1);
        a.addPlayer(t2, role2);
        
        associationCounter++;
        
        return a;
    }
    
    
    
    // -------------------------------------------------------------------------
    // ------ LSystemParser -------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    
    /**
     * LSystemParser takes an <code>Alphabet</code> <code>Word</code> and parses
     * it. Parser's vocabulary is
     * 
     * <pre>
     *  a       Create a topic and associate it with previous topic if such exists in current block.
     *  A-V     Create named topic and associate it with previous topic if such exists in current block.
     *  eyuio   Create colored (=typed) topic and associate it with previous one using colored association.
     *  
     *  [       Start parallel block
     *  ]       Close parallel block
     *  (       Start sequential block
     *  )       Close sequential block
     *  {       Start cycle block
     *  }       Close cycle block
     *
     *  -       Substract topic counter by one
     *  +       Add topic counter by one
     *  0       Reset topic counter
     * 
     *  :c      Set topic/association color c (global setting)
     *  :0      Reset topic/association color (global setting)
     * </pre>
     * 
    **/
    
    private class LSystemParser {
       
        private WandoraTool parent = null;
        private Word input = null;
        private int parsePoint = 0;
        private Stack<T2<Alphabet,Stack<Topic>>> stackStack = new Stack<T2<Alphabet,Stack<Topic>>>();
        private Stack<Topic> stack = new Stack<>();
        private Alphabet defaultGuide = new Alphabet("("); // Sequential by default
        private Alphabet guide = defaultGuide;
        private TopicMap tm = null;
        
        
        
        public LSystemParser(WandoraTool p, Word i, TopicMap topicMap) {
            this.parsePoint = 0;
            this.parent = p;
            this.input = i;
            this.tm = topicMap;
            if(parent != null) {
                parent.setProgressMax(input.size());
            }
        }
        
        public void parse() {
            if(input == null || tm == null) {
                if(parent != null) parent.log("No input and/or topic map specified for the L-System parser!");
            }
            while(parsePoint < input.size() && !forceStop()) {
                Alphabet a = input.get(parsePoint);
                
                // ***** CREATE TOPIC *****
                if(isCreateTopic(a)) {
                    try { parseTopic(); }
                    catch(Exception e) { log(e); }
                }
                
                // ***** COLORED TOPIC *****
                else if(isColoredTopic(a)) {
                    try {
                        String tempColor = currentColor;
                        currentColor = a.getName();
                        parseTopic();
                        currentColor = tempColor;
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
                
                // ***** NAMED TOPIC *****
                else if(isNamedTopic(a)) {
                    try { parseNamedTopic(a); }
                    catch(Exception e) { log(e); }
                }
                
                // ***** OPEN BLOCKS *****
                else if(isCreateParallelBlock(a) || isCreateSequentialBlock(a) || isCreateCycleBlock(a)) {
                    //hlog("Creating Block");
                    stackStack.push(new T2<>(guide, stack));
                    stack = new Stack<>();
                    guide = a;
                }
                
                // ***** CLOSE BLOCKS *****
                else if(isCloseParallelBlock(a) || isCloseSequentialBlock(a) || isCloseCycleBlock(a)) {
                    //hlog("Closing Block");
                    if(isCloseCycleBlock(a)) {
                        if(stack.size() > 2) {
                            try {
                                Topic first = (Topic) stack.get(0);
                                Topic last = (Topic) stack.get(stack.size()-1);
                                createAssociation(tm, last, first);
                            }
                            catch(Exception e) {
                                log(e);
                            }
                        }
                    }
                    if(!stackStack.empty()) {
                        T2<Alphabet,Stack<Topic>> guidedStack = stackStack.pop();
                        guide = guidedStack.e1;
                        stack = guidedStack.e2;
                    }
                    else {
                        stack = new Stack<>();
                        guide = defaultGuide;
                    }
                }
                
                // ***** SET ASSOCIATION COLOR *****
                else if(isSetAssociationColor(a)) {
                    Alphabet associationColor = input.get(parsePoint+1);
                    if(isAssociationColor(associationColor)) {
                        String color = associationColor.getName();
                        if("0".equals(color)) {
                            currentColor = null;
                        }
                        else {
                            currentColor = color;
                        }
                        parsePoint++;
                    }
                }
                
                // ***** RESET TOPIC COUNTER *****
                else if(isResetTopicCount(a)) {
                    topicCounter = 0;
                }
                
                // ***** SUBSTRACT TOPIC COUNTER *****
                else if(isSubstractTopicCounter(a)) {
                    try {
                        topicCounter = topicCounter - 1;
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
                
                // ***** ADD TOPIC COUNTER *****
                else if(isAddTopicCounter(a)) {
                    try {
                        topicCounter = topicCounter + 1;
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
                
                parsePoint++;
                setProgress(parsePoint);
            }
        }
        
        
        
        public void parseNamedTopic(Alphabet a) throws TopicMapException, Exception {
            //hlog("Parser found named topic");
            Topic t = createNamedTopic(tm, a.getName());
            linkTopic(t);
        }
        
        
        public void parseTopic() throws TopicMapException, Exception {
            //hlog("Parser found topic");
            Topic t = createTopic(tm);
            linkTopic(t);
        }
        
        
        
        
        public void linkTopic(Topic t) throws TopicMapException, Exception {
            Topic oldTopic = null;
            // **** HANDLE PARALLEL BLOCK ****
            if(isCreateParallelBlock(guide)) {
                if(!stackStack.empty()) {
                    int peekIndex = stackStack.size();
                    T2<Alphabet,Stack<Topic>> guidedStack = null;
                    do {
                        guidedStack = stackStack.get(--peekIndex);
                    } while(peekIndex > 0 && guidedStack.e2.empty());
                    if(!guidedStack.e2.empty()) {
                        oldTopic = (Topic) guidedStack.e2.peek();
                    }
                }
            }
            
            // **** HANDLE SEQUENTIAL AND CYCLE BLOCK ****
            else if(isCreateSequentialBlock(guide) || isCreateCycleBlock(guide)) {
                if(!stack.empty()) {
                    oldTopic = (Topic) stack.peek();
                }
                else if(!stackStack.empty()) {
                    int peekIndex = stackStack.size();
                    T2<Alphabet,Stack<Topic>> guidedStack = null;
                    do {
                        guidedStack = stackStack.get(--peekIndex);
                    } while(peekIndex > 0 && guidedStack.e2.empty());
                    if(!guidedStack.e2.empty()) {
                        oldTopic = (Topic) guidedStack.e2.peek();
                    }
                }
            }
            if(oldTopic != null) {
                //hlog("Creating association between "+oldTopic+" and "+t);
                createAssociation(tm, oldTopic, t);
            }
            stack.push(t);
        }
        
        
        
        
        // **** TEST PARSE FEED TOKENS *****
        
        
        public boolean isAddTopicCounter(Alphabet a) {
            if("+".indexOf(a.getName()) > -1) return true;
            else return false;
        }
        
        public boolean isSubstractTopicCounter(Alphabet a) {
            if("-".indexOf(a.getName()) > -1) return true;
            else return false;
        }
        
        
        public boolean isResetTopicCount(Alphabet a) {
            if("0".indexOf(a.getName()) > -1) return true;
            else return false;
        }
        
        public boolean isAssociationColor(Alphabet a) {
            if("qwertyuiopasdfghjklmnbvcxzQWERTYUIOPLKJHGFDSAZXCVBNM".indexOf(a.getName()) > -1) return true;
            else return false;
        }
        public boolean isSetAssociationColor(Alphabet a) {
            if(":".indexOf(a.getName()) > -1) return true;
            else return false;
        }
        public boolean isNamedTopic(Alphabet a) {
            if("ABCDEFGHIJKLMNOPQRSTUWV".indexOf(a.getName()) > -1) return true;
            else return false;
        }
        public boolean isColoredTopic(Alphabet a) {
            if("euioy".indexOf(a.getName()) > -1) return true;
            else return false;
        }
        public boolean isCreateTopic(Alphabet a) {
            if("a".equals(a.getName())) return true;
            else return false;
        }
        public boolean isCreateParallelBlock(Alphabet a) {
            if("[".equals(a.getName())) return true;
            else return false;
        }
        public boolean isCloseParallelBlock(Alphabet a) {
            if("]".equals(a.getName())) return true;
            else return false;
        }
        public boolean isCreateSequentialBlock(Alphabet a) {
            if("(".equals(a.getName())) return true;
            else return false;
        }
        public boolean isCloseSequentialBlock(Alphabet a) {
            if(")".equals(a.getName())) return true;
            else return false;
        }
        public boolean isCreateCycleBlock(Alphabet a) {
            if("{".equals(a.getName())) return true;
            else return false;
        }
        public boolean isCloseCycleBlock(Alphabet a) {
            if("}".equals(a.getName())) return true;
            else return false;
        }
        


    
    }
    
    
    
    
    
    
    
    
    // -------------------------------------------------------------------------
    // ------ LSystem ----------------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    
    private class LSystem {
        private ArrayList<Rule> rules = new ArrayList<Rule>();
        private Word state = new Word();
        
        public LSystem(Word initial, ArrayList<Rule> ruleArray, int n) {
            initialize(initial, ruleArray.toArray( new Rule[] {} ), n);
        }
        public LSystem(Word initial, Rule[] ruleArray, int n) {
            initialize(initial, ruleArray, n);
        }
        public LSystem() {
            
        }
        
        
        public void initialize(Word initiator, Rule[] ruleArray, int n) {
            clearRules();
            if(ruleArray != null) {
                for(int i=0; i<ruleArray.length; i++) {
                    addRule(ruleArray[i]);
                }
            }
            setInitialState(initiator);
            iterate(n);
        }
        
        
        public void setInitialState(Word s) {
            if(s == null) s = new Word();
            state = s;
        }

        public void addRule(Rule r) {
            rules.add(r);
        }
        
        public void clearRules() {
            rules = new ArrayList<Rule>();
        }
        
        public Word getState() {
            return state;
        }
        
        public void iterate() {
            iterate(1);
        }

        public void iterate(int n) {
            Rule r = null;
            for(int i=0; i<n && !forceStop(); i++) {
                for(Iterator<Rule> it = rules.iterator(); it.hasNext() && !forceStop(); ) {
                    r = it.next();
                    state = r.apply(state);
                }
                state.setFresh(false);
            }
        }
        
        @Override
        public String toString() {
            String s = "LSystem state: "+state.toString()+", rules: ";
            for(int i=0; i<rules.size(); i++) {
                s=s+rules.get(i).toString();
                if(i<rules.size()-1) s=s+", ";
            }
            return s;
        }
    }
    
    
    
    
    
    
    
    private class Word {
        List<Alphabet> alphabets = new ArrayList<Alphabet>();
        
        public Word() {
            
        }
        public Word(List<Alphabet> w) {
            this.alphabets = w;
        }
        public Word(String[] str) {
            this.alphabets = new ArrayList<Alphabet>();
            for(int i=0; i<str.length; i++) {
                alphabets.add(new Alphabet(str[i]));
            }
        }
        public Word(String str) {
            this(str, true);
        }
        public Word(String str, boolean split) {
            if(split) {
                for(int i=0; i<str.length(); i++) {
                    alphabets.add(new Alphabet(str.charAt(i)));
                }
            }
            else {
                alphabets.add(new Alphabet( str ));
            }
        }
        
        public boolean startsWith(Word otherWord, int index) {
            boolean found = false;
            //System.out.println("startsWith-1");
            if(alphabets != null && otherWord != null && alphabets.size() >= index+otherWord.size() ) {
                Alphabet as = null;
                Alphabet aw = null;
                int kmax = otherWord.size();
                found = true;
                //System.out.println("startsWith-2");
                for(int j=0; j<kmax; j++) {
                    as = alphabets.get(index+j);
                    if(as.isFresh()) {
                        found = false;
                        break;
                    }
                    aw = otherWord.get(j);
                    //System.out.println("startsWith-3");
                    if(!as.equals(aw)) {
                        //System.out.println("startsWith-4");
                        found = false;
                        break;
                    }
                }
            }
            return found;
        }
        
        
        
        public void add(Word word, boolean fresh) {
            List<Alphabet> alphas = word.getAlphabets();
            for(int i=0; i<alphas.size(); i++) {
                this.alphabets.add(alphas.get(i).duplicate(fresh));
            }
        }
        
        public void add(Alphabet alphabet, boolean fresh) {
            this.alphabets.add(alphabet.duplicate(fresh));
        }
        
        public void setFresh(boolean f) {
            for(int i=0; i<alphabets.size(); i++) {
                alphabets.get(i).setFresh(f);
            }
        }
        
        public List<Alphabet> getAlphabets() {
            return alphabets;
        }
        
        public int size() {
            if(alphabets == null) return 0;
            return alphabets.size();
        }
        
        public Alphabet get(int i) {
            return alphabets.get(i);
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for(int i=0; i<alphabets.size(); i++) {
                sb.append(alphabets.get(i).toString());
            }
            return sb.toString();
        }
    }
    
    
    
    
    private class Alphabet {
        private String name = null;
        private boolean fresh = false;
        
        
        public Alphabet(char ch) {
            this.name = "" + ch;
        }
        public Alphabet(String n) {
            this.name = n;
        }
        
        public String getName() {
            return name;
        }

        public void setFresh(boolean f) {
            this.fresh = f;
        }
        public boolean isFresh() {
            return fresh;
        }
        
        
        public boolean equals(Alphabet a) {
            if(a == null) return false;
            if(a.getName() == null && getName() == null) return true;
            if(getName() != null) return getName().equals(a.getName());
            return false;
        }
        @Override
        public String toString() {
            return name;
        }
        
        public Alphabet duplicate(boolean fresh) {
            Alphabet a = new Alphabet(this.getName());
            a.setFresh(fresh);
            return a;
        }
        
    }
    
    
    
    private class Rule {
        private Word predecessor = null;
        private Word successor = null;
        
        public Rule(Word p, Word s) {
            predecessor = p;
            successor = s;
        }
        
        
        public Word apply(Word state) {
            Word newState = new Word();
            for(int i=0; i<state.size() && !forceStop(); i++) {
                if(state.startsWith(predecessor, i)) {
                    newState.add(successor, true);
                    i = i + predecessor.size() - 1;
                }
                else {
                    newState.add(state.get(i), false);
                }
            }
            return newState;
        }

        @Override
        public String toString() {
            return (predecessor == null ? "null" : predecessor.toString()) +"-->"+(successor == null ? "null" : successor.toString());
        }
    }
    

}
