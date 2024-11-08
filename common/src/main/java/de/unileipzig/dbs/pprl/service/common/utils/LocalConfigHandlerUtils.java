package de.unileipzig.dbs.pprl.service.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unileipzig.dbs.pprl.core.common.model.impl.PersonalAttributeType;
import de.unileipzig.dbs.pprl.service.common.data.dto.AttributeDescriptionDto;
import de.unileipzig.dbs.pprl.service.common.data.dto.RecordRequirementsDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LocalConfigHandlerUtils {

  private final static ObjectMapper om = new ObjectMapper();

  private final static Logger log = LoggerFactory.getLogger(LocalConfigHandlerUtils.class);

  public static <T> T parse(Path path, Class<T> tClass) throws IOException {
    return om.readValue(path.toFile(), tClass);
  }

  public static List<Path> scanForConfigs(String locationString) {
    Path path = new File(locationString).toPath().toAbsolutePath();
    log.info("Scanning for configs in: " + path);
    try {
      return LocalConfigHandlerUtils.getConfigPaths(path);
    } catch (IOException e) {
      log.error("Failed to read configs from " + path);
      return new ArrayList<>();
    }
  }

  public static List<Path> scanForLocallyAvailableConfigs() {
    final Resource resource = new ClassPathResource("configs/");
    try {
      Path folder = resource.getFile().toPath().toAbsolutePath();
      return LocalConfigHandlerUtils.getConfigPaths(folder);
    } catch (IOException e) {
      log.error("Failed to read configs from classpath");
      return new ArrayList<>();
    }
  }

  public static String readFileContent(Path path) {
    try {
      final BufferedReader reader =
        new BufferedReader(new InputStreamReader(Files.newInputStream(path)));
      return reader.lines().collect(Collectors.joining());
    } catch (IOException e) {
      throw new RuntimeException("Failed to read content from " + path);
    }
  }

  public static List<Path> getConfigPaths(Path folder) throws IOException {
    return Files.list(folder)
      .filter(Files::isDirectory)
      .collect(Collectors.toList());
  }

  public static void addDisplayNames(RecordRequirementsDto recordRequirementsDto) {
    for (AttributeDescriptionDto attribute : recordRequirementsDto.getAttributes()) {
      if (attribute.getDisplayName() == null || attribute.getDisplayName().isEmpty()) {
        try {
          PersonalAttributeType type = PersonalAttributeType.valueOf(attribute.getName());
          attribute.setDisplayName(PersonalAttributeType.getDisplayName(type));
        } catch (IllegalArgumentException e) {
          attribute.setDisplayName(attribute.getName());
        }
      }
    }
  }
}
