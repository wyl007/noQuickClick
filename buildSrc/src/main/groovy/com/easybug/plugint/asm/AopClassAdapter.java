package com.easybug.plugint.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * 描述:决定需要处理哪些方法
 *
 * @outhor wangyuelin
 * @create 2018-10-19 下午4:18
 */
public class AopClassAdapter extends ClassVisitor implements Opcodes {
    private String className;
    private String constructorName = "<init>";
    private String CL_INIT = "<clinit>";
    private int classAccess;


    public AopClassAdapter(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        className = name;//类名称
        classAccess = access;//类的访问标志
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        //卧槽 这样竟然解决了错误，NB
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        //构造方法、抽象方法和接口中的方法都不处理
        if (name == null
                || name.equals("")
                || name.equalsIgnoreCase(CL_INIT)
                || ((classAccess & Opcodes.ACC_INTERFACE) != 0) //目前访问的类是接口
                || ((access & Opcodes.ACC_ABSTRACT) != 0)  //目前访问的方法是抽象方法
            //还需要把系统生成的文件过滤了，如BuildConfig
        ) {
            return mv;
        }
        className = className.replaceAll("/", ".");
        return new MethodInsertAdapter(this.api, mv, access, name, signature ,desc, className);
    }
}