package com.easybug.plugint;


class AopConfig {
    List<String> needPackages; // 需要处理的包名
    public boolean isAop;//表示是否进行代码注入
    public boolean isDebug;//标志是否输出debug信息
    public String name;

    @Override
    String toString() {
        return "isAop:" + isAop + "; isDebug:" + isDebug;
    }
}
