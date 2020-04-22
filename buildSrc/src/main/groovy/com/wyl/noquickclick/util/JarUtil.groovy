package com.wyl.noquickclick.util

import com.wyl.noquickclick.AopConfig
import com.wyl.noquickclick.bean.ClassBean
import com.wyl.noquickclick.inject.ASMClassHandle
import com.wyl.noquickclick.inject.IClassHandle
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Project

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

public class JarUtil {

    /**
     * jar中的字节码注入代码
     * @param jarFile 需要被注入的jar包
     * @param tempDir 临时文件存放
     * @param project
     */
    public static File injectJar(File jarFile, String tempDir, Project project, AopConfig aopConfig) {

        //读取原来的jar
        JarFile originJar = new JarFile(jarFile)

        //输出的临时jar文件
        String hexName = DigestUtils.md5Hex(jarFile.getAbsolutePath()).substring(0, 8)
        //避免和现有的jar文件重复
        File outputJar = new File(tempDir, hexName + jarFile.getName())
        LogUtil.e("injectJar 待修改的jar文件：" + jarFile.path)
        LogUtil.e("injectJar 修改后的jar文件：" + outputJar)
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputJar))

        //开始遍历jar文件里面的class
        Enumeration enumeration = originJar.entries()
        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) enumeration.nextElement()
            InputStream inputStream = originJar.getInputStream(jarEntry)

            String entryName = jarEntry.getName()

            ZipEntry zipEntry = new ZipEntry(entryName)
            jarOutputStream.putNextEntry(zipEntry)

            byte[] modifiedClassBytes = null//修改后的class字节码
            byte[] originClassBytes = IOUtils.toByteArray(inputStream)//未修改的class字节码
            boolean needHandle = ClassUtil.needHandle(entryName, aopConfig.needPackages)
            LogUtil.e "injectJar 开始判断是否需要处理：" + entryName + " ==== " + needHandle
            ClassBean bean = new ClassBean(entryName, originClassBytes)
            if (needHandle) {//确认是class文件并且位于需要修改的包下，然后修改
                LogUtil.e("injectJar 需要处理的Class文件：" + entryName)
                IClassHandle iClassHandle = new ASMClassHandle(bean)
                modifiedClassBytes = iClassHandle.insertCode()
            }
            if (modifiedClassBytes == null) {
                if (!needHandle) {
                    LogUtil.e("class文件不需要修改")
                } else {
                    LogUtil.e("class文件修改失败")
                }
                jarOutputStream.write(originClassBytes)//使用未修改的字节码
            } else {
                if (needHandle) {
                    LogUtil.e("class文件修改成功---------------end")
                    ClassUtil.saveToFile(modifiedClassBytes, ClassUtil.tempDir + File.separator + (bean.path.substring(bean.path.lastIndexOf("/") + 1)))
                }
                jarOutputStream.write(modifiedClassBytes)//使用修改后的字节码
            }
        }

        jarOutputStream.close()
        originJar.close()
        return outputJar
    }
}