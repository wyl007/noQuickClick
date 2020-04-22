package com.easybug.plugint.util;

import com.easybug.plugint.inject.ASMClassHandle;
import com.easybug.plugint.bean.ClassBean;
import com.easybug.plugint.inject.IClassHandle;
import com.easybug.plugint.util.LogUtil;

import org.apache.http.util.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * 类相关工具
 */
public class ClassUtil {
    public static String tempDir;
    /**
     * 将字节数组写到class文件
     * @param bytes
     * @param filePath
     */
    public static void saveToFile(byte[] bytes, String filePath) {
        if (bytes == null || bytes.length == 0 || TextUtils.isEmpty(filePath)) {
            return;
        }
        LogUtil.d("将class保存到临时文件夹：" + filePath);
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 对于class文件进行注入，返回注入后的代码
     * @param classPath
     * @return
     */
    public static byte[] inject(String classPath) {
        byte[] bytes = FileIOUtils.readFile2BytesByStream(new File(classPath));
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ClassBean bean = new ClassBean(classPath, bytes);
        IClassHandle iClassHandle = new ASMClassHandle(bean);
        return  iClassHandle.insertCode();
    }

    /**
     * 以.分隔的类，是否是需要处理的
     * @param classFullName
     * @param packges
     */
    public static boolean containes(String classFullName, List<String> packges) {
        if (TextUtils.isEmpty(classFullName) || packges == null) {
            return false;
        }

        for (String packge : packges) {
            if (classFullName.contains(packge)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 文件是否需要处理
     * @param path
     * @param packages
     * @return
     */
    public static boolean needHandle(String path, List<String> packages) {
        LogUtil.d("needHandle  path:" + path + " packages：" + packages);
        if (TextUtils.isEmpty(path) || packages == null) {
            return false;
        }
        String classFullName = path.replaceAll("/", ".");
        if (classFullName.endsWith(".class") //class文件
                && !classFullName.contains("R.class") //不是R类
                && !classFullName.contains("BuildConfig.class")
                && !classFullName.contains("performance")) //将统计类排除，避免循环调用
        {
            LogUtil.d("needHandle  class校验通过 classFullName：" + classFullName + " packages：" + packages );
            return containes(classFullName, packages);
        }
        return false;

    }
}
