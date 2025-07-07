package cn.money.manager;

import cn.money.CNMoney;
import cn.money.model.PlayerAccount;
import cn.money.model.Transaction;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * 数据库管理器
 *
 * @author CNMoney Team
 */
public class DatabaseManager {

    /**
     * 数据库类型枚举
     */
    public enum DatabaseType {
        SQLITE, MYSQL
    }

    private final CNMoney plugin;
    private Connection connection;
    private DatabaseType databaseType;
    
    public DatabaseManager(CNMoney plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 初始化数据库连接
     * 
     * @return 是否初始化成功
     */
    public boolean initialize() {
        try {
            String dbTypeString = plugin.getConfigManager().getDatabaseType().toLowerCase();

            switch (dbTypeString) {
                case "sqlite":
                    this.databaseType = DatabaseType.SQLITE;
                    return initializeSQLite();
                case "mysql":
                    this.databaseType = DatabaseType.MYSQL;
                    return initializeMySQL();
                default:
                    plugin.getLogger().severe("不支持的数据库类型: " + dbTypeString);
                    return false;
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "数据库初始化失败！", e);
            return false;
        }
    }
    
    /**
     * 初始化SQLite数据库
     * 
     * @return 是否初始化成功
     */
    private boolean initializeSQLite() {
        try {
            // 确保数据文件夹存在
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            
            String fileName = plugin.getConfigManager().getSQLiteFile();
            File databaseFile = new File(plugin.getDataFolder(), fileName);
            
            String url = "jdbc:sqlite:" + databaseFile.getAbsolutePath();
            connection = DriverManager.getConnection(url);
            
            // 创建表
            createTables();
            
            if (plugin.getConfigManager().isDatabaseOperationLoggingEnabled()) {
                plugin.getLogger().info("SQLite数据库连接成功: " + fileName);
            }
            return true;
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "SQLite数据库连接失败！", e);
            return false;
        }
    }
    
    /**
     * 初始化MySQL数据库
     * 
     * @return 是否初始化成功
     */
    private boolean initializeMySQL() {
        try {
            String host = plugin.getConfigManager().getMySQLHost();
            int port = plugin.getConfigManager().getMySQLPort();
            String database = plugin.getConfigManager().getMySQLDatabase();
            String username = plugin.getConfigManager().getMySQLUsername();
            String password = plugin.getConfigManager().getMySQLPassword();
            boolean ssl = plugin.getConfigManager().isMySQLSSLEnabled();
            
            String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=%s&serverTimezone=UTC&autoReconnect=true&failOverReadOnly=false&maxReconnects=3&initialTimeout=2&useUnicode=true&characterEncoding=UTF-8",
                                     host, port, database, ssl);

            connection = DriverManager.getConnection(url, username, password);
            
            // 创建表
            createTables();
            
            if (plugin.getConfigManager().isDatabaseOperationLoggingEnabled()) {
                plugin.getLogger().info("MySQL数据库连接成功: " + host + ":" + port + "/" + database);
            }
            return true;
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "MySQL数据库连接失败！", e);
            return false;
        }
    }
    
    /**
     * 创建数据库表
     */
    private void createTables() throws SQLException {
        String createBalancesTable;
        String createTransactionsTable;

        if (databaseType == DatabaseType.MYSQL) {
            // MySQL语法
            createBalancesTable = """
                CREATE TABLE IF NOT EXISTS player_balances (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    player_uuid VARCHAR(36) NOT NULL,
                    player_name VARCHAR(16) NOT NULL DEFAULT '',
                    currency_id VARCHAR(50) NOT NULL,
                    balance DECIMAL(20,8) NOT NULL DEFAULT 0,
                    last_updated BIGINT NOT NULL,
                    UNIQUE KEY unique_player_currency (player_uuid, currency_id)
                )
                """;

            createTransactionsTable = """
                CREATE TABLE IF NOT EXISTS transactions (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    transaction_type VARCHAR(20) NOT NULL,
                    from_player VARCHAR(36),
                    to_player VARCHAR(36),
                    currency_id VARCHAR(50) NOT NULL,
                    amount DECIMAL(20,8) NOT NULL,
                    description TEXT,
                    timestamp BIGINT NOT NULL
                )
                """;
        } else {
            // SQLite语法
            createBalancesTable = """
                CREATE TABLE IF NOT EXISTS player_balances (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    player_uuid VARCHAR(36) NOT NULL,
                    player_name VARCHAR(16) NOT NULL DEFAULT '',
                    currency_id VARCHAR(50) NOT NULL,
                    balance DECIMAL(20,8) NOT NULL DEFAULT 0,
                    last_updated BIGINT NOT NULL,
                    UNIQUE(player_uuid, currency_id)
                )
                """;

            createTransactionsTable = """
                CREATE TABLE IF NOT EXISTS transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    transaction_type VARCHAR(20) NOT NULL,
                    from_player VARCHAR(36),
                    to_player VARCHAR(36),
                    currency_id VARCHAR(50) NOT NULL,
                    amount DECIMAL(20,8) NOT NULL,
                    description TEXT,
                    timestamp BIGINT NOT NULL
                )
                """;
        }
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createBalancesTable);
            stmt.execute(createTransactionsTable);

            // 创建索引
            createIndexes(stmt);

            // 升级数据库结构（添加用户名字段）
            upgradeDatabase(stmt);

            if (plugin.getConfigManager().isDatabaseOperationLoggingEnabled()) {
                plugin.getLogger().info("数据库表创建完成。");
            }
        }
    }

    /**
     * 创建数据库索引
     */
    private void createIndexes(Statement stmt) throws SQLException {
        if (databaseType == DatabaseType.MYSQL) {
            // MySQL语法 - 需要检查索引是否存在
            createMySQLIndex(stmt, "idx_player_uuid", "player_balances", "player_uuid");
            createMySQLIndex(stmt, "idx_currency_id", "player_balances", "currency_id");
            createMySQLIndex(stmt, "idx_transaction_timestamp", "transactions", "timestamp");
        } else {
            // SQLite语法 - 支持IF NOT EXISTS
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_player_uuid ON player_balances(player_uuid)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_currency_id ON player_balances(currency_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_transaction_timestamp ON transactions(timestamp)");
        }
    }

    /**
     * 创建MySQL索引（检查是否存在）
     */
    private void createMySQLIndex(Statement stmt, String indexName, String tableName, String columnName) {
        try {
            // 检查索引是否存在
            String checkIndexSQL = "SELECT COUNT(*) FROM information_schema.statistics " +
                                  "WHERE table_schema = DATABASE() AND table_name = ? AND index_name = ?";

            try (PreparedStatement checkStmt = connection.prepareStatement(checkIndexSQL)) {
                checkStmt.setString(1, tableName);
                checkStmt.setString(2, indexName);

                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        // 索引不存在，创建它
                        String createIndexSQL = String.format("CREATE INDEX %s ON %s(%s)",
                                                             indexName, tableName, columnName);
                        stmt.execute(createIndexSQL);
                        if (plugin.getConfigManager().isDatabaseOperationLoggingEnabled()) {
                            plugin.getLogger().info("创建索引: " + indexName);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("创建索引 " + indexName + " 时出错: " + e.getMessage());
        }
    }

    /**
     * 升级数据库结构
     *
     * @param stmt Statement对象
     * @throws SQLException SQL异常
     */
    private void upgradeDatabase(Statement stmt) throws SQLException {
        // 检查是否需要添加player_name字段
        boolean playerNameExists = false;

        if (databaseType == DatabaseType.MYSQL) {
            // MySQL检查字段
            try (ResultSet rs = stmt.executeQuery(
                "SELECT COUNT(*) FROM information_schema.columns " +
                "WHERE table_schema = DATABASE() AND table_name = 'player_balances' AND column_name = 'player_name'")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    playerNameExists = true;
                }
            }
        } else {
            // SQLite检查字段
            try (ResultSet rs = stmt.executeQuery("PRAGMA table_info(player_balances)")) {
                while (rs.next()) {
                    if ("player_name".equals(rs.getString("name"))) {
                        playerNameExists = true;
                        break;
                    }
                }
            }
        }

        // 如果player_name字段不存在，添加它
        if (!playerNameExists) {
            if (plugin.getConfigManager().isDatabaseOperationLoggingEnabled()) {
                plugin.getLogger().info("检测到数据库需要升级，正在添加player_name字段...");
            }

            if (databaseType == DatabaseType.MYSQL) {
                stmt.execute("ALTER TABLE player_balances ADD COLUMN player_name VARCHAR(16) NOT NULL DEFAULT '' AFTER player_uuid");
            } else {
                stmt.execute("ALTER TABLE player_balances ADD COLUMN player_name VARCHAR(16) NOT NULL DEFAULT ''");
            }

            if (plugin.getConfigManager().isDatabaseOperationLoggingEnabled()) {
                plugin.getLogger().info("数据库升级完成：已添加player_name字段");
            }

            // 异步更新现有记录的玩家名称
            updateExistingPlayerNames();
        }
    }

    /**
     * 更新现有记录的玩家名称
     */
    private void updateExistingPlayerNames() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String selectSql = "SELECT DISTINCT player_uuid FROM player_balances WHERE player_name = '' OR player_name IS NULL";
                String updateSql = "UPDATE player_balances SET player_name = ? WHERE player_uuid = ?";

                try (PreparedStatement selectStmt = connection.prepareStatement(selectSql);
                     PreparedStatement updateStmt = connection.prepareStatement(updateSql);
                     ResultSet rs = selectStmt.executeQuery()) {

                    int updatedCount = 0;
                    while (rs.next()) {
                        String uuidStr = rs.getString("player_uuid");
                        try {
                            UUID playerId = UUID.fromString(uuidStr);
                            String playerName = getPlayerName(playerId);

                            if (playerName != null && !playerName.isEmpty()) {
                                updateStmt.setString(1, playerName);
                                updateStmt.setString(2, uuidStr);
                                updateStmt.addBatch();
                                updatedCount++;

                                // 每100条记录执行一次批处理
                                if (updatedCount % 100 == 0) {
                                    updateStmt.executeBatch();
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("无效的UUID格式: " + uuidStr);
                        }
                    }

                    // 执行剩余的批处理
                    if (updatedCount % 100 != 0) {
                        updateStmt.executeBatch();
                    }

                    if (updatedCount > 0 && plugin.getConfigManager().isDatabaseOperationLoggingEnabled()) {
                        plugin.getLogger().info("已更新 " + updatedCount + " 个玩家的用户名信息");
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "更新玩家名称失败", e);
            }
        });
    }

    /**
     * 获取玩家名称
     *
     * @param playerId 玩家UUID
     * @return 玩家名称
     */
    private String getPlayerName(UUID playerId) {
        // 首先尝试从在线玩家获取
        Player player = plugin.getServer().getPlayer(playerId);
        if (player != null) {
            return player.getName();
        }

        // 尝试从离线玩家获取
        try {
            OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(playerId);
            String name = offlinePlayer.getName();
            return (name != null && !name.isEmpty()) ? name : "Unknown";
        } catch (Exception e) {
            return "Unknown";
        }
    }

    /**
     * 加载玩家账户数据
     *
     * @param playerId 玩家UUID
     * @return 玩家账户
     */
    public PlayerAccount loadPlayerAccount(UUID playerId) {
        PlayerAccount account = new PlayerAccount(playerId);

        // 检查连接有效性
        if (!ensureConnection()) {
            if (plugin.getConfigManager().isDatabaseOperationLoggingEnabled()) {
                plugin.getLogger().warning("数据库连接无效，无法加载玩家账户: " + playerId);
            }
            return account;
        }

        String sql = "SELECT currency_id, balance FROM player_balances WHERE player_uuid = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String currencyId = rs.getString("currency_id");
                    BigDecimal balance = rs.getBigDecimal("balance");
                    account.setBalance(currencyId, balance);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "加载玩家账户数据失败: " + playerId, e);
            // 尝试重连
            if (e.getMessage().contains("Communications link failure")) {
                plugin.getLogger().info("检测到连接断开，尝试重新连接...");
                reconnect();
            }
        }

        return account;
    }
    
    /**
     * 保存玩家账户数据
     *
     * @param account 玩家账户
     */
    public void savePlayerAccount(PlayerAccount account) {
        // 检查连接有效性
        if (!ensureConnection()) {
            if (plugin.getConfigManager().isDatabaseOperationLoggingEnabled()) {
                plugin.getLogger().warning("数据库连接无效，无法保存玩家账户: " + account.getPlayerId());
            }
            return;
        }

        // 获取玩家名称
        String playerName = getPlayerName(account.getPlayerId());

        String sql = """
            INSERT OR REPLACE INTO player_balances (player_uuid, player_name, currency_id, balance, last_updated)
            VALUES (?, ?, ?, ?, ?)
            """;

        // MySQL使用不同的语法
        if (databaseType == DatabaseType.MYSQL) {
            sql = """
                INSERT INTO player_balances (player_uuid, player_name, currency_id, balance, last_updated)
                VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE player_name = VALUES(player_name), balance = VALUES(balance), last_updated = VALUES(last_updated)
                """;
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (var entry : account.getAllBalances().entrySet()) {
                stmt.setString(1, account.getPlayerId().toString());
                stmt.setString(2, playerName);
                stmt.setString(3, entry.getKey());
                stmt.setBigDecimal(4, entry.getValue());
                stmt.setLong(5, account.getLastUpdated());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "保存玩家账户数据失败: " + account.getPlayerId(), e);
            // 尝试重连
            if (e.getMessage().contains("Communications link failure")) {
                if (plugin.getConfigManager().isDatabaseOperationLoggingEnabled()) {
                    plugin.getLogger().info("检测到连接断开，尝试重新连接...");
                }
                reconnect();
            }
        }
    }
    
    /**
     * 记录交易
     *
     * @param type 交易类型
     * @param fromPlayer 转出玩家
     * @param toPlayer 转入玩家
     * @param currencyId 货币ID
     * @param amount 金额
     * @param description 描述
     */
    public void logTransaction(String type, UUID fromPlayer, UUID toPlayer,
                              String currencyId, BigDecimal amount, String description) {
        if (!plugin.getConfigManager().isTransactionLoggingEnabled()) {
            return;
        }

        // 检查连接有效性
        if (!ensureConnection()) {
            if (plugin.getConfigManager().isDatabaseOperationLoggingEnabled()) {
                plugin.getLogger().warning("数据库连接无效，无法记录交易");
            }
            return;
        }

        String sql = """
            INSERT INTO transactions (transaction_type, from_player, to_player, currency_id, amount, description, timestamp)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, type);
            stmt.setString(2, fromPlayer != null ? fromPlayer.toString() : null);
            stmt.setString(3, toPlayer != null ? toPlayer.toString() : null);
            stmt.setString(4, currencyId);
            stmt.setBigDecimal(5, amount);
            stmt.setString(6, description);
            stmt.setLong(7, System.currentTimeMillis());

            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "记录交易失败", e);
            // 尝试重连
            if (e.getMessage().contains("Communications link failure")) {
                plugin.getLogger().info("检测到连接断开，尝试重新连接...");
                reconnect();
            }
        }
    }
    
    /**
     * 检查数据库连接是否有效
     *
     * @return 是否有效
     */
    public boolean isConnectionValid() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * 确保数据库连接有效，如果无效则尝试重连
     *
     * @return 连接是否有效
     */
    private boolean ensureConnection() {
        if (isConnectionValid()) {
            return true;
        }

        if (plugin.getConfigManager().isDatabaseOperationLoggingEnabled()) {
            plugin.getLogger().warning("数据库连接无效，尝试重新连接...");
        }
        reconnect();
        return isConnectionValid();
    }
    
    /**
     * 重新连接数据库
     */
    public void reconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            initialize();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "重新连接数据库失败！", e);
        }
    }
    
    /**
     * 关闭数据库连接
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("数据库连接已关闭。");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "关闭数据库连接时发生错误", e);
        }
    }
    
    /**
     * 获取数据库连接
     *
     * @return 数据库连接
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * 获取事务记录
     *
     * @param playerId 玩家ID（可为null表示所有玩家）
     * @param currencyId 货币ID（可为null表示所有货币）
     * @param limit 限制数量
     * @return 事务记录列表
     */
    public List<Transaction> getTransactions(UUID playerId, String currencyId, int limit) {
        List<Transaction> transactions = new ArrayList<>();

        // 检查连接有效性
        if (!ensureConnection()) {
            if (plugin.getConfigManager().isDatabaseOperationLoggingEnabled()) {
                plugin.getLogger().warning("数据库连接无效，无法获取事务记录");
            }
            return transactions;
        }

        StringBuilder sql = new StringBuilder("SELECT * FROM transactions WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (playerId != null) {
            sql.append(" AND (from_player = ? OR to_player = ?)");
            params.add(playerId.toString());
            params.add(playerId.toString());
        }

        if (currencyId != null) {
            sql.append(" AND currency_id = ?");
            params.add(currencyId);
        }

        sql.append(" ORDER BY timestamp DESC");

        if (limit > 0) {
            sql.append(" LIMIT ?");
            params.add(limit);
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String type = rs.getString("transaction_type");
                    String fromPlayerStr = rs.getString("from_player");
                    String toPlayerStr = rs.getString("to_player");
                    String currency = rs.getString("currency_id");
                    BigDecimal amount = rs.getBigDecimal("amount");
                    String description = rs.getString("description");
                    long timestamp = rs.getLong("timestamp");

                    UUID fromPlayer = fromPlayerStr != null ? UUID.fromString(fromPlayerStr) : null;
                    UUID toPlayer = toPlayerStr != null ? UUID.fromString(toPlayerStr) : null;

                    transactions.add(new Transaction(id, type, fromPlayer, toPlayer,
                                                   currency, amount, description, timestamp));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "获取事务记录失败", e);
            // 尝试重连
            if (e.getMessage().contains("Communications link failure")) {
                if (plugin.getConfigManager().isDatabaseOperationLoggingEnabled()) {
                    plugin.getLogger().info("检测到连接断开，尝试重新连接...");
                }
                reconnect();
            }
        }

        return transactions;
    }

    /**
     * 获取玩家的事务记录数量
     *
     * @param playerId 玩家ID
     * @param currencyId 货币ID（可为null）
     * @return 事务记录数量
     */
    public int getTransactionCount(UUID playerId, String currencyId) {
        // 检查连接有效性
        if (!ensureConnection()) {
            if (plugin.getConfigManager().isDatabaseOperationLoggingEnabled()) {
                plugin.getLogger().warning("数据库连接无效，无法获取事务记录数量");
            }
            return 0;
        }

        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM transactions WHERE (from_player = ? OR to_player = ?)");
        List<Object> params = new ArrayList<>();
        params.add(playerId.toString());
        params.add(playerId.toString());

        if (currencyId != null) {
            sql.append(" AND currency_id = ?");
            params.add(currencyId);
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "获取事务记录数量失败", e);
            // 尝试重连
            if (e.getMessage().contains("Communications link failure")) {
                plugin.getLogger().info("检测到连接断开，尝试重新连接...");
                reconnect();
            }
        }

        return 0;
    }
}
