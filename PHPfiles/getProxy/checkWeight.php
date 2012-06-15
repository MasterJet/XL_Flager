<?php
set_time_limit(0);
require_once(dirname(__FILE__)."/include/crawler.class.php");
require_once(dirname(__FILE__)."/include/config.inc.php");
require_once(dirname(__FILE__)."/include/functions.php");

	
		$resProxies = mysql_query("SELECT * FROM `proxy` WHERE `weight` = 0 ORDER BY `weight` DESC");

        while($row = mysql_fetch_assoc($resProxies))
        {
        	$proxyID = $row['id'];        	
            $proxy   = $row['proxy'];
            
            $text    = testProxy(VALIDATION_URL, $proxy);
            
            if (!empty($text['ERR']))
            {
            	mysql_query("UPDATE `proxy` SET `weight` = -1 WHERE `id` = " .$proxyID);
            	continue;
            }
            if ($text['INF']['http_code'] != 200) 
            {
            	mysql_query("UPDATE `proxy` SET `weight` = -1 WHERE `id` = " .$proxyID);
            	continue;
            }
            else
            {
	            if( $text['INF']['url'] == "http://www.craigslist.org" || $text['INF']['url'] == "http://www.craigslist.org/" || $text['INF']['url'] == "http://www.craigslist.org/about/sites"  || $text['INF']['url'] == "http://www.craigslist.org/about/sites/" )
				{	
					mysql_query("UPDATE `proxy` SET `weight` = 100 WHERE `id` = " .$proxyID); // if proxy not found it's location
					continue;
				}
				else
				{
					$marketURL = $text['INF']['url'];
					$ifExists = mysql_query("SELECT * FROM `markets` WHERE `marketUrl` = '".$marketURL."' ");
					if (mysql_num_rows($ifExists) > 0 ) {
						
						$rowMarket = mysql_fetch_assoc($ifExists);
						$marketID = $rowMarket['id'];
						
						mysql_query("UPDATE `proxy` SET `weight` = 1, `mid` = ".$marketID."  WHERE `id` = " .$proxyID );
					}
					else
					{
						mysql_query("UPDATE `proxy` SET `weight` = 200 WHERE `id` = " .$proxyID); // if market not exists
					}					
					mysql_free_result($ifExists);
				}
            }
             unset($text);
        }
        mysql_free_result($resProxies);
        mysql_query("DELETE FROM `proxy` WHERE `weight` < 1 ");
        echo "proxies weight is checked.";