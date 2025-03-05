@echo off
REM Example batch file to run the file uploader

REM Install required dependencies
pip install -r uploader_requirements.txt

REM Example 1: Upload all PDF files from a directory
python file_uploader.py --directory "C:\path\to\your\files" --url "http://localhost:8080/api/upload" --extensions pdf

REM Example 2: Upload all files recursively with authentication header
REM python file_uploader.py --directory "C:\path\to\your\files" --url "http://localhost:8080/api/upload" --recursive --headers "Authorization: Bearer YOUR_TOKEN_HERE"

REM Example 3: Upload images with more concurrent threads
REM python file_uploader.py --directory "C:\path\to\your\files" --url "http://localhost:8080/api/upload" --extensions jpg,png --threads 8

pause
