package de.unileipzig.dbs.pprl.service.protocol.utils;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.FileFilterUtils;

import java.io.File;
import java.io.FileFilter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {

  private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-kkmmss");

  //TODO Add filter by filename (extension)
  public static File findNewestFileInDirectory(String directory) {
    File dir = new File(directory);
    if (dir.isDirectory()) {
      File[] dirFiles = dir.listFiles((FileFilter) FileFilterUtils.fileFileFilter());
      if (dirFiles != null && dirFiles.length > 0) {
        Arrays.sort(dirFiles, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
        return dirFiles[0];
      }
    }
    return null;
  }

  public static String getCurrentTimeStamp() {
    return formatter.format(LocalDateTime.now());
  }

  public static <T> List<T> combineList(List<T> list, T... elements) {
    List<T> extendableList = new ArrayList<>(list);
    extendableList.addAll(Arrays.asList(elements));
    return extendableList;
  }
}
