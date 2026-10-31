This folder contains keystore files used by Jetty web server. Wandora
uses Jetty for embedded web server. Default keystore file name is 
'keystore'. The keystore file *MUST* be recreated in production environments
as the default keystore is publicly available in Wandora's GIT repository.

You can recreate the keystore with 'keytool' command, for example.
The keytool command is part of Java distribution package.
Example command is

$ keytool -keystore keystore -alias jetty -genkey -keyalg RSA

The keystore filename and keystore's password should be updated in Wandora
options, file 'options.xml'. XML paths for keystore settings are

 * Keystore filename: httpmodulesserver.keystore.file
 * Keystore password: httpmodulesserver.keystore.password

These options can be set in Wandora application too, adjusting server
settings. By default Wandora uses keystore filename 
'resource/conf/keystore/keystore' and password 'wandora'.

Notice, if you don't use the SSL option, the keystore is not used and
you don't need to worry recreating the keystore nor the keystore in any
way.

Read more about creating keystore here:
http://www.eclipse.org/jetty/documentation/current/configuring-ssl.html
