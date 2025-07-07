package cn.money.gui;

import cn.money.CNMoney;
import cn.money.gui.impl.MainGUI;
import cn.money.gui.impl.BalanceGUI;
import cn.money.gui.impl.ExchangeGUI;
import cn.money.gui.impl.ExchangeSelectGUI;
import cn.money.gui.impl.AdminGUI;
import cn.money.gui.impl.PlayerBalanceManagementGUI;
import cn.money.gui.impl.PlayerBalanceEditGUI;
import cn.money.gui.impl.CurrencyManagementGUI;
import cn.money.gui.impl.ExchangeRateManagementGUI;
import cn.money.gui.impl.CurrencyToggleGUI;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * GUI管理器
 * 
 * @author CNMoney Team
 */
public class GUIManager implements Listener {
    
    private final CNMoney plugin;
    private final Map<UUID, BaseGUI> openGUIs;
    
    public GUIManager(CNMoney plugin) {
        this.plugin = plugin;
        this.openGUIs = new HashMap<>();
        
        // 注册事件监听器
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * 打开主GUI
     */
    public void openMainGUI(Player player) {
        MainGUI gui = new MainGUI(plugin, player);
        openGUI(player, gui);
    }
    
    /**
     * 打开余额查看GUI
     */
    public void openBalanceGUI(Player player) {
        BalanceGUI gui = new BalanceGUI(plugin, player);
        openGUI(player, gui);
    }
    
    /**
     * 打开兑换选择GUI
     */
    public void openExchangeSelectGUI(Player player) {
        ExchangeSelectGUI gui = new ExchangeSelectGUI(plugin, player);
        openGUI(player, gui);
    }
    
    /**
     * 打开兑换GUI
     */
    public void openExchangeGUI(Player player, String fromCurrency, String toCurrency) {
        ExchangeGUI gui = new ExchangeGUI(plugin, player, fromCurrency, toCurrency);
        openGUI(player, gui);
    }

    /**
     * 打开管理员GUI
     */
    public void openAdminGUI(Player player) {
        AdminGUI gui = new AdminGUI(plugin, player);
        openGUI(player, gui);
    }

    /**
     * 打开玩家余额管理GUI
     */
    public void openPlayerBalanceManagementGUI(Player player) {
        PlayerBalanceManagementGUI gui = new PlayerBalanceManagementGUI(plugin, player);
        openGUI(player, gui);
    }

    /**
     * 打开玩家余额编辑GUI
     */
    public void openPlayerBalanceEditGUI(Player player, OfflinePlayer targetPlayer) {
        PlayerBalanceEditGUI gui = new PlayerBalanceEditGUI(plugin, player, targetPlayer);
        openGUI(player, gui);
    }

    /**
     * 打开货币管理GUI
     */
    public void openCurrencyManagementGUI(Player player) {
        CurrencyManagementGUI gui = new CurrencyManagementGUI(plugin, player);
        openGUI(player, gui);
    }

    /**
     * 打开汇率管理GUI
     */
    public void openExchangeRateManagementGUI(Player player) {
        ExchangeRateManagementGUI gui = new ExchangeRateManagementGUI(plugin, player);
        openGUI(player, gui);
    }

    /**
     * 打开货币启用管理GUI
     */
    public void openCurrencyToggleGUI(Player player) {
        CurrencyToggleGUI gui = new CurrencyToggleGUI(plugin, player);
        openGUI(player, gui);
    }
    
    /**
     * 打开GUI（内部方法）
     */
    private void openGUIInternal(Player player, BaseGUI gui) {
        // 关闭之前的GUI
        closeGUI(player);

        // 打开新GUI
        gui.open();
        openGUIs.put(player.getUniqueId(), gui);

        // 只有在配置启用时才显示GUI操作日志
        if (plugin.getConfigManager().isGUIOperationLoggingEnabled()) {
            plugin.getLogger().info("为玩家 " + player.getName() + " 打开GUI: " + gui.getClass().getSimpleName());
        }
    }

    /**
     * 打开GUI（公共方法）
     */
    public void openGUI(Player player, BaseGUI gui) {
        openGUIInternal(player, gui);
    }
    
    /**
     * 关闭玩家的GUI
     */
    public void closeGUI(Player player) {
        BaseGUI gui = openGUIs.remove(player.getUniqueId());
        if (gui != null) {
            gui.close();
        }
    }
    
    /**
     * 获取玩家当前打开的GUI
     */
    public BaseGUI getOpenGUI(Player player) {
        return openGUIs.get(player.getUniqueId());
    }
    
    /**
     * 检查玩家是否有打开的GUI
     */
    public boolean hasOpenGUI(Player player) {
        return openGUIs.containsKey(player.getUniqueId());
    }
    
    /**
     * 刷新所有打开的GUI
     */
    public void refreshAllGUIs() {
        openGUIs.values().forEach(BaseGUI::refresh);
    }
    
    /**
     * 刷新指定玩家的GUI
     */
    public void refreshPlayerGUI(Player player) {
        BaseGUI gui = openGUIs.get(player.getUniqueId());
        if (gui != null) {
            gui.refresh();
        }
    }
    
    /**
     * 关闭所有GUI
     */
    public void closeAllGUIs() {
        openGUIs.values().forEach(BaseGUI::close);
        openGUIs.clear();
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        BaseGUI gui = openGUIs.get(player.getUniqueId());
        
        if (gui != null && gui.getInventory().equals(event.getInventory())) {
            event.setCancelled(true);
            gui.handleClick(event);
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        BaseGUI gui = openGUIs.get(player.getUniqueId());
        
        if (gui != null && gui.getInventory().equals(event.getInventory())) {
            gui.onClose();
            openGUIs.remove(player.getUniqueId());
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        openGUIs.remove(event.getPlayer().getUniqueId());
    }
}
