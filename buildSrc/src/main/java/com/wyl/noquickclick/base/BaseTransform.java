package com.wyl.noquickclick.base;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Status;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.android.utils.FileUtils;
import com.google.common.io.Files;

import org.apache.commons.io.IOUtils;
import org.gradle.api.Project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * author : wangyuelin
 * time   : 2020/4/22 5:49 PM
 * desc   : 封装了通用的逻辑，可以通过集成定制，主要逻辑：多输入的jar和和输入class目录的处理流程
 */
public abstract class BaseTransform extends Transform {

    protected Project project;

    public BaseTransform(Project project) {
        this.project = project;
    }

    @Override
    public String getName() {
        return "BaseTransform";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        //编译后的字节码文件，可能是jar里面的也可能是文件夹里面的
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        //所有的module+第三方库
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        //支持增量编译
        return true;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation);
        long startTime = System.currentTimeMillis();
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();
        //不支持增量编译，将之前的输出产物，全部删除，避免出现错乱
        if (!transformInvocation.isIncremental()) {
            outputProvider.deleteAll();
        }
        //遍历输入，然后处理，最后保存处理结果
        for (TransformInput input : transformInvocation.getInputs()) {
            //处理jar包里面的class
            for (JarInput jarInput : input.getJarInputs()) {
//                System.out.println("开始处理Jar文件：" + jarInput.getFile().getAbsolutePath());
                handleJarInput(jarInput, transformInvocation);
            }
            //处理文件夹下的class
            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
//                System.out.println("开始处理目录：" + directoryInput.getFile().getAbsolutePath());
                handleDirectoryInput(directoryInput, transformInvocation);
            }

        }

        System.out.println("处理耗时：" + (System.currentTimeMillis() - startTime));
    }

    /**
     * 处理Directory类型的输入
     *
     * @param directoryInput
     * @param transformInvocation
     */
    private void handleDirectoryInput(DirectoryInput directoryInput, TransformInvocation transformInvocation) {
        if (directoryInput == null || transformInvocation == null) {
            return;
        }
        File inputDir = directoryInput.getFile();
        //查询对应的输入位置
        File outputDir = transformInvocation.getOutputProvider().getContentLocation(directoryInput.getName(), directoryInput.getContentTypes(), directoryInput.getScopes(), Format.DIRECTORY);
        if (transformInvocation.isIncremental()) {
            //增量方式处理
//            System.out.println("增量处理");
            directoryInput.getChangedFiles().forEach(new BiConsumer<File, Status>() {
                @Override
                public void accept(File inputFile, Status status) {
                    File out = toOutputFile(outputDir, inputDir, inputFile);
                    switch (status) {
                        case NOTCHANGED:
//                            System.out.println("文件状态 ：NOTCHANGED");
                            break;
                        case CHANGED:
//                            System.out.println("文件状态 ：CHANGED");
                        case ADDED:
//                            System.out.println("文件状态 ：ADDED");
                            if (!inputFile.isDirectory()) {
//                                System.out.println("输入：" + inputFile.getAbsolutePath());
//                                System.out.println("输出：" + out.getAbsolutePath());
                                if(classFilter(inputFile.getAbsolutePath())) {
                                    copyFile(inputFile, out);
                                } else {
                                    transformFile(inputFile, out, inject());
                                }
                            }
                            break;
                        case REMOVED:
//                            System.out.println("文件状态 ：REMOVED");
                            try {
                                FileUtils.deleteIfExists(out);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                    }

                }
            });
        } else {
//            System.out.println("非增量处理");
            for (File in : FileUtils.getAllFiles(inputDir)) {
//                System.out.println("outputDir:" + outputDir.getAbsolutePath());
//                System.out.println("输入：" + in.getAbsolutePath());
                File out = toOutputFile(outputDir, inputDir, in);
                if (!classFilter(in.getAbsolutePath())) {
//                    System.out.println("不过滤");

                    transformFile(in, out, inject());
                } else {
//                    System.out.println("过滤");
                    copyFile(in, out);
                }
//                System.out.println("输出：" + out.getAbsolutePath());
            }
        }
    }

    private void copyFile(File in, File out) {
        try {
            //将源文件直接拷贝到输出文件
            out.mkdirs();
            if (!out.exists()) {
                if (out.createNewFile()) {
                    //文件创建失败
                    System.out.println("文件创建失败");
                    return;
                }
            }
            FileUtils.copyFile(in, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 对输入的class文件处理
     *
     * @param inputFile
     * @param outputFile
     * @param inject
     */
    private void transformFile(File inputFile, File outputFile, BiConsumer<InputStream, OutputStream> inject) {
        if (inputFile == null || outputFile == null || inject == null) {
            return;
        }
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            Files.createParentDirs(outputFile);
            fis = new FileInputStream(inputFile);
            fos = new FileOutputStream(outputFile);
            inject.accept(fis, fos);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    /**
     * 据输入的文件相应的输出文件
     *
     * @param outputDir
     * @param inputDir
     * @param inputFile
     * @return
     */
    private File toOutputFile(File outputDir, File inputDir, File inputFile) {
        return new File(
                outputDir,
                FileUtils.relativePossiblyNonExistingPath(inputFile, inputDir)
        );
    }

    /**
     * 处理jar类型的输入
     *
     * @param jarInput
     * @param transformInvocation
     */
    private void handleJarInput(JarInput jarInput, TransformInvocation transformInvocation) {
        if (jarInput == null || transformInvocation == null) {
            return;
        }
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();
        File jarInputFile = jarInput.getFile();
        //查询得到输入对应的输出路径
        File jarOutPutFile = outputProvider.getContentLocation(jarInput.getName(), jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR);
        jarOutPutFile.deleteOnExit();
        try {
            jarOutPutFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.out.println("输入：" + jarInputFile.getAbsolutePath());
//        System.out.println("输出：" + jarOutPutFile.getAbsolutePath());
        if (transformInvocation.isIncremental()) {
            //增量处理jar包
            System.out.println("增量处理");
            switch (jarInput.getStatus()) {
                case ADDED:
//                    System.out.println("文件状态：ADDED");
                case CHANGED:
//                    System.out.println("文件状态：CHANGED");
                    //新增或者修改的jar需要处理
                    transformJar(jarInputFile, jarOutPutFile, inject());
                    break;
                case REMOVED:
//                    System.out.println("文件状态：REMOVED");
                    //删除输出jar文件
                    try {
                        FileUtils.delete(jarOutPutFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case NOTCHANGED:
                    //没有改变，不需要处理
//                    System.out.println("文件状态：NOTCHANGED");
                    break;
            }
        } else {
//            System.out.println("非增量处理");
            //不是增量编译，直接对处理
            transformJar(jarInputFile, jarOutPutFile, inject());
        }
    }

    /**
     * 对输入的jar文件处理，然后保存到输出jar文件
     *
     * @param inputJarFile
     * @param outputJarFile
     * @param inject
     */
    private void transformJar(File inputJarFile, File outputJarFile, BiConsumer<InputStream, OutputStream> inject) {
        if (inputJarFile == null || outputJarFile == null || inject == null) {
            return;
        }
        //确保输出目录存在
        outputJarFile.mkdirs();
        ZipInputStream zis = null;
        ZipOutputStream zos = null;
        try {
            zis = new ZipInputStream(new FileInputStream(inputJarFile));
            zos = new ZipOutputStream(new FileOutputStream(outputJarFile));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null && isValidZipEntryName(zipEntry)) {
                if (!zipEntry.isDirectory()) {
                    zos.putNextEntry(new ZipEntry(zipEntry.getName()));
                    if (classFilter(zipEntry.getName())) {
//                        System.out.println("过滤掉的的jar中的文件：" + zipEntry.getName());
                        //过滤掉的文件，原封不动写入新的jar
                        zos.write(IOUtils.toByteArray(zis));
                    } else {
//                        System.out.println("处理的jar中的类：" + zipEntry.getName());
                        //不过滤，需要处理的文件
                        inject.accept(zis, zos);
                    }


                }
                zipEntry = zis.getNextEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (zis != null) {
                try {
                    zis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (zos != null) {
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 验证名称是否有效
     *
     * @param zipEntry
     * @return
     */
    private boolean isValidZipEntryName(ZipEntry zipEntry) {
        return !zipEntry.getName().contains("../");
    }

    /**
     * class过滤，子类覆写实现自己的过滤
     *
     * @param classPath
     * @return true：表示需要过滤；false：表示不需要过滤
     */
    protected boolean classFilter(String classPath) {
        //默认类全部过滤掉
        return true;
    }

    /**
     * 子类覆写，实现代码的注入逻辑
     *
     * @return
     */
    protected abstract BiConsumer<InputStream, OutputStream> inject();


}
