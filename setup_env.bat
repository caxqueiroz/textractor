@echo off
REM Script to set up environment variables for Textractor application

echo Setting up environment variables for Textractor...
echo.

REM OpenAI API Key
set /p OPENAI_API_KEY_INPUT="Enter your OpenAI API key [sk-your-api-key]: "
if "%OPENAI_API_KEY_INPUT%"=="" (
    set OPENAI_API_KEY=sk-your-api-key
    echo Using default value for OPENAI_API_KEY: sk-your-api-key
) else (
    set OPENAI_API_KEY=%OPENAI_API_KEY_INPUT%
    echo Set OPENAI_API_KEY to: %OPENAI_API_KEY_INPUT%
)

REM Redis configuration
set /p REDIS_HOST_INPUT="Enter Redis host [localhost]: "
if "%REDIS_HOST_INPUT%"=="" (
    set REDIS_HOST=localhost
    echo Using default value for REDIS_HOST: localhost
) else (
    set REDIS_HOST=%REDIS_HOST_INPUT%
    echo Set REDIS_HOST to: %REDIS_HOST_INPUT%
)

set /p REDIS_PASSWORD_INPUT="Enter Redis password (leave empty for none): "
set REDIS_PASSWORD=%REDIS_PASSWORD_INPUT%
if "%REDIS_PASSWORD_INPUT%"=="" (
    echo Using empty value for REDIS_PASSWORD
) else (
    echo Set REDIS_PASSWORD to: *****
)

REM Database configuration
set /p DB_USERNAME_INPUT="Enter database username [sa]: "
if "%DB_USERNAME_INPUT%"=="" (
    set DB_USERNAME=sa
    echo Using default value for DB_USERNAME: sa
) else (
    set DB_USERNAME=%DB_USERNAME_INPUT%
    echo Set DB_USERNAME to: %DB_USERNAME_INPUT%
)

set /p DB_PASSWORD_INPUT="Enter database password (leave empty for none): "
set DB_PASSWORD=%DB_PASSWORD_INPUT%
if "%DB_PASSWORD_INPUT%"=="" (
    echo Using empty value for DB_PASSWORD
) else (
    echo Set DB_PASSWORD to: *****
)

echo.
echo Environment variables set:
echo ==========================
echo OPENAI_API_KEY: %OPENAI_API_KEY:~0,5%... (hidden for security)
echo REDIS_HOST: %REDIS_HOST%
if "%REDIS_PASSWORD%"=="" (
    echo REDIS_PASSWORD: (empty)
) else (
    echo REDIS_PASSWORD: *****
)
echo DB_USERNAME: %DB_USERNAME%
if "%DB_PASSWORD%"=="" (
    echo DB_PASSWORD: (empty)
) else (
    echo DB_PASSWORD: *****
)
echo.

echo To run tests with these environment variables, use:
echo mvn test -DOPENAI_API_KEY="%OPENAI_API_KEY%" -DREDIS_HOST="%REDIS_HOST%" -DREDIS_PASSWORD="%REDIS_PASSWORD%" -DDB_USERNAME="%DB_USERNAME%" -DDB_PASSWORD="%DB_PASSWORD%"
echo.
