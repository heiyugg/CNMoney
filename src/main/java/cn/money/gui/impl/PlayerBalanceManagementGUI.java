package cn.money.gui.impl;

import cn.money.CNMoney;
import cn.money.gui.BaseGUI;
import cn.money.model.Currency;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 玩家余额管理GUI
 * 
 * @author CNMoney Team
 */
public class PlayerBalanceManagementGUI extends BaseGUI {
    
    private final List<OfflinePlayer> onlinePlayers;
    private int currentPage = 0;
    private final int playersPerPage = 28; // 7x4 区域
    
    public PlayerBalanceManagementGUI(CNMoney plugin, Player player) {
        super(plugin, player, "玩家余额管理", 54);
        this.onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
    }
    
    @Override
    protected void initializeItems() {
        // 清空界面
        inventory.clear();
        
        // 设置玻璃边框
        setGlassBorder();
        
        // 显示玩家列表
        displayPlayers();
        
        // 设置导航按钮
        setupNavigationButtons();
        
        // 设置功能按钮
        setupFunctionButtons();
        
        // 返回和关闭按钮
        addBackButton(45);
        addCloseButton(53);
    }
    
    /**
     * 显示玩家列表
     */
    private void displayPlayers() {
        int startIndex = currentPage * playersPerPage;
        int endIndex = Math.min(startIndex + playersPerPage, onlinePlayers.size());
        
        // 玩家显示区域：第2-5行，第2-8列 (slots 10-16, 19-25, 28-34, 37-43)
        int[] playerSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        };
        
        for (int i = 0; i < playerSlots.length && (startIndex + i) < endIndex; i++) {
            OfflinePlayer targetPlayer = onlinePlayers.get(startIndex + i);
            ItemStack playerItem = createPlayerItem(targetPlayer);
            inventory.setItem(playerSlots[i], playerItem);
        }
    }
    
    /**
     * 创建玩家物品
     */
    private ItemStack createPlayerItem(OfflinePlayer targetPlayer) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        
        if (meta != null) {
            meta.setOwningPlayer(targetPlayer);
            meta.setDisplayName("§6" + targetPlayer.getName());
            
            List<String> lore = new ArrayList<>();
            lore.add("§7玩家: §f" + targetPlayer.getName());
            lore.add("§7状态: " + (targetPlayer.isOnline() ? "§a在线" : "§c离线"));
            lore.add("");
            
            // 显示各货币余额
            lore.add("§e货币余额:");
            for (Currency currency : plugin.getCurrencyManager().getEnabledCurrencies()) {
                BigDecimal balance = plugin.getCurrencyManager().getBalance(targetPlayer.getUniqueId(), currency.getId());
                lore.add("§7• " + currency.getName() + ": §f" + currency.formatAmount(balance));
            }
            
            lore.add("");
            lore.add("§e左键: 管理余额");
            lore.add("§e右键: 查看详情");
            lore.add("§eShift+左键: 重置所有余额");
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * 设置导航按钮
     */
    private void setupNavigationButtons() {
        // 上一页按钮
        if (currentPage > 0) {
            ItemStack prevButton = createItem(Material.ARROW, 
                                            "§6上一页", 
                                            List.of("§7点击查看上一页玩家"));
            inventory.setItem(48, prevButton);
        }
        
        // 下一页按钮
        if ((currentPage + 1) * playersPerPage < onlinePlayers.size()) {
            ItemStack nextButton = createItem(Material.ARROW, 
                                            "§6下一页", 
                                            List.of("§7点击查看下一页玩家"));
            inventory.setItem(50, nextButton);
        }
        
        // 页面信息
        int totalPages = (int) Math.ceil((double) onlinePlayers.size() / playersPerPage);
        ItemStack pageInfo = createItem(Material.BOOK, 
                                      "§6页面信息", 
                                      List.of("§7当前页: §f" + (currentPage + 1) + "/" + totalPages,
                                             "§7玩家总数: §f" + onlinePlayers.size()));
        inventory.setItem(49, pageInfo);
    }
    
    /**
     * 设置功能按钮
     */
    private void setupFunctionButtons() {
        // 搜索玩家按钮
        ItemStack searchButton = createItem(Material.COMPASS, 
                                          "§6搜索玩家", 
                                          List.of("§7点击搜索特定玩家",
                                                 "§e功能开发中..."));
        inventory.setItem(46, searchButton);
        
        // 批量操作按钮
        ItemStack batchButton = createItem(Material.COMMAND_BLOCK, 
                                         "§6批量操作", 
                                         List.of("§7批量管理玩家余额",
                                                "§e功能开发中..."));
        inventory.setItem(47, batchButton);
        
        // 刷新列表按钮
        ItemStack refreshButton = createItem(Material.EMERALD, 
                                           "§6刷新列表", 
                                           List.of("§7刷新在线玩家列表"));
        inventory.setItem(51, refreshButton);
        
        // 导出数据按钮
        ItemStack exportButton = createItem(Material.PAPER, 
                                          "§6导出数据", 
                                          List.of("§7导出玩家余额数据",
                                                 "§e功能开发中..."));
        inventory.setItem(52, exportButton);
    }
    
    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = getClickedSlot(event);
        
        // 处理玩家点击
        if (isPlayerSlot(slot)) {
            handlePlayerClick(event, slot);
            return;
        }
        
        switch (slot) {
            case 45: // 返回
                plugin.getGUIManager().openAdminGUI(player);
                playSound(Sound.UI_BUTTON_CLICK);
                break;
            case 46: // 搜索玩家
                handleSearchClick(event);
                break;
            case 47: // 批量操作
                handleBatchClick(event);
                break;
            case 48: // 上一页
                handlePreviousPageClick(event);
                break;
            case 49: // 页面信息
                playSound(Sound.UI_BUTTON_CLICK);
                break;
            case 50: // 下一页
                handleNextPageClick(event);
                break;
            case 51: // 刷新列表
                handleRefreshClick(event);
                break;
            case 52: // 导出数据
                handleExportClick(event);
                break;
            case 53: // 关闭
                player.closeInventory();
                playSound(Sound.UI_BUTTON_CLICK);
                break;
        }
    }
    
    /**
     * 检查是否为玩家槽位
     */
    private boolean isPlayerSlot(int slot) {
        int[] playerSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        };
        
        for (int playerSlot : playerSlots) {
            if (slot == playerSlot) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 处理玩家点击
     */
    private void handlePlayerClick(InventoryClickEvent event, int slot) {
        OfflinePlayer targetPlayer = getPlayerFromSlot(slot);
        if (targetPlayer == null) {
            return;
        }
        
        if (isLeftClick(event)) {
            if (isShiftClick(event)) {
                // Shift+左键：重置所有余额
                handleResetAllBalances(targetPlayer);
            } else {
                // 左键：管理余额
                plugin.getGUIManager().openPlayerBalanceEditGUI(player, targetPlayer);
                playSound(Sound.UI_BUTTON_CLICK);
            }
        } else if (isRightClick(event)) {
            // 右键：查看详情
            showPlayerDetails(targetPlayer);
            playSound(Sound.UI_BUTTON_CLICK);
        }
    }
    
    /**
     * 从槽位获取玩家
     */
    private OfflinePlayer getPlayerFromSlot(int slot) {
        int[] playerSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        };
        
        for (int i = 0; i < playerSlots.length; i++) {
            if (playerSlots[i] == slot) {
                int playerIndex = currentPage * playersPerPage + i;
                if (playerIndex < onlinePlayers.size()) {
                    return onlinePlayers.get(playerIndex);
                }
                break;
            }
        }
        return null;
    }
    
    /**
     * 重置玩家所有余额
     */
    private void handleResetAllBalances(OfflinePlayer targetPlayer) {
        sendMessage("§c确认要重置 " + targetPlayer.getName() + " 的所有余额吗？");
        sendMessage("§c请在聊天中输入 'confirm' 确认操作");
        playSound(Sound.ENTITY_VILLAGER_NO);
        
        // TODO: 实现确认机制
        // 这里应该实现一个确认对话框或聊天确认机制
    }
    
    /**
     * 显示玩家详情
     */
    private void showPlayerDetails(OfflinePlayer targetPlayer) {
        sendMessage("§6=== " + targetPlayer.getName() + " 详细信息 ===");
        sendMessage("§7UUID: §f" + targetPlayer.getUniqueId());
        sendMessage("§7状态: " + (targetPlayer.isOnline() ? "§a在线" : "§c离线"));
        sendMessage("§7首次游戏: §f" + (targetPlayer.hasPlayedBefore() ? "是" : "否"));
        
        sendMessage("§e货币余额详情:");
        for (Currency currency : plugin.getCurrencyManager().getEnabledCurrencies()) {
            BigDecimal balance = plugin.getCurrencyManager().getBalance(targetPlayer.getUniqueId(), currency.getId());
            sendMessage("§7• " + currency.getName() + ": §f" + currency.formatAmountWithColor(balance));
        }
    }
    
    /**
     * 处理搜索点击
     */
    private void handleSearchClick(InventoryClickEvent event) {
        playSound(Sound.UI_BUTTON_CLICK);
        sendMessage("§e搜索功能开发中...");
        // TODO: 实现玩家搜索功能
    }
    
    /**
     * 处理批量操作点击
     */
    private void handleBatchClick(InventoryClickEvent event) {
        playSound(Sound.UI_BUTTON_CLICK);
        sendMessage("§e批量操作功能开发中...");
        // TODO: 实现批量操作功能
    }
    
    /**
     * 处理上一页点击
     */
    private void handlePreviousPageClick(InventoryClickEvent event) {
        if (currentPage > 0) {
            currentPage--;
            refresh();
            playSound(Sound.UI_BUTTON_CLICK);
        }
    }
    
    /**
     * 处理下一页点击
     */
    private void handleNextPageClick(InventoryClickEvent event) {
        if ((currentPage + 1) * playersPerPage < onlinePlayers.size()) {
            currentPage++;
            refresh();
            playSound(Sound.UI_BUTTON_CLICK);
        }
    }
    
    /**
     * 处理刷新点击
     */
    private void handleRefreshClick(InventoryClickEvent event) {
        // 刷新在线玩家列表
        onlinePlayers.clear();
        onlinePlayers.addAll(Bukkit.getOnlinePlayers());
        
        // 重置到第一页
        currentPage = 0;
        
        // 刷新界面
        refresh();
        
        sendMessage("§a玩家列表已刷新！当前在线玩家: " + onlinePlayers.size());
        playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
    }
    
    /**
     * 处理导出点击
     */
    private void handleExportClick(InventoryClickEvent event) {
        playSound(Sound.UI_BUTTON_CLICK);
        sendMessage("§e数据导出功能开发中...");
        // TODO: 实现数据导出功能
    }
}
