<?php
/**
 * DataBase
 * Host Name
 */
define("DB_HOST","localhost");

/**
 * DataBase
 * User Name
 */
define("DB_USER","flager");

/**
 * DataBase
 * dtabase Password
 */
define("DB_PASSWORD","flager");

/**
 * DataBase
 * database name.
 */
define("DB_NAME","flager");

/**
 * Proxy Validation
 * Url to test a proxy.
 * Application will try to access 'VALIDATION_URL' by using proxy. 
 * 
 */
define("VALIDATION_URL","http://craigslist.org/");

/**
 * Proxy TEST_TIMEOUT
 * script will spend a maximmum  TEST_TIMEOUT to test a proxy.
 * lower value will be more strict testing.
 */
define("TEST_TIMEOUT",20);


$cON = mysql_connect(DB_HOST, DB_USER, DB_PASSWORD) or die("Database Connection Problem.");
mysql_select_db(DB_NAME,$cON) or die("Database selection Problem.");


