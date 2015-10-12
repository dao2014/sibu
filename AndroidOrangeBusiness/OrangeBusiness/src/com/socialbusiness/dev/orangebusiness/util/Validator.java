package com.socialbusiness.dev.orangebusiness.util;

import java.util.regex.Pattern;

/**
 * User: Peng
 * Date: 2013-11-09
 * Time: 10:46
 */
public final class Validator {

	private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    
    private static final String TRUE_PHONE = "^1\\d{10}$";

    private static final String TRUE_TEL = "((^(13|15|18)[0-9]{9}$)|(^0[1,2]{1}\\d{1}\\d{8}$)|"
            + "(^0[3-9]{1}\\d{2}\\d{7,8}$)|"
            + "(^0[1,2]{1}\\d{1}\\d{8}(\\d{1,4})$)|"
            + "(^0[3-9]{1}\\d{2}\\d{7,8}(\\d{1,4})$))";

    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);;
    private static final Pattern phonePattern = Pattern.compile(TRUE_PHONE);
    private static final Pattern telPattern = Pattern.compile(TRUE_TEL);

    public static boolean isEmail(String input){
        return pattern.matcher(input).matches();
    }
    
    public static boolean isPhone(String input) {
    	if (input == null) {
    		return false;
    	}
    	return phonePattern.matcher(input).matches();
    }

    public static boolean isTel(String input) {
        if (input == null) {
            return false;
        }
        return telPattern.matcher(input).matches();
    }
}
