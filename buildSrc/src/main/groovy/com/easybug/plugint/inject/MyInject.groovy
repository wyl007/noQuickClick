package com.easybug.plugint.inject

import com.easybug.plugint.AopConfig
import com.easybug.plugint.util.ClassUtil
import com.easybug.plugint.util.JarUtil
import com.easybug.plugint.util.LogUtil
import org.gradle.api.Project

/**
 * 代码的注入
 */
public class MyInject {
    /**
     * 对文件夹下的class注入
     * @param dir 存放class的文件夹路径
     * @param needPackageName 需要注入的包名/包名的一部分
     * @param project
     */
    public static void injectDir(String dirPath, AopConfig aopConfig, Project project) {
        File dir = new File(dirPath)
        LogUtil.e("injectDir 输入的目录：" + dirPath)
        if (dir.isDirectory()) {
            dir.eachFileRecurse { File file ->
                LogUtil.e("injectDir 开始判断是否需要处理Class文件：" + file.getAbsolutePath())
                if (ClassUtil.needHandle(file.path, aopConfig.needPackages)) {
                    LogUtil.e("需要处理")
                    byte[] bytes = ClassUtil.inject(file.path)
                    if (bytes == null) {
                        LogUtil.e("处理失败")
                    } else {
                        def classPath = file.parentFile.absolutePath + File.separator + file.name
                        FileOutputStream fos = new FileOutputStream(classPath)
                        fos.write(bytes)
                        fos.close()
                        LogUtil.e("处理成功")
                    }

                } else {
                    LogUtil.e("不需要处理")
                }

            }
        }



    }

    /**
     * 对jar进行注入
     * @param path
     * @param needPackageName
     * @param project
     */
    public static File injectJar(String jarPath, String tempDir, AopConfig aopConfig, Project project) {
        if (jarPath == null) {
            return
        }

        //1.判断是否是需要处理饿jar（jar是否是处理过的）

        //2.处理jar
        return JarUtil.injectJar(new File(jarPath), tempDir, project, aopConfig)

    }
}