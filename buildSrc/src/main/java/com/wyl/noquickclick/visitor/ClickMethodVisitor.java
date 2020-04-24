package com.wyl.noquickclick.visitor;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * author : wangyuelin
 * time   : 2020/4/23 5:13 PM
 * desc   : 处理OnClick的方法的
 */
public class ClickMethodVisitor extends AdviceAdapter {
    /**
     * Creates a new {@link AdviceAdapter}.
     *
     * @param api    the ASM API version implemented by this visitor. Must be one
     *               of {@link Opcodes#ASM4} or {@link Opcodes#ASM5}.
     * @param mv     the method visitor to which this adapter delegates calls.
     * @param access the method's access flags (see {@link Opcodes}).
     * @param name   the method's name.
     * @param desc   the method's descriptor (see {@link Type Type}).
     */
    protected ClickMethodVisitor(int api, MethodVisitor mv, int access, String name, String desc) {
        super(api, mv, access, name, desc);
    }

    @Override
    protected void onMethodEnter() {
        super.onMethodEnter();
        mv.visitMethodInsn(INVOKESTATIC, "com/talk51/basiclib/widget/CheckOnClick", "isFastClick", "()Z", false);
        Label label = new Label();
        mv.visitJumpInsn(IFEQ,label);
        mv.visitInsn(RETURN);
        mv.visitLabel(label);
    }
}
