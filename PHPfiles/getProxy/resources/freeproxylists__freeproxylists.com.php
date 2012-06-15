<?php

function DBout($string) 
{
	$string = trim($string);
	return htmlspecialchars_decode($string);
}


function freeproxylists()
{
	
	$url = "http://www.freeproxylists.com/us.php";
	
	$spider = new crawler;
	
		
			$pageHTML = $spider->getContent($url);
			if(empty($pageHTML['ERR']))
			{
				/************** load in DOM **********************/
				$html = new DOMDocument;
				@$html->loadHTML($pageHTML['EXE']);
				$xPath = new DOMXPath($html);			  
				/************** load in DOM **********************/
				$detailedlistRow_Xpath = "/html/body/table/tr[4]/td[3]/table/tr[2]/td/table/tr";
				$detailedlistRow_Arr = $xPath->query($detailedlistRow_Xpath);
				
				if($detailedlistRow_Arr->length > 0)
				{
					
					$proxies = array();
					$count = 0;
					for($i = 2; $i <= 6; $i++)
					{
						$detailedlistURL_Xpath = "/html/body/table/tr[4]/td[3]/table/tr[2]/td/table/tr[".$i."]/td[2]/a"; 
						$detailedlistURL_Arr = $xPath->query($detailedlistURL_Xpath);
						$listURL = $detailedlistURL_Arr->item(0)->getAttribute('href');
						$listURL = str_replace("us/","",$listURL);
						$listURL = "http://www.freeproxylists.com/load_us_" . $listURL;
						
						$listpageHTML = $spider->getContent($listURL);
						if(empty($listpageHTML['ERR']))
						{
							
							$data = DBout($listpageHTML['EXE']);
							
							
							/************** load in DOM **********************/
							$listhtml = new DOMDocument;
							@$listhtml->loadHTML($data);
							$listxPath = new DOMXPath($listhtml);			  
							/************** load in DOM **********************/
							$listRow_Xpath = "/html/body/root/quote/table/tr";											  
							$listRow_Arr = $listxPath->query($listRow_Xpath);
								
							if($listRow_Arr->length > 0)
							{ 
								
								$totalProxies = $listRow_Arr->length; 
								for($j = 3; $j <= $totalProxies; $j++)
								{
									$ip_Xpath = "/html/body/root/quote/table/tr[".$j."]/td[1]";
									$port_Xpath = "/html/body/root/quote/table/tr[".$j."]/td[2]";
									
									$ip_Arr = $listxPath->query($ip_Xpath);
									$port_Arr = $listxPath->query($port_Xpath);
				
									$ip = $ip_Arr->item(0)->nodeValue;
									$port = $port_Arr->item(0)->nodeValue;
									
									
									if($port == '8080' || $port == '80')
									{
										$proxy = $ip .":". $port;
										$proxies[$count]['proxy'] = $proxy;
										$proxies[$count]['country'] = 'US';
										$proxies[$count]['source'] = "freeproxylists.com";
									}
									
									$count++;
								}//got list pagination																
							}//if list row exists
						}//if got list page	
					}//got detail pagination					
				}//if detail list row exists								
			}//if got main page		
			return $proxies;		
}
?>



