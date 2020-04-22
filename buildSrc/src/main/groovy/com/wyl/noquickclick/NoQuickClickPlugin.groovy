package com.wyl.noquickclick


import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 绝对不能使用依赖的方式依赖插件，会报错，插件和Library是不一样的
 */

class NoQuickClickPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
//        init(project)

        //读取自定义配置
//         project.extensions.create("aop", AopExtension)
//        LogUtil.e("这里配置时期就执行了")
//
//        project.task("taskTest", {
//            LogUtil.e("这里配置阶段就执行" + config.isAop)
//            doFirst {
//                LogUtil.e("这里执行阶段才执行" + config.isAop)
//            }
//        })
//        project.afterEvaluate {
//            LogUtil.e("这里配置完才执行")
//        }
//


//        project.afterEvaluate {
//            LogUtil.e("开始输出属性" + config.isAop)
//            LogUtil.e("isAop:" + config.isAop)
//        }
//        LogUtil.e("开始添加切面代码")
//        AopConfig config = getConfig(project)
//        project.android.registerTransform(new PreClass(config, project))

        project.task('AopLogPlugin', {
            println ('========================')
            println ('AopLogPlugin')
            println ('========================')
        })

    }

    private void init(Project project) {
        LogUtil.init(project, true)

    }

    private AopConfig getConfig(Project project) {
        if (project == null) {
            return
        }
        ArrayList<String> needPackages = new ArrayList<>()
        needPackages.add("com.talk51")

        AopConfig aopConfig = new AopConfig()
        aopConfig.isDebug = true
        aopConfig.isAop = true;
        aopConfig.needPackages = needPackages
        return aopConfig

    }

}
