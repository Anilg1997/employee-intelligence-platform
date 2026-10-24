param(
    [switch]$RequireDocker
)

$ErrorActionPreference = 'Continue'
$mavenWrapper = Join-Path $PSScriptRoot '..\..\backend\spring-boot-app\mvnw.cmd'
$checks = @(
    @{ Name = 'Java'; Command = 'java'; Arguments = @('-version') },
    @{ Name = 'Maven Wrapper'; Command = $mavenWrapper; Arguments = @('-version') },
    @{ Name = 'Node.js'; Command = 'node'; Arguments = @('--version') },
    @{ Name = 'npm'; Command = 'npm'; Arguments = @('--version') },
    @{ Name = 'Python launcher'; Command = 'py'; Arguments = @('-3.12', '--version') },
    @{ Name = 'Docker'; Command = 'docker'; Arguments = @('--version') },
    @{ Name = 'Docker Compose'; Command = 'docker'; Arguments = @('compose', 'version') },
    @{ Name = 'Git'; Command = 'git'; Arguments = @('--version') }
)

$missingRequired = $false
foreach ($check in $checks) {
    $required = $check.Name -notin @('Docker', 'Docker Compose') -or $RequireDocker
    $available = if ([IO.Path]::IsPathRooted($check.Command) -or $check.Command.Contains('\')) {
        Test-Path $check.Command
    } else {
        [bool](Get-Command $check.Command -ErrorAction SilentlyContinue)
    }
    if (-not $available) {
        $state = if ($required) { 'MISSING (required)' } else { 'not installed (optional)' }
        Write-Output "$($check.Name): $state"
        if ($required) { $missingRequired = $true }
        continue
    }

    $output = & $check.Command @($check.Arguments) 2>&1
    $firstLine = ($output | Select-Object -First 1).ToString().Trim()
    Write-Output "$($check.Name): $firstLine"
    if ($LASTEXITCODE -ne 0 -and $required) {
        $missingRequired = $true
    }
}

if ($missingRequired) {
    exit 1
}
