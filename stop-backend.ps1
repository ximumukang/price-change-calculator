# ============================================
# 停止后端服务脚本 (Windows PowerShell)
# 功能：停止后端 + 停止 MySQL 容器
# ============================================

$ErrorActionPreference = "SilentlyContinue"
$MYSQL_CONTAINER = "mysql-local"

Write-Host "正在停止服务..." -ForegroundColor Yellow

# 停止 MySQL 容器
docker stop $MYSQL_CONTAINER 2>$null | Out-Null
Write-Host "  MySQL 容器已停止" -ForegroundColor Green

# 查找并停止后端进程
$javaProcess = Get-Process -Name java -ErrorAction SilentlyContinue | Where-Object {
    $_.CommandLine -like "*spring-boot*" -or $_.CommandLine -like "*price-change*"
}
if ($javaProcess) {
    $javaProcess | Stop-Process -Force
    Write-Host "  后端服务已停止" -ForegroundColor Green
}

Write-Host "所有服务已停止" -ForegroundColor Green
