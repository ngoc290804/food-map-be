$ErrorActionPreference = "Stop"

function Get-JavaMajorVersion {
    param(
        [Parameter(Mandatory = $true)]
        [string]$JavaHome
    )

    $releaseFile = Join-Path $JavaHome "release"
    if (-not (Test-Path $releaseFile)) {
        return $null
    }

    $releaseContent = Get-Content $releaseFile
    $versionLine = $releaseContent | Where-Object { $_ -match '^JAVA_VERSION=' } | Select-Object -First 1
    if (-not $versionLine) {
        return $null
    }

    $version = [regex]::Match($versionLine, '"([^"]+)"').Groups[1].Value
    if (-not $version) {
        return $null
    }

    if ($version.StartsWith("1.")) {
        return [int]($version.Split(".")[1])
    }

    return [int](($version -split '[.-]')[0])
}

function Add-Candidate {
    param(
        [System.Collections.Generic.List[string]]$Candidates,
        [string]$PathValue
    )

    if (-not $PathValue) {
        return
    }

    try {
        $resolved = (Resolve-Path $PathValue -ErrorAction Stop).Path
    } catch {
        return
    }

    if (-not $Candidates.Contains($resolved)) {
        $Candidates.Add($resolved)
    }
}

$candidates = [System.Collections.Generic.List[string]]::new()

Add-Candidate -Candidates $candidates -PathValue $env:JAVA_HOME

try {
    $javaCommands = & where.exe java 2>$null
    foreach ($javaCommand in $javaCommands) {
        $javaBinDir = Split-Path $javaCommand -Parent
        $javaHome = Split-Path $javaBinDir -Parent
        Add-Candidate -Candidates $candidates -PathValue $javaHome
    }
} catch {
}

foreach ($pattern in @(
    "C:\Program Files\Java\jdk-*",
    "C:\Program Files\Eclipse Adoptium\jdk-*",
    "C:\Program Files\Microsoft\jdk-*"
)) {
    foreach ($item in Get-ChildItem -Path $pattern -Directory -ErrorAction SilentlyContinue) {
        Add-Candidate -Candidates $candidates -PathValue $item.FullName
    }
}

$selectedJavaHome = $null
$selectedJavaVersion = -1

foreach ($candidate in $candidates) {
    $majorVersion = Get-JavaMajorVersion -JavaHome $candidate
    if ($majorVersion -ge 17 -and $majorVersion -gt $selectedJavaVersion) {
        $selectedJavaHome = $candidate
        $selectedJavaVersion = $majorVersion
    }
}

if ($selectedJavaHome) {
    Write-Output $selectedJavaHome
}
