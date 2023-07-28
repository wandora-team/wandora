package org.wandora.topicmap.undowrapper.tests;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import org.wandora.topicmap.*;
import org.wandora.topicmap.diff.BasicDiffOutput;
import org.wandora.topicmap.diff.PatchDiffEntryFormatter;
import org.wandora.topicmap.diff.TopicMapDiff;
import org.wandora.topicmap.memory.TopicMapImpl;
import org.wandora.topicmap.undowrapper.UndoException;
import org.wandora.topicmap.undowrapper.UndoTopicMap;

/**
 *
 * @author olli
 */


public class RandomTest implements Test {

    private long seed;
    
    private boolean passed=false;
    private String messages=null;
    
    private boolean checkRedo=true;
    
    private static long seedCounter=0;
    
    public RandomTest(){
        this(System.currentTimeMillis()+(seedCounter++));
    }
    
    public RandomTest(long seed) {
        this.seed=seed;
    }
    
    public TopicMap createRandomTopicMap(Random random,int numTopics) throws TopicMapException {
        TopicMap tm=new TopicMapImpl();
        Topic[] topics=new Topic[numTopics];
        
        for(int i=0;i<numTopics;i++){
            Topic t=tm.createTopic();
            t.addSubjectIdentifier(new Locator("http://wandora.org/si/test/"+i));
            t.setBaseName(""+i);
            topics[i]=t;
        }
        
        for(int i=0;i<numTopics;i++){
            int numAssociations=random.nextInt(numTopics);
            for(int j=0;j<numAssociations;j++){                
                HashMap<Topic,Topic> players=new HashMap<Topic,Topic>();
                Topic role=topics[random.nextInt(topics.length)];
                Topic player=topics[i];
                players.put(role,player);
                
                int numRoles=random.nextInt(Math.min(5,numTopics/2));
                for(int k=0;k<numRoles;k++){
                    role=null;
                    while(role==null || players.containsKey(role))
                        role=topics[random.nextInt(topics.length)];
                    player=topics[random.nextInt(topics.length)];
                    players.put(role,player);
                }
                
                Topic type=topics[random.nextInt(topics.length)];
                Association a=tm.createAssociation(type);
                a.addPlayers(players);
            }
            
            int numSubjectIdentifiers=random.nextInt(5);
            for(int j=0;j<numSubjectIdentifiers;j++){
                topics[i].addSubjectIdentifier(new Locator("http://wandora.org/si/test/"+i+"/si"+random.nextInt(1000000)));
            }
            
            if(random.nextBoolean()){
                topics[i].setSubjectLocator(new Locator("http://wandora.org/si/test/"+i+"/sl"+random.nextInt(1000000)));
            }
            
            int numOccurrences=random.nextInt(5);
            for(int j=0;j<numOccurrences;j++){
                Topic type=topics[random.nextInt(topics.length)];
                Topic version=topics[random.nextInt(topics.length)];
                topics[i].setData(type, version, "occurrence "+i+"/"+j);
            }
            
            int numVariants=random.nextInt(5);
            for(int j=0;j<numVariants;j++){
                int numScopeTopics=1+random.nextInt(Math.min(3,numTopics-1));
                HashSet<Topic> scope=new HashSet<Topic>();
                for(int k=0;k<numScopeTopics;k++){
                    Topic s=null;
                    while(s==null || scope.contains(s))
                        s=topics[random.nextInt(topics.length)];
                    scope.add(s);
                }
                topics[i].setVariant(scope, "variant "+i+"/"+j);
            }
            
            int numTypes=random.nextInt(5);
            for(int j=0;j<numTypes;j++){
                Topic t=topics[random.nextInt(topics.length)];
                topics[i].addType(t);
            }
        }
        
        return tm;
    }
    
    /*
     * Many topic map methods return things in unpredictable random orders.
     * To be able to get same behavior always with the same random seed,
     * randomly picking things from the collections returned by the topic map
     * must be first sorted. Thus the different gerRandom* methods.
     */
    
    public Association getRandomAssociation(TopicMap tm,Random random) throws TopicMapException {
        ArrayList<Association> as=new ArrayList<Association>();
        Iterator<Association> iter=tm.getAssociations();
        while(iter.hasNext()) as.add(iter.next());
        
        TMBox.AssociationTypeComparator comp=new TMBox.AssociationTypeComparator(new TMBox.TopicBNAndSIComparator());
        comp.setFullCompare(true);
        Collections.sort(as, comp);
        return getRandom(as,random);
    }
    
    public Topic getRandomTopic(TopicMap tm,Random random) throws TopicMapException {
        ArrayList<Topic> ts=new ArrayList<Topic>();
        Iterator<Topic> iter=tm.getTopics();
        while(iter.hasNext()) ts.add(iter.next());
        
        Comparator<Topic> comp=new TMBox.TopicBNAndSIComparator();
        Collections.sort(ts, comp);
        return getRandom(ts,random);
        
    }
    
    public Topic getRandomTopic(Collection<Topic> c,Random random){
        ArrayList<Topic> ts=new ArrayList<Topic>(c);
        Comparator<Topic> comp=new TMBox.TopicBNAndSIComparator();
        Collections.sort(ts, comp);
        return getRandom(ts,random);
    }
    
    public Locator getRandomLocator(Collection<Locator> c,Random random){
        ArrayList<Locator> ts=new ArrayList<Locator>(c);
        Collections.sort(ts);
        return getRandom(ts,random);        
    }
    
    public Set<Topic> getRandomScope(Collection<Set<Topic>> c, Random random){
        ArrayList<Set<Topic>> ts=new ArrayList<Set<Topic>>(c);        
        Comparator<Set<Topic>> comp=new TMBox.ScopeComparator(new TMBox.TopicBNAndSIComparator());
        Collections.sort(ts, comp);
        return getRandom(ts,random);        
    }
    
    /*
     * These two generic getRandom methods will only work predictably if the
     * collection or the iterator always have elements in the same order,
     * unlike what most topic map methods return.
     */
    
    public <T> T getRandom(Collection<T> c,Random random){
        return getRandom(c.iterator(),c.size(),random);
    }
    
    public <T> T getRandom(Iterator<T> iter,int count,Random random){
        if(count==0) return null;
        int ind=random.nextInt(count);
        while(true){
            T t=iter.next();
            if(ind==0) return t;
            else ind--;
        }        
    }
    
    
    public void performRandomOperation(TopicMap tm,Random random) throws TopicMapException {
        int r=random.nextInt(14);
        if(r==0){ // add subject identifier
            Topic t=getRandomTopic(tm,random);
            if(t==null) return;
            
            if(random.nextInt(10)<3){ // do intentional merge
                Topic t2=getRandomTopic(tm,random);
                Locator l=getRandomLocator(t2.getSubjectIdentifiers(),random);
                t.addSubjectIdentifier(l);
            }
            else {
                t.addSubjectIdentifier(new Locator("http://wandora.org/si/test/random/"+random.nextInt(1000000)));
            }
        }
        else if(r==1){ // add type
            Topic t=getRandomTopic(tm,random);
            Topic t2=getRandomTopic(tm,random);
            if(t==null || t2==null) return;
            t.addType(t2);
        }
        else if(r==2){ // create association 
            if(tm.getNumTopics()<2) return;
            
            HashMap<Topic,Topic> players=new HashMap<Topic,Topic>();
            int numRoles=1+random.nextInt(Math.min(5,tm.getNumTopics()/2));
            for(int k=0;k<numRoles;k++){
                Topic role=null;
                while(role==null || players.containsKey(role))
                    role=getRandomTopic(tm, random);
                Topic player=getRandomTopic(tm, random);
                players.put(role,player);
            }

            Topic type=getRandomTopic(tm, random);
            Association a=tm.createAssociation(type);
            a.addPlayers(players);
        }
        else if(r==3){ // create topic
            Topic t=tm.createTopic();
            t.addSubjectIdentifier(new Locator("http://wandora.org/si/test/random/"+random.nextInt(1000000)));
        }
        else if(r==4){ // merge
            if(tm.getNumTopics()<2) return;
            Topic t=getRandomTopic(tm,random);
            Topic t2=getRandomTopic(tm,random);
            while(t2.mergesWithTopic(t)) t2=getRandomTopic(tm, random);
            t.addSubjectIdentifier(t2.getOneSubjectIdentifier());
        }
        else if(r==5){ // modify association
            Association a=getRandomAssociation(tm, random);
            if(a==null) return;

            int r2=random.nextInt(3);
            if(r2==0){ // add player
                Topic role=getRandomTopic(tm, random);
                Topic player=getRandomTopic(tm, random);
                a.addPlayer(player, role);
            }
            else if(r2==1){ // remove player
                if(a.getRoles().size()<=1) return;
                Topic role=getRandomTopic(a.getRoles(),random);
                a.removePlayer(role);
            }
            else if(r2==2){ // set type
                Topic type=getRandomTopic(tm, random);
                a.setType(type);
            }
        }
        else if(r==6){ // remove association
            Association a=getRandomAssociation(tm, random);
            if(a==null) return;
            a.remove();
        }
        else if(r==7){ // remove subject identifier
            Topic t=getRandomTopic(tm,random);
            if(t.getSubjectIdentifiers().size()>1) {
                t.removeSubjectIdentifier(getRandomLocator(t.getSubjectIdentifiers(),random));
            }
        }
        else if(r==8){ // remove topic
            if(tm.getNumTopics()<2) return;
            Topic t=getRandomTopic(tm,random);
            if(!t.isDeleteAllowed()) return;
            t.remove();
        }
        else if(r==9){ // remove type
            Topic t=getRandomTopic(tm,random);
            Topic type=getRandomTopic(t.getTypes(),random);
            if(type!=null) t.removeType(type);
        }
        else if(r==10){ // set base name
            Topic t=getRandomTopic(tm,random);
            if(random.nextInt(10)<3){ // do intentional merge
                Topic t2=getRandomTopic(tm,random);
                String bn=t2.getBaseName();
                if(bn==null) return;
                t.setBaseName(bn);
            }
            else {
                t.setBaseName("random bn "+random.nextInt(1000000));
            }            
        }
        else if(r==11){ // set occurrence
            Topic t=getRandomTopic(tm,random);
            if(t.getDataTypes().isEmpty() || random.nextBoolean()) {
                Topic type=getRandomTopic(tm,random);
                Topic version=getRandomTopic(tm,random);
                t.setData(type, version, "random occurrence "+random.nextInt(1000000));
            }
            else {
                Topic type=getRandomTopic(t.getDataTypes(),random);
                if(type==null) return;
                Hashtable<Topic,String> data=t.getData(type);
                Topic version=getRandomTopic(data.keySet(),random);
                t.removeData(type, version);
            }
        }
        else if(r==12){ // set subject locator
            Topic t=getRandomTopic(tm,random);
            if(t.getSubjectLocator()==null || random.nextBoolean()){
                if(random.nextInt(10)<3) {
                    Topic t2=getRandomTopic(tm,random);
                    if(t2.getSubjectLocator()!=null) {
                        t.setSubjectLocator(t2.getSubjectLocator());
                        return;
                    }
                }

                t.setSubjectLocator(new Locator("http://wandora.org/si/test/random"+random.nextInt(1000000)));
            }
            else {
                t.setSubjectLocator(null);
            }
        }
        else if(r==13){ // set variant
            Topic t=getRandomTopic(tm,random);
            if(t.getVariantScopes().isEmpty() || random.nextBoolean()){
                int numScopeTopics=1+random.nextInt(Math.min(3,tm.getNumTopics()));
                HashSet<Topic> scope=new HashSet<Topic>();
                for(int k=0;k<numScopeTopics;k++){
                    Topic s=null;
                    while(s==null || scope.contains(s))
                        s=getRandomTopic(tm, random);
                    scope.add(s);
                }
                t.setVariant(scope, "random variant "+random.nextInt(1000000));
            }
            else {
                Set<Topic> scope=getRandomScope(t.getVariantScopes(),random);
                t.removeVariant(scope);
            }
        }
    }
    
    @Override
    public String getLabel() {
        return "Random test "+this.seed;
    }
    
    private String makeDiff(TopicMap tm1,TopicMap tm2) throws TopicMapException {
        StringWriter sw=new StringWriter();
        TopicMapDiff diff=new TopicMapDiff();
        diff.makeDiff(tm1, tm2, new BasicDiffOutput(new PatchDiffEntryFormatter(),sw));
        sw.flush();
        String ret=sw.toString();
        return ret;
    }
    
    private String stringifyException(Throwable t){
        StringWriter sw=new StringWriter();
        PrintWriter pw=new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    @Override
    public void run() throws TopicMapException {
        Random random=new Random(this.seed);
        UndoTopicMap tm=new UndoTopicMap(createRandomTopicMap(random,10+random.nextInt(10)), false);
//        String tmstring=makeDiff(new TopicMapImpl(),tm); // just for debugging
        TopicMap copy=new TopicMapImpl();
        copy.mergeIn(tm);
        
        String diff=makeDiff(tm,copy);
        if(diff.length()>0) {
            passed=false;
            messages="Initial clone failed diff check\n";
            messages+=diff;
            return;
        }
        
        int numPhases=1+random.nextInt(5);
        
        TopicMap[] copies=new TopicMap[numPhases+1];
        copies[0]=copy;
        
        for(int i=0;i<numPhases;i++){
            int numOperations=1+random.nextInt(20);
            for(int j=0;j<numOperations;j++) {
                performRandomOperation(tm, random);
            }
            tm.getUndoBuffer().addMarker("operation "+i);
            copies[i+1]=new TopicMapImpl();
            copies[i+1].mergeIn(tm);

            diff=makeDiff(tm,copies[i+1]);
            if(diff.length()>0) {
                passed=false;
                messages="Copy "+i+" failed diff check\n";
                messages+=diff;
                return;
            }
        }
        
        for(int i=numPhases-1;i>=0;i--){
            try{
                if(tm.getUndoBuffer().canUndo())
                    tm.getUndoBuffer().undo();
            } catch(UndoException ue){
                passed=false;
                messages="Undo exception at undo "+i+"\n";
                messages+=stringifyException(ue);
                return;
            }
            
            diff=makeDiff(tm,copies[i]);
            if(diff.length()>0){
                passed=false;
                messages="Undo diff "+i+" failed diff check\n";
                messages+=diff;
                return;
            }
        }
        
        if(checkRedo){
            for(int i=0;i<numPhases;i++){
                try{
                    if(tm.getUndoBuffer().canRedo())
                        tm.getUndoBuffer().redo();
                } catch(UndoException ue){
                    passed=false;
                    messages="Undo exception at redo "+i+"\n";
                    messages+=stringifyException(ue);
                    return;
                }

                diff=makeDiff(tm,copies[i+1]);
                if(diff.length()>0){
                    passed=false;
                    messages="Redo diff "+i+" failed diff check\n";
                    messages+=diff;
                    return;
                }            
            }
        }
        
        passed=true;
    }

    @Override
    public boolean isPassed() {
        return passed;
    }

    @Override
    public void getMessages(Writer out) throws IOException {
        if(messages!=null && messages.length()>0) out.write(messages+"\n");
    }
    
}
