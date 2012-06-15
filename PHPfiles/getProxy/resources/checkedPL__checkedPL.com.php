<?php

function checkedPL()
{
	
	$spider = new crawler;
	
	$homeURL = "http://www.checkedproxylists.com/";
	
	$homeHTML = $spider->getContent($homeURL);
	
	if(empty($homeHTML['ERR']))
	{
		/********************/
				
		$html = new DOMDocument;
		@$html->loadHTML($homeHTML['EXE']);
		$xPath = new DOMXPath($html);	
				
		/********************/
		
		$targetXpath = "//div[contains(@style, 'border-left: dashed 1px;')]/ul/li/a[1]";
		$targetXpathARR = $xPath->query($targetXpath);
		
		$targetURL = '';
		if($targetXpathARR->length > 0)
		{
			$totalURLs = $targetXpathARR->length;			
			for ($i = 0; $i < $totalURLs; $i++)
			{
				if($targetXpathARR->item($i)->nodeValue == 'us proxy list')
				{
					$targetURL = $targetXpathARR->item($i)->getAttribute('href');
				}
			}
			
		}
											   
		$url = "www.checkedproxylists.com/load_".$targetURL;
		
		$pageHTML = $spider->getContent($url);
		
		$proxies = array();
		$proxyCount = 0;
		if(empty($pageHTML['ERR']))
		{
			$data = $pageHTML['EXE'];
		
		
			$proxyARR = $spider->getSelectiveContent($data,"left;'","/div");
		
			foreach ($proxyARR as $proxyStr )
			{
				$proxyRefine = str_replace('&gt;','',$proxyStr);
				$proxyRefine1 = str_replace('&lt;','',$proxyRefine);
				$proxy = str_replace('/span',':',$proxyRefine1);
					
				$proxies[$proxyCount]['proxy']   = $proxy;
				$proxies[$proxyCount]['country'] = "US";
				$proxies[$proxyCount]['source']  = "checkedProxyList.com";
				$proxyCount++;
					
			}
		}
		return $proxies;
	}		
}