call build.bat
if errorlevel 1 goto :eof

cd ..\derivation-tests
VSTest.Console.exe bin\Debug\net6.0\derivation-tests.dll
cd ..\derivation
