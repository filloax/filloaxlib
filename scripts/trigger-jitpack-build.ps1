param (
    [string]$version
)

if ([string]::IsNullOrWhiteSpace($version)) {
    Write-Error "Version parameter cannot be blank. Please provide a valid version (for example: 0.30.0-1.21)."
    exit 1
}

# Define URLs
$commonUrl = "https://jitpack.io/com/github/filloax/filloaxlib/v$version-common/filloaxlib-v$version-common.pom"
$neoforgedUrl = "https://jitpack.io/com/github/filloax/filloaxlib/v$version-neoforged/filloaxlib-v$version-neoforged.pom"
$fabricUrl = "https://jitpack.io/com/github/filloax/filloaxlib/v$version-fabric/filloaxlib-v$version-fabric.pom"

# Start async curl calls
Write-Host "Starting common build to $commonUrl"
Invoke-RestMethod -Uri $commonUrl &

# Write-Host "Starting neoforged build"
# Invoke-RestMethod -Uri $neoforgedUrl &

# Write-Host "Starting fabric build"
# Invoke-RestMethod -Uri $fabricUrl &