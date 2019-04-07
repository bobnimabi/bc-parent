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
                ="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOm51bGwsInVzZXJfbmFtZSI6ImJhdGNqMSIsInNjb3BlIjpbImFwcCJdLCJuYW1lIjoi6LaF57qn566h55CG5ZGYIiwidXR5cGUiOjAsImhlYWRVcmwiOm51bGwsImlkIjoxLCJleHAiOjE1NTQ1ODI3NzAsImF1dGhvcml0aWVzIjpbInJlZHVjZV9zYWxhcnkiLCJxdWVyeV9zYWxhcnkiXSwianRpIjoiNWIwOTY5MWUtNjllZS00YWRlLThlMWMtZjg2MDY3ZWZhN2ExIiwiY2xpZW50X2lkIjoiQmNXZWJBcHAifQ.Vq3jP71vxci8nOp3x4iyYB4vqpQMkzA5UiAgtUmW9Azs7jluQ6oOWn-iOY-wUoPZW-3gnOvxO7WkB8zZQjhT_scc7cornt8-Zf5q6TP1Z-Om85pr1_ylE-zO4H1V0Gql_2GPBDN1Sg9C2vJXqIsH90IyB5ydlsZ12f7DhhLxFwrPltqlYxPogcqs6owrAsQeIhn_HiGmaAzBXytFv22tCfcqWdMfbn5ds0Lx0D6sHi4qc5t7nZju4KjY5JMkqDR0d_BjEFIaaX0SzCxJrSBI9UcsFLpGu0z3r-GMVYHKdggNhlRByH7It_FsK1a1JR4KF9hjtkQGVKQ0euzrDFOTUA";
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
