package cn.money.model;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * 货币模型类
 * 
 * @author CNMoney Team
 */
public class Currency {
    
    private final String id;
    private String name;
    private String symbol;
    private String plural;
    private int decimals;
    private BigDecimal defaultBalance;
    private boolean enabled;
    private boolean vaultPrimary;
    
    // 显示设置
    private String color;
    private String format;
    private Material icon;
    
    // 限制设置
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    
    // 兑换汇率 (相对于其他货币)
    private Map<String, BigDecimal> exchangeRates;
    
    // 格式化器
    private DecimalFormat decimalFormat;
    
    /**
     * 构造函数
     * 
     * @param id 货币ID
     */
    public Currency(String id) {
        this.id = id;
        this.exchangeRates = new HashMap<>();
        this.enabled = true;
        this.vaultPrimary = false;
        this.decimals = 2;
        this.defaultBalance = BigDecimal.ZERO;
        this.minAmount = BigDecimal.ZERO;
        this.maxAmount = BigDecimal.valueOf(-1); // -1表示无限制
        this.color = "§6";
        this.format = "{symbol}{amount}";
        this.icon = Material.GOLD_INGOT;
        
        updateDecimalFormat();
    }
    
    /**
     * 从配置文件加载货币信息
     * 
     * @param config 配置节
     */
    public void loadFromConfig(ConfigurationSection config) {
        this.name = config.getString("name", id);
        this.symbol = config.getString("symbol", "$");
        this.plural = config.getString("plural", name);
        this.decimals = config.getInt("decimals", 2);
        this.defaultBalance = BigDecimal.valueOf(config.getDouble("default-balance", 0.0));
        this.enabled = config.getBoolean("enabled", true);
        this.vaultPrimary = config.getBoolean("vault-primary", false);
        
        // 显示设置
        ConfigurationSection displayConfig = config.getConfigurationSection("display");
        if (displayConfig != null) {
            this.color = displayConfig.getString("color", "§6");
            this.format = displayConfig.getString("format", "{symbol}{amount}");
            String iconName = displayConfig.getString("icon", "GOLD_INGOT");
            try {
                this.icon = Material.valueOf(iconName.toUpperCase());
            } catch (IllegalArgumentException e) {
                this.icon = Material.GOLD_INGOT;
            }
        }
        
        // 限制设置
        ConfigurationSection limitsConfig = config.getConfigurationSection("limits");
        if (limitsConfig != null) {
            this.minAmount = BigDecimal.valueOf(limitsConfig.getDouble("min", 0.0));
            this.maxAmount = BigDecimal.valueOf(limitsConfig.getDouble("max", -1));
        }
        
        // 兑换汇率
        ConfigurationSection ratesConfig = config.getConfigurationSection("exchange-rates");
        if (ratesConfig != null) {
            this.exchangeRates.clear();
            for (String currencyId : ratesConfig.getKeys(false)) {
                double rate = ratesConfig.getDouble(currencyId);
                this.exchangeRates.put(currencyId, BigDecimal.valueOf(rate));
            }
        }
        
        updateDecimalFormat();
    }
    
    /**
     * 更新小数格式化器
     */
    private void updateDecimalFormat() {
        StringBuilder pattern = new StringBuilder("#,##0");
        if (decimals > 0) {
            pattern.append(".");
            for (int i = 0; i < decimals; i++) {
                pattern.append("0");
            }
        }
        this.decimalFormat = new DecimalFormat(pattern.toString());
    }
    
    /**
     * 格式化金额显示
     * 
     * @param amount 金额
     * @return 格式化后的字符串
     */
    public String formatAmount(BigDecimal amount) {
        String formattedAmount = decimalFormat.format(amount);
        return format.replace("{symbol}", symbol)
                    .replace("{amount}", formattedAmount)
                    .replace("{color}", color);
    }
    
    /**
     * 格式化金额显示（带颜色）
     * 
     * @param amount 金额
     * @return 格式化后的字符串
     */
    public String formatAmountWithColor(BigDecimal amount) {
        return color + formatAmount(amount);
    }
    
    /**
     * 四舍五入金额到指定小数位
     * 
     * @param amount 原始金额
     * @return 四舍五入后的金额
     */
    public BigDecimal roundAmount(BigDecimal amount) {
        return amount.setScale(decimals, RoundingMode.HALF_UP);
    }
    
    /**
     * 检查金额是否在允许范围内
     * 
     * @param amount 金额
     * @return 是否在范围内
     */
    public boolean isAmountValid(BigDecimal amount) {
        if (amount.compareTo(minAmount) < 0) {
            return false;
        }
        if (maxAmount.compareTo(BigDecimal.ZERO) >= 0 && amount.compareTo(maxAmount) > 0) {
            return false;
        }
        return true;
    }
    
    /**
     * 获取到指定货币的兑换汇率
     * 
     * @param targetCurrencyId 目标货币ID
     * @return 兑换汇率，如果不存在则返回null
     */
    public BigDecimal getExchangeRate(String targetCurrencyId) {
        return exchangeRates.get(targetCurrencyId);
    }
    
    /**
     * 设置兑换汇率
     * 
     * @param targetCurrencyId 目标货币ID
     * @param rate 汇率
     */
    public void setExchangeRate(String targetCurrencyId, BigDecimal rate) {
        exchangeRates.put(targetCurrencyId, rate);
    }
    
    // Getter和Setter方法
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public String getPlural() {
        return plural;
    }

    public void setPlural(String plural) {
        this.plural = plural;
    }

    /**
     * 获取复数形式名称（别名方法）
     */
    public String getPluralName() {
        return plural;
    }

    /**
     * 获取小数位数（别名方法）
     */
    public int getDecimalPlaces() {
        return decimals;
    }

    /**
     * 检查是否为主要货币（Vault主要货币）
     */
    public boolean isPrimary() {
        return vaultPrimary;
    }
    
    public int getDecimals() {
        return decimals;
    }
    
    public void setDecimals(int decimals) {
        this.decimals = decimals;
        updateDecimalFormat();
    }
    
    public BigDecimal getDefaultBalance() {
        return defaultBalance;
    }
    
    public void setDefaultBalance(BigDecimal defaultBalance) {
        this.defaultBalance = defaultBalance;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isVaultPrimary() {
        return vaultPrimary;
    }
    
    public void setVaultPrimary(boolean vaultPrimary) {
        this.vaultPrimary = vaultPrimary;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
    
    public Material getIcon() {
        return icon;
    }
    
    public void setIcon(Material icon) {
        this.icon = icon;
    }
    
    public BigDecimal getMinAmount() {
        return minAmount;
    }
    
    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }
    
    public BigDecimal getMaxAmount() {
        return maxAmount;
    }
    
    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }
    
    public Map<String, BigDecimal> getExchangeRates() {
        return new HashMap<>(exchangeRates);
    }
    
    @Override
    public String toString() {
        return "Currency{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", symbol='" + symbol + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
