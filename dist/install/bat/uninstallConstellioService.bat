cd %~dp0

call stopService.bat
call uninstallService.bat

call setenv.bat
copy /y NUL %conf_file%