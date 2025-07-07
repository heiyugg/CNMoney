package cn.money.manager;

import cn.money.CNMoney;
import cn.money.model.Currency;
import cn.money.model.PlayerAccount;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * 货币管理器
 * 
 * @author CNMoney Team
 */
public class CurrencyManager {
    
    private final CNMoney plugin;
    private final Map<String, Currency> currencies;
    private final Map<UUID, PlayerAccount> playerAccounts;
    private String defaultCurrencyId;
    
    public CurrencyManager(CNMoney plugin) {
        this.plugin = plugin;
        this.currencies = new ConcurrentHashMap<>();
        this.playerAccounts = new ConcurrentHashMap<>();
    }
    
    /**
     * 初始化货币管理器
     * 
     * @return 是否初始化成功
     */
    public boolean initialize() {
        try {
            // 加载货币配置
            loadCurrencies();
            
            // 设置默认货币
            this.defaultCurrencyId = plugin.getConfigManager().getDefaultCurrency();
            if (!currencies.containsKey(defaultCurrencyId)) {
                plugin.getLogger().warning("默认货币 '" + defaultCurrencyId + "' 不存在，使用第一个可用货币。");
                if (!currencies.isEmpty()) {
                    defaultCurrencyId = currencies.keySet().iterator().next();
                } else {
                    plugin.getLogger().severe("没有可用的货币配置！");
                    return false;
                }
            }
            
            // 加载玩家账户数据
            loadPlayerAccounts();
            
            plugin.getLogger().info("货币管理器初始化完成，加载了 " + currencies.size() + " 种货币。");
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "货币管理器初始化失败！", e);
            return false;
        }
    }
    
    /**
     * 从配置文件加载货币
     */
    private void loadCurrencies() {
        ConfigurationSection currenciesSection = plugin.getConfigManager()
            .getCurrenciesConfig().getConfigurationSection("currencies");
        
        if (currenciesSection == null) {
            plugin.getLogger().warning("currencies.yml 中没有找到 'currencies' 节！");
            return;
        }
        
        currencies.clear();
        
        for (String currencyId : currenciesSection.getKeys(false)) {
            ConfigurationSection currencyConfig = currenciesSection.getConfigurationSection(currencyId);
            if (currencyConfig != null) {
                Currency currency = new Currency(currencyId);
                currency.loadFromConfig(currencyConfig);
                
                if (currency.isEnabled()) {
                    currencies.put(currencyId, currency);
                    plugin.getLogger().info("加载货币: " + currency.getName() + " (" + currencyId + ")");
                }
            }
        }
    }
    
    /**
     * 从数据库加载玩家账户数据
     */
    private void loadPlayerAccounts() {
        if (plugin.getDatabaseManager() != null) {
            // 数据库加载将在玩家首次访问时进行懒加载
            plugin.getLogger().info("玩家账户数据将在需要时从数据库加载。");
        } else {
            plugin.getLogger().warning("数据库管理器未初始化，无法加载玩家账户数据。");
        }
    }
    
    /**
     * 保存所有数据
     */
    public void saveAllData() {
        try {
            if (plugin.getDatabaseManager() != null) {
                // 保存所有玩家账户数据到数据库
                for (PlayerAccount account : playerAccounts.values()) {
                    plugin.getDatabaseManager().savePlayerAccount(account);
                }

                // 保存货币配置
                saveCurrencyConfigs();

                if (plugin.getConfigManager().isDatabaseOperationLoggingEnabled()) {
                    plugin.getLogger().info("所有数据已保存到数据库。");
                }
            } else {
                plugin.getLogger().warning("数据库管理器未初始化，无法保存数据。");
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "保存数据时发生错误！", e);
        }
    }
    
    /**
     * 获取玩家账户
     *
     * @param playerId 玩家UUID
     * @return 玩家账户
     */
    public PlayerAccount getPlayerAccount(UUID playerId) {
        return playerAccounts.computeIfAbsent(playerId, id -> {
            PlayerAccount account;

            // 尝试从数据库加载
            if (plugin.getDatabaseManager() != null) {
                account = plugin.getDatabaseManager().loadPlayerAccount(id);
            } else {
                account = new PlayerAccount(id);
            }

            // 为新账户或缺少的货币设置默认余额
            boolean hasNewDefaults = false;
            for (Currency currency : currencies.values()) {
                if (account.getBalance(currency.getId()).equals(BigDecimal.ZERO) &&
                    currency.getDefaultBalance().compareTo(BigDecimal.ZERO) > 0) {
                    account.setBalance(currency.getId(), currency.getDefaultBalance());
                    hasNewDefaults = true;
                }
            }

            // 如果设置了默认余额，立即保存到数据库
            if (hasNewDefaults && plugin.getDatabaseManager() != null) {
                plugin.getDatabaseManager().savePlayerAccount(account);
            }

            return account;
        });
    }
    
    /**
     * 获取玩家账户
     * 
     * @param player 玩家
     * @return 玩家账户
     */
    public PlayerAccount getPlayerAccount(Player player) {
        return getPlayerAccount(player.getUniqueId());
    }
    
    /**
     * 获取玩家指定货币的余额
     * 
     * @param playerId 玩家UUID
     * @param currencyId 货币ID
     * @return 余额
     */
    public BigDecimal getBalance(UUID playerId, String currencyId) {
        PlayerAccount account = getPlayerAccount(playerId);
        return account.getBalance(currencyId);
    }
    
    /**
     * 获取玩家默认货币的余额
     * 
     * @param playerId 玩家UUID
     * @return 余额
     */
    public BigDecimal getBalance(UUID playerId) {
        return getBalance(playerId, defaultCurrencyId);
    }
    
    /**
     * 设置玩家指定货币的余额
     *
     * @param playerId 玩家UUID
     * @param currencyId 货币ID
     * @param amount 金额
     * @return 是否设置成功
     */
    public boolean setBalance(UUID playerId, String currencyId, BigDecimal amount) {
        return setBalance(playerId, currencyId, amount, "管理员设置余额");
    }

    /**
     * 设置玩家指定货币的余额（带描述）
     *
     * @param playerId 玩家UUID
     * @param currencyId 货币ID
     * @param amount 金额
     * @param description 操作描述
     * @return 是否设置成功
     */
    public boolean setBalance(UUID playerId, String currencyId, BigDecimal amount, String description) {
        Currency currency = getCurrency(currencyId);
        if (currency == null) {
            return false;
        }

        // 检查金额是否有效
        if (!currency.isAmountValid(amount)) {
            return false;
        }

        PlayerAccount account = getPlayerAccount(playerId);
        BigDecimal oldBalance = account.getBalance(currencyId);
        BigDecimal newAmount = currency.roundAmount(amount);

        account.setBalance(currencyId, newAmount);

        // 立即保存到数据库
        if (plugin.getDatabaseManager() != null) {
            plugin.getDatabaseManager().savePlayerAccount(account);

            // 记录事务日志
            plugin.getDatabaseManager().logTransaction(
                "SET_BALANCE",
                null,
                playerId,
                currencyId,
                newAmount,
                description + " (原余额: " + currency.formatAmount(oldBalance) + ")"
            );
        }

        return true;
    }
    
    /**
     * 给玩家添加指定货币
     *
     * @param playerId 玩家UUID
     * @param currencyId 货币ID
     * @param amount 金额
     * @return 是否添加成功
     */
    public boolean addBalance(UUID playerId, String currencyId, BigDecimal amount) {
        return addBalance(playerId, currencyId, amount, "管理员增加余额");
    }

    /**
     * 给玩家添加指定货币（带描述）
     *
     * @param playerId 玩家UUID
     * @param currencyId 货币ID
     * @param amount 金额
     * @param description 操作描述
     * @return 是否添加成功
     */
    public boolean addBalance(UUID playerId, String currencyId, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        BigDecimal currentBalance = getBalance(playerId, currencyId);
        BigDecimal newBalance = currentBalance.add(amount);

        boolean success = setBalance(playerId, currencyId, newBalance, description);

        return success;
    }
    
    /**
     * 从玩家扣除指定货币
     *
     * @param playerId 玩家UUID
     * @param currencyId 货币ID
     * @param amount 金额
     * @return 是否扣除成功
     */
    public boolean subtractBalance(UUID playerId, String currencyId, BigDecimal amount) {
        return subtractBalance(playerId, currencyId, amount, "管理员扣除余额");
    }

    /**
     * 从玩家扣除指定货币（带描述）
     *
     * @param playerId 玩家UUID
     * @param currencyId 货币ID
     * @param amount 金额
     * @param description 操作描述
     * @return 是否扣除成功
     */
    public boolean subtractBalance(UUID playerId, String currencyId, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        BigDecimal currentBalance = getBalance(playerId, currencyId);
        if (currentBalance.compareTo(amount) < 0) {
            return false; // 余额不足
        }

        BigDecimal newBalance = currentBalance.subtract(amount);
        return setBalance(playerId, currencyId, newBalance, description);
    }
    
    /**
     * 检查玩家是否有足够的余额
     * 
     * @param playerId 玩家UUID
     * @param currencyId 货币ID
     * @param amount 金额
     * @return 是否有足够余额
     */
    public boolean hasBalance(UUID playerId, String currencyId, BigDecimal amount) {
        BigDecimal currentBalance = getBalance(playerId, currencyId);
        return currentBalance.compareTo(amount) >= 0;
    }
    
    /**
     * 转账
     * 
     * @param fromPlayerId 转出玩家UUID
     * @param toPlayerId 转入玩家UUID
     * @param currencyId 货币ID
     * @param amount 金额
     * @return 是否转账成功
     */
    public boolean transfer(UUID fromPlayerId, UUID toPlayerId, String currencyId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        if (!hasBalance(fromPlayerId, currencyId, amount)) {
            return false;
        }
        
        // 扣除转出方余额
        if (!subtractBalance(fromPlayerId, currencyId, amount)) {
            return false;
        }
        
        // 增加转入方余额
        if (!addBalance(toPlayerId, currencyId, amount)) {
            // 如果转入失败，回滚转出方余额
            addBalance(fromPlayerId, currencyId, amount);
            return false;
        }
        
        return true;
    }
    
    /**
     * 货币兑换
     * 
     * @param playerId 玩家UUID
     * @param fromCurrencyId 源货币ID
     * @param toCurrencyId 目标货币ID
     * @param amount 兑换金额
     * @return 是否兑换成功
     */
    public boolean exchange(UUID playerId, String fromCurrencyId, String toCurrencyId, BigDecimal amount) {
        Currency fromCurrency = getCurrency(fromCurrencyId);
        Currency toCurrency = getCurrency(toCurrencyId);
        
        if (fromCurrency == null || toCurrency == null) {
            return false;
        }
        
        // 获取兑换汇率
        BigDecimal exchangeRate = fromCurrency.getExchangeRate(toCurrencyId);
        if (exchangeRate == null) {
            return false;
        }
        
        // 计算兑换后的金额
        BigDecimal exchangedAmount = amount.multiply(exchangeRate);
        
        // 计算手续费
        double feePercentage = plugin.getConfigManager().getExchangeFeePercentage();
        BigDecimal fee = exchangedAmount.multiply(BigDecimal.valueOf(feePercentage));
        BigDecimal finalAmount = exchangedAmount.subtract(fee);
        
        // 检查余额是否足够
        if (!hasBalance(playerId, fromCurrencyId, amount)) {
            return false;
        }
        
        // 执行兑换
        if (!subtractBalance(playerId, fromCurrencyId, amount)) {
            return false;
        }
        
        if (!addBalance(playerId, toCurrencyId, finalAmount)) {
            // 兑换失败，回滚
            addBalance(playerId, fromCurrencyId, amount);
            return false;
        }
        
        return true;
    }
    
    /**
     * 获取货币
     * 
     * @param currencyId 货币ID
     * @return 货币对象
     */
    public Currency getCurrency(String currencyId) {
        return currencies.get(currencyId);
    }
    
    /**
     * 获取所有货币
     *
     * @return 货币集合
     */
    public Collection<Currency> getAllCurrencies() {
        return currencies.values();
    }

    /**
     * 获取所有玩家账户
     *
     * @return 所有玩家账户的映射
     */
    public Map<UUID, PlayerAccount> getAllPlayerAccounts() {
        return new HashMap<>(playerAccounts);
    }

    /**
     * 获取汇率
     *
     * @param fromCurrencyId 源货币ID
     * @param toCurrencyId 目标货币ID
     * @return 汇率，如果不存在则返回1.0
     */
    public BigDecimal getExchangeRate(String fromCurrencyId, String toCurrencyId) {
        if (fromCurrencyId.equals(toCurrencyId)) {
            return BigDecimal.ONE;
        }

        Currency fromCurrency = getCurrency(fromCurrencyId);
        if (fromCurrency == null) {
            return BigDecimal.ONE;
        }

        BigDecimal rate = fromCurrency.getExchangeRate(toCurrencyId);
        return rate != null ? rate : BigDecimal.ONE;
    }

    /**
     * 保存货币配置到文件
     */
    public void saveCurrencyConfigs() {
        try {
            FileConfiguration config = plugin.getConfigManager().getCurrenciesConfig();

            // 清空现有配置
            config.set("currencies", null);

            // 保存所有货币配置
            for (Currency currency : currencies.values()) {
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

            // 保存配置文件
            plugin.getConfigManager().saveConfigs();

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "保存货币配置失败！", e);
        }
    }
    
    /**
     * 获取启用的货币
     * 
     * @return 启用的货币集合
     */
    public Collection<Currency> getEnabledCurrencies() {
        return currencies.values().stream()
            .filter(Currency::isEnabled)
            .toList();
    }
    
    /**
     * 获取默认货币
     * 
     * @return 默认货币
     */
    public Currency getDefaultCurrency() {
        return getCurrency(defaultCurrencyId);
    }
    
    /**
     * 获取货币数量
     * 
     * @return 货币数量
     */
    public int getCurrencyCount() {
        return currencies.size();
    }
    
    /**
     * 检查货币是否存在
     * 
     * @param currencyId 货币ID
     * @return 是否存在
     */
    public boolean currencyExists(String currencyId) {
        return currencies.containsKey(currencyId);
    }
    
    /**
     * 重新加载货币配置
     */
    public void reloadCurrencies() {
        Map<String, Currency> oldCurrencies = new HashMap<>(currencies);
        loadCurrencies();

        // 检查是否有新添加的货币
        for (String currencyId : currencies.keySet()) {
            if (!oldCurrencies.containsKey(currencyId)) {
                // 发现新货币，为所有现有玩家添加默认余额
                addNewCurrencyToAllPlayers(currencyId);
            }
        }

        plugin.getLogger().info("货币配置已重新加载。");
    }

    /**
     * 为所有现有玩家添加新货币的默认余额
     *
     * @param currencyId 新货币ID
     */
    private void addNewCurrencyToAllPlayers(String currencyId) {
        Currency currency = getCurrency(currencyId);
        if (currency == null || currency.getDefaultBalance().compareTo(BigDecimal.ZERO) <= 0) {
            return; // 如果货币不存在或默认余额为0，则不需要处理
        }

        plugin.getLogger().info("为所有现有玩家添加新货币: " + currency.getName() + " (默认余额: " + currency.getDefaultBalance() + ")");

        int updatedCount = 0;
        for (PlayerAccount account : playerAccounts.values()) {
            // 只为余额为0的玩家设置默认余额（避免覆盖已有数据）
            if (account.getBalance(currencyId).equals(BigDecimal.ZERO)) {
                account.setBalance(currencyId, currency.getDefaultBalance());

                // 立即保存到数据库
                if (plugin.getDatabaseManager() != null) {
                    plugin.getDatabaseManager().savePlayerAccount(account);
                }
                updatedCount++;
            }
        }

        plugin.getLogger().info("已为 " + updatedCount + " 个玩家添加新货币 " + currency.getName() + " 的默认余额");
    }

    /**
     * 手动为所有玩家添加指定货币的默认余额
     *
     * @param currencyId 货币ID
     * @return 更新的玩家数量
     */
    public int addCurrencyToAllPlayers(String currencyId) {
        Currency currency = getCurrency(currencyId);
        if (currency == null) {
            return 0;
        }

        int updatedCount = 0;
        for (PlayerAccount account : playerAccounts.values()) {
            // 只为余额为0的玩家设置默认余额
            if (account.getBalance(currencyId).equals(BigDecimal.ZERO) &&
                currency.getDefaultBalance().compareTo(BigDecimal.ZERO) > 0) {
                account.setBalance(currencyId, currency.getDefaultBalance());

                // 立即保存到数据库
                if (plugin.getDatabaseManager() != null) {
                    plugin.getDatabaseManager().savePlayerAccount(account);

                    // 记录事务日志
                    plugin.getDatabaseManager().logTransaction(
                        "ADD_NEW_CURRENCY",
                        null,
                        account.getPlayerId(),
                        currencyId,
                        currency.getDefaultBalance(),
                        "系统为玩家添加新货币默认余额"
                    );
                }
                updatedCount++;
            }
        }

        return updatedCount;
    }
}
