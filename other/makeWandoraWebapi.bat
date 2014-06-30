java -classpath ..\lib\axis-ant.jar;..\lib\axis.jar;..\lib\commons-discovery-0.2.jar;..\lib\commons-logging-1.0.4.jar;..\lib\jaxrpc.jar;..\lib\saaj.jar;..\lib\wsdl4j-1.5.1.jar;\wandora\build\classes org.apache.axis.wsdl.Java2WSDL -n "urn:WandoraService" -l "http://localhost:8080/wandora/services/WandoraService" -o wandora.wsdl org.wandora.webapi.definition.WandoraService
java -classpath ..\lib\axis-ant.jar;..\lib\axis.jar;..\lib\commons-discovery-0.2.jar;..\lib\commons-logging-1.0.4.jar;..\lib\jaxrpc.jar;..\lib\saaj.jar;..\lib\wsdl4j-1.5.1.jar org.apache.axis.wsdl.WSDL2Java -s -S true -p org.wandora.webapi.implementation wandora.wsdl



