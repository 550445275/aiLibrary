# 串联：前端 build → Maven package → docker build。在项目根执行。
# 使用：从仓库根执行 .\scripts\docker-build.ps1
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
Set-Location $root

Write-Host ">> frontend: npm ci && npm run build" -ForegroundColor Cyan
Push-Location (Join-Path $root 'frontend')
try {
    npm ci
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    npm run build
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
} finally {
    Pop-Location
}

Write-Host ">> mvn -DskipTests package" -ForegroundColor Cyan
& mvn -DskipTests package
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$tag = 'library:0.0.1'
Write-Host ">> docker build -t $tag" -ForegroundColor Cyan
& docker build -t $tag (Join-Path $root '.')
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Write-Host "Done. Image: $tag" -ForegroundColor Green
