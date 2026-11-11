param(
    [ValidateSet('compile', 'test', 'screens', 'lint', 'debug', 'release', 'verify')]
    [string]$Task = 'compile',
    [switch]$Offline
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$jdk = 'D:\zzz\.toolchain\jdk17'
$sdk = 'D:\zzz\.toolchain\android-sdk'
$gradleHome = 'D:\zzz\.gradle-home'
$androidHome = 'D:\zzz\.android-home'
$temp = 'D:\zzz\.tmp'
$gradle = 'D:\zzz\.toolchain\gradle\bin\gradle.bat'

if (-not (Test-Path $gradle)) {
    $gradle = Join-Path $root 'gradlew.bat'
}
if (-not (Test-Path $jdk)) { throw "JDK not found: $jdk" }
if (-not (Test-Path $sdk)) { throw "Android SDK not found: $sdk" }

$env:JAVA_HOME = $jdk
$env:ANDROID_HOME = $sdk
$env:ANDROID_SDK_ROOT = $sdk
$env:ANDROID_USER_HOME = $androidHome
$env:GRADLE_USER_HOME = $gradleHome
$env:TEMP = $temp
$env:TMP = $temp
$env:PATH = "$jdk\bin;$sdk\platform-tools;$env:PATH"

$tasks = switch ($Task) {
    'compile' { @(':app:compileDebugKotlin') }
    'test' { @(':app:testDebugUnitTest') }
    'screens' { @(':app:recordPaparazziDebug', '--tests', 'com.yinian.app.ScreenshotTest') }
    'lint' { @(':app:lintDebug') }
    'debug' { @(':app:assembleDebug') }
    'release' { @(':app:assembleRelease') }
    'verify' { @(':app:testDebugUnitTest', ':app:lintDebug', ':app:assembleRelease') }
}

$logDir = Join-Path $root 'app\build\cli'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$log = Join-Path $logDir "$Task.log"

$argsList = @($tasks) + @('--console=plain', '--warning-mode=summary')
if ($Offline) { $argsList += '--offline' }

$sw = [System.Diagnostics.Stopwatch]::StartNew()
& $gradle @argsList *> $log
$code = $LASTEXITCODE
$sw.Stop()

if ($code -ne 0) {
    Write-Host ("FAILED {0} in {1:N1}s; tail {2}" -f $Task, $sw.Elapsed.TotalSeconds, $log) -ForegroundColor Red
    Get-Content $log -Tail 80
    exit $code
}

Write-Host ("OK {0} in {1:N1}s" -f $Task, $sw.Elapsed.TotalSeconds) -ForegroundColor Green

if ($Task -in @('debug', 'release', 'verify')) {
    $apk = Join-Path $root 'app\build\outputs\apk\release\app-release.apk'
    if ($Task -eq 'debug') {
        $apk = Join-Path $root 'app\build\outputs\apk\debug\app-debug.apk'
    }
    if (Test-Path $apk) {
        $item = Get-Item $apk
        $hash = (Get-FileHash $apk -Algorithm SHA256).Hash.ToLower()
        Write-Host ("APK {0} ({1:N2} MB)" -f $apk, ($item.Length / 1MB))
        Write-Host ("SHA256 {0}" -f $hash)
        if ($Task -eq 'verify') {
            $apksigner = Join-Path $sdk 'build-tools\35.0.0\apksigner.bat'
            $zipalign = Join-Path $sdk 'build-tools\35.0.0\zipalign.exe'
            if (Test-Path $apksigner) {
                $sig = & $apksigner verify --verbose $apk 2>&1
                if ($LASTEXITCODE -ne 0) { Write-Host 'SIGNATURE FAILED' -ForegroundColor Red; $sig | Select-Object -Last 10; exit 1 }
                Write-Host 'SIGNATURE OK'
            }
            if (Test-Path $zipalign) {
                $align = & $zipalign -c -v 4 $apk 2>&1
                if ($LASTEXITCODE -ne 0) { Write-Host 'ZIPALIGN FAILED' -ForegroundColor Red; $align | Select-Object -Last 10; exit 1 }
                Write-Host 'ZIPALIGN OK'
            }
        }
    }
}
