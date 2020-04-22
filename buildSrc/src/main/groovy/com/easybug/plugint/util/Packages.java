package com.easybug.plugint.util;

import org.apache.http.util.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 要处理的包的集合
 */
public class Packages {
    public static List<String> packages;

    static {
        packages = new ArrayList<>();
    }

    /**
     * 添加需要处理的包，支持简单的匹配
     * @param packageArray
     */
    public static void addPackages(String ...packageArray) {
        if (packageArray == null) {
            return;
        }
        for (String s : packageArray) {
            packages.add(s.toLowerCase());
        }
    }

    /**
     * 是否包含
     * @param inPackage
     * @return
     */
    public static boolean contains(String inPackage) {
        if (TextUtils.isEmpty(inPackage)) {
            return false;
        }
        return packages.contains(inPackage.toLowerCase());
    }
}
