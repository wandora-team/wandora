This directory contains a sample project 'QuerySample.wpr' for
Wandora. Project file demonstrates the use of query topic maps
and linked topic maps. See
http://wandora.org/si/wandora/wiki/index.php?title=Query_topic_map
for more information about query topic maps.




Project contains three layers: Base, query and Link->Base.

Base topic map is Wandora's default topic map with a few added topics
that are explained below.

Query layer is a query topic map with two different queries. One
query returns the transitional closure of superclass-subclass
associations. The other converts instances and classes to associations.

Link layer is the context layer for the query layer. This means that
all queries are run in this layer. The layer is a link to the Base
layer. That is, it contains exactly the same content as Base layer.
All this information is linked however so there is only one copy of it
in memory and changes to one layer affect the other.

The Base layer contains a few extra topics in addition to the default
Wandora topic map. "Test class", "sub class" and "sub sub class" topics
demonstrate how the subclass closure works. If you set query layer to
invisible you will see how the Base topic map is defined. "Test class" is
a sub class of Wandora class. "Sub class" is a sub class of "Test class"
and "sub sub class" is a sub class of "sub class". The subclass hierarchy
looks like this.

Wandora class
- Schema type
  - <default Wandora topics>
- Test class
  - sub class
    - sub sub class

When you turn query topic map to visible, you will see that all sub classes
of sub classes and so on are shown as direct sub classes of Wandora class.
This is result of the superclass-subclass closure. 

You can also open any topic to see how toggling query layer visibilty
affects the instance associations. The Base topic map contains topics for
instance association type and roles. These are not included in the default
Wandora base topic map and are required for the query in the query layer
to work.
