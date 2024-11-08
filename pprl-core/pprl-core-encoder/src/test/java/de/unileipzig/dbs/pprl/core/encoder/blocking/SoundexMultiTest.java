package de.unileipzig.dbs.pprl.core.encoder.blocking;

import de.unileipzig.dbs.pprl.core.common.model.api.BlockingKey;
import de.unileipzig.dbs.pprl.core.common.model.api.Record;
import de.unileipzig.dbs.pprl.core.encoder.TestBase;
import org.junit.jupiter.api.Test;

import java.util.Set;

class SoundexMultiTest extends TestBase {

  @Test
  void bothNames() {
    Record record = getPersonalRecord();
    SoundexMulti blocker = new SoundexMulti("sFNLN", "FIRSTNAME", "LASTNAME");
    Set<BlockingKey> bks = blocker.extract(record);
    for (BlockingKey bk : bks) {
      System.out.println(bk.getKey());
    }
  }

  @Test
  void nameAndDOB() {
    Record record = getPersonalRecord();
    SoundexMulti blocker = new SoundexMulti("sFNDOB", "FIRSTNAME", "DATEOFBIRTH");
    Set<BlockingKey> bks = blocker.extract(record);
    for (BlockingKey bk : bks) {
      System.out.println(bk.getKey());
    }
  }
}