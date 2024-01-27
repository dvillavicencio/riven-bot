package com.danielvm.destiny2bot.util;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.danielvm.destiny2bot.exception.InvalidSignatureException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import software.pando.crypto.nacl.Crypto;

public class CryptoUtil {

  private CryptoUtil() {

  }

  /**
   * Validates if the signature that comes from the interaction is valid
   *
   * @param signature The passed signature from Discord
   * @param publicKey The public key of the bot
   * @param timestamp The passed timestamp from Discord
   * @return False if the signature is invalid, else True
   */
  public static boolean validateSignature(
      byte[] bytes, String signature,
      String publicKey, String timestamp) {
    String rawBody = new String(bytes, UTF_8);
    try {
      return Crypto.signVerify(
          Crypto.signingPublicKey(Hex.decodeHex(publicKey.toCharArray())),
          (timestamp + rawBody).getBytes(UTF_8),
          Hex.decodeHex(signature.toCharArray()));
    } catch (DecoderException de) {
      throw new InvalidSignatureException(
          "The passed in signature was invalid. Signature [%s], Timestamp [%s], Message [%s]"
              .formatted(signature, timestamp, rawBody), de);
    }
  }
}
