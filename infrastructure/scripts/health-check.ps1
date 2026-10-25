#requires -Version 5.1
<#
.SYNOPSIS
    Check health of running platform services.

.DESCRIPTION
    Checks PostgreSQL, ML service, and backend readiness endpoints.
    Uses localhost ports published by Docker Compose.

.EXAMPLE
    .\infrastructure\scripts\health-check.ps1
#>

$ErrorActionPreference = 'Continue'

$pgPort = if ($env:POSTGRES_PORT) { $env:POSTGRES_PORT } else { '5432' }
$mlPort = if ($env:ML_SERVICE_PORT) { $env:ML_SERVICE_PORT } else { '8000' }
$backendPort = if ($env:BACKEND_PORT) { $env:BACKEND_PORT } else { '8080' }
$managementPort = if ($env:BACKEND_MANAGEMENT_PORT) { $env:BACKEND_MANAGEMENT_PORT } else { '8081' }

function Check-Uri {
    param(
        [string]$Uri,
        [string]$Description
    )
    try {
        $response = Invoke-WebRequest -Uri $Uri -UseBasicParsing -TimeoutSec 5 -Method Get
        Write-Output "OK   $Description -> $($response.StatusCode)"
    } catch {
        $reason = if ($_.Exception.Response) { $_.Exception.Response.StatusDescription } else { $_.Exception.Message }
        Write-Output "FAIL $Description -> $($_.Exception.GetType().Name): $reason"
    }
}

Write-Output '=== Platform health check ==='
Write-Output ''

Check-Uri -Uri "http://localhost:$mlPort/health" -Description "ML /health"
Check-Uri -Uri "http://localhost:$mlPort/ready" -Description "ML /ready"
Check-Uri -Uri "http://localhost:$backendPort/health" -Description "Backend /health"
Check-Uri -Uri "http://localhost:$managementPort/actuator/health/liveness" -Description "Backend liveness"
Check-Uri -Uri "http://localhost:$managementPort/actuator/health/readiness" -Description "Backend readiness"

Write-Output ''
Write-Output 'PostgreSQL port check (TCP only, not a pg_isready substitute):'
try {
    $tcp = Test-NetConnection -ComputerName '127.0.0.1' -Port $pgPort -WarningAction SilentlyContinue
    Write-Output "      $pgPort -> $($tcp.TcpTestSucceeded)"
} catch {
    Write-Output "      $pgPort -> failed to check: $($_.Exception.Message)"
}
