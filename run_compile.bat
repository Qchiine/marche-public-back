@echo off
cd /d C:\Users\DELL\Desktop\marche-public-back0
call mvnw.cmd clean compile > compile_output.txt 2>&1
type compile_output.txt
