#Requires -Version 5.1
<#
.SYNOPSIS
  Pull from GitHub branch, build frontend dist, commit and push to GitLab

.DESCRIPTION
  - Remote A: origin -> https://github.com/AlphaKitty/docs.git
  - Remote B: main   -> https://gitlab.goertek.com/barlin.zhang/expert-link.git
  - Backup expert-link-backend/.../application.yml before pull, then restore it
  - Run npm ci/npm install and npm run build in expert-link-frontend

.PARAMETER Branch
  Branch name, default: dev

.PARAMETER GithubRemote
  GitHub remote name, default: origin

.PARAMETER GitlabRemote
  GitLab remote name, default: main

.PARAMETER CommitMessage
  Commit message

.PARAMETER SkipPull
  Skip fetch/pull from GitHub

.PARAMETER SkipBuild
  Skip npm build

.PARAMETER SkipPush
  Skip push to GitLab

.PARAMETER AllowDirty
  Continue even if there are local changes except dist/application.yml checks
#>
param(
    [string]$Branch = "dev",
    [string]$GithubRemote = "origin",
    [string]$GitlabRemote = "main",
    [string]$CommitMessage = "chore(gitlab): sync github dev and build dist",
    [switch]$SkipPull,
    [switch]$SkipBuild,
    [switch]$SkipPush,
    [switch]$AllowDirty
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# Support running from scripts/ or repo root: walk up to find .git
if ($PSScriptRoot) {
    $cursor = $PSScriptRoot
    while ($cursor) {
        if (Test-Path -LiteralPath (Join-Path $cursor ".git")) {
            Set-Location -LiteralPath $cursor
            break
        }
        $parent = Split-Path -Parent $cursor
        if (-not $parent -or ($parent -eq $cursor)) { break }
        $cursor = $parent
    }
}

function Test-GitRepo {
    git rev-parse --git-dir 2>$null | Out-Null
    if ($LASTEXITCODE -ne 0) { throw "Current directory is not a Git repository" }
}

function Get-RepoRoot {
    (git rev-parse --show-toplevel).Trim()
}

function Test-RemoteExists {
    param([string]$Name)
    $remotes = git remote
    if ($remotes -notcontains $Name) {
        throw "Remote '$Name' not found. Check with: git remote -v"
    }
}

function Get-MeaningfulDirtyLines {
    git -c core.quotepath=false status --porcelain | Where-Object {
        $line = $_
        if ($line.Length -lt 4) { return $true }
        $path = $line.Substring(3).Trim('"')
        $norm = $path -replace "\\", "/"
        if ($norm -match '(^|/)expert-link-frontend/dist(/|$)') { return $false }
        if ($norm -match 'application\.yml$') { return $false }
        return $true
    }
}

Test-GitRepo
$repoRoot = Get-RepoRoot
Set-Location -LiteralPath $repoRoot
Test-RemoteExists -Name $GithubRemote
Test-RemoteExists -Name $GitlabRemote

$appYmlRel = "expert-link-backend/src/main/resources/application.yml"
$appYmlFull = Join-Path $repoRoot $appYmlRel
$backupDir = Join-Path $env:LOCALAPPDATA "expert-link-sync"
$backupFile = Join-Path $backupDir "application.yml.bak"

if (-not $AllowDirty) {
    $dirty = @(Get-MeaningfulDirtyLines)
    if ($dirty.Count -gt 0) {
        Write-Host "Found local changes (excluding dist/application.yml). Commit or stash first, or use -AllowDirty:" -ForegroundColor Yellow
        $dirty | ForEach-Object { Write-Host "  $_" }
        exit 1
    }
}

if (-not $SkipPull) {
    $ymlBackedUp = $false
    if (Test-Path -LiteralPath $appYmlFull) {
        New-Item -ItemType Directory -Force -Path $backupDir | Out-Null
        Copy-Item -LiteralPath $appYmlFull -Destination $backupFile -Force
        $ymlBackedUp = $true
        Write-Host "Backed up application.yml -> $backupFile" -ForegroundColor Cyan
    }
    else {
        Write-Host "application.yml not found at $appYmlRel, skip backup" -ForegroundColor Yellow
    }

    try {
        Write-Host "git fetch $GithubRemote $Branch ..." -ForegroundColor Cyan
        git fetch $GithubRemote $Branch
        if ($LASTEXITCODE -ne 0) { throw "git fetch failed" }

        git show-ref --verify --quiet "refs/heads/$Branch" 2>$null | Out-Null
        if ($LASTEXITCODE -eq 0) {
            git checkout $Branch
            if ($LASTEXITCODE -ne 0) { throw "git checkout $Branch failed" }
        }
        else {
            Write-Host "Local branch $Branch not found, create from $GithubRemote/$Branch" -ForegroundColor Cyan
            git checkout -b $Branch "$GithubRemote/$Branch"
            if ($LASTEXITCODE -ne 0) { throw "create and checkout branch failed" }
        }

        Write-Host "git pull $GithubRemote $Branch ..." -ForegroundColor Cyan
        git pull $GithubRemote $Branch
        if ($LASTEXITCODE -ne 0) { throw "git pull failed. Resolve conflicts and retry" }
    }
    finally {
        if ($ymlBackedUp -and (Test-Path -LiteralPath $backupFile)) {
            Copy-Item -LiteralPath $backupFile -Destination $appYmlFull -Force
            Write-Host "Restored $appYmlRel from local backup" -ForegroundColor Cyan
        }
    }
}
else {
    Write-Host "Skipped pull (-SkipPull)" -ForegroundColor Yellow
}

$frontend = Join-Path $repoRoot "expert-link-frontend"
if (-not (Test-Path -LiteralPath $frontend)) {
    throw "Directory not found: expert-link-frontend"
}

if (-not $SkipBuild) {
    Push-Location $frontend
    try {
        if (Test-Path -LiteralPath (Join-Path $frontend "package-lock.json")) {
            npm ci
            if ($LASTEXITCODE -ne 0) { throw "npm ci failed" }
        }
        else {
            npm install
            if ($LASTEXITCODE -ne 0) { throw "npm install failed" }
        }
        npm run build
        if ($LASTEXITCODE -ne 0) { throw "npm run build failed" }
    }
    finally {
        Pop-Location
    }
}
else {
    Write-Host "Skipped build (-SkipBuild)" -ForegroundColor Yellow
}

git add --all

git diff --staged --quiet 2>$null | Out-Null
if ($LASTEXITCODE -eq 0) {
    Write-Host "No staged changes, skip commit" -ForegroundColor Yellow
}
else {
    git commit -m $CommitMessage
    if ($LASTEXITCODE -ne 0) { throw "git commit failed" }
}

if (-not $SkipPush) {
    Write-Host "git push $GitlabRemote ${Branch}:$Branch ..." -ForegroundColor Cyan
    git push $GitlabRemote "${Branch}:$Branch"
    if ($LASTEXITCODE -ne 0) { throw "git push failed" }
}
else {
    Write-Host "Skipped push (-SkipPush). Run manually: git push $GitlabRemote ${Branch}:$Branch" -ForegroundColor Yellow
}

Write-Host "Done." -ForegroundColor Green
