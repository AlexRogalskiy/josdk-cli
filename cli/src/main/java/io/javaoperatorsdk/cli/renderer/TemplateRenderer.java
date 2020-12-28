package io.javaoperatorsdk.cli.renderer;

import com.samskivert.mustache.Mustache;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.List;
import java.util.Map;
import io.javaoperatorsdk.quarkus.resources.ResourceURLLocator;

public class TemplateRenderer {

  Mustache.Compiler mustacheCompiler = Mustache.compiler();
  private ResourceURLLocator resourceURLLocator;

  public TemplateRenderer(ResourceURLLocator resourceURLLocator) {
    this.resourceURLLocator = resourceURLLocator;
  }

  public void render(String templatePath, Map<String, String> data, String outDirectory) {
    List<URL> urls = resourceURLLocator.contains(templatePath);
    try {
      for (URL u : urls) {
        boolean isDirectory = u.getPath().endsWith(File.separator);
        System.out.println(String.format("is %s a dir? %s", u.getPath(), isDirectory));
        if (isDirectory) {
          continue;
        }
        final var relativePath = getRelativePath(templatePath, u);
        System.out.println(relativePath);
        final var template = mustacheCompiler.compile(new InputStreamReader(u.openStream()));
        System.out.println(template);
        writeFile(data, relativePath, template, outDirectory);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void writeFile(
      Map<String, String> data,
      String relativePath,
      com.samskivert.mustache.Template template,
      String outDirectory)
      throws IOException {
    System.out.println(outDirectory + File.separator + relativePath);
    final var file = new File(outDirectory + File.separator + relativePath);
    file.getParentFile().mkdirs();
    try (final var outputStream = new OutputStreamWriter(new FileOutputStream(file))) {
      template.execute(data, outputStream);
      outputStream.flush();
    }
  }

  private String getRelativePath(String templatePath, URL url) {
    final var startIndex = url.getPath().indexOf(templatePath);
    return url.getPath().substring(startIndex + templatePath.length() + 1);
  }
}
