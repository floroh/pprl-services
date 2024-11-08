package de.unileipzig.dbs.pprl.service.dataowner.controller;

import de.unileipzig.dbs.pprl.service.common.data.dto.SecretDto;
import de.unileipzig.dbs.pprl.service.dataowner.services.EncoderService;
import de.unileipzig.dbs.pprl.service.dataowner.services.SecretManagerService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "secret", produces = MediaType.APPLICATION_JSON_VALUE)
@Timed
@Slf4j
@Tag(name = SecretController.TAG, description = "Manage project-specific secrets")
@CrossOrigin
public class SecretController {

  public static final String TAG = "Project secret management";

  private final SecretManagerService secretManagerService;

  private final EncoderService encoderService;

  public SecretController(SecretManagerService secretManagerService,
    EncoderService encoderService) {
    this.secretManagerService = secretManagerService;
    this.encoderService = encoderService;
  }

  @Operation(summary = "Add a secret for a project", tags = TAG)
  @PostMapping("/")
  public void add(@Valid @RequestBody SecretDto dto) {
    secretManagerService.addSecret(
      dto.getProject(),
      SecretManagerService.toSecretKey(dto.getSecret())
    );
    log.info("Added secret for project: " + dto.getProject());
  }

  @Operation(summary = "Override a secret of a project", tags = TAG)
  @PutMapping("/")
  public void update(@Valid @RequestBody SecretDto dto) {
    secretManagerService.addSecret(
      dto.getProject(),
      SecretManagerService.toSecretKey(dto.getSecret())
    );
    encoderService.removeProject(dto.getProject());
    log.info("Updated secret for project: " + dto.getProject());
  }

  @Operation(summary = "Remove a secret of a project", tags = TAG)
  @DeleteMapping("/")
  public void remove(@Valid @RequestBody String project) {
    project = project.replaceAll("^\"|\"$", "");
    secretManagerService.removeSecret(project);
    encoderService.removeProject(project);
  }

  @Operation(summary = "Get a list of all projects that are registered with a secret", tags = TAG)
  @GetMapping("/findAll")
  public List<String> getProjects() {
    return secretManagerService.getProjects();
  }
}