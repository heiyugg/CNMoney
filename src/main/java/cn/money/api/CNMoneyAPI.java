package cn.money.api;

import cn.money.CNMoney;
import cn.money.model.Currency;
import cn.money.model.PlayerAccount;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.UUID;

/**
 * CNMoney API接口
 * 供其他插件调用的公共API
 * 
 * @author CNMoney Team
 */
public class CNMoneyAPI {
    
    private final CNMoney plugin;
    
    public CNMoneyAPI(CNMoney plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 获取玩家指定货币的余额
     * 
     * @param playerId 玩家UUID
     * @param currencyId 货币ID
     * @return 余额
     */
    public BigDecimal getBalance(UUID playerId, String currencyId) {
        return plugin.getCurrencyManager().getBalance(playerId, currencyId);
    }
    
    /**
     * 获取玩家指定货币的余额
     * 
     * @param player 玩家
     * @param currencyId 货币ID
     * @return 余额
     */
    public BigDecimal getBalance(Player player, String currencyId) {
        return getBalance(player.getUniqueId(), currencyId);
    }
    
    /**
     * 获取玩家默认货币的余额
     * 
     * @param playerId 玩家UUID
     * @return 余额
     */
    public BigDecimal getBalance(UUID playerId) {
        return plugin.getCurrencyManager().getBalance(playerId);
    }
    
    /**
     * 获取玩家默认货币的余额
     * 
     * @param player 玩家
     * @return 余额
     */
    public BigDecimal getBalance(Player player) {
        return getBalance(player.getUniqueId());
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
        return plugin.getCurrencyManager().setBalance(playerId, currencyId, amount);
    }
    
    /**
     * 设置玩家指定货币的余额
     * 
     * @param player 玩家
     * @param currencyId 货币ID
     * @param amount 金额
     * @return 是否设置成功
     */
    public boolean setBalance(Player player, String currencyId, BigDecimal amount) {
        return setBalance(player.getUniqueId(), currencyId, amount);
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
        return plugin.getCurrencyManager().addBalance(playerId, currencyId, amount);
    }
    
    /**
     * 给玩家添加指定货币
     * 
     * @param player 玩家
     * @param currencyId 货币ID
     * @param amount 金额
     * @return 是否添加成功
     */
    public boolean addBalance(Player player, String currencyId, BigDecimal amount) {
        return addBalance(player.getUniqueId(), currencyId, amount);
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
        return plugin.getCurrencyManager().subtractBalance(playerId, currencyId, amount);
    }
    
    /**
     * 从玩家扣除指定货币
     * 
     * @param player 玩家
     * @param currencyId 货币ID
     * @param amount 金额
     * @return 是否扣除成功
     */
    public boolean subtractBalance(Player player, String currencyId, BigDecimal amount) {
        return subtractBalance(player.getUniqueId(), currencyId, amount);
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
        return plugin.getCurrencyManager().hasBalance(playerId, currencyId, amount);
    }
    
    /**
     * 检查玩家是否有足够的余额
     * 
     * @param player 玩家
     * @param currencyId 货币ID
     * @param amount 金额
     * @return 是否有足够余额
     */
    public boolean hasBalance(Player player, String currencyId, BigDecimal amount) {
        return hasBalance(player.getUniqueId(), currencyId, amount);
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
        return plugin.getCurrencyManager().transfer(fromPlayerId, toPlayerId, currencyId, amount);
    }
    
    /**
     * 转账
     * 
     * @param fromPlayer 转出玩家
     * @param toPlayer 转入玩家
     * @param currencyId 货币ID
     * @param amount 金额
     * @return 是否转账成功
     */
    public boolean transfer(Player fromPlayer, Player toPlayer, String currencyId, BigDecimal amount) {
        return transfer(fromPlayer.getUniqueId(), toPlayer.getUniqueId(), currencyId, amount);
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
        return plugin.getCurrencyManager().exchange(playerId, fromCurrencyId, toCurrencyId, amount);
    }
    
    /**
     * 货币兑换
     * 
     * @param player 玩家
     * @param fromCurrencyId 源货币ID
     * @param toCurrencyId 目标货币ID
     * @param amount 兑换金额
     * @return 是否兑换成功
     */
    public boolean exchange(Player player, String fromCurrencyId, String toCurrencyId, BigDecimal amount) {
        return exchange(player.getUniqueId(), fromCurrencyId, toCurrencyId, amount);
    }
    
    /**
     * 获取货币
     * 
     * @param currencyId 货币ID
     * @return 货币对象
     */
    public Currency getCurrency(String currencyId) {
        return plugin.getCurrencyManager().getCurrency(currencyId);
    }
    
    /**
     * 获取所有货币
     * 
     * @return 货币集合
     */
    public Collection<Currency> getAllCurrencies() {
        return plugin.getCurrencyManager().getAllCurrencies();
    }
    
    /**
     * 获取启用的货币
     * 
     * @return 启用的货币集合
     */
    public Collection<Currency> getEnabledCurrencies() {
        return plugin.getCurrencyManager().getEnabledCurrencies();
    }
    
    /**
     * 获取默认货币
     * 
     * @return 默认货币
     */
    public Currency getDefaultCurrency() {
        return plugin.getCurrencyManager().getDefaultCurrency();
    }
    
    /**
     * 检查货币是否存在
     * 
     * @param currencyId 货币ID
     * @return 是否存在
     */
    public boolean currencyExists(String currencyId) {
        return plugin.getCurrencyManager().currencyExists(currencyId);
    }
    
    /**
     * 获取玩家账户
     * 
     * @param playerId 玩家UUID
     * @return 玩家账户
     */
    public PlayerAccount getPlayerAccount(UUID playerId) {
        return plugin.getCurrencyManager().getPlayerAccount(playerId);
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
     * 格式化金额显示
     * 
     * @param currencyId 货币ID
     * @param amount 金额
     * @return 格式化后的字符串
     */
    public String formatAmount(String currencyId, BigDecimal amount) {
        Currency currency = getCurrency(currencyId);
        if (currency == null) {
            return amount.toString();
        }
        return currency.formatAmount(amount);
    }
    
    /**
     * 格式化金额显示（带颜色）
     * 
     * @param currencyId 货币ID
     * @param amount 金额
     * @return 格式化后的字符串
     */
    public String formatAmountWithColor(String currencyId, BigDecimal amount) {
        Currency currency = getCurrency(currencyId);
        if (currency == null) {
            return amount.toString();
        }
        return currency.formatAmountWithColor(amount);
    }
    
    /**
     * 获取插件版本
     * 
     * @return 版本字符串
     */
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    /**
     * 检查API是否可用
     * 
     * @return 是否可用
     */
    public boolean isAvailable() {
        return plugin.isEnabled() && plugin.getCurrencyManager() != null;
    }
}
