package com.wyl.noquickclick.bean;

import java.util.List;

/**
 * author : wangyuelin
 * time   : 2020/4/24 7:26 PM
 * desc   : NoQuickClick 插件配置
 */
public class NoQuickClickConfig {
    public boolean isEnable;//配置插件是否使用
    public List<String> includePackages;//需要处理的包名
    public List<String> filterClass;//需要过滤掉的class
}
