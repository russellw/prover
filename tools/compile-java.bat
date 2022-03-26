del *.class
C:\jdk-17.0.1\bin\javac -Xlint:unchecked -d . C:\olivine\script\%1.java
if errorlevel 1 goto :eof

C:\jdk-17.0.1\bin\jar -c -e Main -f %1.jar *.class
if errorlevel 1 goto :eof
