FROM eclipse-temurin:8-jdk-jammy

ENV DEBIAN_FRONTEND=noninteractive

# 安装 MySQL 8 服务端
RUN apt-get update && \
    apt-get install -y --no-install-recommends mysql-server && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# 后端应用（Spring Boot，context-path=/api）
COPY app.jar /app/app.jar

# 数据库演示数据（首次启动导入）
COPY finance_dump.sql /app/finance_dump.sql

# 财报 PDF：数据库中存有硬编码的 Windows 路径，
# 这里在 Linux 下创建同名目录（首段目录名 "D:"），实现零代码兼容
COPY uploads-reports/ "/app/D:/财报/CaiZhiTong/finance/backend/uploads/reports/"

COPY start.sh /app/start.sh
RUN chmod +x /app/start.sh

EXPOSE 7860

CMD ["/app/start.sh"]
