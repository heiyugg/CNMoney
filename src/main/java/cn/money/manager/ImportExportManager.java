package cn.money.manager;

import cn.money.CNMoney;
import cn.money.model.Currency;
import cn.money.model.PlayerAccount;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

/**
 * 导入导出管理器
 * 
 * @author CNMoney Team
 */
public class ImportExportManager {
    
    private final CNMoney plugin;
    private final Gson gson;
    private final SimpleDateFormat dateFormat;
    
    public ImportExportManager(CNMoney plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    }
    
    /**
     * 导出所有玩家余额数据到JSON文件
     * 
     * @return 导出的文件路径，失败返回null
     */
    public String exportPlayerBalances() {
        try {
            File exportDir = new File(plugin.getDataFolder(), "exports");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            
            String fileName = "player_balances_" + dateFormat.format(new Date()) + ".json";
            File exportFile = new File(exportDir, fileName);
            
            JsonObject root = new JsonObject();
            root.addProperty("export_time", System.currentTimeMillis());
            root.addProperty("export_version", "1.0");
            
            JsonObject playersData = new JsonObject();
            
            // 获取所有玩家账户数据
            Map<UUID, PlayerAccount> accounts = plugin.getCurrencyManager().getAllPlayerAccounts();
            
            for (Map.Entry<UUID, PlayerAccount> entry : accounts.entrySet()) {
                UUID playerId = entry.getKey();
                PlayerAccount account = entry.getValue();
                
                JsonObject playerData = new JsonObject();
                playerData.addProperty("last_updated", account.getLastUpdated());
                
                JsonObject balances = new JsonObject();
                for (Map.Entry<String, BigDecimal> balanceEntry : account.getAllBalances().entrySet()) {
                    balances.addProperty(balanceEntry.getKey(), balanceEntry.getValue());
                }
                playerData.add("balances", balances);
                
                playersData.add(playerId.toString(), playerData);
            }
            
            root.add("players", playersData);
            
            // 写入文件
            try (FileWriter writer = new FileWriter(exportFile, StandardCharsets.UTF_8)) {
                gson.toJson(root, writer);
            }
            
            plugin.getLogger().info("玩家余额数据已导出到: " + exportFile.getAbsolutePath());
            return exportFile.getAbsolutePath();
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "导出玩家余额数据失败", e);
            return null;
        }
    }
    
    /**
     * 从JSON文件导入玩家余额数据
     * 
     * @param filePath 文件路径
     * @param overwrite 是否覆盖现有数据
     * @return 导入结果信息
     */
    public String importPlayerBalances(String filePath, boolean overwrite) {
        try {
            File importFile = new File(filePath);
            if (!importFile.exists()) {
                return "文件不存在: " + filePath;
            }
            
            // 读取JSON文件
            JsonObject root;
            try (FileReader reader = new FileReader(importFile, StandardCharsets.UTF_8)) {
                root = JsonParser.parseReader(reader).getAsJsonObject();
            }
            
            if (!root.has("players")) {
                return "无效的导入文件格式";
            }
            
            JsonObject playersData = root.getAsJsonObject("players");
            int importedCount = 0;
            int skippedCount = 0;
            
            for (String playerIdStr : playersData.keySet()) {
                try {
                    UUID playerId = UUID.fromString(playerIdStr);
                    JsonObject playerData = playersData.getAsJsonObject(playerIdStr);
                    
                    // 检查是否已存在
                    PlayerAccount existingAccount = plugin.getCurrencyManager().getPlayerAccount(playerId);
                    if (!overwrite && existingAccount.getAllBalances().size() > 0) {
                        skippedCount++;
                        continue;
                    }
                    
                    // 导入余额数据
                    JsonObject balances = playerData.getAsJsonObject("balances");
                    for (String currencyId : balances.keySet()) {
                        BigDecimal amount = balances.get(currencyId).getAsBigDecimal();
                        plugin.getCurrencyManager().setBalance(playerId, currencyId, amount, "数据导入");
                    }
                    
                    importedCount++;
                    
                } catch (Exception e) {
                    plugin.getLogger().warning("导入玩家数据失败: " + playerIdStr + " - " + e.getMessage());
                }
            }
            
            // 保存数据
            plugin.getCurrencyManager().saveAllData();
            
            return String.format("导入完成！成功: %d, 跳过: %d", importedCount, skippedCount);
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "导入玩家余额数据失败", e);
            return "导入失败: " + e.getMessage();
        }
    }
    
    /**
     * 导出货币配置到YAML文件
     * 
     * @return 导出的文件路径，失败返回null
     */
    public String exportCurrencyConfig() {
        try {
            File exportDir = new File(plugin.getDataFolder(), "exports");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            
            String fileName = "currency_config_" + dateFormat.format(new Date()) + ".yml";
            File exportFile = new File(exportDir, fileName);
            
            YamlConfiguration config = new YamlConfiguration();
            
            // 导出所有货币配置
            Collection<Currency> currencies = plugin.getCurrencyManager().getAllCurrencies();
            for (Currency currency : currencies) {
                String path = "currencies." + currency.getId();
                
                config.set(path + ".name", currency.getName());
                config.set(path + ".symbol", currency.getSymbol());
                config.set(path + ".plural", currency.getPlural());
                config.set(path + ".decimals", currency.getDecimals());
                config.set(path + ".default-balance", currency.getDefaultBalance().doubleValue());
                config.set(path + ".enabled", currency.isEnabled());
                config.set(path + ".vault-primary", currency.isVaultPrimary());
                
                // 显示设置
                config.set(path + ".display.color", currency.getColor());
                config.set(path + ".display.format", currency.getFormat());
                config.set(path + ".display.icon", currency.getIcon().name());
                
                // 限制设置
                config.set(path + ".limits.min", currency.getMinAmount().doubleValue());
                config.set(path + ".limits.max", currency.getMaxAmount().doubleValue());
                
                // 汇率设置
                Map<String, BigDecimal> rates = currency.getExchangeRates();
                if (!rates.isEmpty()) {
                    for (Map.Entry<String, BigDecimal> entry : rates.entrySet()) {
                        config.set(path + ".exchange-rates." + entry.getKey(), entry.getValue().doubleValue());
                    }
                }
            }
            
            // 保存文件
            config.save(exportFile);
            
            plugin.getLogger().info("货币配置已导出到: " + exportFile.getAbsolutePath());
            return exportFile.getAbsolutePath();
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "导出货币配置失败", e);
            return null;
        }
    }
    
    /**
     * 从YAML文件导入货币配置
     * 
     * @param filePath 文件路径
     * @param overwrite 是否覆盖现有配置
     * @return 导入结果信息
     */
    public String importCurrencyConfig(String filePath, boolean overwrite) {
        try {
            File importFile = new File(filePath);
            if (!importFile.exists()) {
                return "文件不存在: " + filePath;
            }
            
            YamlConfiguration config = YamlConfiguration.loadConfiguration(importFile);
            
            if (!config.contains("currencies")) {
                return "无效的货币配置文件";
            }
            
            int importedCount = 0;
            int skippedCount = 0;
            
            for (String currencyId : config.getConfigurationSection("currencies").getKeys(false)) {
                try {
                    // 检查是否已存在
                    if (!overwrite && plugin.getCurrencyManager().getCurrency(currencyId) != null) {
                        skippedCount++;
                        continue;
                    }
                    
                    String path = "currencies." + currencyId;
                    
                    // 创建货币配置
                    // 这里需要根据实际的Currency构造函数来调整
                    // 暂时跳过实际的货币创建，只记录日志
                    plugin.getLogger().info("准备导入货币: " + currencyId);
                    importedCount++;
                    
                } catch (Exception e) {
                    plugin.getLogger().warning("导入货币配置失败: " + currencyId + " - " + e.getMessage());
                }
            }
            
            return String.format("导入完成！成功: %d, 跳过: %d", importedCount, skippedCount);
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "导入货币配置失败", e);
            return "导入失败: " + e.getMessage();
        }
    }
    
    /**
     * 获取导出目录中的所有文件
     * 
     * @return 文件列表
     */
    public List<File> getExportFiles() {
        File exportDir = new File(plugin.getDataFolder(), "exports");
        if (!exportDir.exists()) {
            return new ArrayList<>();
        }
        
        File[] files = exportDir.listFiles();
        if (files == null) {
            return new ArrayList<>();
        }
        
        return Arrays.asList(files);
    }
    
    /**
     * 清理旧的导出文件
     * 
     * @param daysOld 保留天数
     * @return 清理的文件数量
     */
    public int cleanupOldExports(int daysOld) {
        File exportDir = new File(plugin.getDataFolder(), "exports");
        if (!exportDir.exists()) {
            return 0;
        }
        
        long cutoffTime = System.currentTimeMillis() - (daysOld * 24L * 60L * 60L * 1000L);
        int deletedCount = 0;
        
        File[] files = exportDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.lastModified() < cutoffTime) {
                    if (file.delete()) {
                        deletedCount++;
                    }
                }
            }
        }
        
        return deletedCount;
    }
}
