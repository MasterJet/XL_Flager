package misc;

import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

public class Misc {
	public void pause (int seconds)
	{
    	int miliSeconds = seconds * 1000;
    	 try {
				Thread.sleep(miliSeconds);
			 } 
    	     catch (InterruptedException e) 
    	     {
				
				e.printStackTrace();
			 }
    }
	
	public DesiredCapabilities capabilities(String p)
	{
		String PROXY = p;
		org.openqa.selenium.Proxy proxy = new org.openqa.selenium.Proxy();
		proxy.setHttpProxy(PROXY)
	     .setFtpProxy(PROXY)
	     .setSslProxy(PROXY);
		DesiredCapabilities cap = new DesiredCapabilities();
		cap.setCapability(CapabilityType.PROXY, proxy);
		return cap;
	}
	public long getUnixTimeStamp()
	{
		return System.currentTimeMillis()/1000;
	}
	public void Echo (String str)
	{
		System.out.println(str);
	}
}
