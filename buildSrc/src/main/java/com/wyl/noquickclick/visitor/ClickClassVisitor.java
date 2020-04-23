package com.wyl.noquickclick.visitor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * author : wangyuelin
 * time   : 2020/4/23 4:59 PM
 * desc   : Click类的Visito
 */
public class ClickClassVisitor extends ClassVisitor {

    private boolean needHack;//表示此类是否需要处理

    public ClickClassVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        //确定哪些包下的类需要处理
        System.out.println("访问的class：" + name);
        if (name.startsWith("com/wylnoquickclick")) {
            //确定当前需要处理的类是View$OnClickListener的子类
            for (String anInterface : interfaces) {
                if (anInterface.equals("android/view/View$OnClickListener")) {
                    System.out.println("需要处理的class：" + name);
                    needHack = true;
                    break;
                }
            }
        }
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        System.out.println("访问的method：" + name);
        if (needHack) {
            //对onClick方法处理
            if (name.equals("onClick")) {
                System.out.println("需要处理的method：" + name);
                methodVisitor = new ClickMethodVisitor(this.api, methodVisitor, access, name, desc);
            }
        }
        return methodVisitor;
    }
}
