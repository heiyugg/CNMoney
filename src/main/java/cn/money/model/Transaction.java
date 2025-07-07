package cn.money.model;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 事务记录模型类
 * 
 * @author CNMoney Team
 */
public class Transaction {
    
    private final long id;
    private final String type;
    private final UUID fromPlayer;
    private final UUID toPlayer;
    private final String currencyId;
    private final BigDecimal amount;
    private final String description;
    private final long timestamp;
    
    /**
     * 构造函数
     * 
     * @param id 事务ID
     * @param type 事务类型
     * @param fromPlayer 源玩家
     * @param toPlayer 目标玩家
     * @param currencyId 货币ID
     * @param amount 金额
     * @param description 描述
     * @param timestamp 时间戳
     */
    public Transaction(long id, String type, UUID fromPlayer, UUID toPlayer, 
                      String currencyId, BigDecimal amount, String description, long timestamp) {
        this.id = id;
        this.type = type;
        this.fromPlayer = fromPlayer;
        this.toPlayer = toPlayer;
        this.currencyId = currencyId;
        this.amount = amount;
        this.description = description;
        this.timestamp = timestamp;
    }
    
    /**
     * 构造函数（不带ID，用于创建新事务）
     * 
     * @param type 事务类型
     * @param fromPlayer 源玩家
     * @param toPlayer 目标玩家
     * @param currencyId 货币ID
     * @param amount 金额
     * @param description 描述
     */
    public Transaction(String type, UUID fromPlayer, UUID toPlayer, 
                      String currencyId, BigDecimal amount, String description) {
        this(0, type, fromPlayer, toPlayer, currencyId, amount, description, System.currentTimeMillis());
    }
    
    // Getter方法
    public long getId() {
        return id;
    }
    
    public String getType() {
        return type;
    }
    
    public UUID getFromPlayer() {
        return fromPlayer;
    }
    
    public UUID getToPlayer() {
        return toPlayer;
    }
    
    public String getCurrencyId() {
        return currencyId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * 检查事务是否涉及指定玩家
     * 
     * @param playerId 玩家ID
     * @return 是否涉及该玩家
     */
    public boolean involvesPlayer(UUID playerId) {
        return (fromPlayer != null && fromPlayer.equals(playerId)) || 
               (toPlayer != null && toPlayer.equals(playerId));
    }
    
    /**
     * 检查事务是否为指定类型
     * 
     * @param transactionType 事务类型
     * @return 是否为指定类型
     */
    public boolean isType(String transactionType) {
        return type != null && type.equalsIgnoreCase(transactionType);
    }
    
    /**
     * 检查事务是否涉及指定货币
     * 
     * @param currency 货币ID
     * @return 是否涉及该货币
     */
    public boolean involvesCurrency(String currency) {
        return currencyId != null && currencyId.equals(currency);
    }
    
    /**
     * 获取事务的简短描述
     * 
     * @return 简短描述
     */
    public String getShortDescription() {
        StringBuilder sb = new StringBuilder();
        
        switch (type.toUpperCase()) {
            case "SET_BALANCE":
                sb.append("设置余额");
                break;
            case "ADD_BALANCE":
                sb.append("增加余额");
                break;
            case "SUBTRACT_BALANCE":
            case "REMOVE_BALANCE":
                sb.append("扣除余额");
                break;
            case "TRANSFER":
                sb.append("转账");
                break;
            case "EXCHANGE":
                sb.append("货币兑换");
                break;
            default:
                sb.append(type);
                break;
        }
        
        sb.append(" ").append(amount).append(" ").append(currencyId);
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", fromPlayer=" + fromPlayer +
                ", toPlayer=" + toPlayer +
                ", currencyId='" + currencyId + '\'' +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Transaction that = (Transaction) o;
        return id == that.id;
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
