cd %~dp0
java -server -Xmx4096m -XX:MaxPermSize=512m -classpath ..\webapp\WEB-INF\lib\*;..\webapp\WEB-INF\classes com.constellio.app.start.MainConstellio

call installService.bat