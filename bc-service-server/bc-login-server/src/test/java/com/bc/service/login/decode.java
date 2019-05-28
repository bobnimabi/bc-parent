package com.bc.service.login;

import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;

/**
 * Created by mrt on 2019/4/6 0006 下午 4:52
 */
public class decode {
    public static void main(String[] args) {
        //jwt令牌
        String token
                ="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOm51bGwsInNjb3BlIjpbImFwcCJdLCJuYW1lIjpudWxsLCJ1dHlwZSI6bnVsbCwiaGVhZFVybCI6bnVsbCwiaWQiOm51bGwsImV4cCI6MTU1ODkwNzUxNSwiYXV0aG9yaXRpZXMiOlsiaXBBZGQiLCJndGltZURlbGV0ZSIsImJhdGNoUmViYXRlUHJldmlvdXNEYXkiLCJyb2JvdExvZ2luIiwiZ2FtZURlbGV0ZSIsImV4cG9ydEJhbGFuY2UiLCJyZWJhdGVUaW1lVXBkYXRlIiwiZ3RpbWVBZGQiLCJyZWJhdGVUaW1lRGVsZXRlIiwiZ2FtZVVwZGF0ZSIsImNoYW5nZVBhc3N3b3JkIiwiZXhwb3J0UGxheWVycyIsImFkZFJvYm90IiwiYWRkQXBwbHlUZXh0Iiwicm9ib3RTdGF0dXMiLCJnYW1lQWRkIiwidXBkYXRlUm9ib3QiLCJhZGRDaGlsZFVzZXIiLCJkZWxDaGlsZFVzZXIiLCJ1cGRhdGVBcHBseURldGFpbCIsInJlYmF0ZVRpbWVBZGQiLCJpcERlbCJdLCJqdGkiOiJkY2Y5YWU1Ny02ZTNkLTRkZjctOWM1My0xOTQ4MmJiNTAyMjIiLCJjbGllbnRfaWQiOiJCY1dlYkFwcCIsInVzZXJuYW1lIjoidGVzdDEifQ.kv2j8xQ1pavFU5CR0dOf4ZHptxY8s4fwJCW4D8hPZ5ekQjPUFwY9UZVS7de8yk_H3vhrjwRCYpGavlChN0-iLi0l2JB2zZ3TtChl2JjmKWSVp9iDrI1taEOeK0uFUwXwglQSvSJRgXEBpyt1ptjnyZzYAOSwMty7WCzQZw6v6XbK1-4l8SEMwMwHtktqDnsG_MN1gZmzAbLV1YY30TGXigjBsRZd54ispq_nJ_H8cPQMPbyBcZL5lvasnVnggt65_hRUUj4F1avSgN5i3wLR0AfuRnz374yyfohCh8hfzcq5cA_ZxkQfG5aFJI6yUmOjlPcIAMKb4O8NmE7-kDU5RQ";
        //公钥
        String publickey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwdlBspVxTzCp+ogA7Y5K02ZN0L5wQNOse8uKUvH/hBEPVSe3Kkw8eWlIECnJfzpsto5KLHmTgWsGmeaWsADP6o7X/esjP9Z6REa/+Rm3qNfBvrTguTejPGKg37MR9YYz52VOlKvIV2fll5RlnlQWN0dItf2rU0p93ZA7Zdp7MZ0lcGNmViijtuDqd7mxoCa2jL8j46GQEMyUrlfcSQcfHsXlCMQ/Y+4HV1CvV2KcFIn7vVMUvOiWarAb16mwFK7IOGQEN3ZtYuyzUm1SiOhSij3UVvSa/6qOCrMD+psUglOL7VsFa1qHkT33Mlgmqi8iPWYcjESw6aEn1hgbRggBgQIDAQAB-----END PUBLIC KEY-----";
        //校验jwt
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(publickey));
        //获取jwt原始内容
        String claims = jwt.getClaims();
        System.out.println(claims);
        //jwt令牌
        String encoded = jwt.getEncoded();
        System.out.println(encoded);
    }

}
