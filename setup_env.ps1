# PowerShell script to set up environment variables for Textractor application

function Prompt-WithDefault {
    param (
        [string]$promptMessage,
        [string]$defaultValue,
        [string]$varName,
        [bool]$isSecret = $false
    )
    
    if ($isSecret) {
        $promptText = "$promptMessage [$('*' * [Math]::Min($defaultValue.Length, 5))...]: "
    } else {
        $promptText = "$promptMessage [$defaultValue]: "
    }
    
    $input = Read-Host -Prompt $promptText
    
    # Use default if no input provided
    if ([string]::IsNullOrEmpty($input)) {
        [Environment]::SetEnvironmentVariable($varName, $defaultValue, "Process")
        if ($isSecret) {
            Write-Host "Using default value for $varName: $('*' * [Math]::Min($defaultValue.Length, 5))..."
        } else {
            Write-Host "Using default value for $varName: $defaultValue"
        }
    } else {
        [Environment]::SetEnvironmentVariable($varName, $input, "Process")
        if ($isSecret) {
            Write-Host "Set $varName to: $('*' * [Math]::Min($input.Length, 5))..."
        } else {
            Write-Host "Set $varName to: $input"
        }
    }
}

Write-Host "Setting up environment variables for Textractor..." -ForegroundColor Cyan
Write-Host ""

# OpenAI API Key
Prompt-WithDefault -promptMessage "Enter your OpenAI API key" -defaultValue "sk-your-api-key" -varName "OPENAI_API_KEY" -isSecret $true

# Redis configuration
Prompt-WithDefault -promptMessage "Enter Redis host" -defaultValue "localhost" -varName "REDIS_HOST"
Prompt-WithDefault -promptMessage "Enter Redis password (leave empty for none)" -defaultValue "" -varName "REDIS_PASSWORD" -isSecret $true

# Database configuration
Prompt-WithDefault -promptMessage "Enter database username" -defaultValue "sa" -varName "DB_USERNAME"
Prompt-WithDefault -promptMessage "Enter database password (leave empty for none)" -defaultValue "" -varName "DB_PASSWORD" -isSecret $true

# Display all set variables
Write-Host ""
Write-Host "Environment variables set:" -ForegroundColor Green
Write-Host "==========================" -ForegroundColor Green

$openaiKey = [Environment]::GetEnvironmentVariable("OPENAI_API_KEY", "Process")
$redisHost = [Environment]::GetEnvironmentVariable("REDIS_HOST", "Process")
$redisPassword = [Environment]::GetEnvironmentVariable("REDIS_PASSWORD", "Process")
$dbUsername = [Environment]::GetEnvironmentVariable("DB_USERNAME", "Process")
$dbPassword = [Environment]::GetEnvironmentVariable("DB_PASSWORD", "Process")

Write-Host "OPENAI_API_KEY: $($openaiKey.Substring(0, [Math]::Min(5, $openaiKey.Length)))... (hidden for security)"
Write-Host "REDIS_HOST: $redisHost"
if ([string]::IsNullOrEmpty($redisPassword)) {
    Write-Host "REDIS_PASSWORD: (empty)"
} else {
    Write-Host "REDIS_PASSWORD: *****"
}
Write-Host "DB_USERNAME: $dbUsername"
if ([string]::IsNullOrEmpty($dbPassword)) {
    Write-Host "DB_PASSWORD: (empty)"
} else {
    Write-Host "DB_PASSWORD: *****"
}

Write-Host ""
Write-Host "To run tests with these environment variables, use:" -ForegroundColor Yellow
Write-Host "mvn test -DOPENAI_API_KEY=`"$openaiKey`" -DREDIS_HOST=`"$redisHost`" -DREDIS_PASSWORD=`"$redisPassword`" -DDB_USERNAME=`"$dbUsername`" -DDB_PASSWORD=`"$dbPassword`"" -ForegroundColor Yellow
Write-Host ""

Write-Host "Note: These environment variables are only set for the current PowerShell session." -ForegroundColor Magenta
Write-Host "To make them permanent for your user account, you can use the following commands:" -ForegroundColor Magenta
Write-Host "[Environment]::SetEnvironmentVariable('OPENAI_API_KEY', '$openaiKey', 'User')" -ForegroundColor DarkGray
Write-Host "[Environment]::SetEnvironmentVariable('REDIS_HOST', '$redisHost', 'User')" -ForegroundColor DarkGray
Write-Host "[Environment]::SetEnvironmentVariable('REDIS_PASSWORD', '$redisPassword', 'User')" -ForegroundColor DarkGray
Write-Host "[Environment]::SetEnvironmentVariable('DB_USERNAME', '$dbUsername', 'User')" -ForegroundColor DarkGray
Write-Host "[Environment]::SetEnvironmentVariable('DB_PASSWORD', '$dbPassword', 'User')" -ForegroundColor DarkGray
Write-Host ""
