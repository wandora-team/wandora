#
# WANDORA
# Knowledge Extraction, Management, and Publishing Application
# http://wandora.org/
# 
# Copyright (C) 2004-2015 Wandora Team
# 
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#
# 
#
#
#
# This file is a startup R script for Wandora integration of R language.
# It defines functions that help topic map model usage in R language.
#


library(rJava)
.jinit(silent=TRUE)

q <- function(){
    print("Function q is disabled in R Wandora integration because of it's instability.")
}
quit <- function(){
    print("Function quit is disabled in R Wandora integration because of it's instability.")
}
demo <- function(){
    print("Function demo is disabled in R Wandora integration because of it's instability.")
}
contributors <- function(){
    print("Function contributors is disabled in R Wandora integration because of it's instability.")
}
citation <- function(){
    print("Function citation is disabled in R Wandora integration because of it's instability.")
}

getContextTopics <- function(){
    as.list(J("org.wandora.application.tools.r.RHelper")$getContextTopics())
}

getCurrentTopic <- function() {
    t <- J("org.wandora.application.Wandora")$getWandora()$getOpenTopic()
    t <- .jcast(t,"org/wandora/topicmap/Topic")
    t
}

getContextAssociations <- function(){
    as.list(J("org.wandora.application.tools.r.RHelper")$getContextAssociations())
}

getTopicMap <- function(){
    J("org.wandora.application.Wandora")$getWandora()$getTopicMap()
}

unwrapIterator <- function(iter){
    as.list(J("org.wandora.application.tools.r.RHelper")$unwrapIterator(iter))
}

extractPolygon <- function(data,pointDelim=" ",componentDelim=",",reverse=FALSE) {
    if(!is.null(data)) {
        coords<-strsplit(data,paste("[",pointDelim,componentDelim,"]+",sep=""))[[1]]
        yind<-(1:(length(coords)/2))*2
        xind<-yind-1
        x<-sapply(coords[xind],as.numeric,USE.NAMES=FALSE)
        y<-sapply(coords[yind],as.numeric,USE.NAMES=FALSE)
        if(reverse) list(x=y,y=x)
        else list(x=x,y=y)
    }
    else {
        c()
    }
}


reversePoints <- function(pts) {
    for(i in 1:length(pts)){ 
        tmp = pts[[i]]$x
        pts[[i]]$x = pts[[i]]$y
        pts[[i]]$y = tmp
    }
    pts
}


plotPoints <- function(pts,labels=NULL,clr=TRUE) {
    if(!is.null(labels)) {
        for(i in 1:length(labels)) {
            label<-labels[i]
            if(is.na(label)) {
                pts[[i]]$label=""
            }
            else {
                pts[[i]]$label=label
            }
        }
    }

    if(clr) {
        xs<-sapply(pts,function(p){p$x})
        ys<-sapply(pts,function(p){p$y})
        minx<-min(sapply(xs,min))
        maxx<-max(sapply(xs,max))
        miny<-min(sapply(ys,min))
        maxy<-max(sapply(ys,max))
        plot.new()
        plot.window(ylim=c(miny,maxy),xlim=c(minx,maxx))
    }

    for(p in pts) {
        if(!is.null(p) && !is.null(p$x) && !is.null(p$y)) {
            points(p$x,p$y)
            if(!is.null(p$label)) {
                text(p$x,p$y,p$label,cex=0.4)
            }
        }
    }
}



plotPolygons <- function(polygons,values=NULL,labels=NULL,clr=TRUE) {
    colorf<-NULL
    if(!is.null(values)) {
        maxv<-max(values[!is.na(values)])
        minv<-min(values[!is.na(values)])
        dv<-maxv-minv
        for(i in 1:length(polygons)) {
            value<-values[i]
            if(is.na(value)) {
                polygons[[i]]$color=NA
            }
            else {
                value<- (value-minv)/dv
                value<- 1-(value*0.6+0.4)
                polygons[[i]]$color=rgb(value,value,value)
            }
        }
    }
    if(!is.null(labels)) {
        for(i in 1:length(labels)) {
            label<-labels[i]
            if(is.na(label)) {
                polygons[[i]]$label=""
            }
            else {
                polygons[[i]]$label=label
            }
        }
    }

    if(clr) {
        xs<-sapply(polygons,function(p){p$x})
        ys<-sapply(polygons,function(p){p$y})
        minx<-min(sapply(xs,min))
        maxx<-max(sapply(xs,max))
        miny<-min(sapply(ys,min))
        maxy<-max(sapply(ys,max))
        plot.new()
        plot.window(ylim=c(miny,maxy),xlim=c(minx,maxx))
    }

    for(p in polygons) {
        if(!is.null(p)) {
            if(!is.null(p$x) && !is.null(p$y)) {
                polygon(p$x,p$y,col=p$color)
                if(!is.null(p$label)) {
                    ctr=getPolygonCenter(p)
                    text(ctr$x,ctr$y,p$label,cex=0.4)
                }
            }
        }
    }
}


getPolygonCenter <- function(polygon) {
    minx<-min(polygon$x)
    maxx<-max(polygon$x)
    miny<-min(polygon$y)
    maxy<-max(polygon$y)

    center <- c()
    center$x = minx+(maxx-minx)/2
    center$y = miny+(maxy-miny)/2
    center
}



getPlayers <- function(associations,role){
    if(class(role)=="list") role=role[[1]]
    if(class(role)=="character"){
        role<-.jcall(getTopicMap(),"Lorg/wandora/topicmap/Topic;","getTopicWithBaseName",role)
    }
    if(!is.null(role)) {
        role<-.jcast(role,"org/wandora/topicmap/Topic")
        sapply(associations,function(a){
            .jcall(a,"Lorg/wandora/topicmap/Topic;","getPlayer",role)
        },USE.NAMES=FALSE)
    }
    else c()
}





getOccurrences <- function(topics,type,lang="en") {
    if(class(type)=="list") type=type[[1]]
    if(class(type)=="character"){
        type<-.jcall(getTopicMap(),"Lorg/wandora/topicmap/Topic;","getTopicWithBaseName",type)
    }
    if(!is.null(type)) {
        type<-.jcast(type,"org/wandora/topicmap/Topic")
        sapply(topics,function(a){
            .jcall(a,"Ljava/lang/String;","getData",type,lang)
        },USE.NAMES=FALSE)
    }
    else c()
}


relaxedToNumeric <- function(s){
    if(is.null(s)) NA
    else {
        match<-regexpr("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?",s)
        if(match[1]==-1) NA
        else as.numeric(substr(s,match[1],match[1]+attr(match,"match.length")-1))
    }
}

playersToString <- function(associations,role){
    sapply(getPlayers(associations,role),as.character,USE.NAMES=FALSE)
}

playersToNumeric <- function(associations,role){
    sapply(getPlayers(associations,role),as.numeric,USE.NAMES=FALSE)
}

as.character.jobjRef<-function(t,...){
    if(t %instanceof% J("org/wandora/topicmap/Topic")){
        t$getBaseName()
    }
    else NextMethod()
}

as.numeric.jobjRef<-function(t,...){
    if(t %instanceof% J("org/wandora/topicmap/Topic")){
        relaxedToNumeric(t$getBaseName())
    }
    else NextMethod()
}
as.double.jobjRef<-as.numeric.jobjRef

getAllTopics <- function(){
    unwrapIterator(getTopicMap()$getTopics())
}

getDisplayName <- function(t,lang="en") {
    .jcall(t,"Ljava/lang/String;","getDisplayName",lang)
}

bn <- function(t){
    t$getBaseName()
}

# The graph functions need the igraph library. Install inside r with
# 'install.packages("igraph")' and load with 'library("igraph")' before calling 
# either of the makeGraph functions.

makeGraphJava <- function(ts){
    ts<-.jarray(ts,"org/wandora/topicmap/Topic")
    go<-J("org.wandora.application.tools.r.RHelper")$makeGraph(ts)
    g<-graph(edges=go$edges,n=go$numNodes,directed=FALSE)
    V(g)$label<-go$labels
    V(g)$color<-go$colors
    g
}

makeGraph <- function(ts, ind = NULL){
    
    labels<-c()
    colors<-c()
    edges<-c()
    
    print("Adding topics")

    if(!is.null(ind)) {
        indices <- ind;
        for(t in ts){
            labels<-c(labels,.jcall(t,"Ljava/lang/String;","getBaseName"))
            colors<-c(colors,"lightblue")
        }  
    } else {
       indices<-list()
        for(t in ts){
            indices[[ .jcall(.jcall(t,"Lorg/wandora/topicmap/Locator;","getOneSubjectIdentifier"),"Ljava/lang/String;","toString") ]]<-length(labels)+1
            labels<-c(labels,.jcall(t,"Ljava/lang/String;","getBaseName"))
            colors<-c(colors,"lightblue")
        } 
    }
    
    print("Adding associations")
    
    for(t in ts){
        for(a in as.list( .jcall(t,"Ljava/util/Collection;","getAssociations") )){
            rs<-as.list( .jcall(a,"Ljava/util/Collection;","getRoles") )
            player1<-.jcall(a,"Lorg/wandora/topicmap/Topic;","getPlayer",.jcast(rs[[1]],"org/wandora/topicmap/Topic") )
            if(t==player1) {
                ps<-lapply(rs,function(r){ .jcall(a,"Lorg/wandora/topicmap/Topic;","getPlayer",.jcast(r,"org/wandora/topicmap/Topic")) })
                
                includeTopics<-unlist(
                    lapply(ps,function(p){
                        !is.null(indices[[ .jcall(.jcall(p,"Lorg/wandora/topicmap/Locator;","getOneSubjectIdentifier"),"Ljava/lang/String;","toString") ]])
                    })
                )
                
                ps<-ps[includeTopics]
                
                if(length(ps)<=1) {
                    next
                }
                else if(length(ps)==2){
                    edges<-c(edges, unlist(lapply(ps,function(t){
                        indices[[ .jcall(.jcall(t,"Lorg/wandora/topicmap/Locator;","getOneSubjectIdentifier"),"Ljava/lang/String;","toString") ]]
                    })) )
                }
                else {
                    aind<-length(labels)
                    
                    labels<-c(labels,"")
                    colors<-c(colors,"gray")
                    
                    lapply(ps,function(t){
                        edges<-c(edges, c(indices[[ .jcall(.jcall(t,"Lorg/wandora/topicmap/Locator;","getOneSubjectIdentifier"),"Ljava/lang/String;","toString") ]],aind) )
                    })
                }
            }
        }
    }
    
    print(length(labels))
    print(length(edges))
    
    g<-graph(edges=edges,n=length(labels),directed=FALSE)
    V(g)$label<-labels
    V(g)$color<-colors
    g    
}

createTopics <- function(g, baseNames = NULL, occurrences = NULL){
    edgelist <- get.edgelist(g)
    jEdgeMatrix <- .jarray(edgelist, dispatch=T)
    vertexVector <- .jarray(as.vector(V(g)))
    if(!is.null(baseNames)){
        bns <- .jarray(baseNames)
    } else {
        bns <- NULL
    }
    if(!is.null(occurrences)){
        attach( javaImport( "java.util" ), pos = 2 , name = "java:java.util" )
        occ <- new (HashMap)
        for (key in names(occurrences)){
            print(occurrences[[key]])
            print(typeof(occurrences[[key]]))
            occ$put(key,.jarray(occurrences[[key]]))
        }
    } else {
        occ <- NULL
    }
    helper <- J("org.wandora.application.tools.r.RHelper")
    helper$createTopics(jEdgeMatrix,vertexVector,bns,occ)
}
