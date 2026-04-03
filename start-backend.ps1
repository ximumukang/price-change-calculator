$BACKEND_DIR = "D:\zang\price-change-backend"
$MYSQL_CONTAINER = "mysql-local"

Write-Host "[1/3] Check MySQL container..."
$running = docker ps --filter "name=$MYSQL_CONTAINER" --format "{{.Names}}" 2>$null
if ($running -ne $MYSQL_CONTAINER) {
    $exists = docker ps -a --filter "name=$MYSQL_CONTAINER" --format "{{.Names}}" 2>$null
    if ($exists -eq $MYSQL_CONTAINER) {
        Write-Host "  Starting MySQL container..."
        docker start $MYSQL_CONTAINER 2>$null | Out-Null
    } else {
        Write-Host "Error: MySQL container not found" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "  MySQL is running" -ForegroundColor Green
}

Write-Host "[2/3] Wait for MySQL ready..."
for ($i = 0; $i -lt 30; $i++) {
    $output = docker exec $MYSQL_CONTAINER mysqladmin ping -h localhost -u root -proot --silent 2>&1
    if ($output -match "alive") {
        Write-Host "  MySQL ready" -ForegroundColor Green
        break
    }
    Start-Sleep -Seconds 2
}

Write-Host "[3/3] Start backend..."
Set-Location $BACKEND_DIR
mvn spring-boot:run
