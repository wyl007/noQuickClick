package com.easybug.plugint.util

import org.gradle.api.Project
class LogUtil {
    private static Project project
    public static boolean debug

    /**
     * 初始化
     * @param pro
     */
    public static void init(Project pro, boolean isDebug) {
        project = pro
        debug = isDebug
    }

    public static void d(String msg) {
        if (project != null && debug) {
            project.logger.debug(msg)
        }
    }

    public static void e(String msg) {
        if (project != null && debug) {
            project.logger.error(msg)
        }
    }
}