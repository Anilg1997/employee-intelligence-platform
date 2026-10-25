#requires -Version 5.1
<#
.SYNOPSIS
    Stop and remove local infrastructure containers.

.DESCRIPTION
    Runs "docker compose down" for the core stack.
    Persistent volumes for PostgreSQL, Kafka, and Ollama are preserved
    unless "-RemoveData" is specified.

.PARAMETER RemoveData
    If set, also remove named volumes. Use with care; this is not the
    default because it can destroy database and model runtime state.

.EXAMPLE
    .\infrastructure\scripts\stop-env.ps1

.EXAMPLE
    .\infrastructure\scripts\stop-env.ps1 -RemoveData
#>

param(
    [switch]$RemoveData
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot

Push-Location $root
try {
    if ($RemoveData) {
        Write-Output 'Stopping services AND removing named volumes. This may destroy container-local data.'
        docker compose down -v
    } else {
        docker compose down
    }
} finally {
    Pop-Location
}
