package org.apache.geode.spring.module;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.ResourceLoader;

import org.apache.geode.module.service.ModuleService;
import org.apache.geode.service.SampleService;
import org.apache.geode.spring.module.config.DemoAppConfig;

@SpringBootApplication(scanBasePackageClasses = DemoAppConfig.class)
public class DemoApplication implements SampleService {

  private ConfigurableApplicationContext applicationContext;
  private ModuleService moduleService;
  private ClassLoader classloader;

  @Override
  public String getValue() {
    return null;
  }

  @Override
  public void init(Object... initObjects) {
    moduleService = (ModuleService) initObjects[0];
    classloader = (ClassLoader) initObjects[1];
    ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(classloader);
      applicationContext = new SpringApplicationBuilder(DemoApplication.class).web(
          WebApplicationType.SERVLET).build().run();
    } finally {
      Thread.currentThread().setContextClassLoader(previousClassLoader);
    }
  }

  @Override
  public void shutDown() {
    applicationContext.close();
  }

  private class DemoSpringApplicationRunnerBuilder extends SpringApplicationBuilder {
    private final ClassLoader classLoader;

    public DemoSpringApplicationRunnerBuilder(ClassLoader classLoader, Class<?>... sources) {
      super(sources);
      this.classLoader = classLoader;
    }

    @Override
    protected SpringApplication createSpringApplication(Class<?>... sources) {
      return new DemoSpringApplication(classLoader, sources);
    }
  }

  private class DemoSpringApplication extends SpringApplication {
    private ClassLoader classLoader;

    public DemoSpringApplication(ClassLoader classLoader, Class<?>... primarySources) {
      super(primarySources);
      this.classLoader = classLoader;
    }

    public DemoSpringApplication(ResourceLoader resourceLoader,
                                 Class<?>... primarySources) {
      super(resourceLoader, primarySources);
    }

    @Override
    public ClassLoader getClassLoader() {
      return this.classLoader;
    }

    @Override
    protected ConfigurableApplicationContext createApplicationContext() {
      AbstractApplicationContext applicationContext =
          (AbstractApplicationContext) super.createApplicationContext();
      applicationContext.setClassLoader(this.classLoader);
      return applicationContext;
    }
  }
}