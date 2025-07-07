package cn.money.model;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 玩家账户模型类
 * 
 * @author CNMoney Team
 */
public class PlayerAccount {
    
    private final UUID playerId;
    private final Map<String, BigDecimal> balances;
    private long lastUpdated;
    
    /**
     * 构造函数
     * 
     * @param playerId 玩家UUID
     */
    public PlayerAccount(UUID playerId) {
        this.playerId = playerId;
        this.balances = new ConcurrentHashMap<>();
        this.lastUpdated = System.currentTimeMillis();
    }
    
    /**
     * 获取指定货币的余额
     * 
     * @param currencyId 货币ID
     * @return 余额
     */
    public BigDecimal getBalance(String currencyId) {
        return balances.getOrDefault(currencyId, BigDecimal.ZERO);
    }
    
    /**
     * 设置指定货币的余额
     * 
     * @param currencyId 货币ID
     * @param amount 金额
     */
    public void setBalance(String currencyId, BigDecimal amount) {
        balances.put(currencyId, amount);
        updateLastModified();
    }
    
    /**
     * 添加指定货币的余额
     * 
     * @param currencyId 货币ID
     * @param amount 金额
     */
    public void addBalance(String currencyId, BigDecimal amount) {
        BigDecimal currentBalance = getBalance(currencyId);
        setBalance(currencyId, currentBalance.add(amount));
    }
    
    /**
     * 扣除指定货币的余额
     * 
     * @param currencyId 货币ID
     * @param amount 金额
     * @return 是否扣除成功
     */
    public boolean subtractBalance(String currencyId, BigDecimal amount) {
        BigDecimal currentBalance = getBalance(currencyId);
        if (currentBalance.compareTo(amount) < 0) {
            return false; // 余额不足
        }
        setBalance(currencyId, currentBalance.subtract(amount));
        return true;
    }
    
    /**
     * 检查是否有足够的余额
     * 
     * @param currencyId 货币ID
     * @param amount 金额
     * @return 是否有足够余额
     */
    public boolean hasBalance(String currencyId, BigDecimal amount) {
        return getBalance(currencyId).compareTo(amount) >= 0;
    }
    
    /**
     * 获取所有余额
     * 
     * @return 余额映射
     */
    public Map<String, BigDecimal> getAllBalances() {
        return new ConcurrentHashMap<>(balances);
    }
    
    /**
     * 更新最后修改时间
     */
    private void updateLastModified() {
        this.lastUpdated = System.currentTimeMillis();
    }
    
    // Getter方法
    public UUID getPlayerId() {
        return playerId;
    }
    
    public long getLastUpdated() {
        return lastUpdated;
    }
    
    @Override
    public String toString() {
        return "PlayerAccount{" +
                "playerId=" + playerId +
                ", balances=" + balances +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
