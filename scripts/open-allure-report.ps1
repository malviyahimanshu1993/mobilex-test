param(
  [string]$ReportDir = "target/site/allure-maven-plugin",
  [int]$Port = 0
)

$root = (Resolve-Path (Join-Path $PSScriptRoot ".." )).Path
$dir = Join-Path $root $ReportDir
if (!(Test-Path $dir)) {
  Write-Error "Allure report directory not found: $dir"
  exit 1
}

# Choose a free port if not provided
if ($Port -eq 0) {
  $listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback, 0)
  $listener.Start()
  $Port = ($listener.LocalEndpoint).Port
  $listener.Stop()
}

$prefix = "http://127.0.0.1:$Port/"
$listener = [System.Net.HttpListener]::new()
$listener.Prefixes.Add($prefix)
$listener.Start()

Write-Host "Serving Allure report from: $dir"
Write-Host "Open in browser: ${prefix}index.html"
Start-Process "${prefix}index.html"

try {
  while ($listener.IsListening) {
    $ctx = $listener.GetContext()
    try {
      $rawUrl = $ctx.Request.RawUrl
      if ([string]::IsNullOrWhiteSpace($rawUrl) -or $rawUrl -eq "/") { $rawUrl = "/index.html" }

      # Strip query string if present
      $pathOnly = $rawUrl.Split('?')[0]

      # prevent path traversal
      $rel = $pathOnly.TrimStart('/').Replace('/', [IO.Path]::DirectorySeparatorChar)
      $path = Join-Path $dir $rel
      $full = [IO.Path]::GetFullPath($path)
      $base = [IO.Path]::GetFullPath($dir)
      if (!$full.StartsWith($base, [System.StringComparison]::OrdinalIgnoreCase)) {
        $ctx.Response.StatusCode = 400
        $ctx.Response.Close()
        continue
      }

      if (!(Test-Path $full) -or (Get-Item $full).PSIsContainer) {
        $ctx.Response.StatusCode = 404
        $ctx.Response.Close()
        continue
      }

      # Important for SPA loading JSON/assets correctly
      $ctx.Response.Headers["Access-Control-Allow-Origin"] = "*"
      $ctx.Response.Headers["Access-Control-Allow-Methods"] = "GET, HEAD, OPTIONS"
      $ctx.Response.Headers["Access-Control-Allow-Headers"] = "*"
      $ctx.Response.Headers["Cache-Control"] = "no-cache, no-store, must-revalidate"
      $ctx.Response.Headers["Pragma"] = "no-cache"
      $ctx.Response.Headers["Expires"] = "0"

      $bytes = [IO.File]::ReadAllBytes($full)
      $ext = [IO.Path]::GetExtension($full).ToLowerInvariant()
      switch ($ext) {
        ".html" { $ctx.Response.ContentType = "text/html; charset=utf-8" }
        ".js"   { $ctx.Response.ContentType = "application/javascript; charset=utf-8" }
        ".mjs"  { $ctx.Response.ContentType = "application/javascript; charset=utf-8" }
        ".css"  { $ctx.Response.ContentType = "text/css; charset=utf-8" }
        ".json" { $ctx.Response.ContentType = "application/json; charset=utf-8" }
        ".csv"  { $ctx.Response.ContentType = "text/csv; charset=utf-8" }
        ".svg"  { $ctx.Response.ContentType = "image/svg+xml" }
        ".png"  { $ctx.Response.ContentType = "image/png" }
        ".jpg"  { $ctx.Response.ContentType = "image/jpeg" }
        ".jpeg" { $ctx.Response.ContentType = "image/jpeg" }
        ".gif"  { $ctx.Response.ContentType = "image/gif" }
        ".woff" { $ctx.Response.ContentType = "font/woff" }
        ".woff2" { $ctx.Response.ContentType = "font/woff2" }
        ".ttf"  { $ctx.Response.ContentType = "font/ttf" }
        ".wasm" { $ctx.Response.ContentType = "application/wasm" }
        ".map"  { $ctx.Response.ContentType = "application/json; charset=utf-8" }
        ".ico"  { $ctx.Response.ContentType = "image/x-icon" }
        default  { $ctx.Response.ContentType = "application/octet-stream" }
      }

      $ctx.Response.StatusCode = 200
      $ctx.Response.ContentLength64 = $bytes.Length
      $ctx.Response.OutputStream.Write($bytes, 0, $bytes.Length)
      $ctx.Response.OutputStream.Close()
    } catch {
      try { $ctx.Response.StatusCode = 500; $ctx.Response.Close() } catch {}
    }
  }
} finally {
  $listener.Stop()
  $listener.Close()
}
