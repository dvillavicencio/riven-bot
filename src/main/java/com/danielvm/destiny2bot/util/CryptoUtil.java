package com.danielvm.destiny2bot.util;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.web.util.ContentCachingRequestWrapper;
import software.pando.crypto.nacl.Crypto;

import static java.nio.charset.StandardCharsets.UTF_8;

public class CryptoUtil {

    private CryptoUtil() {

    }

    /**
     * Validates if the signature that comes from the interaction is valid
     *
     * @param request   The httpServletRequest
     * @param signature The passed signature from Discord
     * @param publicKey The public key of the bot
     * @param timestamp The passed timestamp from Discord
     * @return False if the signature is invalid, else True
     * @throws DecoderException If the {@link Crypto} library fails to verify the signature
     */
    public static boolean validateSignature(
            ContentCachingRequestWrapper request, String signature,
            String publicKey, String timestamp) throws DecoderException {
        String rawBody;
        byte[] bytes = request.getContentAsByteArray();
        rawBody = new String(bytes, UTF_8);
        return Crypto.signVerify(
                Crypto.signingPublicKey(Hex.decodeHex(publicKey.toCharArray())),
                (timestamp + rawBody).getBytes(UTF_8),
                Hex.decodeHex(signature.toCharArray()));
    }
}
