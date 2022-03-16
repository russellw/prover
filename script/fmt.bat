@echo off

rem Space before caret is necessary due to weirdness in the handling of caret
rem In particular, it doesn't work reliably if the space goes on the following line
C:\jdk-17.0.1\bin\java ^
--add-exports jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED ^
--add-exports jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED ^
--add-exports jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED ^
--add-exports jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED ^
--add-exports jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED ^
-jar C:\bin\google-java-format-1.15.0-all-deps.jar -i src/main/java/olivine/*.java

black .
