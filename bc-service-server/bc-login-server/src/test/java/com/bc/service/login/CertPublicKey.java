package com.bc.service.login;

import junit.framework.TestCase;
import sun.misc.BASE64Encoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CertPublicKey extends TestCase{
	
	
	/***
	 * 读取*.cer公钥证书文件， 获取公钥证书信息
	 * @author xgh
	 */
	public void testReadX509CerFile() throws Exception{
		try {
			// 读取证书文件
			File file = new File("F:\\javacodeTemp\\MyStudy\\Mybaits-OptimisticLock\\src\\main\\resources\\publickey.cer");
			InputStream inStream = new FileInputStream(file);
			// 创建X509工厂类
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			//CertificateFactory cf = CertificateFactory.getInstance("X509");
			// 创建证书对象
			X509Certificate oCert = (X509Certificate) cf
					.generateCertificate(inStream);
			inStream.close();
			SimpleDateFormat dateformat = new SimpleDateFormat("yyyy/MM/dd");
			String info = null;
			// 获得证书版本
			info = String.valueOf(oCert.getVersion());
			System.out.println("证书版本:" + info);
			// 获得证书序列号
			info = oCert.getSerialNumber().toString(16);
			System.out.println("证书序列号:" + info);
			// 获得证书有效期
			Date beforedate = oCert.getNotBefore();
			info = dateformat.format(beforedate);
			System.out.println("证书生效日期:" + info);
			Date afterdate = oCert.getNotAfter();
			info = dateformat.format(afterdate);
			System.out.println("证书失效日期:" + info);
			// 获得证书主体信息
			info = oCert.getSubjectDN().getName();
			System.out.println("证书拥有者:" + info);
			// 获得证书颁发者信息
			info = oCert.getIssuerDN().getName();
			System.out.println("证书颁发者:" + info);
			// 获得证书签名算法名称
			info = oCert.getSigAlgName();
			System.out.println("证书签名算法:" + info);
			PublicKey publicKey = oCert.getPublicKey();

			BASE64Encoder base64Encoder=new BASE64Encoder();
			String publicKeyString = base64Encoder.encode(publicKey.getEncoded());
			System.out.println("-----------------公钥--------------------");
			System.out.println(publicKeyString);
			System.out.println("-----------------公钥--------------------");

 
		} catch (Exception e) {
			System.out.println("解析证书出错！");
			e.printStackTrace();
		}
	}
	
}
