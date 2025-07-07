# CNMoney MySQL配置说明

## 问题修复

已修复MySQL数据库连接时的SQL语法错误问题：

### 修复内容

1. **索引创建语法修复**
   - MySQL不支持 `CREATE INDEX IF NOT EXISTS` 语法
   - 改为先检查索引是否存在，再创建索引
   - 使用 `information_schema.statistics` 表查询索引状态

2. **表结构优化**
   - MySQL使用 `INT PRIMARY KEY AUTO_INCREMENT` 语法
   - SQLite使用 `INTEGER PRIMARY KEY AUTOINCREMENT` 语法
   - 分别为两种数据库类型定义不同的表结构

3. **数据类型枚举**
   - 添加 `DatabaseType` 枚举类型
   - 提供类型安全的数据库类型判断

## MySQL配置步骤

### 1. 修改config.yml

```yaml
database:
  # 数据库类型改为mysql
  type: 'mysql'
  
  # MySQL设置
  mysql:
    host: 'localhost'        # 数据库服务器地址
    port: 3306              # 数据库端口
    database: 'cnmoney'     # 数据库名称
    username: 'your_username'  # 数据库用户名
    password: 'your_password'  # 数据库密码
    ssl: false              # 是否启用SSL连接
```

### 2. 创建数据库

在MySQL中执行以下SQL命令：

```sql
CREATE DATABASE cnmoney CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 创建用户（可选）

```sql
CREATE USER 'cnmoney'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON cnmoney.* TO 'cnmoney'@'localhost';
FLUSH PRIVILEGES;
```

### 4. 重启服务器

修改配置后重启Minecraft服务器，插件将自动：
- 连接到MySQL数据库
- 创建必要的表结构
- 创建索引以优化查询性能

## 表结构说明

插件会自动创建以下表：

### player_balances 表
- `id`: 主键，自增
- `player_uuid`: 玩家UUID
- `currency_id`: 货币ID
- `balance`: 余额（精确到8位小数）
- `last_updated`: 最后更新时间戳
- 唯一约束：`(player_uuid, currency_id)`

### transactions 表
- `id`: 主键，自增
- `transaction_type`: 交易类型
- `from_player`: 发送方玩家UUID
- `to_player`: 接收方玩家UUID
- `currency_id`: 货币ID
- `amount`: 交易金额
- `description`: 交易描述
- `timestamp`: 交易时间戳

## 性能优化

插件会自动创建以下索引：
- `idx_player_uuid`: 优化按玩家查询
- `idx_currency_id`: 优化按货币查询
- `idx_transaction_timestamp`: 优化按时间查询交易记录

## 故障排除

如果仍然遇到连接问题，请检查：

1. **网络连接**: 确保Minecraft服务器能访问MySQL服务器
2. **防火墙设置**: 确保MySQL端口（默认3306）未被阻止
3. **用户权限**: 确保数据库用户有足够的权限
4. **字符集**: 建议使用utf8mb4字符集支持完整的Unicode
5. **时区设置**: 确保MySQL服务器时区设置正确

## 日志查看

如果出现问题，请查看服务器日志中的详细错误信息：
- 插件会记录数据库连接状态
- 表创建过程会有详细日志
- 索引创建状态会被记录

## 连接超时问题修复

### 问题描述
MySQL连接在空闲一段时间后会自动断开，导致`Communications link failure`错误。

### 修复内容

1. **连接字符串优化**
   ```java
   jdbc:mysql://host:port/database?useSSL=false&serverTimezone=UTC&autoReconnect=true&failOverReadOnly=false&maxReconnects=3&initialTimeout=2&useUnicode=true&characterEncoding=UTF-8
   ```

2. **自动重连机制**
   - 每次数据库操作前检查连接有效性
   - 连接断开时自动尝试重新连接
   - 智能错误检测和处理

3. **连接参数说明**
   - `autoReconnect=true`: 启用自动重连
   - `failOverReadOnly=false`: 重连后保持读写权限
   - `maxReconnects=3`: 最大重连尝试次数
   - `initialTimeout=2`: 初始超时时间（秒）
   - `useUnicode=true&characterEncoding=UTF-8`: 支持中文字符

### 新增功能

1. **连接健康检查**
   ```java
   public boolean isConnectionValid() {
       try {
           return connection != null && !connection.isClosed() && connection.isValid(5);
       } catch (SQLException e) {
           return false;
       }
   }
   ```

2. **智能重连机制**
   ```java
   private boolean ensureConnection() {
       if (isConnectionValid()) {
           return true;
       }

       plugin.getLogger().warning("数据库连接无效，尝试重新连接...");
       reconnect();
       return isConnectionValid();
   }
   ```

3. **错误恢复处理**
   - 检测到连接错误时自动重连
   - 详细的错误日志记录
   - 优雅的错误处理，避免插件崩溃

## 配置建议

### MySQL服务器配置
在MySQL配置文件（my.cnf或my.ini）中添加：

```ini
[mysqld]
# 连接超时设置
wait_timeout = 28800
interactive_timeout = 28800
max_connections = 200

# 字符集设置
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci

# 性能优化
innodb_buffer_pool_size = 256M
query_cache_size = 64M
```

### 插件配置优化
```yaml
database:
  type: 'mysql'
  mysql:
    host: 'localhost'
    port: 3306
    database: 'cnmoney'
    username: 'cnmoney_user'
    password: 'secure_password'
    ssl: false

# 性能设置
performance:
  cache-size: 1000
  cache-expire-minutes: 30
  async-processing: true

# 日志设置
logging:
  log-transactions: true
  log-retention-days: 30
```

## 版本兼容性

- 支持MySQL 5.7+
- 支持MariaDB 10.2+
- 推荐使用MySQL 8.0+以获得最佳性能
- 完全兼容Paper 1.21.3服务器

## 测试连接

插件启动时会显示连接状态：
```
[INFO] MySQL数据库连接成功: localhost:3306/cnmoney
[INFO] 数据库表创建完成。
[INFO] 创建索引: idx_player_uuid
[INFO] 创建索引: idx_currency_id
[INFO] 创建索引: idx_transaction_timestamp
```

如果出现连接问题，插件会自动尝试重连：
```
[WARNING] 数据库连接无效，尝试重新连接...
[INFO] 检测到连接断开，尝试重新连接...
[INFO] MySQL数据库连接成功: localhost:3306/cnmoney
```
