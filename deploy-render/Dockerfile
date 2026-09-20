FROM eclipse-temurin:8-jdk-jammy

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y --no-install-recommends unzip && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY app.jar /app/app.jar
COPY finance_h2.sql /app/finance_h2.sql

# 财报PDF：数据库存有原 Windows 绝对路径（D:/财报/...），
# Linux 下解析为相对 cwd 的路径 /app/D:/... ，放置同名目录实现零代码兼容
COPY uploads-reports/ "/app/D:/财报/CaiZhiTong/finance/backend/uploads/reports/"

COPY start.sh /app/start.sh
RUN chmod +x /app/start.sh

EXPOSE 10000

CMD ["/app/start.sh"]
