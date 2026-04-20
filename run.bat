@echo off
echo Starting Spring Boot Backend Service...
cd "%~dp0\backend-service"
call mvnw.cmd spring-boot:run
pause