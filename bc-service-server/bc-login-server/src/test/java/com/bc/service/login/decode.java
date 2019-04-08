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
                ="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1NTQ2NzExMzEsImF1dGhvcml0aWVzIjpbInJlZHVjZV9zYWxhcnkiLCJxdWVyeV9zYWxhcnkiXSwianRpIjoiODRjNjkwNzgtYjhmMS00NzFhLTk4NmQtN2VmMTQyNDZiNDhkIiwiY2xpZW50X2lkIjoiQmNXZWJBcHAiLCJ1c2VybmFtZSI6ImJhdGNqMSIsInNjb3BlIjpbImFwcCJdfQ.l5-4Lav7tSs7vGUHUnDHy2zy6nrDL7bh8v3t5oVAAavuJ580XDNpsGJAS2K8X0-MzBk8fYX8vtJdMEtEtUNffwTYN4wV9HBseJWcS5aJqD0mWOv00nRl_XgYwxzIAOd_0koS3gOB_PzU_gWQ0EiMTly77vp14D_trz7NJxewOADlpLxqRcAMbsJqK8tUskptAp-okuAkGResxQyYYktTb_t3oMNySLA8v8cVrTWiw1iJz7Xf8CDz6nUZ8c_P-k0hhcSAGm_n1YwK1VIxPIg7M0wUFWYPNHXv27inBx5AXP3LiI_rt8KwgcHjNPE0DvkOMVAPntALmAlL7OW6Zq0PEA";
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
