package flager;

import java.sql.SQLException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import mySQL.MysqlDB;
import proxy.Proxy;
import users.User;
import listing.Listing;
import misc.*;

public class Main {

	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws SQLException, ClassNotFoundException 
	{
		
		User user = new User();
		Proxy proxy = new Proxy();
		MysqlDB db = new MysqlDB();
		Misc functions = new Misc();		
		ConstVars constantVars = new ConstVars();
		DesiredCapabilities BrowserCapability ; // to hold capabilities object having working proxy.
		int marketID = 0;
		Listing[] listings = new Listing[constantVars.flagLimit];
		while(true)
		{
			proxy = db.getProxy();
			BrowserCapability = functions.capabilities(proxy.proxy);
			WebDriver FireFox = new FirefoxDriver(BrowserCapability);
			
			FireFox.get("http://craigslist.org");
			String pageTitle = FireFox.getTitle();
			if(pageTitle.indexOf("craigslist:") == -1 ) //CL could not recognize IP 
			{	
				if(pageTitle.indexOf("Craigslist >") == -1) // Neither proxy is working
				{
					if(pageTitle.indexOf("ERROR") != -1 || pageTitle.indexOf("Error") != -1 || pageTitle.indexOf("403 Forbidden") != -1)
					{
						functions.Echo(proxy.proxy + " Bad Proxy -> " + pageTitle + " Removing Proxy from DB." );
						db.DeleteProxy(proxy.id);
					}
					else
					{
						functions.Echo(proxy.proxy + " Bad Proxy -> " + pageTitle + " Weight Decrement." );
						db.UpdateProxyUsed(proxy.id);
						FireFox.close();
					}	
					continue;
				}
				else // CL took us to geo page
				{
					// we are CL main page
					while(true)
					{	
					WebElement SfbayAreaLink = FireFox.findElement(By.xpath(constantVars.SfBayAreaXpath)); functions.Echo("On main page - " + proxy.proxy);
					functions.pause(5); //5 second pause
					SfbayAreaLink.click(); functions.Echo("going to sf bay area");
					functions.pause(5); //5 second pause
					WebElement myAcountLink = FireFox.findElement(By.xpath(constantVars.myAcountXpath));
					myAcountLink.click(); functions.Echo("my account clicked");
					functions.pause(5); //5 second pause
					user = db.loadUser(1);functions.Echo("user " + user.id);
					try{
					WebElement loginEmail = FireFox.findElement(By.xpath(constantVars.loginEmailXpath));
					WebElement loginPass = FireFox.findElement(By.id(constantVars.loginPasswordID));
					WebElement loginButton = FireFox.findElement(By.xpath(constantVars.loginButtonXpath));
					/*********** Fillup login form ***********/
					loginEmail.sendKeys(user.userName);
			        loginPass.sendKeys(user.password); functions.Echo("Filling Login form.");
			        loginButton.click();
					}catch(org.openqa.selenium.NoSuchElementException e){
						functions.Echo(e.getMessage());
						continue;
					}
			        listings = db.loadAds(1);
			        break;
					}
				}
				
			}
			else
			{
				// In Some Market. 
				while(true)
				{	
				WebElement marketTitle = FireFox.findElement(By.tagName("h2")); functions.Echo("I am @ some market " + proxy.proxy);
				String Mtitle = marketTitle.getText(); functions.Echo("market is " + Mtitle);
				marketID = db.getMarketId(Mtitle);   functions.Echo("market Id is  " +marketID);
				user = db.loadUser(marketID); functions.Echo("loaded user id  " +user.id);
				WebElement myAcountLink = FireFox.findElement(By.xpath(constantVars.myAcountXpath));
				functions.pause(5); //Stay 5 second on market's main page.
				myAcountLink.click(); functions.Echo("my account clicked");
				functions.pause(5); //Stay 5 second on login main page.
				try{
				WebElement loginEmail = FireFox.findElement(By.xpath(constantVars.loginEmailXpath));
				WebElement loginPass = FireFox.findElement(By.id(constantVars.loginPasswordID));
				WebElement loginButton = FireFox.findElement(By.xpath(constantVars.loginButtonXpath));
				loginEmail.sendKeys(user.userName);
		        loginPass.sendKeys(user.password);functions.Echo("Filling Login form.");
		        loginButton.click();
				}catch(org.openqa.selenium.NoSuchElementException e){
					functions.Echo(e.getMessage());
					continue;
				}
		        listings = db.loadAds(marketID);
		        break;
				}
				/*********** Fillup login form ***********/
				
			}
			
			// user is logged in.
			functions.Echo("I am Logged In.");
			functions.pause(5);
			for(int i = 0 ; i < listings.length; i++)
			{
				while(true)
				{	
				String adUrl = listings[i].url;
				int ListingID = listings[i].listingID;
				int needle1 = adUrl.lastIndexOf("org");
				int needle2 = adUrl.lastIndexOf("/");
				String u1 = adUrl.substring(0,needle1);
				String u2 = adUrl.substring(0,needle2);
					try
					{
					FireFox.get(u1 +"org"); functions.Echo("Listing # " + i + ": Visiting Market page.");
					functions.pause(5);
					FireFox.get(u2);   functions.Echo("Listing # " + i + ": Visiting category page.");
					functions.pause(5);
					}catch( org.openqa.selenium.remote.UnreachableBrowserException e){
						functions.Echo(e.getMessage());
						continue;
					}
					
					// 
					FireFox.get(adUrl);  functions.Echo("Listing # " + i + ": Visiting Listing page.");
					
					WebElement ListingTitle = FireFox.findElement(By.xpath(constantVars.ListingTitleXpath));
					
					String ListingTitleValue = ListingTitle.getText();
					functions.Echo(ListingTitleValue); 
					functions.Echo("Listing # " + i + ": A little pause before flag.");
					functions.pause(5);
					
					if(ListingTitleValue.indexOf("This posting has") != -1 )
					{	
						db.ListingKilled(ListingID);
					}
					else
					{
						WebElement miscategorized = FireFox.findElement(By.id(constantVars.miscategorizedLink));
						miscategorized.click();
						db.Flaged(ListingID);
					}
					functions.Echo("Listing # " + i + ": Flagged.");
						functions.pause(5);
						break ;
				}		
			}
				// logout 
			FireFox.get("https://accounts.craigslist.org/");
			WebElement LogoutLink = FireFox.findElement(By.xpath(constantVars.logoutXpath));
			LogoutLink.click();
				db.UpdateProxyUsed(proxy.id);
				db.UpdateUserLastUsed(user.id);
				functions.pause(5);
				FireFox.close();
				   
		}
		
		
		

	} // main method

}// Class
