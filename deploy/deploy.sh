#!/bin/bash
# ============================================================
# 一键部署脚本 - 校园二手交易平台 (CentOS 7.6)
# 使用方式:
#   1. 将整个 campusTradingPlatform 项目上传至服务器
#   2. cd 到 deploy 目录
#   3. sudo bash deploy.sh
# ============================================================
set -e

# 颜色输出
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo_info()  { echo -e "${GREEN}[INFO]${NC} $1"; }
echo_warn()  { echo -e "${YELLOW}[WARN]${NC} $1"; }
echo_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# 安装目录
INSTALL_DIR="/opt/campus-trading"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

# ============ 1. 检查 root 权限 ============
if [ "$EUID" -ne 0 ]; then
  echo_error "请使用 root 或 sudo 运行此脚本"
  exit 1
fi

# ============ 2. 安装 JDK 17 ============
echo_info "检查 JDK 17..."
if ! command -v java &> /dev/null || ! java -version 2>&1 | grep -q "17"; then
  echo_info "安装 OpenJDK 17..."
  yum install -y java-17-openjdk java-17-openjdk-devel
else
  echo_info "JDK 17 已安装"
fi

# ============ 3. 安装 MySQL 8.0 ============
echo_info "检查 MySQL..."
if ! command -v mysql &> /dev/null; then
  echo_info "安装 MySQL 8.0..."
  # 添加 MySQL 官方 yum 源
  yum localinstall -y https://dev.mysql.com/get/mysql80-community-release-el7-11.noarch.rpm
  yum install -y mysql-community-server
  systemctl enable mysqld
  systemctl start mysqld
  # 获取临时密码
  TEMP_PWD=$(grep 'temporary password' /var/log/mysqld.log | awk '{print $NF}')
  echo_warn "MySQL 临时密码: $TEMP_PWD"
  echo_warn "请登录 MySQL 后修改密码并创建数据库,执行以下命令:"
  echo "  mysql -uroot -p\"$TEMP_PWD\""
  echo "  ALTER USER 'root'@'localhost' IDENTIFIED BY 'YourStrong@Pass123';"
  echo "  CREATE DATABASE campus_trading DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
  echo "  use campus_trading; source $PROJECT_DIR/database/schema.sql;"
  echo "  source $PROJECT_DIR/database/migration_v1.1.sql;"
  echo_warn "完成后请修改 application-prod.yml 中的数据库密码,然后重新运行此脚本的后端部分"
  echo_warn "也可在脚本参数中加 --skip-db 跳过 MySQL 安装"
else
  echo_info "MySQL 已安装"
fi

# ============ 4. 安装 Nginx ============
echo_info "检查 Nginx..."
if ! command -v nginx &> /dev/null; then
  echo_info "安装 Nginx..."
  yum install -y epel-release
  yum install -y nginx
  systemctl enable nginx
else
  echo_info "Nginx 已安装"
fi

# ============ 5. 创建系统用户 ============
echo_info "创建 campus 用户..."
if ! id "campus" &>/dev/null; then
  useradd -r -s /sbin/nologin campus
fi

# ============ 6. 创建目录 ============
echo_info "创建安装目录..."
mkdir -p $INSTALL_DIR/{backend,frontend,uploads,logs}
chown -R campus:campus $INSTALL_DIR

# ============ 7. 部署后端 ============
echo_info "部署后端 jar 包..."
JAR_FILE="$PROJECT_DIR/backend/target/campus-trading-1.0.0.jar"
if [ ! -f "$JAR_FILE" ]; then
  echo_error "后端 jar 包不存在: $JAR_FILE"
  echo_warn "请先在开发机执行: cd backend && mvn clean package -DskipTests"
  exit 1
fi
cp -f "$JAR_FILE" "$INSTALL_DIR/backend/"
chown campus:campus "$INSTALL_DIR/backend/campus-trading-1.0.0.jar"

# ============ 8. 部署前端 ============
echo_info "部署前端静态资源..."
if [ ! -d "$PROJECT_DIR/frontend/dist" ]; then
  echo_error "前端 dist 目录不存在"
  echo_warn "请先在开发机执行: cd frontend && npm install && npm run build"
  exit 1
fi
rm -rf "$INSTALL_DIR/frontend/dist"
cp -rf "$PROJECT_DIR/frontend/dist" "$INSTALL_DIR/frontend/"
chown -R campus:campus "$INSTALL_DIR/frontend"

# ============ 9. 安装 systemd 服务 ============
echo_info "安装 systemd 服务..."
cp -f "$SCRIPT_DIR/campus-trading.service" /etc/systemd/system/
systemctl daemon-reload

# ============ 10. 配置 Nginx ============
echo_info "配置 Nginx..."
cp -f "$SCRIPT_DIR/nginx/campus-trading.conf" /etc/nginx/conf.d/
# 移除默认配置避免冲突
[ -f /etc/nginx/conf.d/default.conf ] && mv /etc/nginx/conf.d/default.conf /etc/nginx/conf.d/default.conf.bak
nginx -t && systemctl reload nginx

# ============ 11. 防火墙 ============
echo_info "配置防火墙..."
if command -v firewall-cmd &> /dev/null; then
  firewall-cmd --permanent --add-service=http
  firewall-cmd --permanent --add-service=https
  firewall-cmd --permanent --add-port=8080/tcp
  firewall-cmd --reload
  echo_info "防火墙已开放 80/443/8080"
else
  echo_warn "firewalld 未运行,请手动开放端口"
fi

# ============ 12. 启动后端 ============
echo_info "启动后端服务..."
systemctl restart campus-trading
systemctl enable campus-trading

# ============ 13. 检查状态 ============
sleep 5
if systemctl is-active --quiet campus-trading; then
  echo_info "后端服务启动成功"
else
  echo_error "后端服务启动失败,查看日志: journalctl -u campus-trading -f"
  exit 1
fi

echo ""
echo_info "================ 部署完成 ================"
echo_info "访问地址: http://$(curl -s ifconfig.me 2>/dev/null || echo '服务器IP')"
echo_info "后端日志: tail -f $INSTALL_DIR/logs/stdout.log"
echo_info "服务管理: systemctl {start|stop|restart|status} campus-trading"
echo_info "Nginx 日志: tail -f /var/log/nginx/campus-trading.access.log"
echo_warn "请确保 application-prod.yml 中数据库密码、邮箱配置已正确填写"
echo_warn "首次部署需手动导入数据库: $PROJECT_DIR/database/schema.sql"
echo "==========================================="
