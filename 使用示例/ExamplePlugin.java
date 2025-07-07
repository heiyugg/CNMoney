package com.example.plugin;

import cn.money.CNMoney;
import cn.money.api.CNMoneyAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;
import java.util.Set;

/**
 * CNMoney API 使用示例插件
 * 
 * 展示如何在其他插件中调用 CNMoney 的功能
 */
public class ExamplePlugin extends JavaPlugin {
    
    private CNMoneyAPI cnMoneyAPI;
    
    @Override
    public void onEnable() {
        // 检查 CNMoney 插件是否存在
        if (getServer().getPluginManager().getPlugin("CNMoney") == null) {
            getLogger().severe("CNMoney 插件未找到！请先安装 CNMoney 插件。");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // 获取 CNMoney API
        cnMoneyAPI = CNMoney.getAPI();
        if (cnMoneyAPI == null) {
            getLogger().severe("无法获取 CNMoney API！");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        getLogger().info("ExamplePlugin 已启用，CNMoney API 连接成功！");
        
        // 显示可用的货币
        Set<String> currencies = cnMoneyAPI.getCurrencies();
        getLogger().info("可用货币: " + String.join(", ", currencies));
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("此命令只能由玩家执行！");
            return true;
        }
        
        Player player = (Player) sender;
        
        switch (command.getName().toLowerCase()) {
            case "balance":
                return handleBalanceCommand(player, args);
            case "pay":
                return handlePayCommand(player, args);
            case "currencies":
                return handleCurrenciesCommand(player);
            case "exchange":
                return handleExchangeCommand(player, args);
            default:
                return false;
        }
    }
    
    /**
     * 处理余额查询命令
     */
    private boolean handleBalanceCommand(Player player, String[] args) {
        if (args.length == 0) {
            // 显示所有货币余额
            player.sendMessage("§6=== 你的货币余额 ===");
            for (String currency : cnMoneyAPI.getCurrencies()) {
                BigDecimal balance = cnMoneyAPI.getBalance(player, currency);
                String displayName = cnMoneyAPI.getCurrencyDisplayName(currency);
                player.sendMessage(String.format("§e%s: §f%s", displayName, balance.toString()));
            }
        } else {
            // 显示指定货币余额
            String currency = args[0];
            if (!cnMoneyAPI.hasCurrency(currency)) {
                player.sendMessage("§c货币 " + currency + " 不存在！");
                return true;
            }
            
            BigDecimal balance = cnMoneyAPI.getBalance(player, currency);
            String displayName = cnMoneyAPI.getCurrencyDisplayName(currency);
            player.sendMessage(String.format("§e%s 余额: §f%s", displayName, balance.toString()));
        }
        return true;
    }
    
    /**
     * 处理转账命令
     */
    private boolean handlePayCommand(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§c用法: /pay <玩家> <货币> <数量>");
            return true;
        }
        
        String targetName = args[0];
        String currency = args[1];
        
        Player target = getServer().getPlayer(targetName);
        if (target == null) {
            player.sendMessage("§c玩家 " + targetName + " 不在线！");
            return true;
        }
        
        if (!cnMoneyAPI.hasCurrency(currency)) {
            player.sendMessage("§c货币 " + currency + " 不存在！");
            return true;
        }
        
        BigDecimal amount;
        try {
            amount = new BigDecimal(args[2]);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                player.sendMessage("§c数量必须大于0！");
                return true;
            }
        } catch (NumberFormatException e) {
            player.sendMessage("§c无效的数量！");
            return true;
        }
        
        // 检查余额是否足够
        BigDecimal balance = cnMoneyAPI.getBalance(player, currency);
        if (balance.compareTo(amount) < 0) {
            player.sendMessage("§c余额不足！");
            return true;
        }
        
        // 执行转账
        boolean success1 = cnMoneyAPI.removeBalance(player, currency, amount);
        boolean success2 = cnMoneyAPI.addBalance(target, currency, amount);
        
        if (success1 && success2) {
            String displayName = cnMoneyAPI.getCurrencyDisplayName(currency);
            player.sendMessage(String.format("§a成功转账 %s %s 给 %s！", amount.toString(), displayName, target.getName()));
            target.sendMessage(String.format("§a收到来自 %s 的转账：%s %s！", player.getName(), amount.toString(), displayName));
        } else {
            player.sendMessage("§c转账失败！");
        }
        
        return true;
    }
    
    /**
     * 处理货币列表命令
     */
    private boolean handleCurrenciesCommand(Player player) {
        player.sendMessage("§6=== 可用货币列表 ===");
        for (String currency : cnMoneyAPI.getCurrencies()) {
            String displayName = cnMoneyAPI.getCurrencyDisplayName(currency);
            boolean enabled = cnMoneyAPI.isCurrencyEnabled(currency);
            String status = enabled ? "§a启用" : "§c禁用";
            player.sendMessage(String.format("§e%s §7(%s) - %s", displayName, currency, status));
        }
        return true;
    }
    
    /**
     * 处理货币兑换命令
     */
    private boolean handleExchangeCommand(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§c用法: /exchange <源货币> <目标货币> <数量>");
            return true;
        }
        
        String fromCurrency = args[0];
        String toCurrency = args[1];
        
        if (!cnMoneyAPI.hasCurrency(fromCurrency)) {
            player.sendMessage("§c源货币 " + fromCurrency + " 不存在！");
            return true;
        }
        
        if (!cnMoneyAPI.hasCurrency(toCurrency)) {
            player.sendMessage("§c目标货币 " + toCurrency + " 不存在！");
            return true;
        }
        
        BigDecimal amount;
        try {
            amount = new BigDecimal(args[2]);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                player.sendMessage("§c数量必须大于0！");
                return true;
            }
        } catch (NumberFormatException e) {
            player.sendMessage("§c无效的数量！");
            return true;
        }
        
        // 获取汇率
        BigDecimal rate = cnMoneyAPI.getExchangeRate(fromCurrency, toCurrency);
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            player.sendMessage("§c无法获取汇率或汇率无效！");
            return true;
        }
        
        // 计算兑换后的数量
        BigDecimal convertedAmount = amount.multiply(rate);
        
        // 显示兑换预览
        String fromDisplayName = cnMoneyAPI.getCurrencyDisplayName(fromCurrency);
        String toDisplayName = cnMoneyAPI.getCurrencyDisplayName(toCurrency);
        
        player.sendMessage(String.format("§6兑换预览: %s %s → %s %s", 
            amount.toString(), fromDisplayName, 
            convertedAmount.toString(), toDisplayName));
        player.sendMessage("§e输入 /confirm 确认兑换，或等待10秒自动取消");
        
        // 这里可以添加确认机制
        // 为了简化示例，直接执行兑换
        boolean success = cnMoneyAPI.exchangeCurrency(player, fromCurrency, toCurrency, amount);
        
        if (success) {
            player.sendMessage("§a兑换成功！");
        } else {
            player.sendMessage("§c兑换失败！可能是余额不足或其他错误。");
        }
        
        return true;
    }
}
