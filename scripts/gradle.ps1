param([string[]]$Tasks = @(':app:testDebugUnitTest', ':app:assembleDebug'))
$ErrorActionPreference = 'Stop'
$project = Join-Path (Split-Path $PSScriptRoot -Parent) 'Vita_Project'
$socketDir = Join-Path $project 'app/build/disabled-unix-sockets'
if (Test-Path -LiteralPath $socketDir) { throw "Socket fallback path must not exist: $socketDir" }
$previousOptions = $env:JAVA_TOOL_OPTIONS
$previousJavaHome = $env:JAVA_HOME
try {
    # A failed Unix socket bind makes the JDK use its working TCP pipe fallback.
    $env:JAVA_TOOL_OPTIONS = "$previousOptions `"-Djdk.net.unixdomain.tmpdir=$socketDir`""
    if (!$env:JAVA_HOME) { $env:JAVA_HOME = Join-Path $env:ProgramFiles 'Android/Android Studio/jbr' }
    Push-Location $project
    try {
        & .\gradlew.bat @Tasks --no-daemon --console=plain
        $result = $LASTEXITCODE
    } finally { Pop-Location }
} finally {
    $env:JAVA_TOOL_OPTIONS = $previousOptions
    $env:JAVA_HOME = $previousJavaHome
}
exit $result
