if not "%minimized%"=="" goto :minimized
set minimized=true
start /min cmd /C "%~dpnx0"
goto :EOF
:minimized
@echo off
TITLE SirRender Server
rem java -jar applications\SirRender.jar server 4444 >> c:\tmp\SirRenderServer.log
java -jar applications\SirRender.jar server 4444
pause