#!/bin/bash
set -e
cd /app

# 首次启动：从 fat jar 中提取 H2 工具并导入演示数据
if [ ! -f ./data/finance.mv.db ]; then
  mkdir -p data
  unzip -o -q app.jar "BOOT-INF/lib/h2-*.jar" -d /tmp/h2x
  java -cp /tmp/h2x/BOOT-INF/lib/h2-*.jar org.h2.tools.RunScript \
    -url "jdbc:h2:file:/app/data/finance;MODE=MySQL;NON_KEYWORDS=USER" \
    -user sa -script /app/finance_h2.sql
  echo "H2 database initialized."
fi

# Render 通过 PORT 环境变量指定监听端口（默认10000）
export SERVER_PORT=${PORT:-10000}
exec java -Xmx350m -Dfile.encoding=UTF-8 -jar /app/app.jar \
  --spring.profiles.active=h2 \
  --spring.datasource.url="jdbc:h2:file:/app/data/finance;MODE=MySQL;NON_KEYWORDS=USER"
