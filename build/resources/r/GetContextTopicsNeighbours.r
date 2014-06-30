#
# WANDORA
# Knowledge Extraction, Management, and Publishing Application
# http://wandora.org/
# 
# Copyright (C) 2004-2014 Wandora Team
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
# This is an example where we create a graph representation of a Topic Map
# while maintaining a map from topics to their respective vertex ids.
# 
# This mapping is then used to find the immediate neighbours of the context
# topics.

# Load igraph
library(igraph)

# Fetch all the topics we want in the graph. Here we fetch all topics contained
# in the Topic Map.
ts <- getAllTopics()

# Construct the indice map where each SI string maps to an index in the graph.
ind <- list()
for(t in ts){
    # Get the string representation of a SI of t.
    # This is equivalent to t.getOneSubjectIdentifier().toString();
    si <- .jcall(t,"Lorg/wandora/topicmap/Locator;","getOneSubjectIdentifier")
    si <- .jcall(si,"Ljava/lang/String;","toString")
    #Append the si-index pair to the map. Indices range from 1 to length(ts).
    ind[[si]]<-length(ind)+1
}
# Call makeGraph with indices specified in order to make sure our indices are
# used to construct the graph.
g <- makeGraph(ts,ind)

# Get the selected topics from Wandora.
cts <- getContextTopics()

# Get the string representation of the SI and using it look up the index from
# the map constructed earlier.
for (ct in cts){
    si <- .jcall(ct,"Lorg/wandora/topicmap/Locator;","getOneSubjectIdentifier")
    si <- .jcall(si,"Ljava/lang/String;","toString")
    cind[[length(cind)+1]] <- ind[[si]]
}

# Print out the immediate neighborhoods of vertices cind in the graph g.

print(neighborhood.size(g,1,cind))
