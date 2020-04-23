package com.wyl.noquickclick;

import com.wyl.noquickclick.visitor.ClickClassVisitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.BiConsumer;

import org.objectweb.asm.*;

/**
 * author : wangyuelin
 * time   : 2020/4/23 4:55 PM
 * desc   : 检测快速点击的Transform
 */
public class NoQuickClickTransform extends BaseTransform{
    @Override
    protected BiConsumer<InputStream, OutputStream> inject() {
        return new BiConsumer<InputStream, OutputStream>() {
            @Override
            public void accept(InputStream in, OutputStream out) {
                try {
                    ClassReader classReader = new ClassReader(in);
                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
                    ClickClassVisitor clickClassVisitor = new ClickClassVisitor(Opcodes.ASM5, classWriter);
                    classReader.accept(clickClassVisitor, ClassReader.EXPAND_FRAMES);
                    out.write(classWriter.toByteArray());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    @Override
    public String getName() {
        return "NoQuickClick";
    }
}
