# 将 mysqldump 输出转换为 H2 (MODE=MySQL) 兼容 SQL
# 用法: powershell -File convert_dump_to_h2.ps1 <input.sql> <output.sql>
param(
    [Parameter(Mandatory=$true)][string]$InputFile,
    [Parameter(Mandatory=$true)][string]$OutputFile
)

$lines = [System.IO.File]::ReadAllLines($InputFile, [System.Text.Encoding]::UTF8)
$out = New-Object System.Collections.Generic.List[string]

foreach ($line in $lines) {
    $t = $line.TrimStart()

    # 跳过：MySQL条件注释、SET/LOCK、建库/USE、注释行
    if ($t -match '^/\*!' -or
        $t -match '^SET ' -or
        $t -match '^LOCK TABLES' -or
        $t -match '^UNLOCK TABLES' -or
        $t -match '^CREATE DATABASE' -or
        $t -match '^USE ' -or
        $t -match '^--' -or
        $t -eq '') { continue }

    # 表尾选项: ) ENGINE=InnoDB ... COMMENT='...';  ->  );
    # 若上一行以逗号结尾（KEY行被跳过导致），去掉上一行尾逗号
    if ($line -match '^\) ENGINE=.*;\s*$') {
        if ($out.Count -gt 0 -and $out[$out.Count-1] -match ',\s*$') {
            $out[$out.Count-1] = $out[$out.Count-1] -replace ',\s*$', ''
        }
        $out.Add(');')
        continue
    }

    # KEY `idx` (...) 行 -> 跳过（普通索引不影响正确性）
    if ($t -match '^KEY `') { continue }

    # UNIQUE KEY `name` (...) -> UNIQUE (...)
    $line = $line -replace 'UNIQUE KEY `[^`]+` (\([^)]*\))', 'UNIQUE $1'

    # CONSTRAINT xxx FOREIGN KEY ... -> 跳过（外键不导入）
    if ($t -match '^CONSTRAINT ') { continue }

    # 列定义清理: CHARACTER SET / COLLATE
    $line = $line -replace ' CHARACTER SET \S+', ''
    $line = $line -replace ' COLLATE \S+', ''

    # ON UPDATE CURRENT_TIMESTAMP -> 移除（H2不支持该子句）
    $line = $line -replace ' ON UPDATE CURRENT_TIMESTAMP', ''

    # 行内 MySQL 条件注释残留在数据中: /*!...*/ 保留原样（数据里一般没有）

    $out.Add($line)
}

[System.IO.File]::WriteAllLines($OutputFile, $out, (New-Object System.Text.UTF8Encoding($false)))
Write-Output "CONVERTED: $OutputFile ($($out.Count) lines)"
