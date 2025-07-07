package cn.money.gui.impl;

import cn.money.CNMoney;
import cn.money.gui.BaseGUI;
import cn.money.model.Currency;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理面板主界面
 * 
 * @author CNMoney Team
 */
public class AdminGUI extends BaseGUI {
    
    public AdminGUI(CNMoney plugin, Player player) {
        super(plugin, player, "管理面板", 54);
    }
    
    @Override
    protected void initializeItems() {
        // 设置玻璃边框
        setGlassBorder();
        
        // 玩家管理区域
        setupPlayerManagement();
        
        // 货币管理区域
        setupCurrencyManagement();
        
        // 系统管理区域
        setupSystemManagement();
        
        // 统计信息区域
        setupStatistics();
        
        // 返回和关闭按钮
        addBackButton(45);
        addCloseButton(53);
    }
    
    /**
     * 设置玩家管理区域
     */
    private void setupPlayerManagement() {
        // 玩家余额管理
        ItemStack playerBalanceItem = createPlayerBalanceItem();
        inventory.setItem(10, playerBalanceItem);
        
        // 玩家账户管理
        ItemStack playerAccountItem = createPlayerAccountItem();
        inventory.setItem(11, playerAccountItem);
        
        // 转账记录查看
        ItemStack transactionLogItem = createTransactionLogItem();
        inventory.setItem(12, transactionLogItem);
    }
    
    /**
     * 设置货币管理区域
     */
    private void setupCurrencyManagement() {
        // 货币配置管理
        ItemStack currencyConfigItem = createCurrencyConfigItem();
        inventory.setItem(19, currencyConfigItem);
        
        // 汇率管理
        ItemStack exchangeRateItem = createExchangeRateItem();
        inventory.setItem(20, exchangeRateItem);
        
        // 货币启用/禁用
        ItemStack currencyToggleItem = createCurrencyToggleItem();
        inventory.setItem(21, currencyToggleItem);
    }
    
    /**
     * 设置系统管理区域
     */
    private void setupSystemManagement() {
        // 配置重载
        ItemStack reloadConfigItem = createReloadConfigItem();
        inventory.setItem(28, reloadConfigItem);

        // 数据备份
        ItemStack backupDataItem = createBackupDataItem();
        inventory.setItem(29, backupDataItem);

        // 系统设置
        ItemStack systemSettingsItem = createSystemSettingsItem();
        inventory.setItem(30, systemSettingsItem);

        // 事务日志
        ItemStack transactionLogItem = createTransactionLogItem();
        inventory.setItem(31, transactionLogItem);

        // 导入导出
        ItemStack importExportItem = createImportExportItem();
        inventory.setItem(32, importExportItem);
    }
    
    /**
     * 设置统计信息区域
     */
    private void setupStatistics() {
        // 服务器统计
        ItemStack serverStatsItem = createServerStatsItem();
        inventory.setItem(37, serverStatsItem);
        
        // 货币统计
        ItemStack currencyStatsItem = createCurrencyStatsItem();
        inventory.setItem(38, currencyStatsItem);
        
        // 交易统计
        ItemStack transactionStatsItem = createTransactionStatsItem();
        inventory.setItem(39, transactionStatsItem);
    }
    
    /**
     * 创建玩家余额管理按钮
     */
    private ItemStack createPlayerBalanceItem() {
        List<String> lore = new ArrayList<>();
        lore.add("&7管理玩家的货币余额");
        lore.add("");
        lore.add("&e功能:");
        lore.add("&7• 查看玩家余额");
        lore.add("&7• 设置玩家余额");
        lore.add("&7• 给予/扣除余额");
        lore.add("");
        lore.add("&e左键: 打开玩家余额管理");
        
        return createItem(Material.PLAYER_HEAD, 
                         "&6玩家余额管理", 
                         lore);
    }
    
    /**
     * 创建玩家账户管理按钮
     */
    private ItemStack createPlayerAccountItem() {
        List<String> lore = new ArrayList<>();
        lore.add("&7管理玩家账户信息");
        lore.add("");
        lore.add("&e功能:");
        lore.add("&7• 查看账户详情");
        lore.add("&7• 重置账户数据");
        lore.add("&7• 账户权限管理");
        lore.add("");
        lore.add("&e左键: 打开账户管理");
        
        return createItem(Material.WRITABLE_BOOK, 
                         "&6玩家账户管理", 
                         lore);
    }
    
    /**
     * 创建交易记录查看按钮
     */
    private ItemStack createTransactionLogItem() {
        List<String> lore = new ArrayList<>();
        lore.add("&7查看和管理交易记录");
        lore.add("");
        lore.add("&e功能:");
        lore.add("&7• 查看交易历史");
        lore.add("&7• 搜索特定交易");
        lore.add("&7• 导出交易数据");
        lore.add("");
        lore.add("&e左键: 打开交易记录");
        
        return createItem(Material.BOOK, 
                         "&6交易记录查看", 
                         lore);
    }
    
    /**
     * 创建货币配置管理按钮
     */
    private ItemStack createCurrencyConfigItem() {
        List<String> lore = new ArrayList<>();
        lore.add("&7管理货币配置设置");
        lore.add("");
        lore.add("&e功能:");
        lore.add("&7• 修改货币属性");
        lore.add("&7• 添加新货币");
        lore.add("&7• 删除货币");
        lore.add("");
        lore.add("&7当前货币数量: &f" + plugin.getCurrencyManager().getCurrencyCount());
        lore.add("");
        lore.add("&e左键: 打开货币配置");
        
        return createItem(Material.GOLD_INGOT, 
                         "&6货币配置管理", 
                         lore);
    }
    
    /**
     * 创建汇率管理按钮
     */
    private ItemStack createExchangeRateItem() {
        List<String> lore = new ArrayList<>();
        lore.add("&7管理货币兑换汇率");
        lore.add("");
        lore.add("&e功能:");
        lore.add("&7• 设置兑换汇率");
        lore.add("&7• 批量更新汇率");
        lore.add("&7• 汇率历史记录");
        lore.add("");
        lore.add("&e左键: 打开汇率管理");
        
        return createItem(Material.EMERALD, 
                         "&6汇率管理", 
                         lore);
    }
    
    /**
     * 创建货币启用/禁用按钮
     */
    private ItemStack createCurrencyToggleItem() {
        List<String> lore = new ArrayList<>();
        lore.add("&7启用或禁用货币");
        lore.add("");
        lore.add("&e功能:");
        lore.add("&7• 快速启用/禁用货币");
        lore.add("&7• 批量操作");
        lore.add("&7• 查看货币状态");
        lore.add("");
        
        // 显示启用的货币数量
        long enabledCount = plugin.getCurrencyManager().getEnabledCurrencies().size();
        long totalCount = plugin.getCurrencyManager().getAllCurrencies().size();
        lore.add("&7启用状态: &a" + enabledCount + "&7/&f" + totalCount);
        lore.add("");
        lore.add("&e左键: 打开货币开关");
        
        return createItem(Material.LEVER, 
                         "&6货币启用管理", 
                         lore);
    }
    
    /**
     * 创建配置重载按钮
     */
    private ItemStack createReloadConfigItem() {
        List<String> lore = new ArrayList<>();
        lore.add("&7重新加载插件配置");
        lore.add("");
        lore.add("&c注意: 重载配置可能会影响");
        lore.add("&c正在进行的操作");
        lore.add("");
        lore.add("&e左键: 重载配置");
        lore.add("&e右键: 重载所有数据");
        
        return createItem(Material.COMMAND_BLOCK, 
                         "&6配置重载", 
                         lore);
    }
    
    /**
     * 创建数据备份按钮
     */
    private ItemStack createBackupDataItem() {
        List<String> lore = new ArrayList<>();
        lore.add("&7备份和恢复数据");
        lore.add("");
        lore.add("&e功能:");
        lore.add("&7• 创建数据备份");
        lore.add("&7• 恢复备份数据");
        lore.add("&7• 自动备份设置");
        lore.add("");
        lore.add("&e左键: 创建备份");
        lore.add("&e右键: 备份管理");
        
        return createItem(Material.CHEST, 
                         "&6数据备份", 
                         lore);
    }
    
    /**
     * 创建系统设置按钮
     */
    private ItemStack createSystemSettingsItem() {
        List<String> lore = new ArrayList<>();
        lore.add("&7系统配置和设置");
        lore.add("");
        lore.add("&e功能:");
        lore.add("&7• 插件基础设置");
        lore.add("&7• 集成配置管理");
        lore.add("&7• 性能优化设置");
        lore.add("");
        lore.add("&e左键: 打开系统设置");
        
        return createItem(Material.REDSTONE, 
                         "&6系统设置", 
                         lore);
    }
    
    /**
     * 创建服务器统计按钮
     */
    private ItemStack createServerStatsItem() {
        List<String> lore = new ArrayList<>();
        lore.add("&7查看服务器经济统计");
        lore.add("");
        lore.add("&e统计信息:");
        lore.add("&7• 在线玩家数量");
        lore.add("&7• 总注册玩家");
        lore.add("&7• 活跃玩家统计");
        lore.add("");
        lore.add("&7在线玩家: &a" + plugin.getServer().getOnlinePlayers().size());
        lore.add("");
        lore.add("&e左键: 查看详细统计");
        
        return createItem(Material.BEACON, 
                         "&6服务器统计", 
                         lore);
    }
    
    /**
     * 创建货币统计按钮
     */
    private ItemStack createCurrencyStatsItem() {
        List<String> lore = new ArrayList<>();
        lore.add("&7查看货币流通统计");
        lore.add("");
        lore.add("&e统计信息:");
        lore.add("&7• 货币总量统计");
        lore.add("&7• 流通量分析");
        lore.add("&7• 通胀率计算");
        lore.add("");
        lore.add("&e左键: 查看货币统计");
        
        return createItem(Material.DIAMOND, 
                         "&6货币统计", 
                         lore);
    }
    
    /**
     * 创建交易统计按钮
     */
    private ItemStack createTransactionStatsItem() {
        List<String> lore = new ArrayList<>();
        lore.add("&7查看交易活动统计");
        lore.add("");
        lore.add("&e统计信息:");
        lore.add("&7• 日交易量统计");
        lore.add("&7• 热门交易对");
        lore.add("&7• 交易趋势分析");
        lore.add("");
        lore.add("&e左键: 查看交易统计");
        
        return createItem(Material.PAPER, 
                         "&6交易统计", 
                         lore);
    }
    
    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = getClickedSlot(event);
        
        switch (slot) {
            case 10: // 玩家余额管理
                handlePlayerBalanceClick(event);
                break;
            case 11: // 玩家账户管理
                handlePlayerAccountClick(event);
                break;
            case 12: // 交易记录查看
                handleTransactionLogClick(event);
                break;
            case 19: // 货币配置管理
                handleCurrencyConfigClick(event);
                break;
            case 20: // 汇率管理
                handleExchangeRateClick(event);
                break;
            case 21: // 货币启用管理
                handleCurrencyToggleClick(event);
                break;
            case 28: // 配置重载
                handleReloadConfigClick(event);
                break;
            case 29: // 数据备份
                handleBackupDataClick(event);
                break;
            case 30: // 系统设置
                handleSystemSettingsClick(event);
                break;
            case 31: // 事务日志
                handleTransactionLogClick(event);
                break;
            case 32: // 导入导出
                handleImportExportClick(event);
                break;
            case 37: // 服务器统计
                handleServerStatsClick(event);
                break;
            case 38: // 货币统计
                handleCurrencyStatsClick(event);
                break;
            case 39: // 交易统计
                handleTransactionStatsClick(event);
                break;
            case 45: // 返回
                plugin.getGUIManager().openMainGUI(player);
                playSound(Sound.UI_BUTTON_CLICK);
                break;
            case 53: // 关闭
                player.closeInventory();
                playSound(Sound.UI_BUTTON_CLICK);
                break;
        }
    }
    
    /**
     * 处理玩家余额管理点击
     */
    private void handlePlayerBalanceClick(InventoryClickEvent event) {
        playSound(Sound.UI_BUTTON_CLICK);
        plugin.getGUIManager().openPlayerBalanceManagementGUI(player);
    }
    
    /**
     * 处理玩家账户管理点击
     */
    private void handlePlayerAccountClick(InventoryClickEvent event) {
        playSound(Sound.UI_BUTTON_CLICK);
        sendMessage("&e玩家账户管理功能开发中...");
        // TODO: 实现玩家账户管理GUI
    }
    
    /**
     * 处理交易记录查看点击
     */
    private void handleTransactionLogClick(InventoryClickEvent event) {
        playSound(Sound.UI_BUTTON_CLICK);
        TransactionLogGUI transactionLogGUI = new TransactionLogGUI(plugin, player);
        transactionLogGUI.open();
    }
    
    /**
     * 处理货币配置管理点击
     */
    private void handleCurrencyConfigClick(InventoryClickEvent event) {
        playSound(Sound.UI_BUTTON_CLICK);
        plugin.getGUIManager().openCurrencyManagementGUI(player);
    }
    
    /**
     * 处理汇率管理点击
     */
    private void handleExchangeRateClick(InventoryClickEvent event) {
        playSound(Sound.UI_BUTTON_CLICK);
        plugin.getGUIManager().openExchangeRateManagementGUI(player);
    }
    
    /**
     * 处理货币启用管理点击
     */
    private void handleCurrencyToggleClick(InventoryClickEvent event) {
        playSound(Sound.UI_BUTTON_CLICK);
        plugin.getGUIManager().openCurrencyToggleGUI(player);
    }
    
    /**
     * 处理配置重载点击
     */
    private void handleReloadConfigClick(InventoryClickEvent event) {
        if (isLeftClick(event)) {
            // 重载配置
            plugin.getConfigManager().reloadConfigs();
            sendMessage("&a配置文件已重载！");
            playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        } else if (isRightClick(event)) {
            // 重载所有数据
            plugin.getConfigManager().reloadConfigs();
            plugin.getCurrencyManager().reloadCurrencies();
            sendMessage("&a所有数据已重载！");
            playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        }
    }
    
    /**
     * 处理数据备份点击
     */
    private void handleBackupDataClick(InventoryClickEvent event) {
        playSound(Sound.UI_BUTTON_CLICK);
        if (isLeftClick(event)) {
            sendMessage("&e正在创建数据备份...");
            // TODO: 实现数据备份功能
        } else if (isRightClick(event)) {
            sendMessage("&e备份管理功能开发中...");
            // TODO: 实现备份管理GUI
        }
    }
    
    /**
     * 处理系统设置点击
     */
    private void handleSystemSettingsClick(InventoryClickEvent event) {
        playSound(Sound.UI_BUTTON_CLICK);
        sendMessage("&e系统设置功能开发中...");
        // TODO: 实现系统设置GUI
    }
    
    /**
     * 处理服务器统计点击
     */
    private void handleServerStatsClick(InventoryClickEvent event) {
        playSound(Sound.UI_BUTTON_CLICK);
        sendMessage("&e服务器统计功能开发中...");
        // TODO: 实现服务器统计GUI
    }
    
    /**
     * 处理货币统计点击
     */
    private void handleCurrencyStatsClick(InventoryClickEvent event) {
        playSound(Sound.UI_BUTTON_CLICK);
        sendMessage("&e货币统计功能开发中...");
        // TODO: 实现货币统计GUI
    }
    
    /**
     * 处理交易统计点击
     */
    private void handleTransactionStatsClick(InventoryClickEvent event) {
        playSound(Sound.UI_BUTTON_CLICK);
        sendMessage("&e交易统计功能开发中...");
        // TODO: 实现交易统计GUI
    }



    /**
     * 创建导入导出按钮
     */
    private ItemStack createImportExportItem() {
        List<String> lore = new ArrayList<>();
        lore.add("&7导入导出数据功能");
        lore.add("&7支持玩家余额和货币配置");
        lore.add("&7支持JSON和YAML格式");
        lore.add("");
        lore.add("&e点击进入导入导出");

        return createItem(Material.ENDER_CHEST, "&6导入导出", lore.toArray(new String[0]));
    }



    /**
     * 处理导入导出点击
     */
    private void handleImportExportClick(InventoryClickEvent event) {
        playSound(Sound.UI_BUTTON_CLICK);
        sendMessage("&e导入导出功能开发中...");
        // TODO: 实现导入导出GUI
    }
}
