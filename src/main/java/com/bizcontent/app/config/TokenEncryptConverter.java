package com.bizcontent.app.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * DB에 저장되는 민감한 값(네이버 OAuth 토큰 등)을 자동으로 암복호화하는 JPA 컨버터.
 * - AES-256-GCM 사용 (JDK 기본 내장, 외부 라이브러리/비용 없음)
 * - 암호화 키는 절대 코드/DB에 저장하지 않고 환경변수(TOKEN_ENCRYPT_KEY)에서만 읽음
 * - 필드에 @Convert(converter = TokenEncryptConverter.class) 만 붙이면 자동 적용됨
 *
 * 환경변수 키 생성 방법 (터미널에서 한 번 실행해서 나온 값을 TOKEN_ENCRYPT_KEY로 등록):
 *   openssl rand -base64 32
 */
@Converter
public class TokenEncryptConverter implements AttributeConverter<String, String> {

    private static final String ALGO = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    @Override
    public String convertToDatabaseColumn(String plainText) {
        if (plainText == null || plainText.isBlank()) return null;
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey(), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // IV + 암호문을 합쳐서 하나의 Base64 문자열로 저장 (복호화 때 IV를 다시 꺼내 씀)
            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new IllegalStateException("토큰 암호화 실패", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbValue) {
        if (dbValue == null || dbValue.isBlank()) return null;
        try {
            byte[] combined = Base64.getDecoder().decode(dbValue);

            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] cipherText = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            System.arraycopy(combined, iv.length, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.DECRYPT_MODE, secretKey(), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] plainBytes = cipher.doFinal(cipherText);

            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("토큰 복호화 실패", e);
        }
    }

    private SecretKeySpec secretKey() {
        String base64Key = System.getenv("TOKEN_ENCRYPT_KEY");
        if (base64Key == null || base64Key.isBlank()) {
            throw new IllegalStateException(
                "TOKEN_ENCRYPT_KEY 환경변수가 설정되지 않았습니다. " +
                "터미널에서 'openssl rand -base64 32' 실행 후 나온 값을 환경변수로 등록하세요."
            );
        }
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        return new SecretKeySpec(keyBytes, "AES");
    }
}
