package com.easybug.plugint.asm;

import com.easybug.plugint.util.MethodUtil;

import org.apache.http.util.TextUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * 方法插入代码before、after、around，都可以通过AdviceAdapter实现
 */
public class MethodInsertAdapter extends AdviceAdapter {
    private Type[] paramTypes;//方法参数的类型
    private String methodSignature;//方法的签名

    protected MethodInsertAdapter(int api, MethodVisitor mv, int access, String name, String signature, String desc, String className) {
        super(api, mv, access, name, desc);
        paramTypes = Type.getArgumentTypes(desc);
        this.methodSignature = MethodUtil.getMethodSignature(className, name, paramTypes);
    }

    @Override
    protected void onMethodEnter() {
        if (TextUtils.isEmpty(methodSignature)) {
            return;
        }
        mv.visitLdcInsn(methodSignature);//将方法的签名加载到操作数栈的栈顶
        int paramSize = paramTypes.length;
        if (paramSize == 0) {//没有参数
            mv.visitMethodInsn(INVOKESTATIC, "com/wangyuelin/performance/MethodCall", "onStart", "(Ljava/lang/String;)V", false);
        } else {//有参数
            //创建一个Object数组，将方法的参数放到数组中
            loadArgArray();
            //3.调用方法，将 Object数组
            mv.visitMethodInsn(INVOKESTATIC, "com/wangyuelin/performance/MethodCall", "onStart", "(Ljava/lang/String;[Ljava/lang/Object;)V", false);
        }

    }


    @Override
    protected void onMethodExit(int opcode) {
        if (TextUtils.isEmpty(methodSignature)) {
            return;
        }
        mv.visitLdcInsn(methodSignature);
        mv.visitMethodInsn(INVOKESTATIC, "com/wangyuelin/performance/MethodCall", "onEnd", "(Ljava/lang/String;)V", false);
    }

}
