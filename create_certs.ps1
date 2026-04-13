Add-Type -AssemblyName System.Drawing

function New-TestImage {
    param([string]$path, [string]$text, [string]$bgColor, [string]$textColor)
    $bmp = New-Object System.Drawing.Bitmap(400, 300)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $brush = New-Object System.Drawing.SolidBrush([System.Drawing.ColorTranslator]::FromHtml($bgColor))
    $font = New-Object System.Drawing.Font('Arial', 16)
    $textBrush = New-Object System.Drawing.SolidBrush([System.Drawing.ColorTranslator]::FromHtml($textColor))
    $g.FillRectangle($brush, 0, 0, 400, 300)
    $sf = New-Object System.Drawing.StringFormat
    $sf.Alignment = 'Center'
    $sf.LineAlignment = 'Center'
    $rect = New-Object System.Drawing.RectangleF(0, 0, 400, 300)
    $g.DrawString($text, $font, $textBrush, $rect, $sf)
    $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose()
    $bmp.Dispose()
}

$uploadDir = "d:\git\daily-chinese-studies\uploads\images"
New-TestImage -path "$uploadDir\cert_001.png" -text "荣誉证书 Certificate 001" -bgColor "#f0e68c" -textColor "#8b0000"
New-TestImage -path "$uploadDir\cert_002.png" -text "学习证书 Certificate 002" -bgColor "#98fb98" -textColor "#006400"
New-TestImage -path "$uploadDir\cert_003.png" -text "结业证书 Certificate 003" -bgColor "#add8e6" -textColor "#00008b"
Write-Host "Test certificate images created successfully"