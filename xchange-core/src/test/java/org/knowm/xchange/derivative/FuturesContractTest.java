package org.knowm.xchange.derivative;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.junit.Test;
import org.knowm.xchange.utils.ObjectMapperHelper;

public class FuturesContractTest {

  @Test
  public void testSerializeDeserialize() throws IOException {
    FuturesContract contractExpire = new FuturesContract("XBT-USD-200925");
    FuturesContract jsonCopy = ObjectMapperHelper.viaJSON(contractExpire);
    assertThat(jsonCopy).isEqualTo(contractExpire);

    FuturesContract contractPerpetual = new FuturesContract("XBT-USD-perpetual");
    FuturesContract jsonCopy2 = ObjectMapperHelper.viaJSON(contractPerpetual);
    assertThat(jsonCopy2).isEqualTo(contractPerpetual);
  }
}
