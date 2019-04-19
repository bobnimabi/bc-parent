import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;

import java.util.ArrayList;
import java.util.List;

public class CodeGenerator {
    public static void main(String[] args) {

        /*********************************************红包系统***********************************************/
        //项目路径
        String projectPath="F:\\javaCode\\bc-parent\\bc-service-common\\bc-redPacket-common"+"\\";
        //父package
        String parentName="com.bc.service.common";
        //模块名
        String modelName="redPacket";
        //生成表的名称
        String[] tables={
                "vs_robot",
                "vs_robot_record",
        };

        //配置数据库信息
        String url="jdbc:mysql://103.44.30.46:3306/red_packet?useUnicode=true&useSSL=false&characterEncoding=utf8";
        String username = "root";
        String password = "W_y1:478!";
        /*********************************************红包系统***********************************************/
        //项目路径
//        String projectPath="F:\\javaCode\\bc-parent\\bc-service-common\\bc-redPacket-common"+"\\";
//        //父package
//        String parentName="com.bc.service.common";
//        //模块名
//        String modelName="redPacket";
//        //生成表的名称
//        String[] tables={
////                "vs_award_active",
////                "vs_award_transform",
////                "vs_award_player",
////                "vs_award_prize",
////                "vs_configure",
////                "vs_log",
////                "vs_media",
//                "vs_pay_record",
////                "vs_site",
////                "vs_nav",
//        };
//
//        //配置数据库信息
//        String url="jdbc:mysql://103.44.30.46:3306/red_packet?useUnicode=true&useSSL=false&characterEncoding=utf8";
//        String username = "root";
//        String password = "W_y1:478!";
      /***********************************************认证和用户中心****************************************************
        //项目路径
        String projectPath="F:\\javaCode\\bc-parent\\bc-service-common\\bc-login-common"+"\\";
        //父package
        String parentName="com.bc.service.common";
        //模块名
        String modelName="login";
        //生成表的名称
        String[] tables={
//                "xc_auth",
//                "xc_role",
//                "xc_role_auth",
                "xc_user",
//                "xc_user_role",
        };

        //配置数据库信息
        String url="jdbc:mysql://103.44.30.46:3306/xc_user?useUnicode=true&useSSL=false&characterEncoding=utf8";
        String username = "root";
        String password = "W_y1:478!";*/

/***************************************以下配置不要动****************************************/
        // 代码生成器
        AutoGenerator mpg = new AutoGenerator();
        // 全局配置
        GlobalConfig gc = new GlobalConfig();
//        String projectPath = System.getProperty("user.dir");
        gc.setOutputDir(projectPath + "src/main/java");
        gc.setAuthor("admin");
        gc.setOpen(false);
        gc.setSwagger2(true); //实体属性 Swagger2 注解
        gc.setFileOverride(true);
        mpg.setGlobalConfig(gc);

        // 数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl(url);
        // dsc.setSchemaName("public");
        dsc.setDriverName("com.mysql.jdbc.Driver");
        dsc.setUsername(username);
        dsc.setPassword(password);
        mpg.setDataSource(dsc);

        // 包配置
        PackageConfig pc = new PackageConfig();
        pc.setModuleName(modelName);
        pc.setParent(parentName);
        mpg.setPackageInfo(pc);

        // 自定义配置
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                // to do nothing
            }
        };

        // 如果模板引擎是 velocity
         String templatePath = "/templates/mapper.xml.vm";

        // 自定义输出配置
        List<FileOutConfig> focList = new ArrayList<>();
        // 自定义配置会被优先输出
        focList.add(new FileOutConfig(templatePath) {
            @Override
            public String outputFile(TableInfo tableInfo) {
                // 自定义输出文件名 ， 如果你 Entity 设置了前后缀、此处注意 xml 的名称会跟着发生变化！！
                return projectPath + "/src/main/resources/mapper/" + pc.getModuleName()
                        + "/" + tableInfo.getEntityName() + "Mapper" + StringPool.DOT_XML;
            }
        });

        cfg.setFileOutConfigList(focList);
        mpg.setCfg(cfg);

        // 配置模板
        TemplateConfig templateConfig = new TemplateConfig();

        // 配置自定义输出模板
        //指定自定义模板路径，注意不要带上.ftl/.vm, 会根据使用的模板引擎自动识别
        // templateConfig.setEntity("templates/entity2.java");
        // templateConfig.setService();
        // templateConfig.setController();

        templateConfig.setXml(null);
        mpg.setTemplate(templateConfig);

        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setNaming(NamingStrategy.underline_to_camel);
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
//        strategy.setSuperEntityClass("com.baomidou.ant.common.BaseEntity");
        strategy.setEntityLombokModel(true);
        strategy.setRestControllerStyle(true);
//        strategy.setSuperControllerClass("com.baomidou.ant.common.BaseController");
        strategy.setInclude(tables);
//        strategy.setSuperEntityColumns("id");
        strategy.setControllerMappingHyphenStyle(true);
        strategy.setTablePrefix(pc.getModuleName() + "_");
        mpg.setStrategy(strategy);
        mpg.setTemplateEngine(new VelocityTemplateEngine());
        mpg.execute();
    }
}