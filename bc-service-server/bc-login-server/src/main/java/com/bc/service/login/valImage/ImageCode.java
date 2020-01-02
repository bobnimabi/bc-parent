package com.bc.service.login.valImage;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;


public class ImageCode {
	
	private BufferedImage image;
	
	private String code;
	
	private int expireTime;

	public ImageCode(BufferedImage image,String code,int expireIn) {
		this.image = image;
		this.code = code;
		this.expireTime = expireIn;
	}

	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(int expireTime) {
		this.expireTime = expireTime;
	}

}
