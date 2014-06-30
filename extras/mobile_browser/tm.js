/*
 * WANDORA - Mobile browser
 *
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2011 Grip Studios Interactive, Inc.
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
 
var SI_TYPE_INSTANCE="http://psi.topicmaps.org/iso13250/model/type-instance";
var SI_TYPE="http://psi.topicmaps.org/iso13250/model/type";
var SI_INSTANCE="http://psi.topicmaps.org/iso13250/model/instance";
var SI_SUPER_SUB="http://www.topicmaps.org/xtm/1.0/core.xtm#superclass-subclass";
var SI_SUPER="http://www.topicmaps.org/xtm/1.0/core.xtm#superclass";
var SI_SUB="http://www.topicmaps.org/xtm/1.0/core.xtm#subclass";

function unwrapPlayers(as,role){
    var ret=[];
    for(var i in as){
        var a=as[i];
        var p=a.getPlayer(role);
        if(p!=null) { ret.push(p); }
    }
    return ret;
}

function topicSiComparator(t1,t2){
    if(t1==t2) return 0;
    if(!t1.sis || t1.sis.length==0){
        if(!t2.sis || t2.sis.length==0) return 0;
        else return 1;
    }
    else if(!t2.sis || t2.sis.length==0) return -1;
    var si1=t1.sis[0].toLowerCase();
    var si2=t2.sis[0].toLowerCase();
    if(si1<si2) return -1;
    else if(si1==si2) return 0;
    else return 1;
}

function topicNameComparator(t1,t2){
    if(t1==t2) return 0;
    if(!t1.bns || t1.bns.length==0){
        if(!t2.bns || t2.bns.length==0) return 0;
        else return 1;
    }
    else if(!t2.bns || t2.bns.length==0) return -1;
    var n1=t1.bns[0].toLowerCase();
    var n2=t2.bns[0].toLowerCase();
    if(n1<n2) return -1;
    else if(n1==n2) return topicSiComparator(t1,t2);
    else return 1;
}

function playerRoleComparator(p1,p2){
    return topicNameComparator(p1.role,p2.role);
}


function associationComparator(a1,a2){
    var c=topicNameComparator(a1.type,a2.type);
    if(c!=0) return c;
    else {
        if(!a1.playersSorted){
            a1.players.sort(playerRoleComparator);
            a1.playersSorted=true;
        }
        if(!a2.playersSorted){
            a2.players.sort(playerRoleComparator);
            a2.playersSorted=true;
        }
        for(var i=0;;i++){
            if(i>=a1.players.length){
                if(i>=a2.players.length) return 0;
                else return -1;
            }
            else if(i>=a2.players.length) return 1;
            c=topicNameComparator(a1.players[i].role,a2.players[i].role);
            if(c!=0) return c;
            c=topicNameComparator(a1.players[i].member,a2.players[i].member);
            if(c!=0) return c;
        }
    }
}


function Topic(tm){ // TopicMap
    this.tm=tm;
    this.sis=[]; // of Strings
    this.sls=[]; // of Strings
    this.bns=[]; // of Strings
    this.os=[];  // of Occurrences
    this.as=[];  // of Associations (where this topic is a member)
    this.vns=[]  // of Variants
}

Topic.prototype.addSi=function(si){
    if(!(si in this.sis)){
        if(si in this.tm.siIndex) {
            this.tm.mergeTopics(this,this.tm.siIndex[si]);
        }
        else {
            this.sis.push(si);
            this.tm.siIndex[si]=this;
        }
    }
};

Topic.prototype.isInstanceOf=function(type){
    return this.getTypes().indexOf(type)>=0;
};

Topic.prototype.getAssociations=function(type,role){
    var ret=[];
    for(var i in this.as){
        var a=this.as[i];
        if(type && a.type!=type) { continue; }
        if(role && a.getPlayer(role)!=this) { continue; }
        ret.push(a);
    }
    return ret;
};

Topic.prototype.getAllPlayers=function(){
    var ret=[];
    for(var i in this.as){
        var a=this.as[i];
        for(var j in a.players){
            var p=a.players[j];
            if(p.member!=this) { ret.push(p.member); }
        }
    }
    return ret;
};

Topic.prototype.getSuperClasses=function(){
    if(this.superClasses) { return this.superClasses; }

    var supSub=this.tm.getTopic(SI_SUPER_SUB);
    var sup=this.tm.getTopic(SI_SUPER);
    var sub=this.tm.getTopic(SI_SUB);
    if(!supSub || !sup || !sub) {
        this.superClasses=[];
    }
    else {
        this.superClasses=this.getPlayers(supSub,sub,sup);
    }
        
    for(var i=0;i<this.superClasses.length;i++){
        var t=this.superClasses[i];
        var t2=t.getTypes();
        for(var j=0;j<t2.length;j++){
            if(this.superClasses.indexOf(t2[j])<0) { this.superClasses.push(t2[j]); }
        }
    }
    return this.superClasses;
};

Topic.prototype.isOfType=function(type){
    return this.getTypes().indexOf(type)>=0;
};

Topic.prototype.isOfSuperType=function(type){
    return this.getSuperTypes().indexOf(type)>=0;
};

Topic.prototype.getPlayers=function(type,thisrole,otherrole){
    return unwrapPlayers(this.getAssociations(type,thisrole),otherrole);
};

Topic.prototype.getSuperTypes=function(){
    if(this.superTypes) { return this.superTypes; }
    
    this.superTypes=this.getTypes().slice(); // slice makes a copy of the array
    var l=this.superTypes.length;
    for(var i=0;i<l;i++){
        var t2=this.superTypes[i].getSuperClasses();
        for(var j=0;j<t2.length;j++){
            if(this.superTypes.indexOf(t2[j])<0) { this.superTypes.push(t2[j]); }
        }
    }
    return this.superTypes;
};

Topic.prototype.getTypes=function(){
    if(this.types) { return this.types; }
    var typeInstance=this.tm.getTopic(SI_TYPE_INSTANCE);
    var type=this.tm.getTopic(SI_TYPE);
    var instance=this.tm.getTopic(SI_INSTANCE);
    if(!typeInstance || !type || !instance) {
        this.types=[];
    }
    else {
        this.types=this.getPlayers(typeInstance,instance,type);
    }
    return this.types;
};

Topic.prototype.getInstances=function(){
    if(this.instances) { return this.instances; }
    var typeInstance=this.tm.getTopic(SI_TYPE_INSTANCE);
    var type=this.tm.getTopic(SI_TYPE);
    var instance=this.tm.getTopic(SI_INSTANCE);
    if(!typeInstance || !type || !instance) {
        this.instances=[];
    }
    else {
        this.instances=this.getPlayers(typeInstance,type,instance);
    }
    return this.instances;
};

Topic.prototype.getOccurrence=function(type,lang){
    // currently ignores lang
    for(var i in this.os){
        var o=this.os[i];
        if(o.type==type){
            return o.value;
        }
    }
    return null;
};

Topic.prototype.getOccurrenceTypes=function(){
    var ret=[];
    for(var i in this.os){
        var o=this.os[i];
        if(ret.indexOf(o.type)<0) ret.push(o.type);
    }
    return ret;
}

function scopesEqual(s1,s2){
    if(s1==null || s2==null) return false;
    if(s1.length!=s2.length) return false;
    for(var t in s1){
        if(s2.indexOf(t)<0) return false;
    }
    return true;
}

Topic.prototype.getVariant=function(scope){ // Array of Topics
    for(var vn in this.vns){
        if(scopesEqual(vn.scope,scope)) { return vn; }
    }
    return null;
}

function Occurrence(topic,type,scope,value){ // Topic,String,Array of Topics,String
    this.topic=topic;

    if(type!=null) { this.type=type; }
    else { this.type=null; }
    
    if(scope!=null) { this.scope=scope; }
    else { this.scope=[]; }
    
    if(value!=null) { this.value=value; }
    else { this.value=""; }
}

function Association(type,players){ // Topic,Array of Players
    if(type!=null) { this.type=type; }
    else { this.type=null; }
    
    this.playersSorted=false;
    if(players!=null) { this.players=players; }
    else { this.players=[]; }
}

Association.prototype.getPlayer=function(role){
    for(var i in this.players){
        var p=this.players[i];
        if(p.role==role) { return p.member; }
    }
    return null;
};

function Player(role,member){ // Topic,Topic
    this.role=role;
    this.member=member;
}

function Variant(scope,value){ // Array of Topics,String
    this.scope=scope;
    this.value=value;
}

function TopicMap(){
    this.siIndex={};
}

TopicMap.prototype.getTopic=function(si){
    if(si in this.siIndex) { return this.siIndex[si]; }
    else { return null; }
};

TopicMap.prototype.getOrCreateTopic=function(si){
    if(si in this.siIndex){
        return this.siIndex[si];
    }
    else {
        var t=new Topic(this);
        t.addSi(si); // adds the topic to siIndex
        return t;
    }
};

TopicMap.prototype.mergeTopics=function(t1,t2){
    var i,j;
    for(i=0;i<t2.sis.length;i++){
        t1.sis.push(t2.sis[i]);
        this.siIndex[t2.sis[i]]=t1;
    }
    for(i=0;i<t2.sls.length;i++){
        if(t1.sls.indexOf(t2.sls[i])<0) { t1.sls.push(t2.sls[i]); }
    }
    for(i=0;i<t2.bns.length;i++){
        if(t1.bns.indexOf(t2.bns[i])<0) { t1.bns.push(t2.bns[i]); }
    }
    for(i=0;i<t2.os.length;i++){
        t1.os.push(t2.os[i]);
    }
    for(i=0;i<t2.as.length;i++){
        t1.as.push(t2.as[i]);
    }
    for(i=0;i<t2.vns.length;i++){
        t1.vns.push(t2.vns[i]);
    }
    
    for(var si in this.siIndex){
        var t=this.siIndex[si];
        for(i=0;i<t.as.length;i++){
            a=t.as[i];
            if(a.type==t2) { a.type=t1; }
            for(j=0;j<a.players.length;j++){
                p=a.players[j];
                if(p.role==t2) { p.role=t1; }
                if(p.player==t2) { p.player=t1; }
            }
        }
        for(i=0;i<t.os.length;i++){
            o=t.os[i];
            if(o.type==t2) { o.type=t1; }
            var ind=o.scope.indexOf(t2);
            if(ind>=0) {
                o.scope.splice(ind,1);
                o.scope.push(t1);
            }
        }
    }
};

TopicMap.prototype.getReference=function(ref){
    if(ref.substring(0,3)=="si:") {
        return this.getOrCreateTopic(ref.substring(3));
    }
    else { alert("unknown topic reference type "+si); }
};

TopicMap.prototype.loadJTM=function(src){
    $.ajax({url:src,dataType:'json',async: false, context: this,
    success:function(data){
        var i,j,k;
        var type;
        for(k in data.topics){
            var tdata=data.topics[k];
            if(!tdata.subject_identifiers || tdata.subject_identifiers.length==0) {
                alert("topic doesn't have a subject identifier");
                continue;
            }
            
            var t=this.getOrCreateTopic(tdata.subject_identifiers[0]);
            for(i=1;i<tdata.subject_identifiers;i++){
                t.addSi(tdata.subject_identifiers[i]);
            }
            
            if(tdata.subject_locators) {
                for(i=0;i<tdata.subject_locators.length;i++) { t.sls.push(tdata.subject_locators[i]); }
            }
            
            if(tdata.names){
                for(i=0;i<tdata.names.length;i++) {
                    t.bns.push(tdata.names[i].value);
                    if(tdata.names[i].variants){
                        var variants=tdata.names[i].variants
                        for(j=0;j<variants.length;j++){
                            var value=variants[j].value;
                            var scope=[];
                            if(variants[j].scope){
                                for(k=0;k<variants[j].scope.length;k++){
                                    var st=this.getReference(variants[j].scope[k]);
                                    if(st) { scope.push(st); }
                                }
                            }
                            t.vns.push(new Variant(scope,value));
                        }
                    }
                }
            }
            
            if(tdata.occurrences){
                for(i=0;i<tdata.occurrences.length;i++){
                    var odata=tdata.occurrences[i];
                    var scope=[];
                    if(!odata.type){
                        alert("occurrence doesn't have a type");
                        continue;
                    }
                    if(odata.scope){
                        for(j=0;j<odata.scope.length;j++){
                            var st=this.getReference(odata.scope[j]);
                            if(st) { scope.push(st); }
                        }
                    }
                    type=this.getReference(odata.type);
                    var value="";
                    if(odata.value) { value=odata.value; }
                    
                    t.os.push(new Occurrence(t,type,scope,value));
                }
            }
        }
        
        for(k in data.associations){
            var adata=data.associations[k];
            
            if(!adata.type){
                alert("association doesn't have a type");
                continue;
            }
            if(!adata.roles || adata.roles.length==0){
                alert("association doesn't have roles");
                continue;
            }
            
            type=this.getReference(adata.type);
            if(!type) { continue; }
            
            var member;
            var players=[];
            for(i=0;i<adata.roles.length;i++){
                var role=this.getReference(adata.roles[i].type);
                member=this.getReference(adata.roles[i].player);
                if(!role || !member) { continue; }
                players.push(new Player(role,member));
            }
            if(players.length==0) { continue; }
            var a=new Association(type,players);
            
            for(i=0;i<players.length;i++){
                member=players[i].member;
                if(member.as.indexOf(a)<0) { member.as.push(a); }
            }
            
        }
    },
    error:function(request,status,error){
        alert("error loading JTM \""+src+"\": "+status+", "+error);
    }});                
};


