param(
  [ValidateSet("api", "ui", "all")]
  [string]$TEST_PROFILE = "api"
)

$IMAGE_NAME = "nbank-tests"
$TIMESTAMP = Get-Date -Format "yyyyMMdd_HHmm"
$BASE_OUTPUT_DIR = ".\test-output\$TIMESTAMP"

$APIBASEURL = "http://host.docker.internal:5000/"
$UIBASEURL = "http://host.docker.internal:3000"
$UI_REMOTE = "http://host.docker.internal:4444/wd/hub"

function Run-Profile($ProfileName) {
  $PROFILE_OUTPUT_DIR = "$BASE_OUTPUT_DIR\$ProfileName"

  New-Item -ItemType Directory -Force -Path "$PROFILE_OUTPUT_DIR\logs" | Out-Null
  New-Item -ItemType Directory -Force -Path "$PROFILE_OUTPUT_DIR\results" | Out-Null
  New-Item -ItemType Directory -Force -Path "$PROFILE_OUTPUT_DIR\report" | Out-Null

  Write-Host ">>> Running tests with profile: $ProfileName"

  docker run --rm `
      -v "${PWD}\test-output\$TIMESTAMP\$ProfileName\logs:/app/logs" `
      -v "${PWD}\test-output\$TIMESTAMP\$ProfileName\results:/app/target/surefire-reports" `
      -v "${PWD}\test-output\$TIMESTAMP\$ProfileName\report:/app/target/site" `
      -e TEST_PROFILE=$ProfileName `
      -e APIBASEURL=$APIBASEURL `
      -e UIBASEURL=$UIBASEURL `
      -e UI_REMOTE=$UI_REMOTE `
      $IMAGE_NAME

  Write-Host ">>> Profile finished: $ProfileName"
  Write-Host "Log file: $PROFILE_OUTPUT_DIR\logs\run.log"
  Write-Host "Results: $PROFILE_OUTPUT_DIR\results"
  Write-Host "Report: $PROFILE_OUTPUT_DIR\report"
  Write-Host ""
}

Write-Host ">>> Building docker image"
docker build -t $IMAGE_NAME .

if ($TEST_PROFILE -eq "all") {
  Run-Profile "api"
  Run-Profile "ui"
}
else {
  Run-Profile $TEST_PROFILE
}

Write-Host ">>> All requested runs finished"