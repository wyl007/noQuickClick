package com.easybug.plugint.util;

import org.apache.http.util.TextUtils;
import org.objectweb.asm.Type;

public class MethodUtil {

    /**
     *获得方法的签名，类似于com.xxx.xxx.xx.Method(int, String)
     * @param className  类似于 com.xxx.xxx.xx.AopPluginExtension.class
     * @param methodName
     * @param paramsType
     * @return
     */
    public static String getMethodSignature(String className, String methodName, Type[] paramsType) {
        if (TextUtils.isEmpty(className) || TextUtils.isEmpty(methodName)) {
            return "";
        }

        String classFileSuffix = ".class";
        if (className.endsWith(classFileSuffix)) {
            className = className.substring(0, className.length() - classFileSuffix.length());
        }

        StringBuilder methodSignature = new StringBuilder();
        methodSignature.append(className).append(".").append(methodName).append("(");
        if (paramsType != null) {
           for (int i = 0; i < paramsType.length; i++) {
               methodSignature.append(paramsType[i].getClassName());
               if (i < paramsType.length - 1) {
                   methodSignature.append(",");
               }
           }
        }
        methodSignature.append(")");
        return methodSignature.toString();
    }
}
