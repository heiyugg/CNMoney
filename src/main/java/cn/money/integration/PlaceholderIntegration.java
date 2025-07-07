package cn.money.integration;

import cn.money.CNMoney;
import cn.money.model.Currency;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;

/**
 * PlaceholderAPI集成
 * 
 * @author CNMoney Team
 */
public class PlaceholderIntegration extends PlaceholderExpansion {
    
    private final CNMoney plugin;
    
    public PlaceholderIntegration(CNMoney plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getIdentifier() {
        return "cnmoney";
    }
    
    @Override
    public String getAuthor() {
        return "CNMoney Team";
    }
    
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) {
            return "";
        }
        
        // %cnmoney_balance% - 默认货币余额
        if (params.equals("balance")) {
            BigDecimal balance = plugin.getCurrencyManager().getBalance(player.getUniqueId());
            Currency defaultCurrency = plugin.getCurrencyManager().getDefaultCurrency();
            return defaultCurrency.formatAmount(balance);
        }
        
        // %cnmoney_balance_<货币ID>% - 指定货币余额
        if (params.startsWith("balance_")) {
            String currencyId = params.substring(8);
            Currency currency = plugin.getCurrencyManager().getCurrency(currencyId);
            if (currency != null) {
                BigDecimal balance = plugin.getCurrencyManager().getBalance(player.getUniqueId(), currencyId);
                return currency.formatAmount(balance);
            }
            return "0";
        }
        
        // %cnmoney_balance_raw% - 默认货币余额（纯数字）
        if (params.equals("balance_raw")) {
            BigDecimal balance = plugin.getCurrencyManager().getBalance(player.getUniqueId());
            return balance.toString();
        }
        
        // %cnmoney_balance_raw_<货币ID>% - 指定货币余额（纯数字）
        if (params.startsWith("balance_raw_")) {
            String currencyId = params.substring(12);
            if (plugin.getCurrencyManager().currencyExists(currencyId)) {
                BigDecimal balance = plugin.getCurrencyManager().getBalance(player.getUniqueId(), currencyId);
                return balance.toString();
            }
            return "0";
        }
        
        // %cnmoney_balance_formatted% - 默认货币余额（带颜色）
        if (params.equals("balance_formatted")) {
            BigDecimal balance = plugin.getCurrencyManager().getBalance(player.getUniqueId());
            Currency defaultCurrency = plugin.getCurrencyManager().getDefaultCurrency();
            return defaultCurrency.formatAmountWithColor(balance);
        }
        
        // %cnmoney_balance_formatted_<货币ID>% - 指定货币余额（带颜色）
        if (params.startsWith("balance_formatted_")) {
            String currencyId = params.substring(18);
            Currency currency = plugin.getCurrencyManager().getCurrency(currencyId);
            if (currency != null) {
                BigDecimal balance = plugin.getCurrencyManager().getBalance(player.getUniqueId(), currencyId);
                return currency.formatAmountWithColor(balance);
            }
            return "§c0";
        }
        
        // %cnmoney_currency_name% - 默认货币名称
        if (params.equals("currency_name")) {
            return plugin.getCurrencyManager().getDefaultCurrency().getName();
        }
        
        // %cnmoney_currency_name_<货币ID>% - 指定货币名称
        if (params.startsWith("currency_name_")) {
            String currencyId = params.substring(14);
            Currency currency = plugin.getCurrencyManager().getCurrency(currencyId);
            if (currency != null) {
                return currency.getName();
            }
            return "未知货币";
        }
        
        // %cnmoney_currency_symbol% - 默认货币符号
        if (params.equals("currency_symbol")) {
            return plugin.getCurrencyManager().getDefaultCurrency().getSymbol();
        }
        
        // %cnmoney_currency_symbol_<货币ID>% - 指定货币符号
        if (params.startsWith("currency_symbol_")) {
            String currencyId = params.substring(16);
            Currency currency = plugin.getCurrencyManager().getCurrency(currencyId);
            if (currency != null) {
                return currency.getSymbol();
            }
            return "$";
        }
        
        // %cnmoney_currency_plural% - 默认货币复数形式
        if (params.equals("currency_plural")) {
            return plugin.getCurrencyManager().getDefaultCurrency().getPlural();
        }
        
        // %cnmoney_currency_plural_<货币ID>% - 指定货币复数形式
        if (params.startsWith("currency_plural_")) {
            String currencyId = params.substring(16);
            Currency currency = plugin.getCurrencyManager().getCurrency(currencyId);
            if (currency != null) {
                return currency.getPlural();
            }
            return "未知货币";
        }
        
        // %cnmoney_top_balance_<排名>% - 余额排行榜（默认货币）
        if (params.startsWith("top_balance_")) {
            try {
                int rank = Integer.parseInt(params.substring(12));
                // TODO: 实现排行榜功能
                return "排行榜功能开发中";
            } catch (NumberFormatException e) {
                return "无效排名";
            }
        }
        
        // %cnmoney_top_balance_<货币ID>_<排名>% - 指定货币余额排行榜
        if (params.startsWith("top_balance_") && params.contains("_")) {
            String[] parts = params.split("_");
            if (parts.length >= 3) {
                try {
                    String currencyId = parts[2];
                    int rank = Integer.parseInt(parts[3]);
                    // TODO: 实现排行榜功能
                    return "排行榜功能开发中";
                } catch (NumberFormatException e) {
                    return "无效排名";
                }
            }
        }
        
        // %cnmoney_has_<金额>% - 检查是否有足够余额（默认货币）
        if (params.startsWith("has_")) {
            try {
                BigDecimal amount = new BigDecimal(params.substring(4));
                boolean hasBalance = plugin.getCurrencyManager().hasBalance(
                    player.getUniqueId(), 
                    plugin.getCurrencyManager().getDefaultCurrency().getId(), 
                    amount
                );
                return hasBalance ? "true" : "false";
            } catch (NumberFormatException e) {
                return "false";
            }
        }
        
        // %cnmoney_has_<货币ID>_<金额>% - 检查指定货币是否有足够余额
        if (params.startsWith("has_") && params.contains("_")) {
            String[] parts = params.split("_", 3);
            if (parts.length >= 3) {
                try {
                    String currencyId = parts[1];
                    BigDecimal amount = new BigDecimal(parts[2]);
                    boolean hasBalance = plugin.getCurrencyManager().hasBalance(
                        player.getUniqueId(), 
                        currencyId, 
                        amount
                    );
                    return hasBalance ? "true" : "false";
                } catch (NumberFormatException e) {
                    return "false";
                }
            }
        }
        
        // %cnmoney_exchange_rate_<源货币>_<目标货币>% - 获取兑换汇率
        if (params.startsWith("exchange_rate_")) {
            String[] parts = params.split("_");
            if (parts.length >= 4) {
                String fromCurrency = parts[2];
                String toCurrency = parts[3];
                Currency currency = plugin.getCurrencyManager().getCurrency(fromCurrency);
                if (currency != null) {
                    BigDecimal rate = currency.getExchangeRate(toCurrency);
                    return rate != null ? rate.toString() : "0";
                }
            }
            return "0";
        }
        
        // %cnmoney_version% - 插件版本
        if (params.equals("version")) {
            return plugin.getDescription().getVersion();
        }
        
        // %cnmoney_currency_count% - 货币数量
        if (params.equals("currency_count")) {
            return String.valueOf(plugin.getCurrencyManager().getCurrencyCount());
        }
        
        // %cnmoney_default_currency% - 默认货币ID
        if (params.equals("default_currency")) {
            return plugin.getCurrencyManager().getDefaultCurrency().getId();
        }
        
        return null; // 未知占位符
    }
}
