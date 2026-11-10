param(
	[string]$Server,
	[string]$JavaHome,
	[switch]$CheckOnly,
	[string]$ConfigFile = (Join-Path $env:USERPROFILE '.gvs-terceirizacoes\launcher.json')
)

$ErrorActionPreference = 'Stop'

function ConvertTo-GvsApiUrl {
	param([string]$Address)
	$value = ([string]$Address).Trim()
	if (-not $value) { throw 'Informe o IP ou o nome do computador servidor.' }
	$hasScheme = $value.Contains('://')
	if (-not $hasScheme) { $value = 'http://' + $value }
	$uri = $null
	if (-not [Uri]::TryCreate($value, [UriKind]::Absolute, [ref]$uri) -or
		$uri.Scheme -notin @('http', 'https') -or -not $uri.Host -or
		[Uri]::CheckHostName($uri.DnsSafeHost) -eq [UriHostNameType]::Unknown -or
		$uri.UserInfo -or $uri.Query -or $uri.Fragment -or
		$uri.AbsolutePath -notin @('/', '/api', '/api/')) {
		throw 'Endereço inválido. Use, por exemplo, 192.168.1.20 ou 192.168.1.20:8080. Não inclua senha ou outros caminhos.'
	}
	$builder = [UriBuilder]::new($uri)
	$authority = [regex]::Match($value, '^https?://([^/]+)', 'IgnoreCase').Groups[1].Value
	if (-not $hasScheme -and $authority -notmatch ':\d+$') { $builder.Port = 8080 }
	if ($builder.Port -lt 1 -or $builder.Port -gt 65535) { throw 'A porta deve estar entre 1 e 65535.' }
	$builder.Path = '/api'
	return $builder.Uri.AbsoluteUri.TrimEnd('/')
}

function Get-GvsJavaHome {
	param([string]$Folder)
	if (-not $Folder -or -not (Test-Path -LiteralPath $Folder -PathType Container)) { return $null }
	$folderPath = (Resolve-Path -LiteralPath $Folder).Path
	$java = Join-Path $folderPath 'bin\java.exe'
	if (-not (Test-Path -LiteralPath $java -PathType Leaf) -or
		-not (Test-Path -LiteralPath (Join-Path $folderPath 'bin\javac.exe') -PathType Leaf)) { return $null }
	$info = [Diagnostics.ProcessStartInfo]::new()
	$info.FileName = $java
	$info.Arguments = '--version'
	$info.UseShellExecute = $false
	$info.CreateNoWindow = $true
	$info.RedirectStandardOutput = $true
	$info.RedirectStandardError = $true
	$process = [Diagnostics.Process]::new()
	$process.StartInfo = $info
	try {
		$null = $process.Start()
		$version = $process.StandardOutput.ReadToEnd()
		$null = $process.StandardError.ReadToEnd()
		$process.WaitForExit()
		if ($process.ExitCode -eq 0 -and $version -match '(?m)^(?:openjdk|java) 25(?:[.\s-]|$)') { return $folderPath }
	} catch { return $null }
	finally { $process.Dispose() }
	return $null
}

function Find-GvsJavaHome {
	param([string]$SavedFolder)
	$candidates = @($SavedFolder, $env:JAVA_HOME)
	$javaCommand = Get-Command java.exe -CommandType Application -ErrorAction SilentlyContinue | Select-Object -First 1
	if ($javaCommand) { $candidates += Split-Path (Split-Path $javaCommand.Source -Parent) -Parent }
	foreach ($vendor in @('Java', 'Eclipse Adoptium', 'Microsoft')) {
		$vendorPath = Join-Path $env:ProgramFiles $vendor
		if (Test-Path -LiteralPath $vendorPath -PathType Container) {
			$candidates += @(Get-ChildItem -LiteralPath $vendorPath -Directory -ErrorAction SilentlyContinue |
				Where-Object { $_.Name -like '*25*' } | Select-Object -ExpandProperty FullName)
		}
	}
	foreach ($candidate in ($candidates | Where-Object { $_ } | Select-Object -Unique)) {
		$found = Get-GvsJavaHome -Folder $candidate
		if ($found) { return $found }
	}
	return $null
}

function Test-GvsServer {
	param([string]$ApiUrl)
	$root = [UriBuilder]::new($ApiUrl)
	$root.Path = '/'
	try {
		$reply = Invoke-RestMethod -Uri $root.Uri.AbsoluteUri -Method Get -TimeoutSec 5 -UseBasicParsing
		if ($reply.message -ne 'GVS Terceirizacoes API is running') { throw 'Resposta inesperada.' }
	} catch {
		throw 'Não consegui conectar à API GVS nesse endereço. Confira se o servidor está ligado, o IP, a porta e o firewall da rede.'
	}
}

function Start-GvsDesktop {
	$saved = $null
	if (Test-Path -LiteralPath $ConfigFile -PathType Leaf) {
		try { $saved = Get-Content -LiteralPath $ConfigFile -Raw -Encoding UTF8 | ConvertFrom-Json }
		catch { Write-Host 'A configuração anterior não pôde ser lida. Vamos informar os dados novamente.' }
	}
	$defaultServer = 'localhost:8080'
	if ($saved -and $saved.serverUrl) {
		try { $defaultServer = ConvertTo-GvsApiUrl -Address $saved.serverUrl }
		catch { Write-Host 'O endereço salvo é inválido. Informe o endereço do servidor.' }
	}
	if (-not $Server) {
		if ($CheckOnly) { $Server = $defaultServer }
		else {
			Write-Host ''
			Write-Host 'GVS Terceirizações'
			Write-Host 'Use localhost neste computador servidor, ou o IP do servidor nos outros computadores.'
			$Server = Read-Host "Servidor [$defaultServer] — Enter mantém"
			if (-not $Server.Trim()) { $Server = $defaultServer }
		}
	}
	$apiUrl = ConvertTo-GvsApiUrl -Address $Server
	$resolvedJava = $null
	if ($JavaHome) { $resolvedJava = Get-GvsJavaHome -Folder $JavaHome }
	else { $resolvedJava = Find-GvsJavaHome -SavedFolder $saved.javaHome }
	if (-not $resolvedJava -and -not $CheckOnly) {
		Write-Host 'O GVS precisa de um JDK 25 completo. A versão instalada pode ser diferente.'
		$selectedFolder = Read-Host 'Cole a pasta do JDK 25 (sem o bin), ou Enter para cancelar'
		if ($selectedFolder) { $resolvedJava = Get-GvsJavaHome -Folder $selectedFolder.Trim().Trim('"') }
	}
	if (-not $resolvedJava) { throw 'JDK 25 não encontrado. Instale ou informe um JDK 25 completo e tente novamente.' }
	Write-Host "Conferindo servidor: $apiUrl"
	Test-GvsServer -ApiUrl $apiUrl
	if ($CheckOnly) {
		return [pscustomobject]@{ApiBaseUrl=$apiUrl; JavaHome=$resolvedJava; Ready=$true}
	}
	$settingsDir = Split-Path $ConfigFile -Parent
	try {
		if ($settingsDir) { $null = New-Item -ItemType Directory -Path $settingsDir -Force }
		@{serverUrl=$apiUrl;javaHome=$resolvedJava} | ConvertTo-Json | Set-Content -LiteralPath $ConfigFile -Encoding UTF8
	} catch { Write-Host 'Não consegui guardar a configuração. Você poderá informá-la novamente na próxima abertura.' }
	$oldJavaHome = $env:JAVA_HOME
	$oldPath = $env:Path
	$oldApiUrl = $env:GVS_API_BASEURL
	Push-Location -LiteralPath (Join-Path $PSScriptRoot '..')
	try {
		$env:JAVA_HOME = $resolvedJava
		$env:Path = (Join-Path $resolvedJava 'bin') + ';' + $env:Path
		$env:GVS_API_BASEURL = $apiUrl
		Write-Host 'Abrindo o GVS. A primeira execução baixa dependências e pode demorar.'
		Write-Host 'Mantenha esta janela aberta enquanto estiver usando o sistema.'
		& .\mvnw.cmd javafx:run
		if ($LASTEXITCODE -ne 0) { throw 'A abertura falhou. Confira a mensagem do Maven acima e a conexão à internet.' }
	} finally {
		$env:JAVA_HOME = $oldJavaHome
		$env:Path = $oldPath
		$env:GVS_API_BASEURL = $oldApiUrl
		Pop-Location
	}
}

if ($MyInvocation.InvocationName -ne '.') {
	try { Start-GvsDesktop }
	catch { Write-Host ''; Write-Host $_.Exception.Message -ForegroundColor Red; exit 1 }
}
