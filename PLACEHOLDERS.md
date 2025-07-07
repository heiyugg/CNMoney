# CNMoney 占位符使用说明

CNMoney插件提供了丰富的PlaceholderAPI占位符，可以在其他插件中使用来显示货币相关信息。

## 基础占位符

### 余额显示

#### 原始数值
- `%cnmoney_balance%` - 显示玩家默认货币的余额（纯数字）
- `%cnmoney_balance_<货币ID>%` - 显示玩家指定货币的余额（纯数字）

**示例：**
```
%cnmoney_balance% → 1000.50
%cnmoney_balance_gold_coin% → 1000.50
%cnmoney_balance_silver_coin% → 500.25
```

#### 格式化显示
- `%cnmoney_formatted%` - 显示玩家默认货币的格式化余额（带颜色和符号）
- `%cnmoney_formatted_<货币ID>%` - 显示玩家指定货币的格式化余额（带颜色和符号）

**示例：**
```
%cnmoney_formatted% → §61,000.50 金币
%cnmoney_formatted_gold_coin% → §61,000.50 金币
%cnmoney_formatted_silver_coin% → §7500.25 银币
```

## 货币信息占位符

### 货币属性
- `%cnmoney_currency_<货币ID>_name%` - 货币名称
- `%cnmoney_currency_<货币ID>_symbol%` - 货币符号
- `%cnmoney_currency_<货币ID>_plural%` - 货币复数形式
- `%cnmoney_currency_<货币ID>_decimals%` - 货币小数位数
- `%cnmoney_currency_<货币ID>_enabled%` - 货币是否启用
- `%cnmoney_currency_<货币ID>_primary%` - 是否为主要货币

**示例：**
```
%cnmoney_currency_gold_coin_name% → 金币
%cnmoney_currency_gold_coin_symbol% → 金
%cnmoney_currency_gold_coin_plural% → 金币
%cnmoney_currency_gold_coin_decimals% → 2
%cnmoney_currency_gold_coin_enabled% → true
%cnmoney_currency_gold_coin_primary% → true
```

## 高级占位符

### 总资产
- `%cnmoney_total%` - 玩家所有货币的总价值（以默认货币计算）

**示例：**
```
%cnmoney_total% → §62,500.75 金币
```

### 排行榜（待实现）
- `%cnmoney_top_<货币ID>_<位置>%` - 指定货币排行榜指定位置的玩家名
- `%cnmoney_top_<货币ID>_<位置>_balance%` - 指定货币排行榜指定位置的余额

**示例：**
```
%cnmoney_top_gold_coin_1% → PlayerName
%cnmoney_top_gold_coin_1_balance% → 10000.00
```

## 可用货币ID

根据默认配置，以下货币ID可用：

- `gold_coin` - 金币（主要货币）
- `silver_coin` - 银币
- `copper_coin` - 铜币
- `diamond_coin` - 钻石币
- `emerald_coin` - 绿宝石币

## 使用示例

### 在聊天插件中显示余额
```yaml
# ChatFormat配置示例
format: "&7[&6%cnmoney_formatted%&7] &f%player_name%: %message%"
```

### 在计分板中显示多种货币
```yaml
# Scoreboard配置示例
lines:
  - "&6金币: &f%cnmoney_formatted_gold_coin%"
  - "&7银币: &f%cnmoney_formatted_silver_coin%"
  - "&c铜币: &f%cnmoney_formatted_copper_coin%"
  - "&b钻石币: &f%cnmoney_formatted_diamond_coin%"
  - "&2绿宝石币: &f%cnmoney_formatted_emerald_coin%"
  - ""
  - "&e总资产: &f%cnmoney_total%"
```

### 在GUI插件中显示货币信息
```yaml
# DeluxeMenus配置示例
items:
  balance_display:
    material: GOLD_INGOT
    display_name: "&6我的金币"
    lore:
      - "&7当前余额: %cnmoney_formatted_gold_coin%"
      - "&7货币符号: %cnmoney_currency_gold_coin_symbol%"
      - "&7小数位数: %cnmoney_currency_gold_coin_decimals%"
```

## 注意事项

1. **依赖要求**：使用占位符需要安装PlaceholderAPI插件
2. **货币ID**：货币ID区分大小写，请确保使用正确的ID
3. **权限**：玩家需要有相应的权限才能查看货币信息
4. **实时更新**：占位符会实时反映玩家的当前余额
5. **颜色代码**：格式化占位符包含颜色代码，在某些地方可能需要处理

## 配置集成

确保在config.yml中启用PlaceholderAPI集成：

```yaml
integrations:
  placeholderapi:
    enabled: true
```

## 故障排除

如果占位符不工作，请检查：

1. PlaceholderAPI插件是否已安装并启用
2. CNMoney插件是否正确加载
3. 货币ID是否正确
4. 玩家是否有相应权限
5. 插件日志中是否有错误信息

更多信息请查看插件配置文件和日志。
