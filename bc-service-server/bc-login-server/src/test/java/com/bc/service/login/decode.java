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
                ="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOm51bGwsInNjb3BlIjpbImFwcCJdLCJuYW1lIjpudWxsLCJ1dHlwZSI6bnVsbCwiaGVhZFVybCI6bnVsbCwiaWQiOm51bGwsImV4cCI6MTU2MzI0MzU4NCwiYXV0aG9yaXRpZXMiOlsiJ3Byb21vdGlvbk06Y2F0ZWdvcnlVbkJpbmRBY3Rpdml0eSIsImlwQWRkIiwicHJvbW90aW9uTTphY3Rpdml0eURlbCIsInByb21vdGlvbk06cmVSZWNvcmRBdWRpdCIsImdhbWVEZWxldGUiLCJwcm9tb3Rpb25NOnJlY29yZEF1ZGl0IiwicHJvbW90aW9uTTpleHBvcnRSZWNvcmRFeGNlbCIsImd0aW1lQWRkIiwicmVzY3VlOmFjdGl2aXR5QmluZEdhbWVBZGQiLCJnYW1lVXBkYXRlIiwicHJvbW90aW9uTTptZW51QWRkIiwiYWRkUm9ib3QiLCJhZGRBcHBseVRleHQiLCJwcm9tb3Rpb25NOmlwRGVsIiwicmVzY3VlOmFjdGl2aXR5QmluZEdhbWVEZWwiLCJnYW1lQWRkIiwicHJvbW90aW9uTTpjYXRlZ29yeURlbGV0ZSIsImRlbENoaWxkVXNlciIsImlwRGVsIiwicmVzY3VlOnVwZGF0ZUNhcmRIdG1sIiwicmVzY3VlOnVwZGF0ZUVsZWN0cm9uSHRtbCIsImd0aW1lRGVsZXRlIiwiYmF0Y2hSZWJhdGVQcmV2aW91c0RheSIsInJvYm90TG9naW4iLCJwcm9tb3Rpb25NOmFjdGl2aXR5VXBkYXRlIiwicmVzY3VlOnVwZGF0ZXN0YXR1cyIsImV4cG9ydEJhbGFuY2UiLCJyZWJhdGVUaW1lVXBkYXRlIiwicHJvbW90aW9uTTppcEFkZCIsInByb21vdGlvbk06bWVudVVwZGF0ZSIsInJlYmF0ZVRpbWVEZWxldGUiLCJwcm9tb3Rpb25NOmFjdGl2aXR5QWRkIiwiY2hhbmdlUGFzc3dvcmQiLCJwcm9tb3Rpb25NOmFjdGl2aXR5VXBkYXRlU3RhdHVzIiwiZXhwb3J0UGxheWVycyIsInJvYm90U3RhdHVzIiwicHJvbW90aW9uTTpjYXRlZ29yeUFkZCIsInVwZGF0ZVJvYm90IiwiYWRkQ2hpbGRVc2VyIiwicHJvbW90aW9uTTpjYXRlZ29yeUJpbmRBY3Rpdml0eSIsInByb21vdGlvbk06bWVudURlbCIsInVwZGF0ZUFwcGx5RGV0YWlsIiwicmViYXRlVGltZUFkZCJdLCJqdGkiOiI1ZjVkNzA3Mi0yMjM1LTQxODAtOTExNS1lYjYxNjdmYjAxOWYiLCJjbGllbnRfaWQiOiJCY1dlYkFwcCIsInVzZXJuYW1lIjoiaG9uZ2dlIn0.Vdp6RPNjW6wJ7vtAYdyIcPSwTAe9UmyRxYXXdnqH2E4Nke-rERy691jkAzg8llUc74eCHjocKAjzpk-9e1hw-ONCfm7q7EVDVlV-IhVkWRge6xz-P2rd3PVpfyzd_68qoZUArrZRDVJBqyVUzeSnDML_vRVztJvbQQ3oz-_zfK3IBZ-qZAbZTtJ2CzFIWUccJJAd92hXwdDTWci0oTS9bNP9RPUJNNa5lur32lqyoQMVP4Vs_BWHLL7Eh4JKuoaKLnBgrFTHc3i_Ove5OkY96jNxGrnCgUfV672Chz31J_YeMp_II9s1J-5-iqd2saif2CsPxU_V9qAGfaPqKgtZ7w";
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
