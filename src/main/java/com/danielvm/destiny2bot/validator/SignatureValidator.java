package com.danielvm.destiny2bot.validator;

import com.danielvm.destiny2bot.annotation.ValidSignature;
import com.danielvm.destiny2bot.config.DiscordConfiguration;
import com.danielvm.destiny2bot.util.CryptoUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.util.ContentCachingRequestWrapper;

public class SignatureValidator implements
    ConstraintValidator<ValidSignature, ContentCachingRequestWrapper> {

  private static final String SIGNATURE_HEADER_NAME = "X-Signature-Ed25519";
  private static final String TIMESTAMP_HEADER_NAME = "X-Signature-Timestamp";

  private final DiscordConfiguration discordConfiguration;

  public SignatureValidator(DiscordConfiguration discordConfiguration) {
    this.discordConfiguration = discordConfiguration;
  }

  @Override
  public boolean isValid(ContentCachingRequestWrapper value, ConstraintValidatorContext context) {
    String signature = value.getHeader(SIGNATURE_HEADER_NAME);
    String timestamp = value.getHeader(TIMESTAMP_HEADER_NAME);
    String botPublicKey = discordConfiguration.getBotPublicKey();
    byte[] bodyBytes = value.getContentAsByteArray();
    return CryptoUtil.validateSignature(bodyBytes, signature, botPublicKey, timestamp);
  }
}
