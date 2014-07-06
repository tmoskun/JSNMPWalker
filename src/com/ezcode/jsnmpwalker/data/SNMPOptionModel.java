package com.ezcode.jsnmpwalker.data;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.util.Hashtable;
import java.util.Map;

import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.AuthSHA;
import org.snmp4j.security.Priv3DES;
import org.snmp4j.security.PrivAES128;
import org.snmp4j.security.PrivAES192;
import org.snmp4j.security.PrivAES256;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.smi.OID;


public class SNMPOptionModel extends Hashtable<String, String> {
	public static final String COMMUNITY_KEY = "community";
	public static final String PORT_KEY = "port";
	public static final String TIMEOUT_KEY = "timeout";
	public static final String RETRIES_KEY = "retries";
	public static final String SNMP_VERSION_KEY = "snmp_version";
	public static final String SECURITY_LEVEL_KEY = "security_level";
	public static final String AUTH_TYPE_KEY = "auth_type";
	public static final String PRIV_TYPE_KEY = "priv_type";
	public static final String AUTH_PASSPHRASE_KEY = "auth_passphrase";
	public static final String SECURITY_NAME_KEY = "security_name";
	public static final String PRIV_PASSPHRASE_KEY = "priv_passphrase";
	
	public static final String SNMP_VERSION_1 = "1";
	public static final String SNMP_VERSION_2c = "2c";
	public static final String SNMP_VERSION_3 = "3";
	
	public static final String SECURITY_LEVEL_NOAUTH_NOPRIV = "noAuthNoPriv";
	public static final String SECURITY_LEVEL_AUTH_NOPRIV = "authNoPriv";
	public static final String SECURITY_LEVEL_AUTH_PRIV = "authPriv";
	public static final String[] SECURITY_LEVELS = {SECURITY_LEVEL_NOAUTH_NOPRIV, SECURITY_LEVEL_AUTH_NOPRIV, SECURITY_LEVEL_AUTH_PRIV};
	
	public static final String AUTH_TYPE_MD5 = "MD5";
	public static final String AUTH_TYPE_SHA = "SHA";
	public static final String[] AUTH_TYPES = {AUTH_TYPE_MD5, AUTH_TYPE_SHA};
	
	public static final String PRIV_TYPE_DES = "DES";
	public static final String PRIV_TYPE_3DES = "3DES";
	public static final String PRIV_TYPE_AES128 = "AES128";
	public static final String PRIV_TYPE_AES192 = "AES192";
	public static final String PRIV_TYPE_AES256 = "AES256";
	public static final String[] PRIV_TYPES = {"DES", "3DES", "AES128", "AES192", "AES256"};
		
	private Map<String, String> _title_to_option_key = new Hashtable<String, String>();
	
	public SNMPOptionModel() {
		//defaults
		put(COMMUNITY_KEY, "public");
		put(PORT_KEY, "161");
		put(TIMEOUT_KEY, "300");
		put(RETRIES_KEY, "6");
		put(SNMP_VERSION_KEY, "2c");
		put(SECURITY_LEVEL_KEY, "authNoPriv");
		put(AUTH_TYPE_KEY, "MD5");
		put(PRIV_TYPE_KEY, "DES");
	}
	
	public SNMPOptionModel(Map<String, String> options) {
		this.putAll(options);
	}
	
	
	public void setTitle(String title, String value) {
		_title_to_option_key.put(title, value);
	}
	
	public String getTitle(String title) {
		return _title_to_option_key.get(title);
	}
	
	
	public boolean isVersion3() {
		return getVersion(get(SNMP_VERSION_KEY)) == SnmpConstants.version3;
	}
	
	public static boolean isAuthRequired(String level) {
		return level.equalsIgnoreCase(SECURITY_LEVEL_AUTH_NOPRIV) || level.equalsIgnoreCase(SECURITY_LEVEL_AUTH_PRIV);
	}
	
	public static boolean isPrivRequired(String level) {
		return level.equalsIgnoreCase(SECURITY_LEVEL_AUTH_PRIV);
	}
	
	public static boolean isVersion3(Map<String, String> options) {
		return getVersion(options.get(SNMP_VERSION_KEY)) == SnmpConstants.version3;
	}
	
	public static int getVersion(String ver) {
		if(ver != null)	{
			if(ver.equalsIgnoreCase(SNMP_VERSION_1)) {
				return SnmpConstants.version1;
			} else if(ver.equalsIgnoreCase(SNMP_VERSION_2c)) {
				return SnmpConstants.version2c;
			} else if(ver.equalsIgnoreCase(SNMP_VERSION_3)) {
				return SnmpConstants.version3;
			}
		}
		return -1;
	}
	
	
	public static int getSecurityLevel(String lev) {
		if(lev != null) {
			if(lev.equalsIgnoreCase(SECURITY_LEVEL_NOAUTH_NOPRIV)) {
				return SecurityLevel.NOAUTH_NOPRIV;
			} else if(lev.equalsIgnoreCase(SECURITY_LEVEL_AUTH_NOPRIV)) {
				return SecurityLevel.AUTH_NOPRIV;
			} else if(lev.equalsIgnoreCase(SECURITY_LEVEL_AUTH_PRIV)) {
				return SecurityLevel.AUTH_PRIV;
			}
		}
		return -1;
	}
	
	public static OID getAuthenticationType(String auth) {
		if(auth.equalsIgnoreCase(AUTH_TYPE_SHA)) {
			return AuthSHA.ID;
		} else if(auth.equalsIgnoreCase(AUTH_TYPE_MD5)) {
			return AuthMD5.ID;
		}
		return null;
	}
	
	public static OID getPrivacyType(String priv) {
		if(priv.equalsIgnoreCase(PRIV_TYPE_DES)) {
			return PrivDES.ID;
		} else if(priv.equalsIgnoreCase(PRIV_TYPE_AES128)) {
			return PrivAES128.ID;
		} else if(priv.equalsIgnoreCase(PRIV_TYPE_AES192)) {
			return PrivAES192.ID;
		} else if(priv.equalsIgnoreCase(PRIV_TYPE_AES256)) {
			return PrivAES256.ID;
		} else if(priv.equalsIgnoreCase(PRIV_TYPE_3DES)) {
			return Priv3DES.ID;
		}
		return null;
	}
	
	public String toString() {
		return get(COMMUNITY_KEY);
	}
	
	public boolean equals(Map<String, String> opts) {
		for(String key: this.keySet()) {
			if(!this.get(key).equalsIgnoreCase(opts.get(key)))
				return false;
		}
		return true;
	}
	

}
