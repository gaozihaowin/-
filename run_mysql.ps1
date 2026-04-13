$mysqlArgs = @(
    '-h', '47.120.31.133',
    '-P', '3306',
    '-u', 'root',
    '-pDailyChineseCultureCODE123.',
    'camp_system',
    '-e', "source d:\git\daily-chinese-studies\insert_certs.sql"
)
& mysql @mysqlArgs
if ($LASTEXITCODE -eq 0) { Write-Host "Certificate data inserted successfully" } else { Write-Host "Failed to insert data, exit code: $LASTEXITCODE" }