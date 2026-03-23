package com.jhzhao.alibaba.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;

/**
 * JWK (JSON Web Key) 配置
 * 管理 RSA 密钥对用于 JWT 签名
 */
@Configuration
@Slf4j
public class JwkConfig {

    private static final String KEY_DIRECTORY = "./keys";
    private static final String PRIVATE_KEY_FILE = KEY_DIRECTORY + "/jwt-private.key";
    private static final String PUBLIC_KEY_FILE = KEY_DIRECTORY + "/jwt-public.key";

    /**
     * JWK Source 用于 JWT 签名和验证
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        try {
            RSAKey rsaKey = loadOrGenerateRsaKey();
            JWKSet jwkSet = new JWKSet(rsaKey);
            return new ImmutableJWKSet<>(jwkSet);
        } catch (Exception e) {
            log.error("Failed to initialize JWK source", e);
            throw new RuntimeException("Failed to initialize JWK source", e);
        }
    }

    /**
     * 加载或生成 RSA 密钥对
     */
    private RSAKey loadOrGenerateRsaKey() throws Exception {
        File privateKeyFile = new File(PRIVATE_KEY_FILE);
        File publicKeyFile = new File(PUBLIC_KEY_FILE);

        if (privateKeyFile.exists() && publicKeyFile.exists()) {
            log.info("Loading existing RSA key pair from files");
            return loadRsaKeyFromFiles(privateKeyFile, publicKeyFile);
        } else {
            log.info("Generating new RSA key pair");
            return generateAndSaveRsaKey(privateKeyFile, publicKeyFile);
        }
    }

    /**
     * 从文件加载 RSA 密钥
     */
    private RSAKey loadRsaKeyFromFiles(File privateKeyFile, File publicKeyFile) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        // 加载私钥
        byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);

        // 加载公钥
        byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);

        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
    }

    /**
     * 生成并保存 RSA 密钥对
     */
    private RSAKey generateAndSaveRsaKey(File privateKeyFile, File publicKeyFile) throws Exception {
        // 创建目录
        File directory = new File(KEY_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 生成密钥对
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        // 保存私钥
        try (FileOutputStream fos = new FileOutputStream(privateKeyFile)) {
            fos.write(privateKey.getEncoded());
        }
        privateKeyFile.setReadable(true, true); // 仅所有者可读
        privateKeyFile.setWritable(true, true); // 仅所有者可写

        // 保存公钥
        try (FileOutputStream fos = new FileOutputStream(publicKeyFile)) {
            fos.write(publicKey.getEncoded());
        }

        log.info("RSA key pair generated and saved to {}", KEY_DIRECTORY);

        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
    }
}
