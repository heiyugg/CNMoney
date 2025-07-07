package cn.money.integration;

import cn.money.CNMoney;
import cn.money.model.Currency;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.ServicePriority;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Level;

/**
 * Vault经济系统集成
 * 
 * @author CNMoney Team
 */
public class VaultIntegration implements Economy {
    
    private final CNMoney plugin;
    private Currency primaryCurrency;
    
    public VaultIntegration(CNMoney plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 设置Vault集成
     * 
     * @return 是否设置成功
     */
    public boolean setup() {
        try {
            // 找到主要货币（Vault主货币）
            this.primaryCurrency = plugin.getCurrencyManager().getAllCurrencies().stream()
                    .filter(Currency::isVaultPrimary)
                    .findFirst()
                    .orElse(plugin.getCurrencyManager().getDefaultCurrency());
            
            // 注册Vault经济服务
            Bukkit.getServer().getServicesManager().register(Economy.class, this, plugin, ServicePriority.Normal);
            
            plugin.getLogger().info("Vault集成已启用，主货币: " + primaryCurrency.getName());
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Vault集成设置失败！", e);
            return false;
        }
    }
    
    @Override
    public boolean isEnabled() {
        return plugin.isEnabled();
    }
    
    @Override
    public String getName() {
        return "CNMoney";
    }
    
    @Override
    public boolean hasBankSupport() {
        return false; // 暂不支持银行系统
    }
    
    @Override
    public int fractionalDigits() {
        return primaryCurrency.getDecimals();
    }
    
    @Override
    public String format(double amount) {
        return primaryCurrency.formatAmount(BigDecimal.valueOf(amount));
    }
    
    @Override
    public String currencyNamePlural() {
        return primaryCurrency.getPlural();
    }
    
    @Override
    public String currencyNameSingular() {
        return primaryCurrency.getName();
    }
    
    @Override
    public boolean hasAccount(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return player.hasPlayedBefore() || player.isOnline();
    }
    
    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return player.hasPlayedBefore() || player.isOnline();
    }
    
    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }
    
    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }
    
    @Override
    public double getBalance(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return getBalance(player);
    }
    
    @Override
    public double getBalance(OfflinePlayer player) {
        BigDecimal balance = plugin.getCurrencyManager().getBalance(player.getUniqueId(), 
                                                                  primaryCurrency.getId());
        return balance.doubleValue();
    }
    
    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }
    
    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }
    
    @Override
    public boolean has(String playerName, double amount) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return has(player, amount);
    }
    
    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return plugin.getCurrencyManager().hasBalance(player.getUniqueId(), 
                                                    primaryCurrency.getId(), 
                                                    BigDecimal.valueOf(amount));
    }
    
    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount);
    }
    
    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }
    
    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return withdrawPlayer(player, amount);
    }
    
    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "金额不能为负数");
        }
        
        BigDecimal withdrawAmount = BigDecimal.valueOf(amount);
        boolean success = plugin.getCurrencyManager().subtractBalance(player.getUniqueId(), 
                                                                     primaryCurrency.getId(), 
                                                                     withdrawAmount);
        
        if (success) {
            double newBalance = getBalance(player);
            plugin.getDatabaseManager().logTransaction("VAULT_WITHDRAW", 
                                                     player.getUniqueId(), 
                                                     null, 
                                                     primaryCurrency.getId(), 
                                                     withdrawAmount, 
                                                     "Vault提取");
            return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, "");
        } else {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "余额不足");
        }
    }
    
    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }
    
    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }
    
    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return depositPlayer(player, amount);
    }
    
    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "金额不能为负数");
        }
        
        BigDecimal depositAmount = BigDecimal.valueOf(amount);
        boolean success = plugin.getCurrencyManager().addBalance(player.getUniqueId(), 
                                                               primaryCurrency.getId(), 
                                                               depositAmount);
        
        if (success) {
            double newBalance = getBalance(player);
            plugin.getDatabaseManager().logTransaction("VAULT_DEPOSIT", 
                                                     null, 
                                                     player.getUniqueId(), 
                                                     primaryCurrency.getId(), 
                                                     depositAmount, 
                                                     "Vault存入");
            return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, "");
        } else {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "存入失败");
        }
    }
    
    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }
    
    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }
    
    @Override
    public boolean createPlayerAccount(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return createPlayerAccount(player);
    }
    
    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        // 账户会在第一次访问时自动创建
        plugin.getCurrencyManager().getPlayerAccount(player.getUniqueId());
        return true;
    }
    
    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return createPlayerAccount(playerName);
    }
    
    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player);
    }
    
    // 银行相关方法（暂不支持）
    @Override
    public EconomyResponse createBank(String name, String player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "不支持银行系统");
    }
    
    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "不支持银行系统");
    }
    
    @Override
    public EconomyResponse deleteBank(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "不支持银行系统");
    }
    
    @Override
    public EconomyResponse bankBalance(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "不支持银行系统");
    }
    
    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "不支持银行系统");
    }
    
    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "不支持银行系统");
    }
    
    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "不支持银行系统");
    }
    
    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "不支持银行系统");
    }
    
    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "不支持银行系统");
    }
    
    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "不支持银行系统");
    }
    
    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "不支持银行系统");
    }
    
    @Override
    public List<String> getBanks() {
        return List.of(); // 返回空列表
    }
}
