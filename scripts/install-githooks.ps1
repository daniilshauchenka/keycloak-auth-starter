$ErrorActionPreference = "Stop"

git config core.hooksPath .githooks
Write-Host "Configured git hooks path: .githooks"

if (Get-Command gitleaks -ErrorAction SilentlyContinue) {
    Write-Host "gitleaks found."
} else {
    Write-Warning "gitleaks is not installed or not in PATH."
    Write-Host "Install: https://github.com/gitleaks/gitleaks"
}

