package cn.money;

import cn.money.api.CNMoneyAPI;
import cn.money.command.MoneyCommand;
import cn.money.manager.CurrencyManager;
import cn.money.manager.DatabaseManager;
import cn.money.manager.ConfigManager;
import cn.money.manager.ImportExportManager;
import cn.money.integration.VaultIntegration;
import cn.money.integration.PlaceholderIntegration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.ServicePriority;

import java.util.logging.Level;

/**
 * CNMoney 多货币插件主类
 * 
 * @author CNMoney Team
 * @version 1.0.0
 */
public class CNMoney extends JavaPlugin {
    
    private static CNMoney instance;
    
    // 管理器
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private CurrencyManager currencyManager;
    private cn.money.gui.GUIManager guiManager;
    private ImportExportManager importExportManager;
    
    // 集成
    private VaultIntegration vaultIntegration;
    private PlaceholderIntegration placeholderIntegration;
    
    // API
    private CNMoneyAPI api;
    
    @Override
    public void onLoad() {
        instance = this;
        getLogger().info("CNMoney 插件正在加载...");
    }
    
    @Override
    public void onEnable() {
        try {
            // 初始化配置管理器
            this.configManager = new ConfigManager(this);
            if (!configManager.loadConfigs()) {
                getLogger().severe("配置文件加载失败！插件将被禁用。");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            
            // 初始化数据库管理器
            this.databaseManager = new DatabaseManager(this);
            if (!databaseManager.initialize()) {
                getLogger().severe("数据库初始化失败！插件将被禁用。");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            
            // 初始化货币管理器
            this.currencyManager = new CurrencyManager(this);
            if (!currencyManager.initialize()) {
                getLogger().severe("货币管理器初始化失败！插件将被禁用。");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

            // 初始化GUI管理器
            this.guiManager = new cn.money.gui.GUIManager(this);
            getLogger().info("GUI管理器已初始化。");

            // 初始化导入导出管理器
            this.importExportManager = new ImportExportManager(this);
            getLogger().info("导入导出管理器已初始化。");

            // 注册命令
            registerCommands();
            
            // 初始化API
            this.api = new CNMoneyAPI(this);
            getServer().getServicesManager().register(CNMoneyAPI.class, api, this, ServicePriority.Normal);
            
            // 集成第三方插件
            setupIntegrations();
            
            // 启动定时任务
            startScheduledTasks();
            
            getLogger().info("CNMoney 插件已成功启用！");
            getLogger().info("版本: " + getDescription().getVersion());
            getLogger().info("支持的货币数量: " + currencyManager.getCurrencyCount());
            
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "插件启用过程中发生错误！", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        try {
            // 保存所有数据
            if (currencyManager != null) {
                currencyManager.saveAllData();
            }
            
            // 关闭数据库连接
            if (databaseManager != null) {
                databaseManager.close();
            }
            
            // 取消所有任务
            getServer().getScheduler().cancelTasks(this);
            
            getLogger().info("CNMoney 插件已安全关闭。");
            
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "插件关闭过程中发生错误！", e);
        } finally {
            instance = null;
        }
    }
    
    /**
     * 注册命令
     */
    private void registerCommands() {
        MoneyCommand moneyCommand = new MoneyCommand(this);
        
        // 注册主命令
        getCommand("cm").setExecutor(moneyCommand);
        getCommand("cm").setTabCompleter(moneyCommand);
        
        // 注册其他命令
        getCommand("balance").setExecutor(moneyCommand);
        getCommand("balance").setTabCompleter(moneyCommand);
        getCommand("pay").setExecutor(moneyCommand);
        getCommand("pay").setTabCompleter(moneyCommand);
        getCommand("eco").setExecutor(moneyCommand);
        getCommand("eco").setTabCompleter(moneyCommand);
        
        getLogger().info("命令注册完成。");
    }
    
    /**
     * 设置第三方插件集成
     */
    private void setupIntegrations() {
        // Vault集成
        if (configManager.isVaultIntegrationEnabled() && 
            getServer().getPluginManager().getPlugin("Vault") != null) {
            try {
                this.vaultIntegration = new VaultIntegration(this);
                if (vaultIntegration.setup()) {
                    getLogger().info("Vault集成已启用。");
                } else {
                    getLogger().warning("Vault集成启用失败。");
                }
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "Vault集成过程中发生错误", e);
            }
        }
        
        // PlaceholderAPI集成
        if (configManager.isPlaceholderAPIIntegrationEnabled() && 
            getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                this.placeholderIntegration = new PlaceholderIntegration(this);
                if (placeholderIntegration.register()) {
                    getLogger().info("PlaceholderAPI集成已启用。");
                } else {
                    getLogger().warning("PlaceholderAPI集成启用失败。");
                }
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "PlaceholderAPI集成过程中发生错误", e);
            }
        }
    }
    
    /**
     * 启动定时任务
     */
    private void startScheduledTasks() {
        // 数据自动保存任务
        int saveInterval = configManager.getSaveInterval() * 20; // 转换为tick
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (currencyManager != null) {
                currencyManager.saveAllData();
            }
        }, saveInterval, saveInterval);
        
        getLogger().info("定时任务已启动。");
    }
    
    // Getter方法
    public static CNMoney getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public CurrencyManager getCurrencyManager() {
        return currencyManager;
    }

    public cn.money.gui.GUIManager getGUIManager() {
        return guiManager;
    }

    public ImportExportManager getImportExportManager() {
        return importExportManager;
    }
    
    public VaultIntegration getVaultIntegration() {
        return vaultIntegration;
    }
    
    public PlaceholderIntegration getPlaceholderIntegration() {
        return placeholderIntegration;
    }
    
    public CNMoneyAPI getAPI() {
        return api;
    }
}
