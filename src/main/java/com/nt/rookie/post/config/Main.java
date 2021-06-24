package com.nt.rookie.post.config;

import java.io.File;

import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import com.nt.rookie.post.data.DatabaseBootstrap;

public class Main {

    public static void main(String[] args) throws Exception {

        String webappDirLocation = "src/main/webapp/";
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8081);

        StandardContext ctx = (StandardContext) tomcat.addWebapp("/", new File(webappDirLocation).getAbsolutePath());

        File additionWebInfClasses = new File("target/classes");
        WebResourceRoot resources = new StandardRoot(ctx);
        resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
                                    additionWebInfClasses.getAbsolutePath(), "/"));
        ctx.setResources(resources);

        initDB();

        tomcat.start();
        tomcat.getServer().await();
    }

    private static void initDB() {
        DatabaseBootstrap bootstrap = new DatabaseBootstrap();
        bootstrap.initializeDatabase();
    }
}