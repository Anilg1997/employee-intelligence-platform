$ErrorActionPreference = 'Stop'
$scripts = @(
    'check-prerequisites.ps1',
    'setup-dev.ps1',
    'start-env.ps1',
    'health-check.ps1',
    'stop-env.ps1'
)
foreach ($name in $scripts) {
    $path = Join-Path $PSScriptRoot $name
    if (-not (Test-Path $path)) {
        Write-Output "MISSING: $name"
        continue
    }
    $tokens = $null
    $errors = $null
    $source = [System.IO.File]::ReadAllText($path)
    $tokens = $null
    $parseErrors = $null
    try {
        $parseErrors = [System.Management.Automation.PSParser]::Tokenize($source, [ref]$tokens)
    } catch {
        Write-Output "PARSE EXCEPTION: $name -> $($_.Exception.Message)"
        continue
    }
    if ($parseErrors.Count -gt 0) {
        Write-Output "PARSE ERRORS: $name"
        foreach ($e in $parseErrors) {
            Write-Output "  line $($e.Line) col $($e.Column): $($e.Message)"
        }
    } else {
        Write-Output "PARSE OK: $name"
    }
}
