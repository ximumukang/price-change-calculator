# 数据库迁移脚本 - 修改 price_item 表的 name 字段长度
# 用途：将 VARCHAR(100) 扩展为 VARCHAR(500) 以支持 AES-256-GCM 加密数据

param(
    [string]$DbHost = "localhost",
    [string]$DbName = "price_change",
    [string]$DbUser = "root",
    [string]$DbPassword = ""
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "数据库迁移：扩展 name 字段长度" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 如果未提供密码，提示输入
if ([string]::IsNullOrEmpty($DbPassword)) {
    $securePassword = Read-Host "请输入数据库密码" -AsSecureString
    $DbPassword = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto(
        [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    )
}

Write-Host "正在连接数据库..." -ForegroundColor Yellow

try {
    # 构建 MySQL 命令
    $mysqlCmd = "mysql"
    $mysqlArgs = @(
        "-h", $DbHost,
        "-u", $DbUser,
        "-p$DbPassword",
        $DbName,
        "-e", "ALTER TABLE price_item MODIFY COLUMN name VARCHAR(500) NOT NULL COMMENT '价格项名称（AES-256-GCM加密存储）';"
    )

    # 执行 SQL
    & $mysqlCmd @mysqlArgs

    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "✓ 迁移成功！" -ForegroundColor Green
        Write-Host ""
        Write-Host "验证结果：" -ForegroundColor Cyan
        
        # 验证表结构
        $verifyArgs = @(
            "-h", $DbHost,
            "-u", $DbUser,
            "-p$DbPassword",
            $DbName,
            "-e", "DESCRIBE price_item;"
        )
        & $mysqlCmd @verifyArgs
    } else {
        Write-Host ""
        Write-Host "✗ 迁移失败，请检查错误信息" -ForegroundColor Red
        Write-Host ""
        Write-Host "可能的原因：" -ForegroundColor Yellow
        Write-Host "1. MySQL 未安装或未添加到系统 PATH" -ForegroundColor Yellow
        Write-Host "2. 数据库连接信息不正确" -ForegroundColor Yellow
        Write-Host "3. 数据库用户权限不足" -ForegroundColor Yellow
        exit 1
    }
}
catch {
    Write-Host ""
    Write-Host "✗ 执行出错: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "迁移完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
