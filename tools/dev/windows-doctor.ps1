param(
    [string]$SdkDir = "$env:LOCALAPPDATA\Android\Sdk"
)

$ErrorActionPreference = "Stop"

function Resolve-SdkDir([string]$InputSdkDir) {
    $candidates = New-Object System.Collections.Generic.List[string]
    if ($InputSdkDir) { $candidates.Add($InputSdkDir) }
    if ($env:ANDROID_SDK_ROOT) { $candidates.Add($env:ANDROID_SDK_ROOT) }
    if ($env:ANDROID_HOME) { $candidates.Add($env:ANDROID_HOME) }
    $candidates.Add("$env:LOCALAPPDATA\Android\Sdk")
    $candidates.Add("D:\Android\Sdk")
    $candidates.Add("E:\Android\Sdk")
    $candidates.Add("H:\Android\Sdk")
    $candidates.Add("C:\Android\Sdk")

    foreach ($c in $candidates) {
        if (-not $c) { continue }
        if (Test-Path $c) { return $c }
    }
    return $InputSdkDir
}

function Write-Section([string]$Text) {
    Write-Host ""
    Write-Host "=== $Text ==="
}

function Get-JavaInfo([string]$JavaExe) {
    try {
        $output = cmd /c "`"$JavaExe`" -version 2>&1"
        if ($LASTEXITCODE -ne 0 -and -not $output) {
            return @{ Ok = $false; Message = "java -version returned $LASTEXITCODE" }
        }

        $line = ($output | Select-Object -First 1)
        if ($line -match '"([^"]+)"') {
            $raw = $Matches[1]
            $parts = $raw.Split(".")
            $major = 0
            if ($parts[0] -eq "1" -and $parts.Length -gt 1) {
                [void][int]::TryParse($parts[1], [ref]$major)
            }
            else {
                [void][int]::TryParse($parts[0], [ref]$major)
            }
            return @{
                Ok = $true
                Raw = $raw
                Major = $major
                FirstLine = $line
            }
        }

        return @{ Ok = $false; Message = "unable to parse java version output" }
    }
    catch {
        return @{ Ok = $false; Message = $_.Exception.Message }
    }
}

function Test-Url([string]$Url) {
    try {
        $resp = Invoke-WebRequest -Uri $Url -Method Head -TimeoutSec 20 -UseBasicParsing
        return @{ Ok = $true; StatusCode = $resp.StatusCode }
    }
    catch {
        $msg = $_.Exception.Message
        if ($msg -match "\(404\)") {
            return @{ Ok = $true; StatusCode = 404 }
        }
        return @{ Ok = $false; Error = $msg }
    }
}

Write-Host "MetaAgent Windows Doctor"
Write-Host "Repo: $(Resolve-Path (Join-Path $PSScriptRoot '..\..'))"
$SdkDir = Resolve-SdkDir $SdkDir

Write-Section "Java"
$javaCmd = Get-Command java -ErrorAction SilentlyContinue
if ($null -eq $javaCmd) {
    Write-Host "[FAIL] java not found in PATH"
}
else {
    $info = Get-JavaInfo $javaCmd.Source
    if (-not $info.Ok) {
        Write-Host "[FAIL] java detection failed: $($info.Message)"
    }
    else {
        Write-Host "[OK] java: $($javaCmd.Source)"
        Write-Host "     version: $($info.Raw) (major=$($info.Major))"
        if ($info.Major -ne 17) {
            Write-Host "[WARN] Project is tested with JDK 17."
        }
    }
}

Write-Section "Android SDK"
if (Test-Path $SdkDir) {
    Write-Host "[OK] sdk dir exists: $SdkDir"
}
else {
    Write-Host "[FAIL] sdk dir not found: $SdkDir"
}

$sdkManagerCandidates = @(
    (Join-Path $SdkDir "cmdline-tools\latest\bin\sdkmanager.bat"),
    (Join-Path $SdkDir "cmdline-tools\latest\bin\sdkmanager"),
    (Join-Path $SdkDir "cmdline-tools\bin\sdkmanager.bat"),
    (Join-Path $SdkDir "cmdline-tools\bin\sdkmanager")
)
$sdkManagerPath = $sdkManagerCandidates | Where-Object { Test-Path $_ } | Select-Object -First 1
if ($null -eq $sdkManagerPath) {
    $sdkManagerCmd = Get-Command sdkmanager -ErrorAction SilentlyContinue
    if ($null -ne $sdkManagerCmd) {
        $sdkManagerPath = $sdkManagerCmd.Source
    }
}

if ($null -eq $sdkManagerPath) {
    Write-Host "[FAIL] sdkmanager not found"
}
else {
    Write-Host "[OK] sdkmanager: $sdkManagerPath"
}

$adbCandidates = @(
    (Join-Path $SdkDir "platform-tools\adb.exe"),
    (Join-Path $SdkDir "platform-tools\adb")
)
$adbPath = $adbCandidates | Where-Object { Test-Path $_ } | Select-Object -First 1
if ($null -eq $adbPath) {
    $adbCmd = Get-Command adb -ErrorAction SilentlyContinue
    if ($null -ne $adbCmd) {
        $adbPath = $adbCmd.Source
    }
}
if ($null -eq $adbPath) {
    Write-Host "[WARN] adb not found"
}
else {
    Write-Host "[OK] adb: $adbPath"
}

Write-Section "Project Files"
$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$localProps = Join-Path $repoRoot "local.properties"
if (Test-Path $localProps) {
    Write-Host "[OK] local.properties exists"
}
else {
    Write-Host "[WARN] local.properties missing"
}

Write-Section "Network Reachability"
$urls = @(
    "https://dl.google.com/dl/android/maven2/com/android/tools/build/gradle/maven-metadata.xml",
    "https://repo.maven.apache.org/maven2/com/android/tools/build/gradle/maven-metadata.xml"
)
foreach ($url in $urls) {
    $check = Test-Url $url
    if ($check.Ok) {
        Write-Host "[OK] $url ($($check.StatusCode))"
    }
    else {
        Write-Host "[FAIL] $url"
        Write-Host "       $($check.Error)"
    }
}

Write-Section "Suggested Next Step"
Write-Host "Run:"
Write-Host "  powershell -ExecutionPolicy Bypass -File .\tools\dev\windows-bootstrap.ps1"
