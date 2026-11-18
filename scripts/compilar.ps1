$ErrorActionPreference = 'Stop'
$SapeRoot = Split-Path -Parent $PSScriptRoot
Push-Location $SapeRoot
try {
    $SapeJavac = 'javac'
    $SapeJar = 'jar'
    if ($env:JAVA_HOME) {
        $SapeJavac = Join-Path $env:JAVA_HOME 'bin\javac.exe'
        $SapeJar = Join-Path $env:JAVA_HOME 'bin\jar.exe'
    }
    New-Item -ItemType Directory -Force -Path 'build\classes', 'bin' | Out-Null
    Remove-Item 'build\classes\*' -Recurse -Force -ErrorAction SilentlyContinue
    $SapeFontes = Get-ChildItem 'src' -Recurse -Filter '*.java' | ForEach-Object { '"' + $_.FullName.Replace('\', '/') + '"' }
    [IO.File]::WriteAllLines((Join-Path $SapeRoot 'build\fontes.txt'), [string[]]$SapeFontes, (New-Object System.Text.UTF8Encoding($false)))
    & $SapeJavac '-J-Dfile.encoding=UTF-8' '-encoding' 'UTF-8' '--release' '17' '--module-path' 'lib\windows' '--add-modules' 'javafx.controls' '-d' 'build\classes' '@build\fontes.txt'
    if ($LASTEXITCODE -ne 0) { throw 'A compilacao falhou. Confira o JDK 17 ou superior e as mensagens acima.' }
    Copy-Item 'resources\*' 'build\classes' -Recurse -Force
    & $SapeJar '--create' '--file' 'bin\sape.jar' '--main-class' 'apresentacao.Launcher' '-C' 'build\classes' '.'
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao gerar bin\sape.jar.' }
    Write-Host 'Projeto compilado. Execute iniciar.bat.'
} catch {
    Write-Error $_
    exit 1
} finally {
    Pop-Location
}
