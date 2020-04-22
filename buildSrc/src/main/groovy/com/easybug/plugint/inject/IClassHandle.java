package com.easybug.plugint.inject;

/**
 * 定义字节处理的接口，字节处理可以交给ASM、Javassist等实现
 */
public interface IClassHandle {

    /**
     * 插入代码
     */
   public byte[] insertCode();



}
