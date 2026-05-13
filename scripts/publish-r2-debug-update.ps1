param(
    [int]$VersionCode = 2,
    [string]$VersionName = "1.0.1",
    [string]$Bucket = "ai-translate-assets",
    [string]$PublicBaseUrl = "https://download.204152.xyz",
    [string]$ApkObjectKey = "releases/ai-translate-1.0.1-debug.apk",
    [string[]]$Notes = @(
        "修复悬浮球在其他 App 中无法稳定读取剪贴板的问题。",
        "修复悬浮剪贴板桥接后回到 AI 翻译主界面的问题。",
        "应用内更新支持下载 APK、校验大小和 SHA256，并拉起系统安装器。"
    )
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$repoRoot = Split-Path -Parent $PSScriptRoot
$manifestPath = Join-Path $repoRoot "docs\r2\latest.json"
$apkPath = Join-Path $repoRoot "app\build\outputs\apk\debug\app-debug.apk"
$apkUrl = "$PublicBaseUrl/$ApkObjectKey"

Push-Location $repoRoot
try {
    & .\gradlew.bat :app:assembleDebug --no-daemon --console=plain "-PappVersionCode=$VersionCode" "-PappVersionName=$VersionName"
    if ($LASTEXITCODE -ne 0) {
        throw "Debug APK 构建失败"
    }

    $apk = Get-Item -LiteralPath $apkPath
    $sha256 = (Get-FileHash -LiteralPath $apk.FullName -Algorithm SHA256).Hash

    $manifest = [ordered]@{
        schemaVersion = 1
        updatedAt = (Get-Date).ToString("yyyy-MM-ddTHH:mm:sszzz")
        android = [ordered]@{
            versionCode = $VersionCode
            versionName = $VersionName
            required = $false
            apkUrl = $apkUrl
            sha256 = $sha256
            sizeBytes = $apk.Length
            notes = $Notes
        }
    }

    $manifest | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath $manifestPath -Encoding UTF8

    & wrangler r2 object put "$Bucket/$ApkObjectKey" `
        --file $apk.FullName `
        --content-type "application/vnd.android.package-archive" `
        --cache-control "public, max-age=31536000, immutable" `
        --remote
    if ($LASTEXITCODE -ne 0) {
        throw "APK 上传 R2 失败"
    }

    & wrangler r2 object put "$Bucket/releases/latest.json" `
        --file $manifestPath `
        --content-type "application/json; charset=utf-8" `
        --cache-control "public, max-age=60" `
        --remote
    if ($LASTEXITCODE -ne 0) {
        throw "更新清单上传 R2 失败"
    }

    $apkHead = Invoke-WebRequest -Uri $apkUrl -Method Head -UseBasicParsing
    if ($apkHead.StatusCode -ne 200) {
        throw "APK 公开访问校验失败：HTTP $($apkHead.StatusCode)"
    }

    $latest = Invoke-WebRequest -Uri "$PublicBaseUrl/releases/latest.json" -UseBasicParsing
    if ($latest.StatusCode -ne 200) {
        throw "Manifest 公开访问校验失败：HTTP $($latest.StatusCode)"
    }

    Write-Host "R2 Debug 更新发布完成：$apkUrl"
    Write-Host "Version: $VersionName ($VersionCode)"
    Write-Host "Size: $($apk.Length)"
    Write-Host "SHA256: $sha256"
} finally {
    Pop-Location
}
