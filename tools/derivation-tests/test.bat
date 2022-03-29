if "%VCINSTALLDIR%"=="" call "C:\Program Files\Microsoft Visual Studio\2022\Community\VC\Auxiliary\Build\vcvars64.bat"
VSTest.Console.exe bin\Debug\net6.0\derivation-tests.dll
