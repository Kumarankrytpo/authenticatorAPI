/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.krypto.demoapi;

/**
 *
 * @author kumaran
 */
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;

public class TokenManager {
    private static final String SECRET = "K#R&Y^P@T%0";
    private static final String ISSUER = "auth0";
    private static final long ACCESS_TOKEN_EXPIRATION = 5 * 60 * 1000; // 5 minutes
//    private static final long REFRESH_TOKEN_EXPIRATION = 2 * 60 * 60 * 1000;// 2 hoursd
    private static final long REFRESH_TOKEN_EXPIRATION = 120 * 60 * 1000;

    private static final Algorithm algorithm = Algorithm.HMAC256(SECRET);

    public static String createAccessToken(String userId, String username) {
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(userId)
                .withClaim("username", username)
                .withExpiresAt(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .sign(algorithm);
    }

    public static String createRefreshToken(String userId) {
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(userId)
                .withExpiresAt(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .sign(algorithm);
    }

    public static DecodedJWT verifyToken(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build();
        return verifier.verify(token);
    }


}
