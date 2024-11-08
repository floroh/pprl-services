package de.unileipzig.dbs.pprl.service.dataowner.services;

import de.unileipzig.dbs.pprl.core.encoder.KeyManager;
import de.unileipzig.dbs.pprl.service.dataowner.config.KeyStoreConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Manages secrets that are needed for project-specific encodings
 */
@Service
@Slf4j
public class SecretManagerService {

  /**
   * [Project name] -> project-specific keystore
   */
  private final Map<String, KeyStore> keyStores;

  private final KeyStoreConfig config;

  public SecretManagerService(KeyStoreConfig config) {
    keyStores = new HashMap<>();
    this.config = config;
  }

  @PostConstruct
  private void loadKeyStore() {
    try {
      KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
      ks.load(new FileInputStream(config.getLocation()), config.getPassword().toCharArray());
      keyStores.put("exampleProject", ks);
      log.info("Added keystore for exampleProject with " + ks.size() + " keys");
    } catch (Exception e) {
      log.error("Failed to load default keystore: " + e.fillInStackTrace());
    }
  }

  public void addSecret(String project, SecretKey secret) {
    log.debug("Adding secret with alias " + KeyManager.BASE_KEY_NAME + " to project " + project);
    KeyStore keyStore = addProjectIfMissing(project);
    KeyManager
      .addSecret(keyStore, KeyManager.BASE_KEY_NAME, secret.getEncoded(), KeyManager.SECRET_PWD_ARRAY);
  }

  public void removeSecret(String project) {
    if (keyStores.containsKey(project)) {
      log.debug("Removing secret of project: " + project);
      keyStores.remove(project);
    } else {
      String message = "No secret to remove for project: " + project;
      log.debug(message);
      throw new RuntimeException(message);
    }
  }

  public Optional<KeyStore> getKeyStore(String project) {
    return Optional.ofNullable(keyStores.get(project));
  }

  public List<String> getProjects() {
    log.debug("Fetching available projects");
    return keyStores.keySet().stream().sorted().collect(Collectors.toList());
  }

  public static SecretKey toSecretKey(String secret) {
    return new SecretKeySpec(secret.getBytes(), "AES");
  }

  private KeyStore addProjectIfMissing(String project) {
    if (!keyStores.containsKey(project)) {
      log.debug("Creating new empty keystore for project: " + project);
      keyStores.put(project, KeyManager.initEmptyKeyStore());
    }
    return keyStores.get(project);
  }
}
