python C:\olivine\etc\gen-cmp-eqns.py >C:\olivine\src\main\java\olivine\EquationComparison.java
if errorlevel 1 goto :eof

dos2unix C:\olivine\src\main\java\olivine\EquationComparison.java
if errorlevel 1 goto :eof

C:\jdk-17.0.1\bin\java -jar C:\bin\google-java-format-1.15.0-all-deps.jar -i C:\olivine\src\main\java\olivine\EquationComparison.java
