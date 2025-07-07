# MySQL连接超时修复测试指南

## 修复内容总结

### 1. 连接字符串优化
- 添加了 `autoReconnect=true` 启用自动重连
- 添加了 `failOverReadOnly=false` 保持读写权限
- 添加了 `maxReconnects=3` 设置最大重连次数
- 添加了 `initialTimeout=2` 设置初始超时时间
- 添加了 `useUnicode=true&characterEncoding=UTF-8` 支持中文

### 2. 智能连接管理
- 每次数据库操作前检查连接有效性
- 连接断开时自动尝试重新连接
- 智能错误检测和恢复处理

### 3. 修复的方法
- `loadPlayerAccount()` - 加载玩家账户数据
- `savePlayerAccount()` - 保存玩家账户数据
- `logTransaction()` - 记录交易日志
- `getTransactions()` - 获取交易记录
- `getTransactionCount()` - 获取交易数量

## 测试步骤

### 1. 部署新版本插件
```bash
# 停止服务器
# 替换插件文件
cp target/CNMoney-1.0.0-SNAPSHOT.jar /path/to/server/plugins/
# 启动服务器
```

### 2. 验证连接成功
查看服务器日志，应该看到：
```
[INFO] MySQL数据库连接成功: localhost:3306/cnmoney
[INFO] 数据库表创建完成。
[INFO] 创建索引: idx_player_uuid
[INFO] 创建索引: idx_currency_id
[INFO] 创建索引: idx_transaction_timestamp
```

### 3. 测试PlaceholderAPI集成
```
# 在游戏中使用命令测试占位符
/papi parse me %cnmoney_balance_gold%
/papi parse me %cnmoney_balance_silver%
```

### 4. 测试长时间连接
1. 启动服务器并确认连接成功
2. 等待2-3分钟（超过MySQL默认超时时间）
3. 执行以下操作测试连接恢复：
   - 使用 `/cm balance` 查看余额
   - 使用 `/cm add <玩家> gold 100` 添加余额
   - 使用 `/cm gui` 打开管理界面
   - 使用PlaceholderAPI占位符

### 5. 监控日志输出
如果连接断开并成功重连，应该看到：
```
[WARNING] 数据库连接无效，尝试重新连接...
[INFO] 检测到连接断开，尝试重新连接...
[INFO] MySQL数据库连接成功: localhost:3306/cnmoney
```

## 预期结果

### 成功指标
1. ✅ 插件启动时成功连接MySQL
2. ✅ PlaceholderAPI占位符正常工作
3. ✅ 长时间空闲后操作仍然正常
4. ✅ 连接断开时自动重连成功
5. ✅ 所有GUI功能正常工作
6. ✅ 交易记录正常保存和查询

### 失败处理
如果仍然出现连接问题：

1. **检查MySQL配置**
   ```sql
   SHOW VARIABLES LIKE 'wait_timeout';
   SHOW VARIABLES LIKE 'interactive_timeout';
   ```

2. **检查网络连接**
   ```bash
   telnet localhost 3306
   ```

3. **检查MySQL用户权限**
   ```sql
   SHOW GRANTS FOR 'cnmoney_user'@'localhost';
   ```

4. **查看详细错误日志**
   - 服务器控制台输出
   - MySQL错误日志
   - 插件调试信息

## 性能优化建议

### MySQL服务器配置
```ini
[mysqld]
# 连接管理
wait_timeout = 28800
interactive_timeout = 28800
max_connections = 200

# 性能优化
innodb_buffer_pool_size = 256M
query_cache_size = 64M
key_buffer_size = 64M

# 字符集
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci
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
    password: 'your_password'
    ssl: false

# 性能设置
performance:
  cache-size: 1000
  cache-expire-minutes: 30
  async-processing: true

# 事务日志
logging:
  log-transactions: true
  log-retention-days: 30
```

## 故障排除

### 常见问题

1. **连接被拒绝**
   - 检查MySQL服务是否运行
   - 检查端口是否正确
   - 检查防火墙设置

2. **认证失败**
   - 检查用户名和密码
   - 检查用户权限
   - 检查主机访问权限

3. **数据库不存在**
   - 创建数据库：`CREATE DATABASE cnmoney CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`
   - 检查数据库名称拼写

4. **字符集问题**
   - 确保数据库使用utf8mb4字符集
   - 检查连接字符串中的编码设置

## 联系支持

如果问题仍然存在，请提供：
1. 完整的错误日志
2. MySQL版本信息
3. 服务器配置信息
4. 插件配置文件内容

修复已经实现了完整的连接管理和自动恢复机制，应该能够解决MySQL连接超时问题。
