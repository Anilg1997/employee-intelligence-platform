#requires -Version 5.1
<#
.SYNOPSIS
    Start local infrastructure for the Employee Intelligence Platform.

.DESCRIPTION
    Starts the core local stack:
      - PostgreSQL/pgvector
      - FastAPI ML service
      - Spring Boot backend

    Kafka, Redis, and Ollama are opt-in and are not started by default.
    Use the messaging or ai Compose profiles only when needed.

.PARAMETER PostgresPassword
    If provided, sets POSTGRES_PASSWORD for the current session.
    Otherwise the script expects POSTGRES_PASSWORD to already be available
    in the environment or in an ignored root .env file.

.EXAMPLE
    .\infrastructure\scripts\start-env.ps1

.EXAMPLE
    $env:POSTGRES_PASSWORD = '<private-value>'
    .\infrastructure\scripts\start-env.ps1
#>

param(
    [Parameter()]
    [string]$PostgresPassword
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot

if ($PostgresPassword) {
    $env:POSTGRES_PASSWORD = $PostgresPassword
}

if (-not $env:POSTGRES_PASSWORD) {
    Write-Error 'POSTGRES_PASSWORD is not set. Set it in the shell or in an ignored root .env file.'
    exit 1
}

if (-not $env:SPRING_PROFILES_ACTIVE) {
    $env:SPRING_PROFILES_ACTIVE = 'local'
    Write-Output "SPRING_PROFILES_ACTIVE defaulted to '$env:SPRING_PROFILES_ACTIVE'"
}

Write-Output '=== Starting core infrastructure ==='
Push-Location $root
try {
    docker compose up -d postgres ml-service backend

    Write-Output ''
    Write-Output 'Waiting for PostgreSQL...'
    $pgHealthy = $false
    foreach ($i in 1..30) {
        Start-Sleep -Seconds 2
        try {
            $status = docker inspect --format '{{.State.Health.Status}}' (
                docker compose ps -q postgres
            ) 2>$null
            if ($status -eq 'healthy') {
                $pgHealthy = $true
                break
            }
        } catch {
            Write-Output "  [waiting] PostgreSQL not yet reachable."
        }
    }

    if (-not $pgHealthy) {
        Write-Warning 'PostgreSQL health check did not complete within the wait loop.'
        Write-Output 'Run "docker compose ps" and "docker compose logs postgres" to investigate.'
    } else {
        Write-Output 'PostgreSQL is healthy.'
    }

    Write-Output ''
    Write-Output 'Waiting for ML service...'
    $mlHealthy = $false
    foreach ($i in 1..20) {
        Start-Sleep -Seconds 3
        try {
            $status = docker inspect --format '{{.State.Health.Status}}' (
                docker compose ps -q ml-service
            ) 2>$null
            if ($status -eq 'healthy') {
                $mlHealthy = $true
                break
            }
        } catch {
            Write-Output "  [waiting] ML service not yet reachable."
        }
    }

    if (-not $mlHealthy) {
        Write-Warning 'ML service health check did not complete within the wait loop.'
    } else {
        Write-Output 'ML service is healthy.'
    }

    Write-Output ''
    Write-Output 'Waiting for backend readiness...'
    $backendHealthy = $false
    foreach ($i in 1..30) {
        Start-Sleep -Seconds 3
        try {
            $status = docker inspect --format '{{.State.Health.Status}}' (
                docker compose ps -q backend
            ) 2>$null
            if ($status -eq 'healthy') {
                $backendHealthy = $true
                break
            }
        } catch {
            Write-Output "  [waiting] backend not yet reachable."
        }
    }

    if (-not $backendHealthy) {
        Write-Warning 'Backend readiness check did not complete within the wait loop.'
        Write-Output 'Check "docker compose logs backend" for Flyway or startup errors.'
    } else {
        Write-Output 'Backend is healthy.'
    }

    Write-Output ''
    Write-Output '=== Started services ==='
    docker compose ps
}
finally {
    Pop-Location
}
