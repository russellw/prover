r:
cd \

del olivine\*.class
C:\jdk-17.0.1\bin\javac -Xlint:unchecked -d . -g C:\olivine\src\main\java\olivine\*.java
if errorlevel 1 goto :eof

python C:\olivine\etc\verify-proof.py "C:\jdk-17.0.1\bin\java -Xmx20g -ea olivine/Prover" %1 r:\proof.p r:\out.p