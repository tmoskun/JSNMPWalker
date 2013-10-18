REM cd "%~dp0\jsnmpwalker"
FOR /F "tokens=*" %%A IN ('COMMAND ^| dir /p/b JSNMPWalker*.jar') DO set filename=%%A

@IF EXIST "%~dp0\java.exe" (
  "%~dp0\java.exe"  -jar "%~dp0\%filename%" %*
) ELSE (
  java  -jar "%~dp0\%filename%" %*
)