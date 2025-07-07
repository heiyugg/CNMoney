package cn.money.command;

import cn.money.CNMoney;
import cn.money.model.Currency;
import cn.money.model.PlayerAccount;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 货币命令处理器
 * 
 * @author CNMoney Team
 */
public class MoneyCommand implements CommandExecutor, TabCompleter {
    
    private final CNMoney plugin;
    
    public MoneyCommand(CNMoney plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandName = command.getName().toLowerCase();
        
        switch (commandName) {
            case "cm":
            case "cnmoney":
            case "money":
                return handleMainCommand(sender, args);
            case "balance":
            case "bal":
                return handleBalanceCommand(sender, args);
            case "pay":
                return handlePayCommand(sender, args);
            case "eco":
                return handleEcoCommand(sender, args);
            default:
                return false;
        }
    }
    
    /**
     * 处理主命令
     */
    private boolean handleMainCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        
        switch (subCommand) {
            case "help":
                sendHelpMessage(sender);
                return true;
            case "balance":
            case "bal":
                return handleBalanceCommand(sender, subArgs);
            case "pay":
                return handlePayCommand(sender, subArgs);
            case "exchange":
                return handleExchangeCommand(sender, subArgs);
            case "gui":
                return handleGUICommand(sender, subArgs);
            case "reload":
                return handleReloadCommand(sender);
            case "info":
                return handleInfoCommand(sender, subArgs);
            case "dbtest":
                return handleDBTestCommand(sender);
            case "save":
                return handleSaveCommand(sender);
            case "log":
                return handleLogCommand(sender, subArgs);
            case "addcurrency":
                return handleAddCurrencyCommand(sender, subArgs);
            default:
                sendMessage(sender, "§c未知的子命令: " + subCommand);
                sendMessage(sender, "§e使用 /cm help 查看帮助");
                return true;
        }
    }
    
    /**
     * 处理余额命令
     */
    private boolean handleBalanceCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("cnmoney.balance")) {
            sendMessage(sender, "§c你没有权限使用此命令！");
            return true;
        }
        
        Player targetPlayer;
        String currencyId = null;
        
        if (args.length == 0) {
            // /balance - 查看自己的默认货币余额
            if (!(sender instanceof Player)) {
                sendMessage(sender, "§c控制台必须指定玩家名！");
                return true;
            }
            targetPlayer = (Player) sender;
        } else if (args.length == 1) {
            // /balance [玩家] 或 /balance [货币]
            Player player = Bukkit.getPlayer(args[0]);
            if (player != null) {
                // 是玩家名
                if (!sender.hasPermission("cnmoney.balance.others")) {
                    sendMessage(sender, "§c你没有权限查看其他玩家的余额！");
                    return true;
                }
                targetPlayer = player;
            } else {
                // 可能是货币ID
                if (!(sender instanceof Player)) {
                    sendMessage(sender, "§c玩家不在线或不存在！");
                    return true;
                }
                if (plugin.getCurrencyManager().currencyExists(args[0])) {
                    targetPlayer = (Player) sender;
                    currencyId = args[0];
                } else {
                    sendMessage(sender, "§c玩家不在线或货币不存在！");
                    return true;
                }
            }
        } else if (args.length == 2) {
            // /balance [玩家] [货币]
            if (!sender.hasPermission("cnmoney.balance.others")) {
                sendMessage(sender, "§c你没有权限查看其他玩家的余额！");
                return true;
            }
            targetPlayer = Bukkit.getPlayer(args[0]);
            if (targetPlayer == null) {
                sendMessage(sender, "§c玩家不在线或不存在！");
                return true;
            }
            currencyId = args[1];
            if (!plugin.getCurrencyManager().currencyExists(currencyId)) {
                sendMessage(sender, "§c货币不存在！");
                return true;
            }
        } else {
            sendMessage(sender, "§c用法: /balance [玩家] [货币]");
            return true;
        }
        
        showBalance(sender, targetPlayer, currencyId);
        return true;
    }
    
    /**
     * 处理转账命令
     */
    private boolean handlePayCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("cnmoney.pay")) {
            sendMessage(sender, "§c你没有权限使用此命令！");
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sendMessage(sender, "§c此命令只能由玩家使用！");
            return true;
        }
        
        if (args.length < 2) {
            sendMessage(sender, "§c用法: /pay <玩家> <金额> [货币]");
            return true;
        }
        
        Player fromPlayer = (Player) sender;
        Player toPlayer = Bukkit.getPlayer(args[0]);
        
        if (toPlayer == null) {
            sendMessage(sender, "§c玩家不在线或不存在！");
            return true;
        }
        
        if (fromPlayer.equals(toPlayer)) {
            sendMessage(sender, "§c你不能转账给自己！");
            return true;
        }
        
        BigDecimal amount;
        try {
            amount = new BigDecimal(args[1]);
        } catch (NumberFormatException e) {
            sendMessage(sender, "§c无效的金额！");
            return true;
        }
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            sendMessage(sender, "§c金额必须大于0！");
            return true;
        }
        
        String currencyId = args.length > 2 ? args[2] : 
                           plugin.getCurrencyManager().getDefaultCurrency().getId();
        
        Currency currency = plugin.getCurrencyManager().getCurrency(currencyId);
        if (currency == null) {
            sendMessage(sender, "§c货币不存在！");
            return true;
        }
        
        // 检查转账限制
        double minAmount = plugin.getConfigManager().getTransferMinAmount();
        double maxAmount = plugin.getConfigManager().getTransferMaxAmount();
        
        if (amount.doubleValue() < minAmount) {
            sendMessage(sender, "§c转账金额不能少于 " + minAmount + "！");
            return true;
        }
        
        if (maxAmount > 0 && amount.doubleValue() > maxAmount) {
            sendMessage(sender, "§c转账金额不能超过 " + maxAmount + "！");
            return true;
        }
        
        // 执行转账
        if (plugin.getCurrencyManager().transfer(fromPlayer.getUniqueId(), 
                                               toPlayer.getUniqueId(), 
                                               currencyId, amount)) {
            sendMessage(fromPlayer, "§a成功转账 " + currency.formatAmountWithColor(amount) + 
                                  " 给 " + toPlayer.getName());
            sendMessage(toPlayer, "§a收到来自 " + fromPlayer.getName() + " 的转账: " + 
                                currency.formatAmountWithColor(amount));
            
            // 记录交易
            plugin.getDatabaseManager().logTransaction("TRANSFER", 
                                                     fromPlayer.getUniqueId(), 
                                                     toPlayer.getUniqueId(), 
                                                     currencyId, amount, 
                                                     "转账");
        } else {
            sendMessage(sender, "§c转账失败！余额不足或其他错误。");
        }
        
        return true;
    }
    
    /**
     * 处理经济管理命令
     */
    private boolean handleEcoCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("cnmoney.admin")) {
            sendMessage(sender, "§c你没有权限使用此命令！");
            return true;
        }
        
        if (args.length < 3) {
            sendMessage(sender, "§c用法: /eco <give|take|set> <玩家> <金额> [货币]");
            return true;
        }
        
        String action = args[0].toLowerCase();
        Player targetPlayer = Bukkit.getPlayer(args[1]);
        
        if (targetPlayer == null) {
            sendMessage(sender, "§c玩家不在线或不存在！");
            return true;
        }
        
        BigDecimal amount;
        try {
            amount = new BigDecimal(args[2]);
        } catch (NumberFormatException e) {
            sendMessage(sender, "§c无效的金额！");
            return true;
        }
        
        String currencyId = args.length > 3 ? args[3] : 
                           plugin.getCurrencyManager().getDefaultCurrency().getId();
        
        Currency currency = plugin.getCurrencyManager().getCurrency(currencyId);
        if (currency == null) {
            sendMessage(sender, "§c货币不存在！");
            return true;
        }
        
        boolean success = false;
        String actionName = "";
        
        switch (action) {
            case "give":
                success = plugin.getCurrencyManager().addBalance(targetPlayer.getUniqueId(), 
                                                               currencyId, amount);
                actionName = "给予";
                break;
            case "take":
                success = plugin.getCurrencyManager().subtractBalance(targetPlayer.getUniqueId(), 
                                                                     currencyId, amount);
                actionName = "扣除";
                break;
            case "set":
                success = plugin.getCurrencyManager().setBalance(targetPlayer.getUniqueId(), 
                                                               currencyId, amount);
                actionName = "设置";
                break;
            default:
                sendMessage(sender, "§c无效的操作！使用: give, take, set");
                return true;
        }
        
        if (success) {
            sendMessage(sender, "§a成功" + actionName + " " + targetPlayer.getName() + " " + 
                              currency.formatAmountWithColor(amount));
            sendMessage(targetPlayer, "§a管理员" + actionName + "了你 " + 
                                    currency.formatAmountWithColor(amount));
            
            // 记录交易
            plugin.getDatabaseManager().logTransaction("ADMIN_" + action.toUpperCase(), 
                                                     null, 
                                                     targetPlayer.getUniqueId(), 
                                                     currencyId, amount, 
                                                     "管理员操作: " + actionName);
        } else {
            sendMessage(sender, "§c操作失败！");
        }
        
        return true;
    }
    
    /**
     * 处理兑换命令
     */
    private boolean handleExchangeCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("cnmoney.exchange")) {
            sendMessage(sender, "§c你没有权限使用此命令！");
            return true;
        }

        if (!(sender instanceof Player)) {
            sendMessage(sender, "§c此命令只能由玩家使用！");
            return true;
        }

        Player player = (Player) sender;

        if (!plugin.getConfigManager().isExchangeEnabled()) {
            sendMessage(sender, "§c兑换功能已被禁用！");
            return true;
        }

        // 打开兑换选择GUI
        plugin.getGUIManager().openExchangeSelectGUI(player);
        return true;
    }
    
    /**
     * 处理GUI命令
     */
    private boolean handleGUICommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("cnmoney.gui")) {
            sendMessage(sender, "§c你没有权限使用此命令！");
            return true;
        }

        if (!(sender instanceof Player)) {
            sendMessage(sender, "§c此命令只能由玩家使用！");
            return true;
        }

        Player player = (Player) sender;

        // 根据参数决定打开哪个GUI
        if (args.length > 0) {
            String subCommand = args[0].toLowerCase();
            switch (subCommand) {
                case "balance":
                case "bal":
                    plugin.getGUIManager().openBalanceGUI(player);
                    break;
                case "exchange":
                    if (plugin.getConfigManager().isExchangeEnabled()) {
                        plugin.getGUIManager().openExchangeSelectGUI(player);
                    } else {
                        sendMessage(sender, "§c兑换功能已被禁用！");
                    }
                    break;
                default:
                    plugin.getGUIManager().openMainGUI(player);
                    break;
            }
        } else {
            // 打开主GUI界面
            plugin.getGUIManager().openMainGUI(player);
        }

        return true;
    }
    
    /**
     * 处理重载命令
     */
    private boolean handleReloadCommand(CommandSender sender) {
        if (!sender.hasPermission("cnmoney.admin")) {
            sendMessage(sender, "§c你没有权限使用此命令！");
            return true;
        }

        plugin.getConfigManager().reloadConfigs();
        plugin.getCurrencyManager().reloadCurrencies();
        sendMessage(sender, "§aCNMoney配置已重新加载！");
        return true;
    }

    /**
     * 处理数据库测试命令
     */
    private boolean handleDBTestCommand(CommandSender sender) {
        if (!sender.hasPermission("cnmoney.admin")) {
            sendMessage(sender, "§c你没有权限使用此命令！");
            return true;
        }

        sendMessage(sender, "§e正在测试数据库连接...");

        // 数据库连接测试
        if (plugin.getDatabaseManager().isConnectionValid()) {
            sendMessage(sender, "§a数据库连接正常！");

            // 显示数据库中的玩家数量
            try {
                int playerCount = plugin.getCurrencyManager().getAllPlayerAccounts().size();
                sendMessage(sender, "§b内存中的玩家账户数量: " + playerCount);

                // 如果是玩家，显示其余额信息
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    BigDecimal goldBalance = plugin.getCurrencyManager().getBalance(player.getUniqueId(), "gold");
                    BigDecimal silverBalance = plugin.getCurrencyManager().getBalance(player.getUniqueId(), "silver");
                    sendMessage(sender, "§b您当前的金币余额: " + goldBalance);
                    sendMessage(sender, "§b您当前的银币余额: " + silverBalance);

                    // 测试立即保存功能
                    sendMessage(sender, "§e正在测试立即保存功能...");
                    plugin.getCurrencyManager().saveAllData();
                    sendMessage(sender, "§a数据已保存到数据库！");
                }

                sendMessage(sender, "§a数据库测试完成！");
            } catch (Exception e) {
                sendMessage(sender, "§c数据库测试时发生错误: " + e.getMessage());
                plugin.getLogger().warning("数据库测试错误: " + e.getMessage());
            }
        } else {
            sendMessage(sender, "§c数据库连接失败！请检查配置。");
        }
        return true;
    }

    /**
     * 处理保存命令
     */
    private boolean handleSaveCommand(CommandSender sender) {
        if (!sender.hasPermission("cnmoney.admin")) {
            sendMessage(sender, "§c你没有权限使用此命令！");
            return true;
        }

        sendMessage(sender, "§e正在保存所有数据...");
        plugin.getCurrencyManager().saveAllData();
        sendMessage(sender, "§a所有数据已保存到数据库！");
        return true;
    }

    /**
     * 处理日志控制命令
     */
    private boolean handleLogCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("cnmoney.admin")) {
            sendMessage(sender, "§c你没有权限使用此命令！");
            return true;
        }

        if (args.length == 0) {
            // 显示当前日志设置
            sendMessage(sender, "§6=== CNMoney 日志设置 ===");
            sendMessage(sender, "§eGUI操作日志: " + (plugin.getConfigManager().isGUIOperationLoggingEnabled() ? "§a启用" : "§c禁用"));
            sendMessage(sender, "§e数据库操作日志: " + (plugin.getConfigManager().isDatabaseOperationLoggingEnabled() ? "§a启用" : "§c禁用"));
            sendMessage(sender, "§e调试信息日志: " + (plugin.getConfigManager().isDebugInfoLoggingEnabled() ? "§a启用" : "§c禁用"));
            sendMessage(sender, "");
            sendMessage(sender, "§7使用方法:");
            sendMessage(sender, "§7/cm log gui <true|false> - 控制GUI操作日志");
            sendMessage(sender, "§7/cm log database <true|false> - 控制数据库操作日志");
            sendMessage(sender, "§7/cm log debug <true|false> - 控制调试信息日志");
            return true;
        }

        if (args.length != 2) {
            sendMessage(sender, "§c用法: /cm log <类型> <true|false>");
            return true;
        }

        String logType = args[0].toLowerCase();
        String value = args[1].toLowerCase();

        if (!value.equals("true") && !value.equals("false")) {
            sendMessage(sender, "§c值必须是 true 或 false！");
            return true;
        }

        boolean enable = value.equals("true");

        switch (logType) {
            case "gui":
                plugin.getConfigManager().getConfig().set("logging.console.gui-operations", enable);
                sendMessage(sender, "§aGUI操作日志已" + (enable ? "启用" : "禁用"));
                break;
            case "database":
                plugin.getConfigManager().getConfig().set("logging.console.database-operations", enable);
                sendMessage(sender, "§a数据库操作日志已" + (enable ? "启用" : "禁用"));
                break;
            case "debug":
                plugin.getConfigManager().getConfig().set("logging.console.debug-info", enable);
                sendMessage(sender, "§a调试信息日志已" + (enable ? "启用" : "禁用"));
                break;
            default:
                sendMessage(sender, "§c未知的日志类型: " + logType);
                sendMessage(sender, "§7可用类型: gui, database, debug");
                return true;
        }

        // 保存配置
        plugin.getConfigManager().saveConfig();
        sendMessage(sender, "§a配置已保存！");

        return true;
    }

    /**
     * 处理添加货币命令
     */
    private boolean handleAddCurrencyCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("cnmoney.admin")) {
            sendMessage(sender, "§c你没有权限使用此命令！");
            return true;
        }

        if (args.length < 1) {
            sendMessage(sender, "§c用法: /cm addcurrency <货币ID>");
            sendMessage(sender, "§e此命令将为所有现有玩家添加指定货币的默认余额");
            return true;
        }

        String currencyId = args[0];
        Currency currency = plugin.getCurrencyManager().getCurrency(currencyId);

        if (currency == null) {
            sendMessage(sender, "§c货币不存在: " + currencyId);
            sendMessage(sender, "§e可用货币: " + String.join(", ",
                plugin.getCurrencyManager().getEnabledCurrencies().stream()
                    .map(Currency::getId)
                    .toArray(String[]::new)));
            return true;
        }

        if (currency.getDefaultBalance().compareTo(BigDecimal.ZERO) <= 0) {
            sendMessage(sender, "§c货币 " + currency.getName() + " 的默认余额为0，无需添加");
            return true;
        }

        sendMessage(sender, "§e正在为所有玩家添加货币 " + currency.getName() + "...");

        int updatedCount = plugin.getCurrencyManager().addCurrencyToAllPlayers(currencyId);

        if (updatedCount > 0) {
            sendMessage(sender, "§a成功为 " + updatedCount + " 个玩家添加了 " +
                              currency.getName() + " (默认余额: " +
                              currency.formatAmount(currency.getDefaultBalance()) + ")");
        } else {
            sendMessage(sender, "§e没有玩家需要添加此货币，所有玩家都已拥有该货币");
        }

        return true;
    }
    
    /**
     * 处理信息命令
     */
    private boolean handleInfoCommand(CommandSender sender, String[] args) {
        sendMessage(sender, "§6§l=== CNMoney 插件信息 ===");
        sendMessage(sender, "§e版本: §f" + plugin.getDescription().getVersion());
        sendMessage(sender, "§e支持的货币数量: §f" + plugin.getCurrencyManager().getCurrencyCount());
        sendMessage(sender, "§e默认货币: §f" + plugin.getCurrencyManager().getDefaultCurrency().getName());
        sendMessage(sender, "§e数据库类型: §f" + plugin.getConfigManager().getDatabaseType());
        return true;
    }
    
    /**
     * 显示余额
     */
    private void showBalance(CommandSender sender, Player targetPlayer, String currencyId) {
        if (currencyId == null) {
            // 显示所有货币余额
            sendMessage(sender, "§6§l=== " + targetPlayer.getName() + " 的余额 ===");
            for (Currency currency : plugin.getCurrencyManager().getEnabledCurrencies()) {
                BigDecimal balance = plugin.getCurrencyManager().getBalance(targetPlayer.getUniqueId(), 
                                                                          currency.getId());
                sendMessage(sender, "§e" + currency.getName() + ": " + 
                                  currency.formatAmountWithColor(balance));
            }
        } else {
            // 显示指定货币余额
            Currency currency = plugin.getCurrencyManager().getCurrency(currencyId);
            BigDecimal balance = plugin.getCurrencyManager().getBalance(targetPlayer.getUniqueId(), 
                                                                      currencyId);
            sendMessage(sender, targetPlayer.getName() + " 的" + currency.getName() + "余额: " + 
                              currency.formatAmountWithColor(balance));
        }
    }
    
    /**
     * 发送帮助信息
     */
    private void sendHelpMessage(CommandSender sender) {
        sendMessage(sender, "§6§l=== CNMoney 命令帮助 ===");
        sendMessage(sender, "§e/cm balance [玩家] [货币] §7- 查看余额");
        sendMessage(sender, "§e/cm pay <玩家> <金额> [货币] §7- 转账");
        sendMessage(sender, "§e/cm exchange §7- 货币兑换");
        sendMessage(sender, "§e/cm gui §7- 打开GUI界面");
        sendMessage(sender, "§e/cm info §7- 查看插件信息");
        if (sender.hasPermission("cnmoney.admin")) {
            sendMessage(sender, "§c/cm reload §7- 重载配置");
            sendMessage(sender, "§c/cm save §7- 保存所有数据");
            sendMessage(sender, "§c/cm dbtest §7- 测试数据库连接");
            sendMessage(sender, "§c/cm log [类型] [true|false] §7- 控制日志输出");
            sendMessage(sender, "§c/cm addcurrency <货币ID> §7- 为所有玩家添加新货币");
            sendMessage(sender, "§c/eco <give|take|set> <玩家> <金额> [货币] §7- 管理经济");
        }
    }
    
    /**
     * 发送消息
     */
    private void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(plugin.getConfigManager().getMessagePrefix() + message);
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (command.getName().equalsIgnoreCase("cm")) {
            if (args.length == 1) {
                completions.addAll(Arrays.asList("balance", "pay", "exchange", "gui", "info", "help"));
                if (sender.hasPermission("cnmoney.admin")) {
                    completions.addAll(Arrays.asList("reload", "save", "dbtest", "log", "addcurrency"));
                }
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("balance") || args[0].equalsIgnoreCase("pay")) {
                    return Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .collect(Collectors.toList());
                } else if (args[0].equalsIgnoreCase("log") && sender.hasPermission("cnmoney.admin")) {
                    // 为log命令提供日志类型补全
                    return Arrays.asList("gui", "database", "debug");
                } else if (args[0].equalsIgnoreCase("addcurrency")) {
                    return plugin.getCurrencyManager().getEnabledCurrencies().stream()
                            .map(Currency::getId)
                            .collect(Collectors.toList());
                }
            } else if (args.length == 3) {
                if (args[0].equalsIgnoreCase("balance") || args[0].equalsIgnoreCase("pay")) {
                    return plugin.getCurrencyManager().getEnabledCurrencies().stream()
                            .map(Currency::getId)
                            .collect(Collectors.toList());
                } else if (args[0].equalsIgnoreCase("log") && sender.hasPermission("cnmoney.admin")) {
                    // 为log命令的第二个参数提供true/false补全
                    return Arrays.asList("true", "false");
                }
            }
        } else if (command.getName().equalsIgnoreCase("balance")) {
            if (args.length == 1) {
                completions.addAll(Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList()));
                completions.addAll(plugin.getCurrencyManager().getEnabledCurrencies().stream()
                        .map(Currency::getId)
                        .collect(Collectors.toList()));
            } else if (args.length == 2) {
                return plugin.getCurrencyManager().getEnabledCurrencies().stream()
                        .map(Currency::getId)
                        .collect(Collectors.toList());
            }
        } else if (command.getName().equalsIgnoreCase("pay")) {
            if (args.length == 1) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList());
            } else if (args.length == 3) {
                return plugin.getCurrencyManager().getEnabledCurrencies().stream()
                        .map(Currency::getId)
                        .collect(Collectors.toList());
            }
        } else if (command.getName().equalsIgnoreCase("eco")) {
            if (args.length == 1) {
                completions.addAll(Arrays.asList("give", "take", "set"));
            } else if (args.length == 2) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList());
            } else if (args.length == 4) {
                return plugin.getCurrencyManager().getEnabledCurrencies().stream()
                        .map(Currency::getId)
                        .collect(Collectors.toList());
            }
        }
        
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
