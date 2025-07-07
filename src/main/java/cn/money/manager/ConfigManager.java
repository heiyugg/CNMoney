package cn.money.manager;

import cn.money.CNMoney;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Level;

/**
 * 配置管理器
 * 
 * @author CNMoney Team
 */
public class ConfigManager {
    
    private final CNMoney plugin;
    private FileConfiguration config;
    private FileConfiguration currenciesConfig;
    private FileConfiguration messagesConfig;
    
    private File configFile;
    private File currenciesFile;
    private File messagesFile;
    
    public ConfigManager(CNMoney plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 加载所有配置文件
     * 
     * @return 是否加载成功
     */
    public boolean loadConfigs() {
        try {
            // 创建插件数据文件夹
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            
            // 加载主配置文件
            loadMainConfig();
            
            // 加载货币配置文件
            loadCurrenciesConfig();
            
            // 加载消息配置文件
            loadMessagesConfig();
            
            plugin.getLogger().info("配置文件加载完成。");
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "配置文件加载失败！", e);
            return false;
        }
    }
    
    /**
     * 加载主配置文件
     */
    private void loadMainConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        
        // 如果配置文件不存在，从资源文件复制
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
        
        // 检查配置版本并更新
        checkAndUpdateConfig();
    }
    
    /**
     * 加载货币配置文件
     */
    private void loadCurrenciesConfig() {
        currenciesFile = new File(plugin.getDataFolder(), "currencies.yml");
        
        // 如果配置文件不存在，从资源文件复制
        if (!currenciesFile.exists()) {
            saveResource("currencies.yml");
        }
        
        currenciesConfig = YamlConfiguration.loadConfiguration(currenciesFile);
    }
    
    /**
     * 加载消息配置文件
     */
    private void loadMessagesConfig() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        
        // 如果配置文件不存在，从资源文件复制
        if (!messagesFile.exists()) {
            saveResource("messages.yml");
        }
        
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }
    
    /**
     * 从资源文件保存到插件目录
     * 
     * @param resourceName 资源文件名
     */
    private void saveResource(String resourceName) {
        try {
            InputStream inputStream = plugin.getResource(resourceName);
            if (inputStream != null) {
                File outputFile = new File(plugin.getDataFolder(), resourceName);
                Files.copy(inputStream, outputFile.toPath());
                inputStream.close();
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "无法保存资源文件: " + resourceName, e);
        }
    }
    
    /**
     * 检查并更新配置文件
     */
    private void checkAndUpdateConfig() {
        // 这里可以添加配置版本检查和自动更新逻辑
        // 暂时留空，后续可以扩展
    }
    
    /**
     * 保存配置文件
     */
    public void saveConfigs() {
        try {
            if (config != null && configFile != null) {
                config.save(configFile);
            }
            if (currenciesConfig != null && currenciesFile != null) {
                currenciesConfig.save(currenciesFile);
            }
            if (messagesConfig != null && messagesFile != null) {
                messagesConfig.save(messagesFile);
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "保存配置文件失败！", e);
        }
    }
    
    /**
     * 重新加载配置文件
     */
    public void reloadConfigs() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        currenciesConfig = YamlConfiguration.loadConfiguration(currenciesFile);
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        
        plugin.getLogger().info("配置文件已重新加载。");
    }
    
    // 配置获取方法
    public String getLanguage() {
        return config.getString("settings.language", "zh_CN");
    }
    
    public boolean isDebugEnabled() {
        return config.getBoolean("settings.debug", false);
    }
    
    public int getSaveInterval() {
        return config.getInt("settings.save-interval", 300);
    }
    
    public String getDefaultCurrency() {
        return config.getString("settings.default-currency", "gold_coin");
    }
    
    public boolean isVaultIntegrationEnabled() {
        return config.getBoolean("settings.vault-integration", true);
    }
    
    public boolean isPlaceholderAPIIntegrationEnabled() {
        return config.getBoolean("settings.placeholderapi-integration", true);
    }
    
    // 数据库配置
    public String getDatabaseType() {
        return config.getString("database.type", "sqlite");
    }
    
    public String getSQLiteFile() {
        return config.getString("database.sqlite.file", "cnmoney.db");
    }
    
    public String getMySQLHost() {
        return config.getString("database.mysql.host", "localhost");
    }
    
    public int getMySQLPort() {
        return config.getInt("database.mysql.port", 3306);
    }
    
    public String getMySQLDatabase() {
        return config.getString("database.mysql.database", "cnmoney");
    }
    
    public String getMySQLUsername() {
        return config.getString("database.mysql.username", "root");
    }
    
    public String getMySQLPassword() {
        return config.getString("database.mysql.password", "password");
    }
    
    public boolean isMySQLSSLEnabled() {
        return config.getBoolean("database.mysql.ssl", false);
    }
    
    // GUI配置
    public String getGUITitle() {
        return config.getString("gui.title", "§6§l货币管理系统");
    }
    
    public int getGUISize() {
        return config.getInt("gui.size", 54);
    }
    
    public boolean isGUISoundEnabled() {
        return config.getBoolean("gui.sound-enabled", true);
    }
    
    public int getGUIRefreshInterval() {
        return config.getInt("gui.refresh-interval", 20);
    }
    
    // 兑换配置
    public boolean isExchangeEnabled() {
        return config.getBoolean("exchange.enabled", true);
    }
    
    public double getExchangeFeePercentage() {
        return config.getDouble("exchange.fee-percentage", 0.02);
    }
    
    public double getExchangeMinAmount() {
        return config.getDouble("exchange.min-amount", 1.0);
    }
    
    public double getExchangeMaxAmount() {
        return config.getDouble("exchange.max-amount", -1);
    }
    
    // 转账配置
    public boolean isTransferEnabled() {
        return config.getBoolean("transfer.enabled", true);
    }
    
    public double getTransferFeePercentage() {
        return config.getDouble("transfer.fee-percentage", 0.01);
    }
    
    public double getTransferMinAmount() {
        return config.getDouble("transfer.min-amount", 1.0);
    }
    
    public double getTransferMaxAmount() {
        return config.getDouble("transfer.max-amount", -1);
    }
    
    public int getTransferCooldown() {
        return config.getInt("transfer.cooldown", 5);
    }
    
    // 消息配置
    public String getMessagePrefix() {
        return config.getString("messages.prefix", "§8[§6CNMoney§8] §r");
    }
    
    public String getSuccessColor() {
        return config.getString("messages.success-color", "§a");
    }
    
    public String getErrorColor() {
        return config.getString("messages.error-color", "§c");
    }
    
    public String getWarningColor() {
        return config.getString("messages.warning-color", "§e");
    }
    
    public String getInfoColor() {
        return config.getString("messages.info-color", "§b");
    }
    
    // 日志配置
    public boolean isTransactionLoggingEnabled() {
        return config.getBoolean("logging.log-transactions", true);
    }
    
    public String getLogFile() {
        return config.getString("logging.log-file", "transactions.log");
    }
    
    public int getLogRetentionDays() {
        return config.getInt("logging.log-retention-days", 30);
    }

    /**
     * 获取是否显示GUI操作日志
     */
    public boolean isGUIOperationLoggingEnabled() {
        return config.getBoolean("logging.console.gui-operations", false);
    }

    /**
     * 获取是否显示数据库操作日志
     */
    public boolean isDatabaseOperationLoggingEnabled() {
        return config.getBoolean("logging.console.database-operations", false);
    }

    /**
     * 获取是否显示调试信息
     */
    public boolean isDebugInfoLoggingEnabled() {
        return config.getBoolean("logging.console.debug-info", false);
    }

    // 性能配置
    public int getCacheSize() {
        return config.getInt("performance.cache-size", 1000);
    }
    
    public int getCacheExpireMinutes() {
        return config.getInt("performance.cache-expire-minutes", 30);
    }
    
    public boolean isAsyncProcessingEnabled() {
        return config.getBoolean("performance.async-processing", true);
    }
    
    /**
     * 保存配置文件
     */
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "保存配置文件失败", e);
        }
    }

    // Getter方法
    public FileConfiguration getConfig() {
        return config;
    }
    
    public FileConfiguration getCurrenciesConfig() {
        return currenciesConfig;
    }
    
    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    /**
     * 获取消息
     *
     * @param key 消息键
     * @return 消息内容
     */
    public String getMessage(String key) {
        if (messagesConfig == null) {
            return "§c消息配置未加载: " + key;
        }

        String message = messagesConfig.getString(key);
        if (message == null) {
            return "§c未找到消息: " + key;
        }

        // 替换颜色代码
        return message.replace('&', '§');
    }

    /**
     * 获取消息列表
     *
     * @param key 消息键
     * @return 消息列表
     */
    public java.util.List<String> getMessageList(String key) {
        if (messagesConfig == null) {
            return java.util.Arrays.asList("§c消息配置未加载: " + key);
        }

        java.util.List<String> messages = messagesConfig.getStringList(key);
        if (messages.isEmpty()) {
            // 尝试获取单个字符串
            String singleMessage = messagesConfig.getString(key);
            if (singleMessage != null) {
                messages = java.util.Arrays.asList(singleMessage);
            } else {
                return java.util.Arrays.asList("§c未找到消息: " + key);
            }
        }

        // 替换颜色代码
        java.util.List<String> coloredMessages = new java.util.ArrayList<>();
        for (String message : messages) {
            coloredMessages.add(message.replace('&', '§'));
        }

        return coloredMessages;
    }
}
