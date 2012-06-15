<?php

function proxyLists()
{	
	$spider = new crawler;
	$return = array();
	$proxyCount = 0;
		for($j = 0; $j < 21; $j++)
		{
			$url = "http://www.proxylists.net/us_"."$j".".html";
			$pageHTML = $spider->getContent($url);
			if(empty($pageHTML['ERR']))
			{
				$html = new DOMdocument;
				@$html->loadHTML($pageHTML['EXE']);
				$xpath = new DOMxpath($html);
				
				$proxyXpath = '//table/tr[1]/td[2]/table/tr';
				$trArr = $xpath->query($proxyXpath); 
								
				$trArrLen = $trArr->length -1;
				if($trArrLen > 0)
				{					
					for($i = 2; $i < $trArrLen; $i++ )
					{
						$data = $trArr->item($i)->nodeValue;
						$data = urldecode($data);
						$code = $spider->extractor($data,"\"","\"");
						$port = $spider->CustomStrStr($data,"Please enable javascript",$position = false,$sub = true);
						$proxy = $code .":". $port;
						
						$return[$proxyCount]['proxy'] = trim($proxy);
						$return[$proxyCount]['country'] = 'US';
						$return[$proxyCount]['source'] = "proxylists.net";
						$proxyCount++;
					}					
				}
			}			
		}			
		return $return;	
}
?>
