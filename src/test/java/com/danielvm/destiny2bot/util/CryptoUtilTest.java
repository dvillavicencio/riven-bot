package com.danielvm.destiny2bot.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.pando.crypto.nacl.Crypto;

public class CryptoUtilTest {

  static final String SEED = "F3AA9759D829FAEBED453AAD8D4EB646AC3721C3D33BE7B71530BE62FB040705";
  static final String MALICIOUS_SEED = "FB937F153FAFC87D0B9B40AF0ECFBCF7DCBE359C0E9FF10C6C6EC234375ADF2A";

  @Test
  @DisplayName("validateSignature is successful for valid signatures")
  public void validateSignatureIsSuccessfulForValidSignatures() throws DecoderException {
    // given: a valid signature and message
    var message = "This is some random message that should be encrypted";
    var timeStamp = "1000";

    byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
    KeyPair keyPair = Crypto.seedSigningKeyPair(Hex.decodeHex(SEED.toCharArray()));
    byte[] signatureBytes =
        Crypto.sign(keyPair.getPrivate(), (timeStamp + message).getBytes(StandardCharsets.UTF_8));
    String signature = Hex.encodeHexString(signatureBytes);
    String publicKey = Hex.encodeHexString(keyPair.getPublic().getEncoded());

    // when: validateSignature is called (ignore request for now)
    boolean response = CryptoUtils.validateSignature(messageBytes, signature, publicKey, timeStamp);

    // then: no exception is thrown
    assertThat(response).isTrue();
  }

  @Test
  @DisplayName("validateSignature is successful for invalid signatures")
  public void validateSignatureIsSuccessfulForInvalidSignatures() throws DecoderException {
    // given: a valid signature and the message
    var message = "This is some random message that should be encrypted";
    var timestamp = "1000";

    byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
    KeyPair nonMaliciousKeyPair = Crypto.seedSigningKeyPair(Hex.decodeHex(SEED.toCharArray()));

    KeyPair maliciousKeyPair = Crypto.seedSigningKeyPair(
        Hex.decodeHex(MALICIOUS_SEED.toCharArray()));
    byte[] maliciousSignature = Crypto.sign(maliciousKeyPair.getPrivate(),
        (timestamp + message).getBytes(StandardCharsets.UTF_8));

    String signature = Hex.encodeHexString(maliciousSignature);
    String publicKey = Hex.encodeHexString(nonMaliciousKeyPair.getPublic().getEncoded());

    // when: validateSignature is called (ignore request for now)
    boolean response = CryptoUtils.validateSignature(messageBytes, signature, publicKey, null);

    // then: the response is false
    assertThat(response).isFalse();
  }

}
