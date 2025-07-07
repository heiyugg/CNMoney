package cn.money.gui.impl;

import cn.money.CNMoney;
import cn.money.gui.BaseGUI;
import cn.money.model.Transaction;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 事务日志查看GUI
 * 
 * @author CNMoney Team
 */
public class TransactionLogGUI extends BaseGUI {
    
    private final UUID targetPlayerId;
    private final String currencyId;
    private List<Transaction> transactions;
    private int currentPage;
    private final int itemsPerPage = 28;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 构造函数 - 查看所有事务
     * 
     * @param plugin 插件实例
     * @param player 玩家
     */
    public TransactionLogGUI(CNMoney plugin, Player player) {
        this(plugin, player, null, null);
    }
    
    /**
     * 构造函数 - 查看指定玩家的事务
     * 
     * @param plugin 插件实例
     * @param player 玩家
     * @param targetPlayerId 目标玩家ID
     */
    public TransactionLogGUI(CNMoney plugin, Player player, UUID targetPlayerId) {
        this(plugin, player, targetPlayerId, null);
    }
    
    /**
     * 构造函数 - 查看指定玩家指定货币的事务
     * 
     * @param plugin 插件实例
     * @param player 玩家
     * @param targetPlayerId 目标玩家ID
     * @param currencyId 货币ID
     */
    public TransactionLogGUI(CNMoney plugin, Player player, UUID targetPlayerId, String currencyId) {
        super(plugin, player, buildTitle(targetPlayerId, currencyId), 54);
        this.targetPlayerId = targetPlayerId;
        this.currencyId = currencyId;
        this.currentPage = 0;
        this.transactions = new ArrayList<>();
        
        loadTransactions();
        initializeItems();
    }
    
    /**
     * 构建标题
     */
    private static String buildTitle(UUID targetPlayerId, String currencyId) {
        StringBuilder title = new StringBuilder("事务日志");
        
        if (targetPlayerId != null) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetPlayerId);
            title.append(" - ").append(target.getName());
        }
        
        if (currencyId != null) {
            title.append(" - ").append(currencyId);
        }
        
        return title.toString();
    }
    
    /**
     * 加载事务记录
     */
    private void loadTransactions() {
        if (plugin.getDatabaseManager() != null) {
            transactions = plugin.getDatabaseManager().getTransactions(targetPlayerId, currencyId, 1000);
        }
    }
    
    @Override
    protected void initializeItems() {
        // 清空界面
        inventory.clear();
        
        // 填充边框
        setGlassBorder();
        
        // 显示事务记录
        displayTransactions();
        
        // 导航按钮
        setupNavigationButtons();
        
        // 功能按钮
        setupFunctionButtons();
    }
    
    /**
     * 显示事务记录
     */
    private void displayTransactions() {
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, transactions.size());
        
        int slot = 10;
        for (int i = startIndex; i < endIndex; i++) {
            Transaction transaction = transactions.get(i);
            
            // 跳过边框位置
            if (slot % 9 == 0 || slot % 9 == 8) {
                slot++;
            }
            if (slot >= 44) break;
            
            inventory.setItem(slot, createTransactionItem(transaction));
            slot++;
        }
    }
    
    /**
     * 创建事务记录物品
     */
    private org.bukkit.inventory.ItemStack createTransactionItem(Transaction transaction) {
        Material material = getTransactionMaterial(transaction.getType());
        String typeDisplay = getTransactionTypeDisplay(transaction.getType());
        
        List<String> lore = new ArrayList<>();
        lore.add("§7类型: §f" + typeDisplay);
        lore.add("§7货币: §f" + transaction.getCurrencyId());
        lore.add("§7金额: §f" + transaction.getAmount());
        
        if (transaction.getFromPlayer() != null) {
            OfflinePlayer fromPlayer = Bukkit.getOfflinePlayer(transaction.getFromPlayer());
            lore.add("§7来源: §f" + fromPlayer.getName());
        }
        
        if (transaction.getToPlayer() != null) {
            OfflinePlayer toPlayer = Bukkit.getOfflinePlayer(transaction.getToPlayer());
            lore.add("§7目标: §f" + toPlayer.getName());
        }
        
        if (transaction.getDescription() != null && !transaction.getDescription().isEmpty()) {
            lore.add("§7描述: §f" + transaction.getDescription());
        }
        
        lore.add("§7时间: §f" + dateFormat.format(new Date(transaction.getTimestamp())));
        lore.add("");
        lore.add("§e点击查看详情");
        
        return createItem(material, "§6事务记录 #" + transaction.getId(), lore.toArray(new String[0]));
    }
    
    /**
     * 获取事务类型对应的材料
     */
    private Material getTransactionMaterial(String type) {
        return switch (type.toUpperCase()) {
            case "SET_BALANCE" -> Material.WRITABLE_BOOK;
            case "ADD_BALANCE" -> Material.EMERALD;
            case "SUBTRACT_BALANCE", "REMOVE_BALANCE" -> Material.REDSTONE;
            case "TRANSFER" -> Material.ENDER_PEARL;
            case "EXCHANGE" -> Material.GOLD_INGOT;
            case "ADMIN_SET" -> Material.COMMAND_BLOCK;
            case "ADMIN_ADD" -> Material.LIME_DYE;
            case "ADMIN_REMOVE" -> Material.RED_DYE;
            default -> Material.PAPER;
        };
    }
    
    /**
     * 获取事务类型显示名称
     */
    private String getTransactionTypeDisplay(String type) {
        return switch (type.toUpperCase()) {
            case "SET_BALANCE" -> "设置余额";
            case "ADD_BALANCE" -> "增加余额";
            case "SUBTRACT_BALANCE", "REMOVE_BALANCE" -> "扣除余额";
            case "TRANSFER" -> "转账";
            case "EXCHANGE" -> "货币兑换";
            case "ADMIN_SET" -> "管理员设置";
            case "ADMIN_ADD" -> "管理员增加";
            case "ADMIN_REMOVE" -> "管理员扣除";
            default -> type;
        };
    }
    
    /**
     * 设置导航按钮
     */
    private void setupNavigationButtons() {
        int totalPages = (int) Math.ceil((double) transactions.size() / itemsPerPage);
        
        // 上一页
        if (currentPage > 0) {
            inventory.setItem(45, createItem(Material.ARROW, "§a上一页",
                "§7当前页: §f" + (currentPage + 1) + "/" + totalPages,
                "",
                "§e点击查看上一页"
            ));
        }

        // 页面信息
        inventory.setItem(49, createItem(Material.BOOK, "§6页面信息",
            "§7当前页: §f" + (currentPage + 1) + "/" + Math.max(1, totalPages),
            "§7总记录: §f" + transactions.size(),
            "",
            "§e显示最近1000条记录"
        ));

        // 下一页
        if (currentPage < totalPages - 1) {
            inventory.setItem(53, createItem(Material.ARROW, "§a下一页",
                "§7当前页: §f" + (currentPage + 1) + "/" + totalPages,
                "",
                "§e点击查看下一页"
            ));
        }
    }
    
    /**
     * 设置功能按钮
     */
    private void setupFunctionButtons() {
        // 刷新按钮
        inventory.setItem(46, createItem(Material.CLOCK, "§a刷新",
            "§7重新加载事务记录",
            "",
            "§e点击刷新"
        ));

        // 筛选按钮
        inventory.setItem(47, createItem(Material.HOPPER, "§e筛选",
            "§7筛选特定类型的事务",
            "",
            "§c功能开发中..."
        ));

        // 导出按钮
        inventory.setItem(48, createItem(Material.WRITABLE_BOOK, "§b导出",
            "§7导出事务记录到文件",
            "",
            "§c功能开发中..."
        ));

        // 返回按钮
        inventory.setItem(52, createItem(Material.BARRIER, "§c返回",
            "§7返回上级菜单",
            "",
            "§e点击返回"
        ));
    }
    
    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = getClickedSlot(event);
        
        switch (slot) {
            case 45: // 上一页
                if (currentPage > 0) {
                    currentPage--;
                    initializeItems();
                    playSound(Sound.UI_BUTTON_CLICK);
                }
                break;
                
            case 53: // 下一页
                int totalPages = (int) Math.ceil((double) transactions.size() / itemsPerPage);
                if (currentPage < totalPages - 1) {
                    currentPage++;
                    initializeItems();
                    playSound(Sound.UI_BUTTON_CLICK);
                }
                break;
                
            case 46: // 刷新
                loadTransactions();
                currentPage = 0;
                initializeItems();
                playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
                sendMessage("§a事务记录已刷新！");
                break;
                
            case 47: // 筛选
                sendMessage("§e筛选功能开发中...");
                playSound(Sound.ENTITY_VILLAGER_NO);
                break;
                
            case 48: // 导出
                sendMessage("§e导出功能开发中...");
                playSound(Sound.ENTITY_VILLAGER_NO);
                break;
                
            case 52: // 返回
                player.closeInventory();
                playSound(Sound.UI_BUTTON_CLICK);
                break;
                
            default:
                // 检查是否点击了事务记录
                if (slot >= 10 && slot <= 43 && slot % 9 != 0 && slot % 9 != 8) {
                    handleTransactionClick(slot);
                }
                break;
        }
    }
    
    /**
     * 处理事务记录点击
     */
    private void handleTransactionClick(int slot) {
        // 计算对应的事务索引
        int row = slot / 9 - 1;
        int col = slot % 9 - 1;
        int index = currentPage * itemsPerPage + row * 7 + col;
        
        if (index >= 0 && index < transactions.size()) {
            Transaction transaction = transactions.get(index);
            // TODO: 打开事务详情GUI或显示详细信息
            sendMessage("§e事务详情功能开发中...");
            playSound(Sound.UI_BUTTON_CLICK);
        }
    }
}
