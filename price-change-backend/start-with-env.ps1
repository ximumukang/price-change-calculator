# Load environment variables from .env file and start the backend
$envFile = Join-Path $PSScriptRoot ".." ".env"

if (Test-Path $envFile) {
    Write-Host "Loading environment variables from .env..." -ForegroundColor Green
    Get-Content $envFile | ForEach-Object {
        if ($_ -match '^([^#][^=]+)=(.*)$') {
            $name = $matches[1].Trim()
            $value = $matches[2].Trim()
            [Environment]::SetEnvironmentVariable($name, $value, "Process")
            Write-Host "  Set: $name" -ForegroundColor Gray
        }
    }
} else {
    Write-Host "Warning: .env file not found" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Starting Spring Boot application..." -ForegroundColor Cyan
Set-Location $PSScriptRoot
mvn spring-boot:run
