package cn.money.gui.impl;

import cn.money.CNMoney;
import cn.money.gui.BaseGUI;
import cn.money.model.Currency;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 兑换操作GUI - 输入兑换金额并执行兑换
 * 
 * @author CNMoney Team
 */
public class ExchangeGUI extends BaseGUI {
    
    private final Currency fromCurrency;
    private final Currency toCurrency;
    private BigDecimal exchangeAmount = BigDecimal.ZERO;
    private final BigDecimal[] quickAmounts = {
        BigDecimal.valueOf(1), BigDecimal.valueOf(5), BigDecimal.valueOf(10),
        BigDecimal.valueOf(50), BigDecimal.valueOf(100), BigDecimal.valueOf(500)
    };
    
    public ExchangeGUI(CNMoney plugin, Player player, String fromCurrencyId, String toCurrencyId) {
        super(plugin, player, "&6&l货币兑换", 54);
        this.fromCurrency = plugin.getCurrencyManager().getCurrency(fromCurrencyId);
        this.toCurrency = plugin.getCurrencyManager().getCurrency(toCurrencyId);
    }
    
    @Override
    protected void initializeItems() {
        // 清空界面
        inventory.clear();
        
        // 设置玻璃边框
        setGlassBorder();
        
        // 显示兑换信息
        setupExchangeInfo();
        
        // 设置快速金额按钮
        setupQuickAmountButtons();
        
        // 设置数字输入按钮
        setupNumberButtons();
        
        // 设置操作按钮
        setupActionButtons();
        
        // 返回和关闭按钮
        addBackButton(48);
        addCloseButton(50);
    }
    
    /**
     * 设置兑换信息显示
     */
    private void setupExchangeInfo() {
        // 源货币信息
        BigDecimal fromBalance = plugin.getCurrencyManager().getBalance(player.getUniqueId(), fromCurrency.getId());
        ItemStack fromItem = createFromCurrencyDisplay(fromBalance);
        inventory.setItem(11, fromItem);
        
        // 箭头
        ItemStack arrowItem = createItem(Material.ARROW, "&e→", "&7兑换方向");
        inventory.setItem(13, arrowItem);
        
        // 目标货币信息
        BigDecimal toBalance = plugin.getCurrencyManager().getBalance(player.getUniqueId(), toCurrency.getId());
        ItemStack toItem = createToCurrencyDisplay(toBalance);
        inventory.setItem(15, toItem);
        
        // 当前兑换金额显示
        ItemStack amountItem = createExchangeAmountDisplay();
        inventory.setItem(22, amountItem);
    }
    
    /**
     * 创建源货币显示
     */
    private ItemStack createFromCurrencyDisplay(BigDecimal balance) {
        List<String> lore = new ArrayList<>();
        lore.add("&7源货币");
        lore.add("");
        lore.add("&7名称: &f" + fromCurrency.getName());
        lore.add("&7符号: &f" + fromCurrency.getSymbol());
        lore.add("&7余额: " + fromCurrency.formatAmountWithColor(balance));
        
        Material material = getMaterialForCurrency(fromCurrency);
        String displayName = "&c&l源货币: " + fromCurrency.getColor() + fromCurrency.getName();
        
        return createItem(material, displayName, lore);
    }
    
    /**
     * 创建目标货币显示
     */
    private ItemStack createToCurrencyDisplay(BigDecimal balance) {
        List<String> lore = new ArrayList<>();
        lore.add("&7目标货币");
        lore.add("");
        lore.add("&7名称: &f" + toCurrency.getName());
        lore.add("&7符号: &f" + toCurrency.getSymbol());
        lore.add("&7余额: " + toCurrency.formatAmountWithColor(balance));
        
        // 计算兑换率
        BigDecimal exchangeRate = calculateExchangeRate();
        lore.add("");
        lore.add("&7兑换率: &e1 " + fromCurrency.getSymbol() + " = " + 
                toCurrency.formatAmount(exchangeRate) + " " + toCurrency.getSymbol());
        
        Material material = getMaterialForCurrency(toCurrency);
        String displayName = "&a&l目标货币: " + toCurrency.getColor() + toCurrency.getName();
        
        return createItem(material, displayName, lore);
    }
    
    /**
     * 创建兑换金额显示
     */
    private ItemStack createExchangeAmountDisplay() {
        List<String> lore = new ArrayList<>();
        lore.add("&7当前兑换金额");
        lore.add("");
        lore.add("&7输入金额: " + fromCurrency.formatAmountWithColor(exchangeAmount));
        
        if (exchangeAmount.compareTo(BigDecimal.ZERO) > 0) {
            // 计算兑换结果
            BigDecimal exchangeRate = calculateExchangeRate();
            BigDecimal grossAmount = exchangeAmount.multiply(exchangeRate);
            
            // 计算手续费
            double feePercentage = plugin.getConfigManager().getExchangeFeePercentage();
            BigDecimal feeAmount = grossAmount.multiply(BigDecimal.valueOf(feePercentage));
            BigDecimal netAmount = grossAmount.subtract(feeAmount);
            
            lore.add("");
            lore.add("&7兑换结果:");
            lore.add("&e总金额: " + toCurrency.formatAmountWithColor(grossAmount));
            lore.add("&c手续费: " + toCurrency.formatAmountWithColor(feeAmount) + 
                    " (" + (feePercentage * 100) + "%)");
            lore.add("&a实得金额: " + toCurrency.formatAmountWithColor(netAmount));
        } else {
            lore.add("");
            lore.add("&7请输入兑换金额");
        }
        
        return createItem(Material.GOLD_BLOCK, "&6&l兑换金额", lore);
    }
    
    /**
     * 设置快速金额按钮
     */
    private void setupQuickAmountButtons() {
        int[] slots = {19, 20, 21, 28, 29, 30};
        
        for (int i = 0; i < Math.min(quickAmounts.length, slots.length); i++) {
            BigDecimal amount = quickAmounts[i];
            ItemStack quickButton = createQuickAmountButton(amount);
            inventory.setItem(slots[i], quickButton);
        }
    }
    
    /**
     * 创建快速金额按钮
     */
    private ItemStack createQuickAmountButton(BigDecimal amount) {
        List<String> lore = new ArrayList<>();
        lore.add("&7快速设置兑换金额");
        lore.add("");
        lore.add("&e左键: 设置为 " + fromCurrency.formatAmount(amount));
        lore.add("&e右键: 增加 " + fromCurrency.formatAmount(amount));
        
        // 检查是否有足够余额
        BigDecimal balance = plugin.getCurrencyManager().getBalance(player.getUniqueId(), fromCurrency.getId());
        if (balance.compareTo(amount) >= 0) {
            lore.add("");
            lore.add("&a余额充足");
        } else {
            lore.add("");
            lore.add("&c余额不足");
        }
        
        String displayName = "&e" + fromCurrency.formatAmount(amount);
        return createItem(Material.GOLD_NUGGET, displayName, lore);
    }
    
    /**
     * 设置数字输入按钮
     */
    private void setupNumberButtons() {
        // 清空按钮
        ItemStack clearButton = createItem(Material.RED_DYE, "&c清空", "&7清空当前输入的金额");
        inventory.setItem(37, clearButton);
        
        // 全部按钮
        ItemStack allButton = createItem(Material.GREEN_DYE, "&a全部", "&7使用全部余额进行兑换");
        inventory.setItem(43, allButton);
    }
    
    /**
     * 设置操作按钮
     */
    private void setupActionButtons() {
        // 确认兑换按钮
        ItemStack confirmButton = createConfirmButton();
        inventory.setItem(40, confirmButton);
        
        // 取消按钮
        ItemStack cancelButton = createItem(Material.BARRIER, "&c取消兑换", "&7取消当前兑换操作");
        inventory.setItem(44, cancelButton);
    }
    
    /**
     * 创建确认按钮
     */
    private ItemStack createConfirmButton() {
        List<String> lore = new ArrayList<>();
        
        if (exchangeAmount.compareTo(BigDecimal.ZERO) <= 0) {
            lore.add("&c请先输入兑换金额");
            return createItem(Material.GRAY_DYE, "&7确认兑换", lore);
        }
        
        // 检查余额
        BigDecimal balance = plugin.getCurrencyManager().getBalance(player.getUniqueId(), fromCurrency.getId());
        if (balance.compareTo(exchangeAmount) < 0) {
            lore.add("&c余额不足");
            lore.add("&7需要: " + fromCurrency.formatAmount(exchangeAmount));
            lore.add("&7拥有: " + fromCurrency.formatAmount(balance));
            return createItem(Material.RED_DYE, "&c余额不足", lore);
        }
        
        // 检查最小金额
        double minAmount = plugin.getConfigManager().getExchangeMinAmount();
        if (exchangeAmount.doubleValue() < minAmount) {
            lore.add("&c金额过小");
            lore.add("&7最小兑换金额: " + fromCurrency.formatAmount(BigDecimal.valueOf(minAmount)));
            return createItem(Material.ORANGE_DYE, "&c金额过小", lore);
        }
        
        // 可以兑换
        lore.add("&a点击确认兑换");
        lore.add("");
        lore.add("&7兑换金额: " + fromCurrency.formatAmount(exchangeAmount));
        
        BigDecimal exchangeRate = calculateExchangeRate();
        BigDecimal grossAmount = exchangeAmount.multiply(exchangeRate);
        double feePercentage = plugin.getConfigManager().getExchangeFeePercentage();
        BigDecimal feeAmount = grossAmount.multiply(BigDecimal.valueOf(feePercentage));
        BigDecimal netAmount = grossAmount.subtract(feeAmount);
        
        lore.add("&7实得金额: " + toCurrency.formatAmount(netAmount));
        lore.add("&7手续费: " + toCurrency.formatAmount(feeAmount));
        
        return createItem(Material.LIME_DYE, "&a&l确认兑换", lore);
    }
    
    /**
     * 计算兑换汇率
     */
    private BigDecimal calculateExchangeRate() {
        // 直接从源货币获取到目标货币的汇率
        BigDecimal rate = fromCurrency.getExchangeRate(toCurrency.getId());
        if (rate != null) {
            return rate;
        }

        // 如果没有直接汇率，尝试反向计算
        BigDecimal reverseRate = toCurrency.getExchangeRate(fromCurrency.getId());
        if (reverseRate != null && reverseRate.compareTo(BigDecimal.ZERO) > 0) {
            return BigDecimal.ONE.divide(reverseRate, toCurrency.getDecimals(), RoundingMode.HALF_UP);
        }

        // 默认返回1:1汇率
        return BigDecimal.ONE;
    }
    
    /**
     * 根据货币获取对应的材料
     */
    private Material getMaterialForCurrency(Currency currency) {
        String currencyId = currency.getId().toLowerCase();
        
        if (currencyId.contains("gold")) {
            return Material.GOLD_INGOT;
        } else if (currencyId.contains("silver")) {
            return Material.IRON_INGOT;
        } else if (currencyId.contains("copper")) {
            return Material.COPPER_INGOT;
        } else if (currencyId.contains("diamond")) {
            return Material.DIAMOND;
        } else if (currencyId.contains("emerald")) {
            return Material.EMERALD;
        } else if (currencyId.contains("coin")) {
            return Material.SUNFLOWER;
        } else {
            return Material.GOLD_NUGGET;
        }
    }
    
    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = getClickedSlot(event);
        
        // 处理快速金额按钮
        if (isQuickAmountSlot(slot)) {
            handleQuickAmountClick(event, slot);
            return;
        }
        
        // 处理其他按钮
        switch (slot) {
            case 37: // 清空
                exchangeAmount = BigDecimal.ZERO;
                refresh();
                playSound(Sound.UI_BUTTON_CLICK);
                sendMessage("&e已清空兑换金额");
                break;
                
            case 43: // 全部
                BigDecimal balance = plugin.getCurrencyManager().getBalance(player.getUniqueId(), fromCurrency.getId());
                exchangeAmount = balance;
                refresh();
                playSound(Sound.UI_BUTTON_CLICK);
                sendMessage("&e已设置为全部余额: " + fromCurrency.formatAmount(balance));
                break;
                
            case 40: // 确认兑换
                handleExchangeConfirm();
                break;
                
            case 44: // 取消
                player.closeInventory();
                playSound(Sound.UI_BUTTON_CLICK);
                sendMessage("&c已取消兑换操作");
                break;
                
            case 48: // 返回
                // 返回到目标货币选择GUI
                ExchangeTargetGUI targetGUI = new ExchangeTargetGUI(plugin, player, fromCurrency);
                plugin.getGUIManager().openGUI(player, targetGUI);
                playSound(Sound.UI_BUTTON_CLICK);
                break;
                
            case 50: // 关闭
                player.closeInventory();
                playSound(Sound.UI_BUTTON_CLICK);
                break;
        }
    }
    
    /**
     * 检查是否为快速金额槽位
     */
    private boolean isQuickAmountSlot(int slot) {
        int[] quickSlots = {19, 20, 21, 28, 29, 30};
        for (int quickSlot : quickSlots) {
            if (slot == quickSlot) return true;
        }
        return false;
    }
    
    /**
     * 处理快速金额点击
     */
    private void handleQuickAmountClick(InventoryClickEvent event, int slot) {
        int[] quickSlots = {19, 20, 21, 28, 29, 30};
        int index = -1;
        
        for (int i = 0; i < quickSlots.length; i++) {
            if (quickSlots[i] == slot) {
                index = i;
                break;
            }
        }
        
        if (index >= 0 && index < quickAmounts.length) {
            BigDecimal amount = quickAmounts[index];
            
            if (isLeftClick(event)) {
                // 设置为指定金额
                exchangeAmount = amount;
                sendMessage("&e已设置兑换金额为: " + fromCurrency.formatAmount(amount));
            } else if (isRightClick(event)) {
                // 增加指定金额
                exchangeAmount = exchangeAmount.add(amount);
                sendMessage("&e已增加兑换金额: " + fromCurrency.formatAmount(amount));
            }
            
            refresh();
            playSound(Sound.UI_BUTTON_CLICK);
        }
    }
    
    /**
     * 处理兑换确认
     */
    private void handleExchangeConfirm() {
        // 执行兑换逻辑
        boolean success = plugin.getCurrencyManager().exchange(
            player.getUniqueId(),
            fromCurrency.getId(),
            toCurrency.getId(),
            exchangeAmount
        );
        
        if (success) {
            playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
            sendMessage("&a兑换成功！");
            
            // 计算实际兑换结果
            BigDecimal exchangeRate = calculateExchangeRate();
            BigDecimal grossAmount = exchangeAmount.multiply(exchangeRate);
            double feePercentage = plugin.getConfigManager().getExchangeFeePercentage();
            BigDecimal feeAmount = grossAmount.multiply(BigDecimal.valueOf(feePercentage));
            BigDecimal netAmount = grossAmount.subtract(feeAmount);
            
            sendMessage("&7兑换了 " + fromCurrency.formatAmount(exchangeAmount) + " " + fromCurrency.getName());
            sendMessage("&7获得了 " + toCurrency.formatAmount(netAmount) + " " + toCurrency.getName());
            sendMessage("&7手续费 " + toCurrency.formatAmount(feeAmount));
            
            // 关闭GUI
            player.closeInventory();
        } else {
            playSound(Sound.ENTITY_VILLAGER_NO);
            sendMessage("&c兑换失败！请检查余额和配置");
        }
    }
}
