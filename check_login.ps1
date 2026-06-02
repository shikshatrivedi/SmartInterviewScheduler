$response = Invoke-WebRequest -Uri "http://localhost:8080/auth/login" -Method POST -Body @{email="admin@scheduler.com"; password="Admin@123"} -SessionVariable session -MaximumRedirection 0 -ErrorAction SilentlyContinue
Write-Output "Status: $($response.StatusCode)"
Write-Output "Location: $($response.Headers.Location)"

$dash = Invoke-WebRequest -Uri "http://localhost:8080$($response.Headers.Location)" -WebSession $session -ErrorAction SilentlyContinue
Write-Output "Dashboard Title: $(if ($dash.Content -match '<title>(.*?)</title>') { $matches[1] })"
