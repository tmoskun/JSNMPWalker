cd "%~dp0"
@ECHO OFF
FOR /F "tokens=*" %%A IN ('dir /p/b JSNMPWalker*.jar') DO set filename=%%A

echo Starting JSNMPWalker...

@IF EXIST "%~dp0\java.exe" (
  "%~dp0\java.exe"  -jar "%~dp0\%filename%" %*
) ELSE (
  java  -jar "%~dp0\%filename%" %*
)
 echo JSNMPWalker stopped.