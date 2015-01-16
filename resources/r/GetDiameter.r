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
# This is an example script where a graph representation is constructed from
# given topics and finally the diameter of the graph is computed. See the 
# Topic Map diameter tool in Wandora for an alternative implementation in Java.

# Load igraph
library("igraph")

print("Getting all topics in the Topic Map")

# Retrieve the Wandora Java object using getWandora() from 
# org.wandora.application.Wandora
wandora <- J("org.wandora.application.Wandora")$getWandora()

# Retrieve the topics themselves using Java mehtod calls and unwrap the 
# returned iterator to a list suitable for futher use.
tm <- wandora$getTopicMap()
ts <- unwrapIterator(getTopicMap()$getTopics())

# Now create the graph itself.
print("Trying to create a graph of the retrieved topics")

result <- tryCatch({
    g <- makeGraphJava(ts)
    print("Calculating the diameter")
    # Calculate the diameter
    d <- diameter(g)
    paste("got: ",d)
}, error <- function(err) {
    return("Failed! Make sure you have igraph included.")
})

print(result);
