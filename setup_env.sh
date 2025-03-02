#!/bin/bash

# Script to set up environment variables for Textractor application
# Usage: source setup_env.sh

# Function to prompt for a value with a default
prompt_with_default() {
    local prompt_message="$1"
    local default_value="$2"
    local var_name="$3"
    
    echo -n "$prompt_message [$default_value]: "
    read input
    
    # Use default if no input provided
    if [ -z "$input" ]; then
        export "$var_name"="$default_value"
        echo "Using default value for $var_name: $default_value"
    else
        export "$var_name"="$input"
        echo "Set $var_name to: $input"
    fi
}

echo "Setting up environment variables for Textractor..."

# OpenAI API Key
prompt_with_default "Enter your OpenAI API key" "sk-your-api-key" "OPENAI_API_KEY"

# Redis configuration
prompt_with_default "Enter Redis host" "localhost" "REDIS_HOST"
prompt_with_default "Enter Redis password (leave empty for none)" "" "REDIS_PASSWORD"

# Database configuration
prompt_with_default "Enter database username" "sa" "DB_USERNAME"
prompt_with_default "Enter database password" "" "DB_PASSWORD"

# Display all set variables
echo ""
echo "Environment variables set:"
echo "=========================="
echo "OPENAI_API_KEY: ${OPENAI_API_KEY:0:5}... (hidden for security)"
echo "REDIS_HOST: $REDIS_HOST"
echo "REDIS_PASSWORD: ${REDIS_PASSWORD:+*****} ${REDIS_PASSWORD:-(empty)}"
echo "DB_USERNAME: $DB_USERNAME"
echo "DB_PASSWORD: ${DB_PASSWORD:+*****} ${DB_PASSWORD:-(empty)}"
echo ""

echo "To use these variables in your current shell session, run:"
echo "source setup_env.sh"
echo ""
echo "To make these variables available to your tests, you can run:"
echo "OPENAI_API_KEY=\"$OPENAI_API_KEY\" REDIS_HOST=\"$REDIS_HOST\" REDIS_PASSWORD=\"$REDIS_PASSWORD\" DB_USERNAME=\"$DB_USERNAME\" DB_PASSWORD=\"$DB_PASSWORD\" mvn test"
echo ""
