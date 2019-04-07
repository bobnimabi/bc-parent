package com.bc.service.login.valImage;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * 
 * @author wanghuan
 *
 */

/**
 * 验证码图片属性设置
 */
@Component
@Data
public final class ImageCodeDefaultProperties {

	private int width = 110;
	
	private int height = 32;
	
	private int size = 4;
	
	private int expireIn = 60;
	
	private String[] url = {"/userlogin"};

}
