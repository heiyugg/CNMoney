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
 * CNMoney Multi-Currency Plugin Main Class
 * 
 * @author CNMoney Team
 * @version 1.0.0
 */
public class CNMoney extends JavaPlugin {
    
    private static CNMoney instance;
    
    // Managers
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private CurrencyManager currencyManager;
    private cn.money.gui.GUIManager guiManager;
    private ImportExportManager importExportManager;
    
    // Integrations
    private VaultIntegration vaultIntegration;
    private PlaceholderIntegration placeholderIntegration;
    
    // API
    private CNMoneyAPI api;
    
    @Override
    public void onLoad() {
        instance = this;
        getLogger().info("CNMoney plugin is loading...");
    }
    
    @Override
    public void onEnable() {
        try {
            // Initialize config manager
            this.configManager = new ConfigManager(this);
            if (!configManager.loadConfigs()) {
                getLogger().severe("Failed to load configuration files! Plugin will be disabled.");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            
            // Initialize database manager
            this.databaseManager = new DatabaseManager(this);
            if (!databaseManager.initialize()) {
                getLogger().severe("Failed to initialize database! Plugin will be disabled.");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            
            // Initialize currency manager
            this.currencyManager = new CurrencyManager(this);
            if (!currencyManager.initialize()) {
                getLogger().severe("Failed to initialize currency manager! Plugin will be disabled.");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

            // Initialize GUI manager
            this.guiManager = new cn.money.gui.GUIManager(this);
            getLogger().info("GUI manager initialized.");

            // Initialize import/export manager
            this.importExportManager = new ImportExportManager(this);
            getLogger().info("Import/Export manager initialized.");

            // Register commands
            registerCommands();
            
            // Initialize API
            this.api = new CNMoneyAPI(this);
            getServer().getServicesManager().register(CNMoneyAPI.class, api, this, ServicePriority.Normal);
            
            // Setup third-party integrations
            setupIntegrations();
            
            // Start scheduled tasks
            startScheduledTasks();
            
            getLogger().info("CNMoney plugin has been successfully enabled!");
            getLogger().info("Version: " + getDescription().getVersion());
            getLogger().info("Supported currencies: " + currencyManager.getCurrencyCount());
            
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error occurred during plugin enabling!", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            // Save all data
            if (currencyManager != null) {
                currencyManager.saveAllData();
            }

            // Close database connections
            if (databaseManager != null) {
                databaseManager.close();
            }

            // Cancel all tasks
            getServer().getScheduler().cancelTasks(this);

            getLogger().info("CNMoney plugin has been safely disabled.");

        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error occurred during plugin disabling!", e);
        } finally {
            instance = null;
        }
    }

    /**
     * Register commands
     */
    private void registerCommands() {
        MoneyCommand moneyCommand = new MoneyCommand(this);
        
        // Register main command
        getCommand("cm").setExecutor(moneyCommand);
        getCommand("cm").setTabCompleter(moneyCommand);
        
        // Register other commands
        getCommand("balance").setExecutor(moneyCommand);
        getCommand("balance").setTabCompleter(moneyCommand);
        getCommand("pay").setExecutor(moneyCommand);
        getCommand("pay").setTabCompleter(moneyCommand);
        getCommand("eco").setExecutor(moneyCommand);
        getCommand("eco").setTabCompleter(moneyCommand);
        
        getLogger().info("Commands registered successfully.");
    }

    /**
     * Setup third-party plugin integrations
     */
    private void setupIntegrations() {
        // Vault integration
        if (configManager.isVaultIntegrationEnabled() && 
            getServer().getPluginManager().getPlugin("Vault") != null) {
            try {
                this.vaultIntegration = new VaultIntegration(this);
                if (vaultIntegration.setup()) {
                    getLogger().info("Vault integration enabled.");
                } else {
                    getLogger().warning("Failed to enable Vault integration.");
                }
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "Error during Vault integration", e);
            }
        }
        
        // PlaceholderAPI integration
        if (configManager.isPlaceholderAPIIntegrationEnabled() && 
            getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                this.placeholderIntegration = new PlaceholderIntegration(this);
                if (placeholderIntegration.register()) {
                    getLogger().info("PlaceholderAPI integration enabled.");
                } else {
                    getLogger().warning("Failed to enable PlaceholderAPI integration.");
                }
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "Error during PlaceholderAPI integration", e);
            }
        }
    }

    /**
     * Start scheduled tasks
     */
    private void startScheduledTasks() {
        // Auto-save task
        int saveInterval = configManager.getSaveInterval() * 20; // Convert to ticks
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (currencyManager != null) {
                currencyManager.saveAllData();
            }
        }, saveInterval, saveInterval);
        
        getLogger().info("Scheduled tasks started.");
    }
    
    // Getter methods
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
    
    public CNMoneyAPI getApiInstance() {
        return api;
    }
    
    /**
     * Get CNMoney API instance (static method)
     * For other plugins to call
     * 
     * @return CNMoneyAPI instance, or null if plugin is not enabled
     */
    public static CNMoneyAPI getAPI() {
        if (instance == null) {
            return null;
        }
        return instance.getApiInstance();
    }
}
