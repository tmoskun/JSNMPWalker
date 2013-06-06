/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under MIT license
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


public class SNMPSessionOptionModel extends Hashtable<String, String> {
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
		
	private Map<String, String> _title_to_option_key = new Hashtable<String, String>();
	
	public SNMPSessionOptionModel() {
		put(COMMUNITY_KEY, "public");
		put(PORT_KEY, "161");
		put(TIMEOUT_KEY, "300");
		put(RETRIES_KEY, "6");
		put(SNMP_VERSION_KEY, "2c");
		put(SECURITY_LEVEL_KEY, "authNoPriv");
		put(AUTH_TYPE_KEY, "MD5");
		put(PRIV_TYPE_KEY, "DES");
	}
	
	
	public void setTitle(String title, String value) {
		_title_to_option_key.put(title, value);
	}
	
	public String getTitle(String title) {
		return _title_to_option_key.get(title);
	}
	
	public static int getVersion(String ver) {
		if(ver != null)	{
			if(ver.equalsIgnoreCase("1")) {
				return SnmpConstants.version1;
			} else if(ver.equalsIgnoreCase("2c")) {
				return SnmpConstants.version2c;
			} else if(ver.equalsIgnoreCase("3")) {
				return SnmpConstants.version3;
			}
		}
		return -1;
	}
	
	public static int getSecurityLevel(String lev) {
		if(lev != null) {
			if(lev.equalsIgnoreCase("noAuthNoPriv")) {
				return SecurityLevel.NOAUTH_NOPRIV;
			} else if(lev.equalsIgnoreCase("authNoPriv")) {
				return SecurityLevel.AUTH_NOPRIV;
			} else if(lev.equalsIgnoreCase("authPriv")) {
				return SecurityLevel.AUTH_PRIV;
			}
		}
		return -1;
	}
	
	public static OID getAuthenticationType(String auth) {
		if(auth.equalsIgnoreCase("sha")) {
			return AuthSHA.ID;
		} else if(auth.equalsIgnoreCase("md5")) {
			return AuthMD5.ID;
		}
		return null;
	}
	
	public static OID getPrivacyType(String priv) {
		if(priv.equalsIgnoreCase("DES")) {
			return PrivDES.ID;
		} else if(priv.equalsIgnoreCase("AES128")) {
			return PrivAES128.ID;
		} else if(priv.equalsIgnoreCase("AES192")) {
			return PrivAES192.ID;
		} else if(priv.equalsIgnoreCase("AES256")) {
			return PrivAES256.ID;
		} else if(priv.equalsIgnoreCase("3DES")) {
			return Priv3DES.ID;
		}
		return null;
	}

}
