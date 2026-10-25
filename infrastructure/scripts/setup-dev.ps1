#requires -Version 5.1
<#
.SYNOPSIS
    Employee Intelligence Platform — local development setup helper.

.DESCRIPTION
    One-time contributor setup:
      1. Run check-prerequisites.ps1 first.
      2. Copy .env.example to an ignored .env only if it does not already exist.
      3. Verify frontend/ and ml-service/ package installation status.

    This script never creates or overwrites an existing .env file.
    Secrets must be injected privately by the developer.

.EXAMPLE
    .\infrastructure\scripts\setup-dev.ps1
#>

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $root '.env'
$envExample = Join-Path $root '.env.example'
$frontendLock = Join-Path $root 'frontend\package-lock.json'
$mlVenv = Join-Path $root 'ml-service\.venv'

Write-Output '=== Employee Intelligence Platform — setup check ==='
Write-Output ''

# 1. Environment template
if (-not (Test-Path $envExample)) {
    Write-Error ".env.example missing at $envExample"
    exit 1
}

if (-not (Test-Path $envFile)) {
    Write-Output 'No root .env found. Create one from .env.example and set POSTGRES_PASSWORD privately.'
    Write-Output "    copy '$envExample' '$envFile'"
    Write-Output ''
} else {
    Write-Output 'Root .env already present. Do not commit it.'
}

# 2. Frontend lockfile
if (Test-Path $frontendLock) {
    Write-Output 'frontend/package-lock.json present. Use npm ci in frontend/ when Node dependencies are missing.'
} else {
    Write-Warning 'frontend/package-lock.json missing. Run "npm install" in frontend/ before building.'
}

# 3. ML virtual environment
if (Test-Path $mlVenv) {
    Write-Output 'ml-service/.venv present.'
} else {
    Write-Output 'ml-service/.venv not found. Create it with: py -3.12 -m venv ml-service\.venv'
    Write-Output 'Then install requirements: ml-service\.venv\Scripts\python.exe -m pip install -r ml-service\requirements.txt'
}

Write-Output ''
Write-Output 'Next steps:'
Write-Output '  1. Set POSTGRES_PASSWORD in the current shell or in the private root .env file.'
Write-Output '  2. docker compose up -d postgres ml-service backend'
Write-Output '  3. Optional: start Ollama separately and pull llama3.2 + nomic-embed-text'
Write-Output '  4. Start Angular in frontend/: npm start'
