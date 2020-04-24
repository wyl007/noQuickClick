package com.wyl.noquickclick;

import com.wyl.noquickclick.base.BaseTransform;
import com.wyl.noquickclick.bean.NoQuickClickConfig;
import com.wyl.noquickclick.visitor.ClickClassVisitor;

import org.gradle.api.Project;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.BiConsumer;

/**
 * author : wangyuelin
 * time   : 2020/4/23 4:55 PM
 * desc   : 检测快速点击的Transform
 */
public class NoQuickClickTransform extends BaseTransform {

    public NoQuickClickTransform(Project project) {
        super(project);
    }

    @Override
    protected BiConsumer<InputStream, OutputStream> inject() {
        return new BiConsumer<InputStream, OutputStream>() {
            @Override
            public void accept(InputStream in, OutputStream out) {
                try {
                    ClassReader classReader = new ClassReader(in);
                    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
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

    @Override
    protected boolean classFilter(String classPath) {
        boolean isFilter = true;
        if (classPath == null) {
            return isFilter;
        }

        String classFullName = classPath.replaceAll("/", ".");
        //确保是class文件
        if(!classFullName.endsWith(".class")) {
            return isFilter;
        }
        Object obj = project.getExtensions().getByName("noQuickClickConfig");

        if(obj instanceof NoQuickClickConfig) {
            NoQuickClickConfig config = (NoQuickClickConfig) obj;
            //检测包匹配
            if(config.includePackages != null) {
                for (String includePackage : config.includePackages) {
                    if(classFullName.contains(includePackage)) {
                        isFilter = false;
                        break;
                    }
                }
            }
            //检测类匹配
            if(config.filterClass != null) {
                for (String filterClass : config.filterClass) {
                    if(classFullName.contains(filterClass)) {
                        isFilter = true;
                        break;
                    }
                }
            }

        }

        return isFilter;
    }
}
