<?php
function freeproxy()
{
			
	$url = "http://www.freeproxy.ch/proxy.txt";
	
	$spider = new crawler;
	
	
		
		$pageHTML = $spider->getContent($url);
		if(empty($pageHTML['ERR']))
		{				
			$data = trim($pageHTML['EXE']);
			$rows = explode("\n",$data);	
			array_shift($rows);
			array_shift($rows);
			array_shift($rows);
			array_shift($rows);
			
			$proxies = array();
			$proxyCount = 0;				
			foreach($rows as $row)
			{	
				if(strstr($row,'US'))
				{
					$proxy = $spider->CustomStrStr($row,'	',$position = false,$sub = false);					
					if(strstr($proxy,'80') || strstr($proxy,'8080') || strstr($proxy,'3128'))
					{
						$proxies[$proxyCount]['proxy'] = $proxy;
						$proxies[$proxyCount]['country'] = "US";
						$proxies[$proxyCount]['source'] = "freeproxy.ch";
						$proxyCount++; 
					}
				}
			}
		}	
		return $proxies;	
}