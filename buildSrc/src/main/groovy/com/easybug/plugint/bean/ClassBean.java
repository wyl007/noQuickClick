package com.easybug.plugint.bean;

/**
 * 表示编译后的class文件
 */
public class ClassBean {
    public String path;//存储的路径:对于jar文件里面的class，路径就是jar路径；对于文件夹下的class，路径就是class的路径
    public byte[] bytes;//字节数组

    public ClassBean(String path, byte[] bytes) {
        this.path = path;
        this.bytes = bytes;
    }
}
