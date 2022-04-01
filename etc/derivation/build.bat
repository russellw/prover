if "%VCINSTALLDIR%"=="" call "C:\Program Files\Microsoft Visual Studio\2022\Community\VC\Auxiliary\Build\vcvars64.bat"
MSBuild.exe derivation.sln /p:Configuration=Debug /p:Platform="Any CPU"
