<?php
set_time_limit(0);
function spysRU()
{
	$spider = new crawler;

	$refrer = "http://spys.ru/en/proxy-by-country/";
	$parameters = array("tldc"=>"234","eport"=>"80","anmm"=>"","0"=>"","vlast"=>"0","submit"=>"Proxy search"); // parameters for login
	$loginActionUrl = "http://spys.ru/en/";//form action url
	$pageHTML = $spider->logIn($loginActionUrl,$parameters,$refrer); //go for login
	
	$proxies = array();
	$proxyCount = 0;
	
	if(empty($pageHTML['ERR']))
	{
		unset($parameters,$loginActionUrl);
		$proxylines = $spider->getSelectiveContent($pageHTML['EXE'],'<font class=spy14>','<script');	
		
		foreach($proxylines as $proxyline)
		{		
			 $pattern = "/\b([0-9]{0,3}\.[0-9]{0,3}\.[0-9]{0,3}\.[0-9]{0,3})\b/i";
			 if(preg_match($pattern, $proxyline ,$matches))
			 {
				 $proxy = $matches[1];//get the add post id number 
				 $proxy = $proxy . ":80";
				 
				 $proxies[$proxyCount]['proxy']   = $proxy;
				 $proxies[$proxyCount]['country'] = "US";
				 $proxies[$proxyCount]['source']  = "spys.ru/en/";			 			 
				 $proxyCount++;
			 }
		}
		unset($pageHTML,$proxylines,$proxyline);

		$parameters = array("tldc"=>"234","eport"=>"8080","anmm"=>"","0"=>"","vlast"=>"0","submit"=>"Proxy search"); // parameters for login
		$loginActionUrl = "http://spys.ru/en/";//form action url
		$pageHTML = $spider->logIn($loginActionUrl,$parameters,$refrer); //go for login
		
		if(empty($pageHTML['ERR']))
		{
			unset($parameters,$loginActionUrl);
			$proxylines = $spider->getSelectiveContent($pageHTML['EXE'],'<font class=spy14>','<script');
		
			foreach($proxylines as $proxyline)
			{
				$pattern = "/\b([0-9]{0,3}\.[0-9]{0,3}\.[0-9]{0,3}\.[0-9]{0,3})\b/i";
				if(preg_match($pattern, $proxyline ,$matches))
				{
					$proxy = $matches[1];//get the add post id number
					$proxy = $proxy . ":8080";
						
					$proxies[$proxyCount]['proxy']   = $proxy;
					$proxies[$proxyCount]['country'] = "US";
					$proxies[$proxyCount]['source']  = "spys.ru/en/";
					$proxyCount++;
				}
			}
			unset($pageHTML,$proxylines,$proxyline);
		}
		
		$parameters = array("tldc"=>"234","eport"=>"3128","anmm"=>"","0"=>"","vlast"=>"0","submit"=>"Proxy search"); // parameters for login
		$loginActionUrl = "http://spys.ru/en/";//form action url
		$pageHTML = $spider->logIn($loginActionUrl,$parameters,$refrer); //go for login
		
		if(empty($pageHTML['ERR']))
		{
			unset($parameters,$loginActionUrl);
			$proxylines = $spider->getSelectiveContent($pageHTML['EXE'],'<font class=spy14>','<script');
		
			foreach($proxylines as $proxyline)
			{
				$pattern = "/\b([0-9]{0,3}\.[0-9]{0,3}\.[0-9]{0,3}\.[0-9]{0,3})\b/i";
				if(preg_match($pattern, $proxyline ,$matches))
				{
					$proxy = $matches[1];//get the add post id number
					$proxy = $proxy . ":3128";
		
					$proxies[$proxyCount]['proxy']   = $proxy;
					$proxies[$proxyCount]['country'] = "US";
					$proxies[$proxyCount]['source']  = "spys.ru/en/";
					$proxyCount++;
				}
			}
			unset($pageHTML,$proxylines,$proxyline);
		}
	}
	
	return $proxies;
}