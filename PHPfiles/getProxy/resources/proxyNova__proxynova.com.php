<?php
function proxyNova()
{
	
	$url = "http://www.proxynova.com/proxy_list.txt?country=US";
	
	$spider = new crawler;
	
		$list = array();
		
			$pageHTML = $spider->getContent($url);
			if(empty($pageHTML['ERR']))
			{
				$lists = $pageHTML['EXE'];
				$list  = explode("\n",$lists);
			}
			
				$return = array();
				$proxyCount = 0;
				foreach($list as $prox1)
				{
					$match = array();
					preg_match('/[a-zA-Z#]/',$prox1,$match);
					
					if ( !count($match) > 0 && $prox1 != "")
					{
						$return[$proxyCount]['proxy'] = trim($prox1);
						$return[$proxyCount]['country'] = 'US';
						$return[$proxyCount]['source'] = "proxynova.com";
						$proxyCount++;
					}	
					
				}				
				
		return $return;	
}

?>