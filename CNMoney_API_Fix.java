    public CNMoneyAPI getAPI() {
        return api;
    }
    
    /**
     * 获取CNMoney API实例（静态方法）
     * 供其他插件调用
     * 
     * @return CNMoneyAPI实例，如果插件未启用则返回null
     */
    public static CNMoneyAPI getAPI() {
        if (instance == null) {
            return null;
        }
        return instance.getAPI();
    }
}
