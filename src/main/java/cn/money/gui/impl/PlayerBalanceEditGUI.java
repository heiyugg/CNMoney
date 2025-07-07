package cn.money.gui.impl;

import cn.money.CNMoney;
import cn.money.gui.BaseGUI;
import cn.money.model.Currency;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 玩家余额编辑GUI
 * 
 * @author CNMoney Team
 */
public class PlayerBalanceEditGUI extends BaseGUI {
    
    private final OfflinePlayer targetPlayer;
    private String selectedCurrencyId;
    private BigDecimal inputAmount = BigDecimal.ZERO;
    
    public PlayerBalanceEditGUI(CNMoney plugin, Player player, OfflinePlayer targetPlayer) {
        super(plugin, player, "编辑余额 - " + targetPlayer.getName(), 54);
        this.targetPlayer = targetPlayer;
        this.selectedCurrencyId = plugin.getCurrencyManager().getDefaultCurrency().getId();
    }
    
    @Override
    protected void initializeItems() {
        // 清空界面
        inventory.clear();
        
        // 设置玻璃边框
        setGlassBorder();
        
        // 显示目标玩家信息
        setupPlayerInfo();
        
        // 显示货币选择
        setupCurrencySelection();
        
        // 显示当前余额
        setupCurrentBalance();
        
        // 设置操作按钮
        setupActionButtons();
        
        // 设置数字输入
        setupNumberInput();
        
        // 返回和关闭按钮
        addBackButton(45);
        addCloseButton(53);
    }
    
    /**
     * 设置玩家信息显示
     */
    private void setupPlayerInfo() {
        List<String> lore = new ArrayList<>();
        lore.add("§7目标玩家: §f" + targetPlayer.getName());
        lore.add("§7UUID: §f" + targetPlayer.getUniqueId());
        lore.add("§7状态: " + (targetPlayer.isOnline() ? "§a在线" : "§c离线"));
        lore.add("");
        lore.add("§7正在编辑此玩家的余额");
        
        ItemStack playerInfo = createItem(Material.PLAYER_HEAD, 
                                        "§6玩家信息", 
                                        lore);
        inventory.setItem(4, playerInfo);
    }
    
    /**
     * 设置货币选择区域
     */
    private void setupCurrencySelection() {
        List<Currency> currencies = new ArrayList<>(plugin.getCurrencyManager().getEnabledCurrencies());
        
        // 货币选择区域：第2行 (slots 10-16)
        int[] currencySlots = {10, 11, 12, 13, 14, 15, 16};
        
        for (int i = 0; i < Math.min(currencies.size(), currencySlots.length); i++) {
            Currency currency = currencies.get(i);
            ItemStack currencyItem = createCurrencySelectionItem(currency);
            inventory.setItem(currencySlots[i], currencyItem);
        }
    }
    
    /**
     * 创建货币选择物品
     */
    private ItemStack createCurrencySelectionItem(Currency currency) {
        List<String> lore = new ArrayList<>();
        lore.add("§7货币: §f" + currency.getName());
        lore.add("§7符号: §f" + currency.getSymbol());
        lore.add("§7小数位: §f" + currency.getDecimalPlaces());
        lore.add("");
        
        BigDecimal currentBalance = plugin.getCurrencyManager().getBalance(targetPlayer.getUniqueId(), currency.getId());
        lore.add("§7当前余额: §f" + currency.formatAmount(currentBalance));
        lore.add("");
        
        if (currency.getId().equals(selectedCurrencyId)) {
            lore.add("§a✓ 已选择");
        } else {
            lore.add("§e点击选择此货币");
        }
        
        Material material = currency.getId().equals(selectedCurrencyId) ? 
                           Material.LIME_STAINED_GLASS : Material.WHITE_STAINED_GLASS;
        
        return createItem(material, 
                         "§6" + currency.getName(), 
                         lore);
    }
    
    /**
     * 设置当前余额显示
     */
    private void setupCurrentBalance() {
        Currency selectedCurrency = plugin.getCurrencyManager().getCurrency(selectedCurrencyId);
        if (selectedCurrency == null) return;
        
        BigDecimal currentBalance = plugin.getCurrencyManager().getBalance(targetPlayer.getUniqueId(), selectedCurrencyId);
        
        List<String> lore = new ArrayList<>();
        lore.add("§7当前余额: §f" + selectedCurrency.formatAmountWithColor(currentBalance));
        lore.add("§7输入金额: §f" + selectedCurrency.formatAmount(inputAmount));
        lore.add("");
        lore.add("§7使用下方按钮输入金额");
        
        ItemStack balanceInfo = createItem(Material.GOLD_INGOT, 
                                         "§6余额信息", 
                                         lore);
        inventory.setItem(22, balanceInfo);
    }
    
    /**
     * 设置操作按钮
     */
    private void setupActionButtons() {
        // 设置余额按钮
        ItemStack setButton = createItem(Material.EMERALD_BLOCK, 
                                       "§a设置余额", 
                                       List.of("§7将余额设置为输入的金额",
                                              "§e左键: 设置余额"));
        inventory.setItem(37, setButton);
        
        // 增加余额按钮
        ItemStack addButton = createItem(Material.GREEN_STAINED_GLASS, 
                                       "§a增加余额", 
                                       List.of("§7在当前余额基础上增加",
                                              "§e左键: 增加余额"));
        inventory.setItem(38, addButton);
        
        // 扣除余额按钮
        ItemStack subtractButton = createItem(Material.RED_STAINED_GLASS, 
                                            "§c扣除余额", 
                                            List.of("§7从当前余额中扣除",
                                                   "§e左键: 扣除余额"));
        inventory.setItem(39, subtractButton);
        
        // 清空输入按钮
        ItemStack clearButton = createItem(Material.BARRIER, 
                                         "§c清空输入", 
                                         List.of("§7清空当前输入的金额",
                                                "§e左键: 清空输入"));
        inventory.setItem(40, clearButton);
        
        // 快速金额按钮
        setupQuickAmountButtons();
    }
    
    /**
     * 设置快速金额按钮
     */
    private void setupQuickAmountButtons() {
        BigDecimal[] quickAmounts = {
            new BigDecimal("1"), new BigDecimal("10"), new BigDecimal("100"), 
            new BigDecimal("1000"), new BigDecimal("10000")
        };
        
        int[] quickSlots = {19, 20, 21, 23, 24};
        
        for (int i = 0; i < quickAmounts.length; i++) {
            BigDecimal amount = quickAmounts[i];
            ItemStack quickButton = createItem(Material.GOLD_NUGGET, 
                                             "§6+" + amount, 
                                             List.of("§7快速添加 " + amount + " 到输入金额",
                                                    "§e左键: 添加到输入",
                                                    "§e右键: 设置为输入"));
            inventory.setItem(quickSlots[i], quickButton);
        }
    }
    
    /**
     * 设置数字输入区域
     */
    private void setupNumberInput() {
        // 数字按钮区域：第5行 (slots 28-34)
        for (int i = 1; i <= 9; i++) {
            ItemStack numberButton = createItem(Material.STONE_BUTTON, 
                                              "§f" + i, 
                                              List.of("§7点击输入数字 " + i));
            inventory.setItem(27 + i, numberButton);
        }
        
        // 0按钮
        ItemStack zeroButton = createItem(Material.STONE_BUTTON, 
                                        "§f0", 
                                        List.of("§7点击输入数字 0"));
        inventory.setItem(31, zeroButton);
        
        // 小数点按钮
        ItemStack dotButton = createItem(Material.STONE_BUTTON, 
                                       "§f.", 
                                       List.of("§7点击输入小数点"));
        inventory.setItem(32, dotButton);
        
        // 退格按钮
        ItemStack backspaceButton = createItem(Material.RED_STAINED_GLASS_PANE, 
                                             "§c退格", 
                                             List.of("§7删除最后一位数字"));
        inventory.setItem(35, backspaceButton);
    }
    
    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = getClickedSlot(event);
        
        // 处理货币选择
        if (slot >= 10 && slot <= 16) {
            handleCurrencySelection(slot);
            return;
        }
        
        // 处理快速金额按钮
        if (slot == 19 || slot == 20 || slot == 21 || slot == 23 || slot == 24) {
            handleQuickAmountClick(event, slot);
            return;
        }
        
        // 处理数字输入
        if (slot >= 28 && slot <= 36) {
            handleNumberInput(slot);
            return;
        }
        
        switch (slot) {
            case 37: // 设置余额
                handleSetBalance();
                break;
            case 38: // 增加余额
                handleAddBalance();
                break;
            case 39: // 扣除余额
                handleSubtractBalance();
                break;
            case 40: // 清空输入
                handleClearInput();
                break;
            case 45: // 返回
                plugin.getGUIManager().openPlayerBalanceManagementGUI(player);
                playSound(Sound.UI_BUTTON_CLICK);
                break;
            case 53: // 关闭
                player.closeInventory();
                playSound(Sound.UI_BUTTON_CLICK);
                break;
        }
    }
    
    /**
     * 处理货币选择
     */
    private void handleCurrencySelection(int slot) {
        List<Currency> currencies = new ArrayList<>(plugin.getCurrencyManager().getEnabledCurrencies());
        int currencyIndex = slot - 10;
        
        if (currencyIndex < currencies.size()) {
            selectedCurrencyId = currencies.get(currencyIndex).getId();
            inputAmount = BigDecimal.ZERO; // 重置输入金额
            refresh();
            playSound(Sound.UI_BUTTON_CLICK);
        }
    }
    
    /**
     * 处理快速金额点击
     */
    private void handleQuickAmountClick(InventoryClickEvent event, int slot) {
        BigDecimal[] quickAmounts = {
            new BigDecimal("1"), new BigDecimal("10"), new BigDecimal("100"), 
            new BigDecimal("1000"), new BigDecimal("10000")
        };
        
        int[] quickSlots = {19, 20, 21, 23, 24};
        
        for (int i = 0; i < quickSlots.length; i++) {
            if (quickSlots[i] == slot) {
                if (isLeftClick(event)) {
                    // 添加到输入金额
                    inputAmount = inputAmount.add(quickAmounts[i]);
                } else if (isRightClick(event)) {
                    // 设置为输入金额
                    inputAmount = quickAmounts[i];
                }
                refresh();
                playSound(Sound.UI_BUTTON_CLICK);
                break;
            }
        }
    }
    
    /**
     * 处理数字输入
     */
    private void handleNumberInput(int slot) {
        if (slot == 31) { // 0按钮
            appendToInput("0");
        } else if (slot == 32) { // 小数点按钮
            appendToInput(".");
        } else if (slot == 35) { // 退格按钮
            handleBackspace();
        } else if (slot >= 28 && slot <= 36) { // 数字1-9
            // 计算数字：28->1, 29->2, 30->3, 33->4, 34->5, 36->6, etc.
            int number;
            if (slot <= 30) {
                number = slot - 27; // 28->1, 29->2, 30->3
            } else if (slot <= 34) {
                number = slot - 29; // 33->4, 34->5
            } else {
                number = slot - 30; // 36->6
            }
            appendToInput(String.valueOf(number));
        }

        playSound(Sound.UI_BUTTON_CLICK);
    }
    
    /**
     * 添加字符到输入
     */
    private void appendToInput(String character) {
        String currentInput = inputAmount.toString();
        
        // 检查小数点
        if (character.equals(".") && currentInput.contains(".")) {
            return; // 已有小数点，不能再添加
        }
        
        try {
            String newInput = currentInput.equals("0") && !character.equals(".") ? 
                             character : currentInput + character;
            inputAmount = new BigDecimal(newInput);
            refresh();
        } catch (NumberFormatException e) {
            // 输入无效，忽略
        }
    }
    
    /**
     * 处理退格
     */
    private void handleBackspace() {
        String currentInput = inputAmount.toString();
        if (currentInput.length() > 1) {
            String newInput = currentInput.substring(0, currentInput.length() - 1);
            try {
                inputAmount = new BigDecimal(newInput);
            } catch (NumberFormatException e) {
                inputAmount = BigDecimal.ZERO;
            }
        } else {
            inputAmount = BigDecimal.ZERO;
        }
        refresh();
    }
    
    /**
     * 处理设置余额
     */
    private void handleSetBalance() {
        if (inputAmount.compareTo(BigDecimal.ZERO) < 0) {
            sendMessage("§c金额不能为负数！");
            playSound(Sound.ENTITY_VILLAGER_NO);
            return;
        }
        
        boolean success = plugin.getCurrencyManager().setBalance(
            targetPlayer.getUniqueId(), selectedCurrencyId, inputAmount);
        
        if (success) {
            Currency currency = plugin.getCurrencyManager().getCurrency(selectedCurrencyId);
            sendMessage("§a成功设置 " + targetPlayer.getName() + " 的" + 
                       currency.getName() + "余额为 " + currency.formatAmountWithColor(inputAmount));
            
            // 记录管理员操作
            plugin.getDatabaseManager().logTransaction("ADMIN_SET", 
                                                     null, 
                                                     targetPlayer.getUniqueId(), 
                                                     selectedCurrencyId, inputAmount, 
                                                     "管理员设置余额: " + player.getName());
            
            refresh();
            playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        } else {
            sendMessage("§c设置余额失败！");
            playSound(Sound.ENTITY_VILLAGER_NO);
        }
    }
    
    /**
     * 处理增加余额
     */
    private void handleAddBalance() {
        if (inputAmount.compareTo(BigDecimal.ZERO) <= 0) {
            sendMessage("§c增加金额必须大于0！");
            playSound(Sound.ENTITY_VILLAGER_NO);
            return;
        }
        
        boolean success = plugin.getCurrencyManager().addBalance(
            targetPlayer.getUniqueId(), selectedCurrencyId, inputAmount);
        
        if (success) {
            Currency currency = plugin.getCurrencyManager().getCurrency(selectedCurrencyId);
            sendMessage("§a成功给 " + targetPlayer.getName() + " 增加 " + 
                       currency.formatAmountWithColor(inputAmount) + " " + currency.getName());
            
            // 记录管理员操作
            plugin.getDatabaseManager().logTransaction("ADMIN_GIVE", 
                                                     null, 
                                                     targetPlayer.getUniqueId(), 
                                                     selectedCurrencyId, inputAmount, 
                                                     "管理员增加余额: " + player.getName());
            
            refresh();
            playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        } else {
            sendMessage("§c增加余额失败！");
            playSound(Sound.ENTITY_VILLAGER_NO);
        }
    }
    
    /**
     * 处理扣除余额
     */
    private void handleSubtractBalance() {
        if (inputAmount.compareTo(BigDecimal.ZERO) <= 0) {
            sendMessage("§c扣除金额必须大于0！");
            playSound(Sound.ENTITY_VILLAGER_NO);
            return;
        }
        
        boolean success = plugin.getCurrencyManager().subtractBalance(
            targetPlayer.getUniqueId(), selectedCurrencyId, inputAmount);
        
        if (success) {
            Currency currency = plugin.getCurrencyManager().getCurrency(selectedCurrencyId);
            sendMessage("§a成功从 " + targetPlayer.getName() + " 扣除 " + 
                       currency.formatAmountWithColor(inputAmount) + " " + currency.getName());
            
            // 记录管理员操作
            plugin.getDatabaseManager().logTransaction("ADMIN_TAKE", 
                                                     null, 
                                                     targetPlayer.getUniqueId(), 
                                                     selectedCurrencyId, inputAmount, 
                                                     "管理员扣除余额: " + player.getName());
            
            refresh();
            playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        } else {
            sendMessage("§c扣除余额失败！余额不足或其他错误。");
            playSound(Sound.ENTITY_VILLAGER_NO);
        }
    }
    
    /**
     * 处理清空输入
     */
    private void handleClearInput() {
        inputAmount = BigDecimal.ZERO;
        refresh();
        playSound(Sound.UI_BUTTON_CLICK);
    }
}
