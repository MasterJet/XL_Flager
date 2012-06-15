<?php
function load_resources($path ) 
{
	$dir_handle = @opendir($path) or die("Appllication's directory structure problem.");
	$content_list = array ();
		while ($file = readdir($dir_handle)) 
		{
			if($file!="." && $file!="..") // not system folders
			  {
				$content_list []= $file;
			  }
		} // loop
			closedir($dir_handle);
			natcasesort($content_list);
			return $content_list;
}

        function testProxy($url, $proxy="")
        {
                $cr = curl_init();
                curl_setopt($cr, CURLOPT_URL, $url);
                curl_setopt($cr, CURLOPT_HEADER, 0);
                curl_setopt($cr, CURLOPT_RETURNTRANSFER, 1);
                if($proxy != "")
                {
                	curl_setopt($cr, CURLOPT_PROXY, $proxy);
                    curl_setopt($cr, CURLOPT_HTTPPROXYTUNNEL, 0);
                }
                curl_setopt($cr, CURLOPT_FOLLOWLOCATION, 1);
                curl_setopt($cr, CURLOPT_CONNECTTIMEOUT, TEST_TIMEOUT);
                curl_setopt($cr, CURLOPT_TIMEOUT, TEST_TIMEOUT);
                curl_setopt($cr, CURLOPT_REFERER, 'http://www.google.com/');
                curl_setopt($cr, CURLOPT_USERAGENT, 'Mozilla/5.0 (X11; U; Linux i586; de; rv:5.0) Gecko/20100101 Firefox/5.0');

                $result['EXE'] = curl_exec($cr);
                $result['INF'] = curl_getinfo($cr);
                $result['ERR'] = curl_error($cr);

                //curl_close($this->curl);
                unset($url);
                return $result;
        }